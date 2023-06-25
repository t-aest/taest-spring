package com.taest.v2.spring.framework.webmvc.servlet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TTHandlerMapping {

    private Pattern pattern;

    private Method method;


    /**
     * method对应的实例对象
     */
    private Object controller;


}
