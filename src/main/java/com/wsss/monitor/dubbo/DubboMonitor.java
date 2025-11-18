/*

 * Copyright (c) 2022 superatomfin.com. All Rights Reserved.

 */
package com.wsss.monitor.dubbo;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.threadpool.manager.ExecutorRepository;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author liudashuang  Date: 2022-12-08 Time: 17:41
 * 适合2.7.8以上版本
 */
@Slf4j
public class DubboMonitor  {

    private MeterRegistry meterRegistry;

    public DubboMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void init(MeterRegistry registry) {
        ExecutorRepository executorRepository = ExtensionLoader.getExtensionLoader(ExecutorRepository.class).getDefaultExtension();
        Collection<Exporter<?>> exporters = DubboProtocol.getDubboProtocol().getExporters();
        exporters.forEach(exporter -> {
            ExecutorService executorService = executorRepository.getExecutor(exporter.getInvoker().getUrl());
            String executorServiceName = executorServiceName(exporter.getInvoker().getUrl());
            ExecutorServiceMetrics executorServiceMetrics = new ExecutorServiceMetrics(executorService, executorServiceName, tags());
            executorServiceMetrics.bindTo(registry);
        });
    }

    protected List<Tag> tags() {
        List<Tag> tags = new ArrayList<>(1);
        return tags;
    }

    protected String executorServiceName(URL url) {
        return "dubboThreadPoolName";
    }


}

