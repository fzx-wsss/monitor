package com.wsss.monitor;

import com.wsss.registry.Registry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author liudashuang
 */
@Slf4j
@Component
public class RegistryInstanceManager {

    @Value("${app.monitor.warn.time:2000}")
    private long warnTime = 2000;

    @Resource
    private List<Registry> registryList;

    public void record(String key, long time) {
        record(key, null, time);
    }

    public void record(String key, String tag, long time) {
        if (warnTime > 0 && time > warnTime) {
            log.warn("key:{},tag:{} is invoke too long,time:{}", key, tag, time);
        }
        registryList.forEach(registry -> registry.record(key, tag, time));
    }

    public void count(String key) {
        count(key,1D);
    }

    public void count(String key,String tag) {
        count(key,tag,1D);
    }

    public void count(String key,double count) {
        count(key,null,count);
    }

    public void count(String key,String tag, double count) {
        if(count <= 0D) return;
        if(tag == null) tag = "";

        for (Registry registry : registryList) {
            registry.count(key, tag, count);
        }
    }

}
