package me.kagura.impl;

import me.kagura.LoginInfo;
import me.kagura.LoginInfoSerializable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class DefaultRedisLoginInfoSerializable implements LoginInfoSerializable, ApplicationContextAware {

    ApplicationContext applicationContext;
    Object stringRedisTemplate;
    Class<?> jdkSerializationRedisSerializer;
    Method serialize;
    Method deserialize;

    @Override
    public void setLoginInfo(LoginInfo loginInfo) {
        try {
            byte[] bytes = (byte[]) serialize.invoke(jdkSerializationRedisSerializer.newInstance(), loginInfo);
            String loginInfoSerializeString = Base64.getEncoder().encodeToString(bytes);

            Method opsForValue = stringRedisTemplate.getClass().getMethod("opsForValue");
            Object valueOperations = opsForValue.invoke(stringRedisTemplate, null);
            Method set = valueOperations.getClass().getMethod("set", Object.class, Object.class, long.class, TimeUnit.class);
            set.setAccessible(true);
            set.invoke(valueOperations, loginInfo.traceID, loginInfoSerializeString, 1000, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public LoginInfo getLoginInfo(String key) {
        try {
            Method opsForValue = stringRedisTemplate.getClass().getMethod("opsForValue");
            Object valueOperations = opsForValue.invoke(stringRedisTemplate, null);
            Method set = valueOperations.getClass().getMethod("get", Object.class);
            set.setAccessible(true);
            String val = (String) set.invoke(valueOperations, key);
            byte[] decode = Base64.getDecoder().decode(val);

            LoginInfo loginInfo = (LoginInfo) deserialize.invoke(jdkSerializationRedisSerializer.newInstance(), decode);
            return loginInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        try {
            stringRedisTemplate = applicationContext.getBean("stringRedisTemplate");
            jdkSerializationRedisSerializer = Class.forName("org.springframework.data.redis.serializer.JdkSerializationRedisSerializer");
            serialize = jdkSerializationRedisSerializer.getMethod("serialize", Object.class);
            deserialize = jdkSerializationRedisSerializer.getMethod("deserialize", byte[].class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
