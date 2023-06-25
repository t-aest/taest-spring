package com.taest.v1.springmvc.servlet;


import com.taest.v1.springmvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class TTDispatchServlet extends HttpServlet {

    /**
     * 配置文件
     */
    private Properties contextConfig = new Properties();

    /**
     * 扫描包得到的类名称
     */
    private List<String> classNames = new ArrayList<String>();

    /**
     * IoC容器  key是默认的类名 首字母小写 beanName ，value是对应的实例对象
     */
    private Map<String,Object> ioc = new HashMap<String,Object>();

    /**
     *
     */
    private Map<String,Method> handlerMapping = new HashMap<String,Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //委派  根据url去找到对应的method,并通过resp返回
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if (!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 NOT FOUND");
            return;
        }

        Map<String, String[]> parameterMap = req.getParameterMap();

        Method method = this.handlerMapping.get(url);

        //获取形参列表
        Class<?>[] parameterTypes = method.getParameterTypes();

        Object[] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class){
                paramValues[i] = req;
            }else if(parameterType == HttpServletResponse.class){
                paramValues[i] = resp;
            } else if(parameterType == String.class){
                //通过运行时状态拿到注解的值
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();

                for (int j = 0; j < parameterAnnotations.length; j++) {
                    for (Annotation annotation : parameterAnnotations[j]) {
                        if (annotation instanceof TTRequestParam){
                            String paramName = ((TTRequestParam) annotation).value();
                            if (!"".equals(paramName)){
                                String value = Arrays.toString(parameterMap.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s+",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }


        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //赋值实参列表
        method.invoke(ioc.get(beanName),paramValues);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
       //加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //初始化IOC容器，将扫面到的相关的类实例化，保存到IOC容器中
        doInstance();

        //完成依赖注入
        doAutoWired();

        //初始化HandlerMapper();
        doInitHandlerMapping();

        System.out.println("TT mvc initialized");

    }

    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(TTController.class)){ continue; }

            //提取controller class上的地址
            String baseUrl = "";
            if (clazz.isAnnotationPresent(TTRequestMapping.class)){
                baseUrl = clazz.getAnnotation(TTRequestMapping.class).value();
            }

            //只获取public 方法
            for (Method method : clazz.getMethods()) {

                if (!method.isAnnotationPresent(TTRequestMapping.class)) continue;

                //提取controller 每个方法上的地址
                TTRequestMapping requestMapping = method.getAnnotation(TTRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println(("Mapped:" + url + "," + method));
            }
        }
    }

    private void doAutoWired() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            //获取所有的参数 private、protect、public等
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(TTAutowired.class)) {
                    continue;
                }
                TTAutowired autowired = field.getAnnotation(TTAutowired.class);

                //判断是否有自定义beanName
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    //获取字段的类型名称
                    beanName = field.getType().getName();
                }

                //强制修改
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) return;
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(TTController.class)) {
                    Object o = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,o);
                } else if (clazz.isAnnotationPresent(TTService.class)){
                    //判断是否 有自定义类名
                    String beanName = clazz.getAnnotation(TTService.class).value();
                    if ("".equals(beanName.trim())){
                        //默认类名首字母小写
                        beanName = toLowerFirstCase(clazz.getSimpleName());

                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);

                    //判断类实现的接口是否已经被注入  防止多个类实现同一个接口
                    for (Class<?> anInterface : clazz.getInterfaces()) {
                        String interfaceName = anInterface.getName();
                        if (ioc.containsKey(interfaceName)){
                            throw new Exception("The" + interfaceName + "is exists !!!");
                        }
                        ioc.put(interfaceName,instance);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void doScanner(String scanPackage) {
        URL resource = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(resource.getFile());

        //遍历ClassPath文件夹
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                if (!file.getName().endsWith(".class")) continue;
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }

    }
    private void doLoadConfig(String applicationProperty) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(applicationProperty);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String toLowerFirstCase(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }
}
