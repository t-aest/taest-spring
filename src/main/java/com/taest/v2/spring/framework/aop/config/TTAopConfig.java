package com.taest.v2.spring.framework.aop.config;


import lombok.Data;

@Data
public class TTAopConfig {

    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
