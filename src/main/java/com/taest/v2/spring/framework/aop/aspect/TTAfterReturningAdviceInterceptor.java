package com.taest.v2.spring.framework.aop.aspect;


import com.taest.v2.spring.framework.aop.intercept.TTMethodInterceptor;
import com.taest.v2.spring.framework.aop.intercept.TTMethodInvocation;

import java.lang.reflect.Method;

public class TTAfterReturningAdviceInterceptor extends TTAbstractAspectJAdvice implements TTMethodInterceptor {

    private TTJoinPoint joinPoint;

    public TTAfterReturningAdviceInterceptor(Object aspect, Method adviceMethod) {
        super(aspect, adviceMethod);
    }

    @Override
    public Object invoke(TTMethodInvocation invocation) throws Throwable {
        this.joinPoint = invocation;
        Object result = invocation.proceed();
        this.afterReturning(result,invocation.getMethod(),invocation.getArguments(),invocation.getThis());
        return result;
    }

    private void afterReturning(Object result, Method method, Object[] arguments, Object aThis) throws Throwable {
        invokeAdviceMethod(this.joinPoint, result, null);
    }
}
