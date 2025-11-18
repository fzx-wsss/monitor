package com.wsss.monitor.http;

import com.wsss.monitor.utils.Metrics;
import com.wsss.monitor.utils.MetricsMethodInterceptor;
import com.wsss.monitor.utils.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Component
@ConditionalOnProperty(name = "actuator.http.enhance.enable", havingValue = "true", matchIfMissing = true)
public class EnhanceMetricsBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    static ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        if (AnnotatedElementUtils.hasAnnotation(clazz, Controller.class) || AnnotatedElementUtils.hasAnnotation(clazz, RequestMapping.class)) {
            String key = "http_" + Metrics.getApplicationName(applicationContext) + "_" + clazz.getSimpleName();
            log.info("增加http默认监控:{}", key);
            return Proxy.getProxy(bean, new MetricsMethodInterceptor(key));
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        EnhanceMetricsBeanPostProcessor.applicationContext = applicationContext;
    }
}
