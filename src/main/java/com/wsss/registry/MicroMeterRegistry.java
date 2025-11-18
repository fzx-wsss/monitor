package com.wsss.registry;

import com.wsss.monitor.utils.CacheMap;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MicroMeterRegistry implements Registry {
    @Resource
    private MeterRegistry registry;

    // monitor tp50 tp90 and tp99
    private static final double[] percentiles = new double[]{0.5, 0.90, 0.99};

    private final Map<String, Map<String, Timer>> timerTagMap = new CacheMap<>(k -> new CacheMap<>(t -> createTimer(k, t)));
    private final Map<String, Map<String, Counter>> counterTagMap = new CacheMap<>(k->new CacheMap<>(t->createCounter(k,t)));

    public Timer get(String key, String tag) {
        if (tag == null) {
            tag = "";
        }
        return timerTagMap.get(key).get(tag);
    }

    private Timer createTimer(String key, String tag) {
        if (Objects.equals(tag, "")) {
            return Timer.builder(key).publishPercentiles(percentiles).register(registry);
        }
        return Timer.builder(key).tags("tag", tag).publishPercentiles(percentiles).register(registry);
    }

    public void record(String key, String tag, long time) {
        Timer timer = get(key, tag);
        timer.record(time, TimeUnit.MILLISECONDS);
    }

    public void count(String key,String tag, double count) {
        Counter counter = counterTagMap.get(key).get(tag);
        counter.increment(count);
    }



    private Counter createCounter(String key,String tag) {
        if(Objects.equals(tag, "")) {
            return Counter.builder(key).register(registry);
        }
        return Counter.builder(key).tag("tag",tag).register(registry);
    }

}
