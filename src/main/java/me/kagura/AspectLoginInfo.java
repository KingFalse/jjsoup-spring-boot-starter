package me.kagura;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(99)
@Component
@Aspect
public class AspectLoginInfo {

    @Autowired(required = false)
    LoginInfoSerializable loginInfoSerializable = null;
    Logger logger = LoggerFactory.getLogger(AspectLoginInfo.class);

    @AfterReturning(value = "within(@org.springframework.stereotype.Service *)", argNames = "joinPoint")
    public void AfterReturning(JoinPoint joinPoint) {
        if (loginInfoSerializable == null) {
            return;
        }
        logger.info("LoginInfo 自动保存...");
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class[] parameterTypes = methodSignature.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(LoginInfo.class)) {
                try {
                    loginInfoSerializable.setLoginInfo((LoginInfo) joinPoint.getArgs()[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @AfterThrowing(value = "within(@org.springframework.stereotype.Service *) ", argNames = "joinPoint")
    public void AfterThrowing(JoinPoint joinPoint) {
        AfterReturning(joinPoint);
    }

}