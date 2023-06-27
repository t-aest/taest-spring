package com.taest.v2.spring.framework.aop.intercept;

public interface TTMethodInterceptor {

    Object invoke(TTMethodInvocation invocation) throws Throwable;
}
