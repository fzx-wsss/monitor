package com.wsss.monitor.dubbo;

import com.wsss.monitor.Monitor;
import com.wsss.monitor.utils.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wsss.monitor.dubbo.DubboMetricsBeanPostProcessor.applicationContext;

@Slf4j
public abstract class DubboRpcMonitorFilter implements Filter {
    private Map<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            if(!isOpen()) {
                return invoker.invoke(invocation);
            }
            Monitor.TimeContext context = buildContext(invoker, invocation);
            try {
                Result result = invoker.invoke(invocation);
                if(result.hasException()) {
                    context.error();
                    log.error("",result.getException());
                }
                return result;
            } finally {
                context.end();
            }
        } catch (Throwable e) {
            log.error("",e);
            throw new RuntimeException("系统异常:"+e.getMessage());
        }
    }

    protected boolean isOpen() {
        return DubboMetricsBeanPostProcessor.isOpen();
    }

    private Monitor.TimeContext buildContext(Invoker<?> invoker, Invocation invocation) {
        String key = cache.get(invocation.getTargetServiceUniqueName());
        if (key == null) {
            key = cache.computeIfAbsent(invocation.getTargetServiceUniqueName(), k -> {
                String pre = isProviderSide() ? "p_" : "c_";
                String application = Metrics.getApplicationName(applicationContext);
                return pre + application + "_" +invoker.getInterface().getSimpleName() + "_" + invocation.getMethodName();
            });
        }
        return Monitor.timer(key);
    }

    protected abstract URL getUrl();

    protected abstract boolean isProviderSide();
}
