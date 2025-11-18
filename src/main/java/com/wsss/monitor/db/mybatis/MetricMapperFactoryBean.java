package com.wsss.monitor.db.mybatis;

import com.wsss.monitor.utils.Metrics;
import com.wsss.monitor.utils.MetricsMethodInterceptor;
import com.wsss.monitor.utils.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;

@Slf4j
public class MetricMapperFactoryBean<T> implements FactoryBean<T> {
    private MapperFactoryBean<T> mapperFactoryBean;
    private ApplicationContext applicationContext;

    public MetricMapperFactoryBean(MapperFactoryBean<T> mapperFactoryBean, ApplicationContext applicationContext) {
        this.mapperFactoryBean = mapperFactoryBean;
        this.applicationContext = applicationContext;
    }

    @Override
    public T getObject() throws Exception {
        Object target = mapperFactoryBean.getObject();
        String key = "mybatis_mapper_" + Metrics.getApplicationName(applicationContext) + "_" + mapperFactoryBean.getObjectType().getSimpleName();
        log.info("增加mybatis默认监控:{}", key);
        return (T) Proxy.getProxy(target,new MetricsMethodInterceptor(key));
    }

    @Override
    public Class<?> getObjectType() {
        return mapperFactoryBean.getObjectType();
    }

    @Override
    public boolean isSingleton() {
        return mapperFactoryBean.isSingleton();
    }
}
