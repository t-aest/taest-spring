package com.taest.v2.spring.framework.aop.aspect;

import com.taest.v2.spring.framework.aop.intercept.TTMethodInterceptor;
import com.taest.v2.spring.framework.aop.intercept.TTMethodInvocation;

import java.lang.reflect.Method;

public class TTMethodBeforeAdviceInterceptor extends TTAbstractAspectJAdvice implements TTMethodInterceptor {

    private TTJoinPoint joinPoint;
    public TTMethodBeforeAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    public void before(Method method, Object[] args, Object target) throws Throwable {
        this.invokeAdviceMethod(this.joinPoint,null,null);
    }

    @Override
    public Object invoke(TTMethodInvocation invocation) throws Throwable {
        joinPoint = invocation;
        this.before(invocation.getMethod(), invocation.getArguments(), invocation.getThis());
        return invocation.proceed();
    }
}
