package com.wsss.monitor.dubbo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class DubboUtils {

    public static <T> List<T> getOptionalBeanByType(ListableBeanFactory beanFactory, Class<T> beanType) {
        String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, beanType, true, false);
        if (beanNames != null && beanNames.length != 0) {
            if (beanNames.length > 1) {
                return getBeans(beanFactory,beanNames,beanType);
            } else {
                return Arrays.asList(getBean(beanFactory,beanNames[0],beanType));
            }
        } else {
            return null;
        }
    }

    public static <T> T getBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) throws BeansException {
        return beanFactory.getBean(beanName, beanType);
    }

    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) throws BeansException {
        if (ObjectUtils.isEmpty(beanNames)) {
            return Collections.emptyList();
        } else {
            List<T> beans = new ArrayList(beanNames.length);
            String[] var4 = beanNames;
            int var5 = beanNames.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String beanName = var4[var6];
                T bean = getBean(beanFactory, beanName, beanType);
                if (bean != null) {
                    beans.add(bean);
                }
            }

            return Collections.unmodifiableList(beans);
        }
    }
}
