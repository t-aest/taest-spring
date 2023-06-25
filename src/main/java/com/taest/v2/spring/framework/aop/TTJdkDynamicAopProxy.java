package com.taest.v2.spring.framework.aop;

import com.taest.v2.spring.framework.aop.aspect.TTAdvice;
import com.taest.v2.spring.framework.aop.support.TTAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class TTJdkDynamicAopProxy implements InvocationHandler {

    private TTAdvisedSupport config;

    public TTJdkDynamicAopProxy(TTAdvisedSupport config) {
        this.config =  config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, TTAdvice> advices = config.getAdvices(method,null);
        Object result;
        try {
            invokeAdvice(advices.get("before"));
            result = method.invoke(this.config.getTarget(),args);
            invokeAdvice(advices.get("after"));
        }catch (Exception e){
            invokeAdvice(advices.get("afterThrow"));
            throw e;
        }

        return result;
    }

    private void invokeAdvice(TTAdvice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.config.getTargetClass().getInterfaces(),this);
    }
}
