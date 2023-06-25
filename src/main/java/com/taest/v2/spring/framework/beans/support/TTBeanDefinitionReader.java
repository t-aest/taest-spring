package com.taest.v2.spring.framework.beans.support;

import com.taest.v2.spring.framework.annotation.TTController;
import com.taest.v2.spring.framework.annotation.TTService;
import com.taest.v2.spring.framework.beans.config.TTBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TTBeanDefinitionReader {

//    private String[] configLocations;

    /**
     * 配置文件
     */
    private Properties contextConfig = new Properties();

    /**
     * 扫描包得到的类名称
     */
    private List<String> regitryBeanClasses = new ArrayList<String>();

    public TTBeanDefinitionReader(String... configLocations) {
//        this.configLocations = configLocations;

        doLoadConfig(configLocations[0]);

        doScanner(contextConfig.getProperty("scanPackage"));


    }

    public List<TTBeanDefinition> loadBeanDefinition() {
        List<TTBeanDefinition> result = new ArrayList<TTBeanDefinition>();

        try {
            for (String className : regitryBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                String beanName = toLowerFirstCase(beanClass.getSimpleName());

                //保存类对应的全类名

                //保存beanName
                // 默认为首字母小写
//                result.add(doCreateBeanDefinition(beanName, beanClass.getName()));

                if (beanClass.isAnnotationPresent(TTController.class)) {
                    result.add(doCreateBeanDefinition(beanName, beanClass.getName()));
                } else if (beanClass.isAnnotationPresent(TTService.class)) {
                    //判断是否 有自定义类名
                    String customerBeanName = beanClass.getAnnotation(TTService.class).value();
                    if ("".equals(customerBeanName.trim())) {
                        //默认类名首字母小写
                        result.add(doCreateBeanDefinition(beanName, beanClass.getName()));
                    }else {
                        result.add(doCreateBeanDefinition(customerBeanName, beanClass.getName()));
                    }
                    //接口注入
                    for (Class<?> anInterface : beanClass.getInterfaces()) {
                        String interfaceName = anInterface.getName();
                        result.add(doCreateBeanDefinition(interfaceName, beanClass.getName()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private TTBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        TTBeanDefinition beanDefinition = new TTBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    private void doLoadConfig(String applicationProperty) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(applicationProperty.replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doScanner(String scanPackage) {
        URL resource = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(resource.getFile());

        //遍历ClassPath文件夹
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) continue;
                String className = scanPackage + "." + file.getName().replace(".class", "");
                regitryBeanClasses.add(className);
            }
        }

    }

    private String toLowerFirstCase(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public Properties getConfig() {
        return this.contextConfig;
    }
}
