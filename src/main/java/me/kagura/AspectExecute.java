package me.kagura;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;

@Order(-1)
@Component
@Aspect
public class AspectExecute {

    @Autowired(required = false)
    FollowFilter followFilter;
    private String lineSeparator = System.lineSeparator();
    private Logger logger = LoggerFactory.getLogger(JJsoup.class);

    //两个切入点均可
    //@Around(value = "execution(* me.kagura.*.execute(..))")
    @Around(value = "execution(* org.jsoup.*.execute(..))")
    public Object Around(ProceedingJoinPoint joinPoint) throws Throwable {
        JJsoup targetJJsoup = (JJsoup) joinPoint.getTarget();
        Connection targetConnection = (Connection) targetJJsoup;

        //自动判断application/json
        Connection.Request targetRequest = targetConnection.request();
        if (targetRequest.method().hasBody() && targetRequest.requestBody() != null) {
            String requestBody = targetRequest.requestBody().trim();
            if ((requestBody.startsWith("{") && requestBody.endsWith("}")) || requestBody.startsWith("[") && requestBody.endsWith("]")) {
                targetConnection.header("Content-Type", "application/json;charset=" + targetRequest.postDataCharset());
            }
        }
        logRequest(targetConnection);
        long startTime = System.currentTimeMillis();
        Object response = null;
        Exception exception = null;
        int retryCount = targetJJsoup.retryCount > 0 ? targetJJsoup.retryCount : 1;
        for (int i = 0; i < retryCount; i++) {
            exception = null;
            try {
                response = joinPoint.proceed();
                if (followFilter != null) {
                    try {
                        followFilter.doFilter(targetConnection, targetJJsoup.loginInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (targetJJsoup.followProcess != null) {
                    if (targetJJsoup.followProcess.isSuccess(targetConnection, targetJJsoup.loginInfo)) {
                        break;
                    } else {
                        logRetryLogic(targetConnection, (i + 1), retryCount);
                    }
                }
                break;
            } catch (Exception e) {
                exception = e;
                logRetryException(targetConnection, exception, (i + 1), retryCount);
            }
        }
        if (targetJJsoup.followProcess != null && exception != null) {
            targetJJsoup.followProcess.doException(exception);
        } else if (targetJJsoup.followProcess == null && exception != null) {
            throw exception;
        } else if (response != null) {
            appendCookie(targetJJsoup, (Connection.Response) response);
            logResponse(targetConnection, System.currentTimeMillis() - startTime);
            if (targetJJsoup.followProcess != null) {
                targetJJsoup.followProcess.result = targetJJsoup.followProcess.doProcess(targetConnection, targetJJsoup.loginInfo);
            }
            return response;
        }
        return null;
    }

    private void logRetryException(Connection target, Exception e, int index, int retryCount) {
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

    private void logRetryLogic(Connection target, int index, int retryCount) {
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
    private void appendCookie(JJsoup target, Connection.Response response) {
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
    private void logRequest(Connection target) {
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
        sb.append("\tProxy         :    " + req.proxy());
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
    private void logResponse(Connection target, long time) {
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