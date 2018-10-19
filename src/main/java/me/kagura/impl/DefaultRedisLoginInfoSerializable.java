package me.kagura.impl;

import me.kagura.LoginInfo;
import me.kagura.LoginInfoSerializable;
import me.kagura.enums.PropertiesEnum;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class DefaultRedisLoginInfoSerializable implements LoginInfoSerializable, ApplicationContextAware {

    ApplicationContext applicationContext;
    Object valueOperations;
    Method set2;
    Method set4;
    Method get;
    long timeout;

    @Override
    public void setLoginInfo(LoginInfo loginInfo) throws InvocationTargetException, IllegalAccessException {
        if (timeout > 0) {
            set4.invoke(valueOperations, loginInfo.key, loginInfo, timeout, TimeUnit.SECONDS);
        } else {
            set2.invoke(valueOperations, loginInfo.key, loginInfo);
        }

    }

    @Override
    public LoginInfo getLoginInfo(String key) throws InvocationTargetException, IllegalAccessException {
        Object val = get.invoke(valueOperations, key);
        if (val == null) {
            return null;
        }
        return (LoginInfo) val;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Environment environment = applicationContext.getEnvironment();
        try {
            if (environment.containsProperty(PropertiesEnum.LOGININFO_TIMEOUT.getValue())) {
                timeout = environment.getProperty(PropertiesEnum.LOGININFO_TIMEOUT.getValue(), long.class);
            }
            Object redisTemplate = applicationContext.getBean("redisTemplate");
            Method opsForValue = redisTemplate.getClass().getMethod("opsForValue");
            valueOperations = opsForValue.invoke(redisTemplate);
            set2 = valueOperations.getClass().getMethod("set", Object.class, Object.class);
            set2.setAccessible(true);
            set4 = valueOperations.getClass().getMethod("set", Object.class, Object.class, long.class, TimeUnit.class);
            set4.setAccessible(true);
            get = valueOperations.getClass().getMethod("get", Object.class);
            get.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
