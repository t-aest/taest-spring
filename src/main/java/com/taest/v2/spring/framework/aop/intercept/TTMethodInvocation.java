package com.taest.v2.spring.framework.aop.intercept;

import com.taest.v2.spring.framework.aop.aspect.TTJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TTMethodInvocation implements TTJoinPoint {

    protected final Object proxy;

    protected final Object target;

    protected final Method method;

    protected Object[] arguments = new Object[0];

    private final Class<?> targetClass;

    private Map<String, Object> userAttributes = new HashMap<String, Object>();

    protected final List<?> interceptorsAndDynamicMethodMatchers;

    private int currentInterceptorIndex = -1;

    public TTMethodInvocation(
            Object proxy, Object target, Method method, Object[] arguments,
            Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {

        this.proxy = proxy;
        this.target = target;
        this.targetClass = targetClass;
        this.method = method;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }


    public Object proceed() throws Throwable {
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(this.target,this.arguments);
        }

        Object interceptorOrInterceptionAdvice =
                this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
        //如果要动态匹配joinPoint
        if (interceptorOrInterceptionAdvice instanceof TTMethodInterceptor) {

            TTMethodInterceptor methodInterceptor = (TTMethodInterceptor) interceptorOrInterceptionAdvice;
            return methodInterceptor.invoke(this);
        }else {
            return proceed();
        }
    }

    public Method getMethod() {
        return this.method;
    }

    public Object[] getArguments() {
        return this.arguments;
    }

    public Object getThis() {
        return this.target;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        this.userAttributes.put(key,value);
    }

    @Override
    public Object getUserAttribute(String key) {
        return this.userAttributes.get(key);
    }
}
