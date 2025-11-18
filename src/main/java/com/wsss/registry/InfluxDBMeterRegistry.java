package com.wsss.registry;

import com.wsss.registry.seconds.SourceStatistician;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class InfluxDBMeterRegistry implements Registry {
    @Value("${app.monitor.seconds.keys:[]}")
    private Set<String> secondsKeys = new HashSet<>();

    @Resource
    private SourceStatistician sourceStatistician;

    public void record(String key, String tag, long time) {
        if(secondsKeys.contains(key)) {
            sourceStatistician.record(key,tag,1D, time);
        }
    }

    public void count(String key,String tag, double count) {
        if(secondsKeys.contains(key)) {
            sourceStatistician.record(key,tag,count,0L);
        }
    }
}
