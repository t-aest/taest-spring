package com.taest.v2.spring.framework.aop.support;

import com.taest.v2.spring.framework.aop.aspect.TTAfterReturningAdviceInterceptor;
import com.taest.v2.spring.framework.aop.aspect.TTAspectJAfterThrowingAdvice;
import com.taest.v2.spring.framework.aop.aspect.TTMethodBeforeAdviceInterceptor;
import com.taest.v2.spring.framework.aop.config.TTAopConfig;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class TTAdvisedSupport {

    private TTAopConfig config;
    private Class<?> targetClass;
    private Object target;

    private Pattern pointCutClassPattern;

    private Map<Method, List<Object>> methodCache;


    public TTAdvisedSupport(TTAopConfig config) {
        this.config = config;

    }

    /**
     * 解析配置文件
     */
    private void parse(){

        String pointCut = config.getPointCut().replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        //方法的修饰符和返回值
        //类名
        //方法的名称和形参列表

        //生成匹配class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        methodCache = new HashMap<Method, List<Object>>();

        //匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try {
            Class<?> aspectClass = Class.forName(this.config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(),method);
            }

            for (Method method : this.targetClass.getMethods()) {
                String m = method.toString();
                if (m.contains("throws")){
                    m = m.substring(0,m.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pointCutPattern.matcher(m);
                if (matcher.matches()){
                    List<Object> advices = new LinkedList<Object>();
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))){
                        advices.add(new TTMethodBeforeAdviceInterceptor(aspectClass.newInstance(),aspectMethods.get(config.getAspectBefore())));
                    }
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))){
                        advices.add(new TTAfterReturningAdviceInterceptor(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfter())));
                    }
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))){
                        TTAspectJAfterThrowingAdvice advice = new TTAspectJAfterThrowingAdvice(aspectClass.newInstance(),aspectMethods.get(config.getAspectAfterThrow()));
                        advices.add(advice);
                        advice.setThrowName(config.getAspectAfterThrowingName());
                    }

                    //跟目标代理类的业务方法和Advices建立一对多关系 便于在Proxy类中获取
                    methodCache.put(method,advices);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    /**
     * 根据一个目标代理类去获取对应的通知
     * @param method
     * @param targetClass
     * @return
     * @throws Exception
     */
    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {

        // 从缓存中获取
        List<Object> cached = this.methodCache.get(method);
        // 缓存未命中，则进行下一步处理
        if (cached == null) {
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cached = this.methodCache.get(m);
            // 存入缓存
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    /**
     * IOC中对象初始化时调用，决定是否生成代理类
     * @return
     */
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    public void setTargetClass(Class<?> clazz) {
        this.targetClass = clazz;
        parse();

    }

    public void setTarget(Object instance) {
        this.target = instance;
    }
}
