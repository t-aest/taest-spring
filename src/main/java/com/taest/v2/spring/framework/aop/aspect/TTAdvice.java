package com.taest.v2.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

@Data
public class TTAdvice {

    private Object aspect;

    private Method adviceMethod;

    private String throwName;

    public TTAdvice(Object aspect, Method adviceMethod){
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

}
