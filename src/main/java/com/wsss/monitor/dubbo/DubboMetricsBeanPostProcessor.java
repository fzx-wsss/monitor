package com.wsss.monitor.dubbo;

import com.wsss.monitor.utils.Metrics;
import com.wsss.monitor.utils.MetricsMethodInterceptor;
import com.wsss.monitor.utils.Proxy;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "actuator.rpc.dubbo.enhance.enable", havingValue = "true",matchIfMissing = true)
public class DubboMetricsBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware {
    static ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof ServiceBean) {
            ServiceBean serviceBean = (ServiceBean) bean;
            Object ref = serviceBean.getRef();
            String key = "p_" + Metrics.getApplicationName(applicationContext) + "_" + ref.getClass().getSimpleName();
            log.info("增加dubbo service默认监控:{}", key);
            serviceBean.setRef(Proxy.getProxy(ref,new MetricsMethodInterceptor(key)));
            return bean;
        }

        if(bean instanceof ReferenceBean) {
            ReferenceBean referenceBean = (ReferenceBean) Proxy.getProxy(bean,new MethodInterceptor() {
                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    Object result = invocation.proceed();
                    if("getObject".equals(invocation.getMethod().getName())) {
                        String name = ((ReferenceBean<?>) bean).getInterfaceClass().getSimpleName();
                        String key = "c_" + Metrics.getApplicationName(applicationContext) + "_" + name;
                        log.info("增加dubbo reference默认监控:{}", key);
                        return Proxy.getProxy(result,new MetricsMethodInterceptor(key));
                    }
                    return result;
                }
            });
            return referenceBean;
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
