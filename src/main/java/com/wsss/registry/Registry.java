package com.wsss.registry;

public interface Registry {

    void record(String key, String tag, long time);

    void count(String key, String tag, double count);
}
