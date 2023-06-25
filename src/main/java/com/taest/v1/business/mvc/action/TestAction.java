package com.taest.v1.business.mvc.action;


//没加注解，控制权不反转，自己管自己
public class TestAction {

    public static void main(String[] args) throws ClassNotFoundException {
        Class<?> aClass = Class.forName("com.taest.v1.business.service.impl.DemoService");
        for (Class<?> anInterface : aClass.getInterfaces()) {
            System.out.println("anInterface = " + anInterface.getName());
        }
    }
}
