package com.wsss.registry.seconds;

import com.wsss.monitor.utils.CacheMap;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Getter
public class SecondData {
    private long time;//以秒为单位
    private ConcurrentHashMap<String, SourceData> data;
    private CacheMap<String, ConcurrentHashMap<String, SourceData>> tagData;

    public SecondData(long time) {
        this.time = time;
        data = new ConcurrentHashMap<>();
        tagData = new CacheMap<>(k-> new ConcurrentHashMap<>());
    }

    public SourceData get(String key) {
        return data.get(key);
    }

    public SourceData get(String key,String tag) {
        if(tag == null || "".equals(tag)) {
            return data.get(key);
        }
        return tagData.get(key).get(tag);
    }

    public SourceData put(String key, SourceData value) {
        data.putIfAbsent(key, value);
        return get(key);
    }

    public SourceData put(String key, String tag, SourceData value) {
        if(tag == null || "".equals(tag)) {
            data.putIfAbsent(key, value);
        } else {
            tagData.get(key).putIfAbsent(tag, value);
        }
        return get(key,tag);
    }


}
