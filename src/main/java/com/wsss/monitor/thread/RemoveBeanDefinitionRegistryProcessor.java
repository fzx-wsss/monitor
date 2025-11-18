package com.wsss.monitor.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "actuator.bean.definition.remove.enable", havingValue = "true")
public class RemoveBeanDefinitionRegistryProcessor implements BeanDefinitionRegistryPostProcessor {
    private final String remove = "jvmThreadMetrics";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if(registry.containsBeanDefinition(remove)) {
            registry.removeBeanDefinition(remove);
            log.info("Removed bean definition: {}", remove);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
