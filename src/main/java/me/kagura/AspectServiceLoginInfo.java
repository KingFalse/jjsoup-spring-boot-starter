package me.kagura;

import me.kagura.anno.LoginInfoSolidify;
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
public class AspectServiceLoginInfo {

    @Autowired(required = false)
    LoginInfoSerializable loginInfoSerializable = null;
    Logger logger = LoggerFactory.getLogger(AspectServiceLoginInfo.class);

    @AfterReturning(value = "within(@org.springframework.stereotype.Service *) && args(*,..)", argNames = "joinPoint")
    public void AfterReturning(JoinPoint joinPoint) {
        if (loginInfoSerializable == null) {
            return;
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        LoginInfoSolidify loginInfoSolidify = methodSignature.getMethod().getDeclaringClass().getAnnotation(LoginInfoSolidify.class);
        boolean solidify = true;
        if (loginInfoSolidify != null) {
            solidify = loginInfoSolidify.value();
        }
        loginInfoSolidify = methodSignature.getMethod().getAnnotation(LoginInfoSolidify.class);
        if (loginInfoSolidify != null) {
            solidify = loginInfoSolidify.value();
        }
        if (!solidify) {
            return;
        }
        logger.info("LoginInfo 自动保存...");
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