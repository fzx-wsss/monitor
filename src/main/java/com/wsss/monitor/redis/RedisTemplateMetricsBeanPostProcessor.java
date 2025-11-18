package com.wsss.monitor.redis;

import com.wsss.monitor.utils.Metrics;
import com.wsss.monitor.utils.MetricsMethodInterceptor;
import com.wsss.monitor.utils.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "actuator.redis.template.enhance.enable", havingValue = "true", matchIfMissing = true)
public class RedisTemplateMetricsBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    static ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RedisTemplate) {
            RedisTemplate redisTemplate = (RedisTemplate) bean;

            RedisConnectionFactory redisConnectionFactory = redisTemplate.getConnectionFactory();
            String key = "redis_metrics_" + Metrics.getApplicationName(applicationContext);
            log.info("增加redisTemplate默认监控:{}", key);
            RedisConnectionFactory proxy = (RedisConnectionFactory) Proxy.getProxy(redisConnectionFactory, new MethodInterceptor() {
                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    Object result = invocation.proceed();
                    if ("getConnection".equals(invocation.getMethod().getName())) {
                        return Proxy.getProxy(result, new MetricsMethodInterceptor("redis_metrics_" + Metrics.getApplicationName(applicationContext)));
                    }
                    return result;
                }
            });
            redisTemplate.setConnectionFactory(proxy);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
