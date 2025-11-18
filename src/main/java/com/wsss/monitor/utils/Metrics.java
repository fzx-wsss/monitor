package com.wsss.monitor.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

public class Metrics {
    public static String getApplicationName(ApplicationContext applicationContext) {
        if(applicationContext != null) {
            String name = applicationContext.getEnvironment().getProperty("spring.application.name");
            if(!StringUtils.isEmpty(name)) return name;

            name = applicationContext.getApplicationName();
            if(!StringUtils.isEmpty(name)) return name;
        }
        String name = System.getenv("SUPERVISOR_PROCESS_NAME");
        if(!StringUtils.isEmpty(name)) return name;

        name = System.getProperty("java.class.path");
        if(!StringUtils.isEmpty(name)) return name;

        name = System.getProperty("user.dir");
        name = name.substring(name.lastIndexOf("/"));
        if(!StringUtils.isEmpty(name)) return name;
        return "unknown";
    }
}
