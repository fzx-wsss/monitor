package com.wsss.monitor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.function.ToDoubleFunction;

/**
 * 监控
 *
 * @dubbo
 */
@Slf4j
@Configuration
public class Monitor {
    private static RegistryInstanceManager registryInstanceManager;
    public static final String IP;
    @Resource
    private MeterRegistry registry;

    static {
        try {
            String tempIp = InetAddress.getLocalHost().getHostAddress();
            if("127.0.0.1".equals(tempIp)) {
                InetAddress address = getAddress();
                if(address != null) {
                    tempIp = address.getHostAddress();
                }
            }
            IP = tempIp;
            log.info("Monitor Ip:{}", IP);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static InetAddress getAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                    return addresses.nextElement();
                }
            }
        } catch (SocketException e) {
            log.debug("Error when getting host ip address: <{}>.", e.getMessage());
        }
        return null;
    }

    @Bean
    MeterRegistryCustomizer<MeterRegistry> configurer() {
        return (registry) -> registry.config().commonTags("host", Monitor.IP);
    }

    public static TimeContext timer(String key) {
        return timer(key, null);
    }

    public static TimeContext timer(String key, String tag) {
        TimeContext timeContext = new TimeContext();
        timeContext.startTime = System.currentTimeMillis();
        timeContext.key = key;
        timeContext.tag = tag;
        return timeContext;
    }

    public static CountContext counter(String key) {
        return counter(key, null);
    }

    public static CountContext counter(String key, String tag) {
        CountContext context = new CountContext();
        context.key = key;
        context.tag = tag;
        return context;
    }

    /**
     * 不能为静态方法
     * 因为一般都是在启动阶段去创建gauge类型的监控，如果是静态方法可能会因为启动顺序导致registry还未注入
     */
    public <T> void gauge(String key, @Nullable T obj, ToDoubleFunction<T> f) {
        Gauge.builder(key, obj, f).register(registry);
    }

    public <T> void gauge(String key,String tag, @Nullable T obj, ToDoubleFunction<T> f) {
        Gauge.builder(key, obj, f).tag("tag", tag).register(registry);
    }

    public static class CountContext {
        private String key;
        private String tag;

        public void end() {
            end(1D);
        }

        public void end(double num) {
            if (registryInstanceManager == null) {
                log.warn("registryInstanceManager is null,key:{}", key);
                return;
            }
            registryInstanceManager.count(key, tag, num);
        }
    }

    public static class TimeContext {
        private String key;
        private String tag;
        private long startTime;


        public long end() {
            long endTime = System.currentTimeMillis();
            long totalTime = (endTime - startTime);
            return end(totalTime);
        }

        public long end(long totalTime) {
            if (registryInstanceManager == null) {
                log.warn("registryInstanceManager is null,key:{}", key);
                return totalTime;
            }

            registryInstanceManager.record(key, tag, totalTime);
            return totalTime;
        }

        public void error() {
            if (registryInstanceManager == null) {
                log.warn("counterInstanceManager is null,key:{}", key);
                return;
            }
            String reaTag = tag == null ? key : key + "_" + tag;
            registryInstanceManager.count("monitor_key_error", reaTag, 1);
        }
    }

    @Resource
    public void setRegistryInstanceManager(RegistryInstanceManager registryInstanceManager) {
        Monitor.registryInstanceManager = registryInstanceManager;
    }

}