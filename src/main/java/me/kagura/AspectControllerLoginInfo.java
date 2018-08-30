package me.kagura;

import me.kagura.anno.LoginInfoKey;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Order(-1101)
@Component
@Aspect
public class AspectControllerLoginInfo {

    @Autowired(required = false)
    LoginInfoSerializable loginInfoSerializable;

    @Around(value = "@within(me.kagura.anno.LoginInfoKey) || @within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController) && args(*,*,..)", argNames = "joinPoint")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (loginInfoSerializable == null) {
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        String[] parameterNames = signature.getParameterNames();

        LoginInfoKey annoLoginInfoKey = AnnotationUtils.findAnnotation(signature.getMethod().getDeclaringClass(), LoginInfoKey.class);

        int loginInfoKeyIndex = -1;
        int loginInfoIndex = -1;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (annoLoginInfoKey != null && parameterNames[i].equals(annoLoginInfoKey.value())) {
                loginInfoKeyIndex = i;
            }
            if (parameter.getAnnotation(LoginInfoKey.class) != null) {
                loginInfoKeyIndex = i;
            }
            if (parameter.getType().equals(LoginInfo.class)) {
                loginInfoIndex = i;
            }
        }
        if (loginInfoIndex > -1 && loginInfoKeyIndex > -1) {
            String loginInfoKey = args[loginInfoKeyIndex].toString();
            LoginInfo loginInfo = loginInfoSerializable.getLoginInfo(loginInfoKey);
            args[loginInfoIndex] = loginInfo;
            return joinPoint.proceed(args);

        }
        return joinPoint.proceed();
    }
}