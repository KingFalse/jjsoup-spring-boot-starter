package me.kagura;

import me.kagura.anno.JSONBodyField;
import org.apache.commons.io.IOUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Order(-1102)
@Conditional(value = ConditionalAspectJSONBodyField.class)
@Component
@Aspect
public class AspectJSONBodyField {

    Class<?> class_RequestBody = Class.forName("org.springframework.web.bind.annotation.RequestBody");
    Method method_getRequestAttributes = Class.forName("org.springframework.web.context.request.RequestContextHolder").getDeclaredMethod("getRequestAttributes", null);
    Method method_getRequest = Class.forName("org.springframework.web.context.request.ServletRequestAttributes").getDeclaredMethod("getRequest", null);
    Class<?> class_HttpServletRequest = Class.forName("javax.servlet.http.HttpServletRequest");
    Method method_getReader = class_HttpServletRequest.getMethod("getReader", null);
    Method method_getMethod = class_HttpServletRequest.getMethod("getMethod", null);
    Method method_getHeader = class_HttpServletRequest.getMethod("getHeader", String.class);
    Method method_read = Class.forName("com.alibaba.fastjson.JSONPath").getDeclaredMethod("read", String.class, String.class);

    public AspectJSONBodyField() throws ClassNotFoundException, NoSuchMethodException {
    }

    @Around(value = "@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller) && args(*,..)", argNames = "joinPoint")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object request = method_getRequest.invoke(method_getRequestAttributes.invoke(null, null), null);
        if (!"POST".equals(method_getMethod.invoke(request, null))) {
            return joinPoint.proceed();
        }
        Object contentType = method_getHeader.invoke(request, "Content-Type");
        if (contentType == null) {
            return joinPoint.proceed();
        }
        if (!((String) contentType).contains("application/json")) {
            return joinPoint.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        String[] parameterNames = methodSignature.getParameterNames();
        Parameter[] parameters = targetMethod.getParameters();

        boolean isRequestBody = false;
        boolean isStringRequestBody = false;
        String stringRequestBody = "";
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(class_RequestBody.asSubclass(Annotation.class)) != null) {
                isRequestBody = true;
                isStringRequestBody = parameters[i].getType().equals(String.class);
                if (isStringRequestBody)
                    stringRequestBody = (String) joinPoint.getArgs()[i];
                break;
            }
        }
        String body;
        if (isRequestBody && isStringRequestBody) {
            body = stringRequestBody;
        } else {
            body = IOUtils.toString((Reader) method_getReader.invoke(request, null));
        }
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            JSONBodyField annotation = parameter.getAnnotation(JSONBodyField.class);
            if (annotation != null) {
                String fieldPath = annotation.value().equals("") ? "$." + parameterNames[i] : annotation.value();
                Object val = read(body, fieldPath);
                if (parameter.getType().equals(String.class)) {
                    args[i] = String.valueOf(read(body, fieldPath));
                } else if (parameter.getType().equals(Integer.class)) {
                    if (val instanceof Integer) {
                        args[i] = val;
                    } else {
                        args[i] = Integer.parseInt((String) val);
                    }
                } else if (parameter.getType().equals(Double.class)) {
                    if (val instanceof Double) {
                        args[i] = val;
                    } else {
                        args[i] = Double.parseDouble((String) val);
                    }
                } else if (parameter.getType().equals(Float.class)) {
                    if (val instanceof Double) {
                        args[i] = val;
                    } else {
                        args[i] = Float.parseFloat((String) val);
                    }
                } else if (parameter.getType().equals(Long.class)) {
                    if (val instanceof Double) {
                        args[i] = val;
                    } else {
                        args[i] = Long.parseLong((String) val);
                    }
                } else if (parameter.getType().equals(Character.class)) {
                    if (val instanceof Character) {
                        args[i] = val;
                    } else {
                        if (((String) val).length() > 0) {
                            args[i] = ((String) val).charAt(0);
                        }
                    }
                }
            }
        }

        return joinPoint.proceed(args);

    }

    private Object read(String body, String path) {
        try {
            return method_read.invoke(null, body, path);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class ConditionalAspectJSONBodyField implements Condition {

    Logger logger = LoggerFactory.getLogger(JJsoup.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        try {
            Class.forName("org.springframework.web.bind.annotation.RequestBody");
        } catch (Exception e) {
            return false;
        }
        try {
            Class.forName("com.alibaba.fastjson.JSONPath");
            return true;
        } catch (Exception e) {
            logger.info("If you want to use @JSONBodyField please add fastjson dependencies.");
            return false;
        }

    }

}