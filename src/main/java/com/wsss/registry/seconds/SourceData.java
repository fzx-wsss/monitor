package com.wsss.registry.seconds;

import lombok.extern.slf4j.Slf4j;
import org.HdrHistogram.Histogram;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class SourceData {
    private LongAdder counter = new LongAdder();
    private Histogram histogram = new Histogram(1, TimeUnit.MINUTES.toMillis(1), 2);

    public void record(double count,long time) {
        counter.add((long) count);
        if(time > 0) {
            try {
                histogram.recordValue(time);
            } catch (Exception e) {
                log.error("SourceStatistician.record error,time:{}",time, e);
            }
        }
    }
    public long getCounter() {
        return counter.sum();
    }

    public long getTP99() {
        return histogram.getValueAtPercentile(99);
    }
    public long getTP90() {
        return histogram.getValueAtPercentile(90);
    }
    public long getTP50() {
        return histogram.getValueAtPercentile(50);
    }
    public long getMax() {
        return histogram.getMaxValue();
    }

}
