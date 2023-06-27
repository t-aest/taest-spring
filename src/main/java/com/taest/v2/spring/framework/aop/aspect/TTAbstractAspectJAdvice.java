package com.taest.v2.spring.framework.aop.aspect;

import java.lang.reflect.Method;

public class TTAbstractAspectJAdvice implements TTAdvice{

    private Object aspect;

    private Method adviceMethod;

    private String throwName;

    public TTAbstractAspectJAdvice(Object aspect, Method adviceMethod){
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    protected Object invokeAdviceMethod(TTJoinPoint jp, Object returnValue, Throwable t) throws Throwable {

        //LogAspect.before(),LogAspect.after()  ...
        Class<?> [] paramTypes = this.adviceMethod.getParameterTypes();
        if(null == paramTypes || paramTypes.length == 0){
            return this.adviceMethod.invoke(this.aspect);
        }else{
            Object [] args = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                if(paramTypes[i] == TTJoinPoint.class){
                    args[i] = jp;
                }else if(paramTypes[i] == Throwable.class){
                    args[i] = t;
                }else if(paramTypes[i] == Object.class){
                    args[i] = returnValue;
                }
            }
            return this.adviceMethod.invoke(aspect,args);
        }

    }

}
