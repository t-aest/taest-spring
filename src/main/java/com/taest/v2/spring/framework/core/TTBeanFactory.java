package com.taest.v2.spring.framework.core;

public interface TTBeanFactory {
    public Object getBean(Class beanClass);

    public Object getBean(String beanName);
}
