package com.taest.v2.spring.framework.context;

import com.taest.v2.spring.framework.annotation.TTAutowired;
import com.taest.v2.spring.framework.annotation.TTController;
import com.taest.v2.spring.framework.annotation.TTService;
import com.taest.v2.spring.framework.aop.TTJdkDynamicAopProxy;
import com.taest.v2.spring.framework.aop.config.TTAopConfig;
import com.taest.v2.spring.framework.aop.support.TTAdvisedSupport;
import com.taest.v2.spring.framework.beans.TTBeanWrapper;
import com.taest.v2.spring.framework.beans.config.TTBeanDefinition;
import com.taest.v2.spring.framework.beans.support.TTBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 完成bean的创建和依赖注入
 */
public class TTApplicationContext {
//    private String[] configLocations;

    private TTBeanDefinitionReader reader;

    private Map<String, TTBeanWrapper> factoryBeanInstanceCache = new HashMap<String, TTBeanWrapper>();

    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();

    private Map<String, TTBeanDefinition> beanDefinitionMap = new HashMap<String, TTBeanDefinition>();

    public TTApplicationContext(String... configLocations) {
//        this.configLocations = configLocations;

        //加载配置文件
        reader = new TTBeanDefinitionReader(configLocations);

        try {
            //解析配置文件  封装为BeanDefinition
            List<TTBeanDefinition> beanDefinitions = reader.loadBeanDefinition();

            //把BeanDefinition缓存起来
            doRegisterBeanDefinition(beanDefinitions);

            doAutowired();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowired() {
        //此时并没有实例化bean只是配置
        for (Map.Entry<String, TTBeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }

    private void doRegisterBeanDefinition(List<TTBeanDefinition> beanDefinitions) throws Exception {
        for (TTBeanDefinition beanDefinition : beanDefinitions) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists !!!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    /**
     * bean实例化 依赖注入
     *
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {

        //拿到配置beanDefinition信息
        TTBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        //反射实例化
        Object instance = instantiateBean(beanName, beanDefinition);

        //封装beanWrapper
        TTBeanWrapper beanWrapper = new TTBeanWrapper(instance);

        //保存到IOC容器
        factoryBeanInstanceCache.put(beanName, beanWrapper);

        //依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return beanWrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, TTBeanDefinition beanDefinition, TTBeanWrapper beanWrapper) {

        //用两个缓存 ，循环两次
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值

        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = beanWrapper.getWrapperClass();

        if (!clazz.isAnnotationPresent(TTController.class) || clazz.isAnnotationPresent(TTService.class)){
            return;
        }
        //获取所有的参数 private、protect、public等
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(TTAutowired.class)) {
                continue;
            }
            TTAutowired autowired = field.getAnnotation(TTAutowired.class);

            //判断是否有自定义beanName
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                //获取字段的类型名称
                autowiredBeanName = field.getType().getName();
            }

            //强制修改
            field.setAccessible(true);
            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    // 实例化
    private Object instantiateBean(String beanName, TTBeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if (factoryBeanObjectCache.containsKey(beanName)){
                instance = factoryBeanObjectCache.get(beanName);
            }else {
                Class<?> clazz = Class.forName(beanClassName);
                instance = clazz.newInstance();

                //如果满足条件，就返回Proxy对象  AOP

                //加载Aop配置文件
                TTAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);

                //判断是否需要生成代理类，如果需要就覆盖原生对象  否则不处理返回原生对象

                if (config.pointCutMatch()){
                    instance = new TTJdkDynamicAopProxy(config).getProxy();
                }

                this.factoryBeanObjectCache.put(beanName, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private TTAdvisedSupport instantionAopConfig(TTBeanDefinition beanDefinition) {
        TTAopConfig config = new TTAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));


        return new TTAdvisedSupport(config);
    }

    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }

    public int getBeanDefinitonCount() {
       return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitonNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();

    }
}
