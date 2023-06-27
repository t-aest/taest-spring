package com.taest.v2.spring.framework.aop.aspect;

import java.lang.reflect.Method;

public interface TTJoinPoint {

    Method getMethod();

    Object[] getArguments();

    Object getThis();

    void setUserAttribute(String key,Object value);

    Object getUserAttribute(String key);
}
