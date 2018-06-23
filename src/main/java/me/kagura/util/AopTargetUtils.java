package me.kagura.util;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * 用于从Spring 代理类中取出被代理的实际对象
 * 类似于环绕通知中的joinPoint.getTarget();
 */
public class AopTargetUtils {

    /**
     * 获取 目标对象
     *
     * @param beanInstance 代理对象
     * @return
     * @throws Exception
     */
    public static <T> T getTargetObject(Object beanInstance, Class<T> targetClass) throws Exception {
        if (!AopUtils.isAopProxy(beanInstance)) {
            return (T) beanInstance;
        }
        if (AopUtils.isJdkDynamicProxy(beanInstance)) {
            return (T) getJdkDynamicProxyTargetObject(beanInstance);
        } else if (AopUtils.isCglibProxy(beanInstance)) {
            return (T) getCglibProxyTargetObject(beanInstance);
        }
        return (T) beanInstance;

    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
        return target;
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        Object target = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
        return target;
    }

}