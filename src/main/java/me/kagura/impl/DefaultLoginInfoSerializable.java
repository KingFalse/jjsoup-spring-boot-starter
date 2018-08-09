package me.kagura.impl;

import me.kagura.LoginInfo;
import me.kagura.LoginInfoSerializable;
import me.kagura.util.SpringContextUtils;

import java.lang.reflect.Method;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class DefaultLoginInfoSerializable implements LoginInfoSerializable {

    @Override
    public void setLoginInfo(LoginInfo loginInfo) {
        try {
            Class<?> jdkSerializationRedisSerializer = Class.forName("org.springframework.data.redis.serializer.JdkSerializationRedisSerializer");
            Method serialize = jdkSerializationRedisSerializer.getMethod("serialize", Object.class);
            byte[] bytes = (byte[]) serialize.invoke(jdkSerializationRedisSerializer.newInstance(), loginInfo);
            String loginInfoSerializeString = Base64.getEncoder().encodeToString(bytes);

            Object stringRedisTemplate = SpringContextUtils.getBeanById("stringRedisTemplate");
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
            Object stringRedisTemplate = SpringContextUtils.getBeanById("stringRedisTemplate");
            Method opsForValue = stringRedisTemplate.getClass().getMethod("opsForValue");
            Object valueOperations = opsForValue.invoke(stringRedisTemplate, null);
            Method set = valueOperations.getClass().getMethod("get", Object.class);
            set.setAccessible(true);
            String val = (String) set.invoke(valueOperations, key);
            byte[] decode = Base64.getDecoder().decode(val);

            Class<?> jdkSerializationRedisSerializer = Class.forName("org.springframework.data.redis.serializer.JdkSerializationRedisSerializer");
            Method deserialize = jdkSerializationRedisSerializer.getMethod("deserialize", byte[].class);
            LoginInfo loginInfo = (LoginInfo) deserialize.invoke(jdkSerializationRedisSerializer.newInstance(), decode);
            return loginInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
