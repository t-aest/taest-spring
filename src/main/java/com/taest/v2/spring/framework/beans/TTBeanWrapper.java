package com.taest.v2.spring.framework.beans;

import lombok.Data;

@Data
public class TTBeanWrapper {

    private Object wrapperInstance;

    private Class<?> wrapperClass;

    public TTBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrapperClass = instance.getClass();
    }
}
