package com.taest.v2.spring.framework.aop.aspect;

import com.taest.v2.spring.framework.aop.intercept.TTMethodInterceptor;
import com.taest.v2.spring.framework.aop.intercept.TTMethodInvocation;

import java.lang.reflect.Method;

public class TTAspectJAfterThrowingAdvice extends TTAbstractAspectJAdvice implements TTMethodInterceptor {

    private String throwName;

    public TTAspectJAfterThrowingAdvice(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    @Override
    public Object invoke(TTMethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        }
        catch (Throwable ex) {
            invokeAdviceMethod(invocation, null, ex.getCause());
            throw ex;
        }
    }

    public void setThrowName(String throwName) {
        this.throwName = throwName;
    }
}
