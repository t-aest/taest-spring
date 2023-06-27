package com.taest.v2.spring.framework.aop;

import com.taest.v2.spring.framework.aop.intercept.TTMethodInvocation;
import com.taest.v2.spring.framework.aop.support.TTAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class TTJdkDynamicAopProxy implements TTAopProxy,InvocationHandler {

    private TTAdvisedSupport advised;


    public TTJdkDynamicAopProxy(TTAdvisedSupport config) {
        this.advised =  config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> chain = advised.getInterceptorsAndDynamicInterceptionAdvice(method, this.advised.getTargetClass());
        TTMethodInvocation invocation = new TTMethodInvocation(proxy, this.advised.getTarget(), method, args, this.advised.getTargetClass(), chain);
        return invocation.proceed();
    }


    public Object getProxy() {
        return getProxy(this.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.advised.getTargetClass().getInterfaces(), this);
    }
}
