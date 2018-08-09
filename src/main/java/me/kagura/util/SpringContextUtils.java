package me.kagura.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@SuppressWarnings({"rawtypes", "unchecked"})
public class SpringContextUtils implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext arg0)
            throws BeansException {
        applicationContext = arg0;
    }

    public static Object getBeanById(String id) {
        return applicationContext.getBean(id);
    }

    public static Object getBeanByClass(Class c) {
        return applicationContext.getBean(c);
    }

    public static Map getBeansByClass(Class c) {
        return applicationContext.getBeansOfType(c);
    }
}