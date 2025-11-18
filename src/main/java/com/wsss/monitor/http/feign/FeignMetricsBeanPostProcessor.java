package com.wsss.monitor.http.feign;

import com.wsss.monitor.utils.Metrics;
import com.wsss.monitor.utils.MetricsMethodInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import com.wsss.monitor.utils.Proxy;

@Slf4j
@Component
@ConditionalOnClass(FeignClient.class)
@ConditionalOnProperty(
    name = {"actuator.http.feign.enhance.enable"},
    havingValue = "true",
    matchIfMissing = true
)
public class FeignMetricsBeanPostProcessor implements BeanPostProcessor,ApplicationContextAware, PriorityOrdered {
    static ApplicationContext applicationContext;

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if(!java.lang.reflect.Proxy.isProxyClass(bean.getClass())) {
            return bean;
        }

        for (Class<?> anInterface : bean.getClass().getInterfaces()) {
            if(anInterface.getAnnotation(FeignClient.class) != null) {
                String key = "feign_" + Metrics.getApplicationName(applicationContext) + "_" + anInterface.getSimpleName();
                log.info("增加feign默认监控:{}", key);
                return Proxy.getProxy(bean,new MetricsMethodInterceptor(key));
            }
        }
        return bean;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        FeignMetricsBeanPostProcessor.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}