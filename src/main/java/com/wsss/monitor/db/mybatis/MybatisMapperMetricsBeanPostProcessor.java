package com.wsss.monitor.db.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "actuator.db.mybatis.enhance.enable", havingValue = "true", matchIfMissing = true)
public class MybatisMapperMetricsBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    static ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof MapperFactoryBean) {
            return new MetricMapperFactoryBean((MapperFactoryBean) bean, applicationContext);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
