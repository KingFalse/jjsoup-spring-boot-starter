package me.kagura.aspect;

import me.kagura.HttpConnection;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Order(-1)
@Component
@Aspect
public class AspectExecute {

    private String lineSeparator = System.lineSeparator();
    private Logger logger = LoggerFactory.getLogger(HttpConnection.class);

    //两个切入点均可
    //@Around(value = "execution(* me.kagura.*.execute(..))")
    @Around(value = "execution(* org.jsoup.*.execute(..))")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpConnection target = (HttpConnection) joinPoint.getTarget();
        //自动判断application/json
        Connection.Request targetRequest = target.request();
        if (targetRequest.method().hasBody() && targetRequest.requestBody() != null) {
            String requestBody = targetRequest.requestBody().trim();
            if ((requestBody.startsWith("{") && requestBody.endsWith("}")) || requestBody.startsWith("[") && requestBody.endsWith("]")) {
                target.header("Content-Type", "application/json;charset=" + targetRequest.postDataCharset());
            }
        }
        logRequest(target);
        long startTime = System.currentTimeMillis();
        Object response = null;
        Exception exception = null;
        int retryCount = target.retryCount > 0 ? target.retryCount : 1;
        for (int i = 0; i < retryCount; i++) {
            exception = null;
            try {
                response = joinPoint.proceed();
                if (target.followProcess != null) {
                    if (target.followProcess.isSuccess(target)) {
                        break;
                    } else {
                        logRetryLogic(target, (i + 1), retryCount);
                    }
                }
                break;
            } catch (Exception e) {
                exception = e;
                logRetryException(target, exception, (i + 1), retryCount);
            }
        }
        if (target.followProcess != null && exception != null) {
            target.followProcess.doException(exception);
        } else if (target.followProcess == null && exception != null) {
            throw exception;
        } else if (response != null) {
            appendCookie(target, (Connection.Response) response);
            logResponse(target, System.currentTimeMillis() - startTime);
            if (target.followProcess != null) {
                target.followProcess.result = target.followProcess.doProcess(target);
            }
            return response;
        }
        return null;
    }

    private void logRetryException(HttpConnection target, Exception e, int index, int retryCount) {
        StringBuffer sb = new StringBuffer(lineSeparator);
        sb.append("RquestFailureRetry-" + index + "/" + retryCount + "----------------------------------->");
        sb.append(target.request());
        sb.append(lineSeparator);
        sb.append("\tReason : Exception");
        sb.append(lineSeparator);
        sb.append("\tException : " + e.toString());
        sb.append(lineSeparator);
        sb.append("RquestFailureRetry-" + index + "/" + retryCount + "----------------------------------->");
        sb.append(target.request());
        logger.info(sb.toString());
    }

    private void logRetryLogic(HttpConnection target, int index, int retryCount) {
        StringBuffer sb = new StringBuffer(lineSeparator);
        sb.append("RquestFailureRetry-" + index + "/" + retryCount + "----------------------------------->");
        sb.append(target.request());
        sb.append(lineSeparator);
        sb.append("\tReason : Logic");
        sb.append(lineSeparator);
        sb.append("RquestFailureRetry-" + index + "/" + retryCount + "----------------------------------->");
        sb.append(target.request());
        logger.info(sb.toString());
    }

    /**
     * 用于自动维护cookie
     *
     * @param target
     * @param response
     */
    private void appendCookie(HttpConnection target, Connection.Response response) {
        if (target.loginInfo != null) {
            Map<String, String> cookies = response.cookies();
            if (!cookies.isEmpty()) {
                target.loginInfo.cookies.putAll(cookies);
            }
        }
    }

    /**
     * 日志打印Request
     *
     * @param target
     */
    private void logRequest(HttpConnection target) {
        Connection.Request req = target.request();
        Connection.Method method = req.method();
        StringBuffer sb = new StringBuffer(lineSeparator);
        sb.append("req------------------------------------>");
        sb.append(req);
        sb.append(lineSeparator);
        sb.append("\tMethod        :    " + method);
        sb.append(lineSeparator);
        sb.append("\tURL           :    " + req.url());
        sb.append(lineSeparator);
        sb.append("\tHeaders       :    " + req.headers().toString());
        sb.append(lineSeparator);
        sb.append("\tCookies       :    " + req.cookies().toString());
        sb.append(lineSeparator);
        if (method.hasBody()) {
            if (req.requestBody() != null) {
                sb.append("\tBody          :    ");
                sb.append(lineSeparator);
                sb.append("\t\tAs String       :    " + req.requestBody());
                sb.append(lineSeparator);
            }
            if (req.data().size() > 0) {
                sb.append("\tBody      :    ");
                sb.append(lineSeparator);
                sb.append("\t\tAs Collection   :    " + req.data());
                sb.append(lineSeparator);
            }
        }
        sb.append("req------------------------------------>");
        sb.append(req);
        logger.info(sb.toString());
    }

    /**
     * 日志打印Response
     *
     * @param target
     */
    private void logResponse(HttpConnection target, long time) {
        StringBuffer sb = new StringBuffer(lineSeparator);
        Connection.Response res = target.response();
        sb.append("res------------------------------------>");
        sb.append(target.request());
        sb.append(lineSeparator);
        sb.append("\tStatusCode    :    " + res.statusCode());
        sb.append(lineSeparator);
        sb.append("\tStatusMessage :    " + res.statusMessage());
        sb.append(lineSeparator);
        sb.append("\tContentType   :    " + res.contentType());
        sb.append(lineSeparator);
        sb.append("\tCharset       :    " + res.charset());
        sb.append(lineSeparator);
        sb.append("\tSetCookies    :    " + res.cookies());
        sb.append(lineSeparator);
        sb.append("\tDuration      :    " + time + "ms");
        sb.append(lineSeparator);
        sb.append("res------------------------------------>");
        sb.append(target.request());
        logger.info(sb.toString());
    }


}