package com.taest.v2.spring.framework.aop;

import com.taest.v2.spring.framework.aop.support.TTAdvisedSupport;

public class TTDefaultAopProxyFactory {

    public TTAopProxy createAopProxy(TTAdvisedSupport config){
        Class targetClass = config.getTargetClass();
        if(targetClass.getInterfaces().length > 0){
            return new TTJdkDynamicAopProxy(config);
        }
        return new TTCglibAopProxy();
    }
}
