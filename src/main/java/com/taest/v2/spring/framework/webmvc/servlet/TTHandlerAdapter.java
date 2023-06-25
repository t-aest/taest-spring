package com.taest.v2.spring.framework.webmvc.servlet;

import com.taest.v2.spring.framework.annotation.TTRequestParam;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
public class TTHandlerAdapter {

    public TTModelAndView handler(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  TTHandlerMapping handlerMapping) throws Exception{

        //保存形参列表   将参数名称和参数位置  这种关系保存
        Map<String,Integer> paramIndexMapping = new HashMap<String, Integer>();

        //通过运行时状态拿到注解的值
        Annotation[][] parameterAnnotations = handlerMapping.getMethod().getParameterAnnotations();

        for (int j = 0; j < parameterAnnotations.length; j++) {
            for (Annotation annotation : parameterAnnotations[j]) {
                if (annotation instanceof TTRequestParam) {
                    String paramName = ((TTRequestParam) annotation).value();
                    if (!"".equals(paramName)) {
//                        String value = Arrays.toString(parameterMap.get(paramName))
//                                .replaceAll("\\[|\\]", "")
//                                .replaceAll("\\s+", ",");
//                        paramValues[i] = value;
                        paramIndexMapping.put(paramName,j);
                    }
                }
            }
        }
        //初始化参数类型
        Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class) {
                paramIndexMapping.put(parameterType.getName(),i);
            }
        }


        //拼接形参列表
        Map<String, String[]> parameterMap = req.getParameterMap();

        Object[] paramValues = new Object[parameterTypes.length];

        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
//            String value = Arrays.toString(parameterMap.get(param.getKey())).replaceAll("\\[|\\]","")
//                    .replaceAll("\\s+",",");
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");
            if (!paramIndexMapping.containsKey(param.getKey())) continue;
            int index = paramIndexMapping.get(param.getKey());

            //允许自定义类型转换器Converter
            paramValues[index] = castStringValue(value,parameterTypes[index]);
        }

        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
//
//        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
//        //赋值实参列表
//        method.invoke(applicationContext.getBean(beanName), paramValues);
        if (result == null || result instanceof Void) return null;

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == TTModelAndView.class;
        if (isModelAndView){
            return (TTModelAndView) result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> parameterType) {
        if (String.class == parameterType){
            return value;
        }
        if (Integer.class == parameterType){
            return Integer.valueOf(value);
        }
        if (Double.class == parameterType){
            return Double.valueOf(value);
        }
        return value;
    }
}
