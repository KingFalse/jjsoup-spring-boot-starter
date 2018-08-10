package me.kagura.impl;

import me.kagura.LoginInfo;
import me.kagura.LoginInfoSerializable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class DefaultRedisLoginInfoSerializable implements LoginInfoSerializable, ApplicationContextAware {

    ApplicationContext applicationContext;
    Object valueOperations;
    Method set;
    Method get;

    @Override
    public void setLoginInfo(LoginInfo loginInfo) throws InvocationTargetException, IllegalAccessException {
        set.invoke(valueOperations, loginInfo.traceID, loginInfo, 1000, TimeUnit.SECONDS);

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
        try {
            Object redisTemplate = applicationContext.getBean("redisTemplate");
            Method opsForValue = redisTemplate.getClass().getMethod("opsForValue");
            valueOperations = opsForValue.invoke(redisTemplate);
            Method set = valueOperations.getClass().getMethod("set", Object.class, Object.class, long.class, TimeUnit.class);
            set.setAccessible(true);
            Method get = valueOperations.getClass().getMethod("get", Object.class);
            get.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
