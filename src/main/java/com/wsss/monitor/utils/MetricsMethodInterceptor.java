package com.wsss.monitor.utils;


import com.wsss.monitor.Monitor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class MetricsMethodInterceptor implements MethodInterceptor {
    private String METRICS_KEY;

    public MetricsMethodInterceptor(String metricsKey) {
        this.METRICS_KEY = metricsKey;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (Object.class.equals(invocation.getMethod().getDeclaringClass())) {
            return invocation.proceed();
        }
        Monitor.TimeContext timeContext = Monitor.timer(METRICS_KEY,invocation.getMethod().getName());
        try {
            return invocation.proceed();
        } catch (Exception e) {
            timeContext.error();
            throw e;
        } finally {
            timeContext.end();
        }

    }


}
