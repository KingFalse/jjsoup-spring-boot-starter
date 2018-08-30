package me.kagura;

import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AopExecute {

    static String lineSeparator = System.lineSeparator();
    static Logger logger = LoggerFactory.getLogger(JJsoup.class);
    static FollowFilter followFilter;

    public static void contentTypeJson(Connection connection) {
        Connection.Request targetRequest = connection.request();
        if (targetRequest.method().hasBody() && targetRequest.requestBody() != null) {
            String requestBody = targetRequest.requestBody().trim();
            if ((requestBody.startsWith("{") && requestBody.endsWith("}")) || requestBody.startsWith("[") && requestBody.endsWith("]")) {
                connection.header("Content-Type", "application/json;charset=" + targetRequest.postDataCharset());
            }
        }
    }

    public static int retryCount(Connection connection) {
        JJsoup jJsoup = (JJsoup) connection;
        return jJsoup.retryCount;
    }

    public static void logRequest(Connection target) {
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

    public static void logRetryLogic(Connection target, int index, int retryCount) {
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

    public static boolean followProcess_isSuccess(Connection connection, int now) {
        JJsoup jJsoup = (JJsoup) connection;
        if (jJsoup.followProcess != null) {
            if (jJsoup.followProcess.isSuccess(connection, jJsoup.loginInfo)) {
                return true;
            } else {
                logRetryLogic(connection, now, retryCount(connection));
                return false;
            }
        }
        return true;
    }

    public static void followFilter(Connection connection) {
        if (followFilter != null) {
            try {
                JJsoup jJsoup = (JJsoup) connection;
                followFilter.doFilter(connection, jJsoup.loginInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void after(Connection connection, Exception e, long startTime) throws Exception {
        JJsoup jJsoup = (JJsoup) connection;
        if (jJsoup.followProcess != null && e != null) {
            jJsoup.followProcess.doException(connection, jJsoup.loginInfo, e);
        } else if (jJsoup.followProcess == null && e != null) {
            throw e;
        } else if (connection.response() != null) {
            appendCookie(jJsoup, connection.response());
            logResponse(connection, System.currentTimeMillis() - startTime);
            if (jJsoup.followProcess != null) {
                jJsoup.followProcess.result = jJsoup.followProcess.doProcess(connection, jJsoup.loginInfo);
            }
        }
    }

    public static void appendCookie(JJsoup target, Connection.Response response) {
        if (target.loginInfo != null) {
            Map<String, String> cookies = response.cookies();
            if (!cookies.isEmpty()) {
                target.loginInfo.cookies.putAll(cookies);
            }
        }
    }

    private static void logResponse(Connection target, long time) {
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

    @Autowired(required = false)
    public void initFollowFilter(FollowFilter _followFilter) {
        followFilter = _followFilter;
    }

}
