package com.wsss.monitor.dubbo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "actuator.rpc.dubbo.enhance.enable", havingValue = "true", matchIfMissing = true)
public class DubboMetricsBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    static ApplicationContext applicationContext;
    static DubboMetricsBeanPostProcessor instance;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DubboMetricsBeanPostProcessor.applicationContext = applicationContext;
        DubboMetricsBeanPostProcessor.instance = this;
    }

    public static boolean isOpen() {
        return instance != null;
    }
}
