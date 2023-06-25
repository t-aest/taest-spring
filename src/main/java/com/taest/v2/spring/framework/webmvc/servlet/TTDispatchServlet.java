package com.taest.v2.spring.framework.webmvc.servlet;


import com.taest.v2.spring.framework.annotation.TTController;
import com.taest.v2.spring.framework.annotation.TTRequestMapping;
import com.taest.v2.spring.framework.context.TTApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 负责任务调度
 */
public class TTDispatchServlet extends HttpServlet {

    private TTApplicationContext applicationContext;


    private Map<TTHandlerMapping, TTHandlerAdapter> handlerAdapterMap = new HashMap<TTHandlerMapping, TTHandlerAdapter>();


    private List<TTHandlerMapping> handlerMappings = new ArrayList<TTHandlerMapping>();

    private List<TTViewResolver> viewResolvers = new ArrayList<TTViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //委派  根据url去找到对应的method,并通过resp返回
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                processDispatchResult(req, resp, new TTModelAndView("500"));
            } catch (Exception ex) {
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {


        TTHandlerMapping handler = getHandler(req);

        //通过url获取HandlerMapping
        if (handler == null) {
            processDispatchResult(req, resp, new TTModelAndView("404"));
            return;
        }

        //通过HandlerMapping获取一个HandlerAdapter
        TTHandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        //解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        TTModelAndView modelAndView = handlerAdapter.handler(req, resp, handler);

        //将ModelAndView变成一个ViewResolver
        processDispatchResult(req, resp, modelAndView);


    }

    private TTHandlerAdapter getHandlerAdapter(TTHandlerMapping handler) {
        if (this.handlerAdapterMap.isEmpty()) return null;
        return this.handlerAdapterMap.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, TTModelAndView modelAndView) throws Exception {
        if (null == modelAndView) return;

        if (this.viewResolvers.isEmpty()) return;

        for (TTViewResolver viewResolver : this.viewResolvers) {
            TTView view = viewResolver.resolveViewName(modelAndView.getViewName());
            //往浏览器渲染
            view.render(modelAndView.getModel(),req,resp);
            return;

        }

    }

    private TTHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        for (TTHandlerMapping mapping : this.handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return mapping;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //初始化spring IOC容器
        applicationContext = new TTApplicationContext(config.getInitParameter("contextConfigLocation"));

        //初始化九大组件
        initStrategies(applicationContext);

        System.out.println("TT mvc initialized");

    }

    private void initStrategies(TTApplicationContext context) {
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化视图转换器
        initViewResolvers(context);
    }

    private void initViewResolvers(TTApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new TTViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(TTApplicationContext context) {
        for (TTHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapterMap.put(handlerMapping,new TTHandlerAdapter());
        }
    }

    private void initHandlerMappings(TTApplicationContext context) {

        if (this.applicationContext.getBeanDefinitonCount() == 0) return;

        for (String beanName : this.applicationContext.getBeanDefinitonNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if (!clazz.isAnnotationPresent(TTController.class)) {
                continue;
            }

            //提取controller class上的地址
            String baseUrl = "";
            if (clazz.isAnnotationPresent(TTRequestMapping.class)) {
                baseUrl = clazz.getAnnotation(TTRequestMapping.class).value();
            }

            //只获取public 方法
            for (Method method : clazz.getMethods()) {

                if (!method.isAnnotationPresent(TTRequestMapping.class)) continue;

                //提取controller 每个方法上的地址
                TTRequestMapping requestMapping = method.getAnnotation(TTRequestMapping.class);
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new TTHandlerMapping(pattern, method, instance));
                System.out.println(("Mapped:" + regex + "," + method));
            }
        }
    }

    private String toLowerFirstCase(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}
