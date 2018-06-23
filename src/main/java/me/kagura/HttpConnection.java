package me.kagura;

import me.kagura.config.BeanConfig;
import me.kagura.util.AopTargetUtils;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.net.Proxy;
import java.net.URL;

/**
 * 其实这个接口没什么卵用，只是为了给这两个方法加下注解
 */
interface ConnectionX extends Connection {

    @Deprecated
    Connection url(URL url);

    @Deprecated
    Connection url(String url);

}

public abstract class HttpConnection implements ConnectionX {

    public LoginInfo loginInfo;
    //解析器
    public FollowProcess followProcess;
    //默认重试次数
    public int retryCount = 3;

    @Autowired
    private BeanConfig beanConfig;
    @Autowired(required = false)
    private InitHttpConnection initHttpConnection;

    /**
     * 用于替代Jsoup.connect(String url);
     *
     * @param url
     * @return
     */
    public HttpConnection connect(@NonNull String url) {
        try {
            HttpConnection httpConnection = (HttpConnection) beanConfig.getHttpConnection()
                    .url(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            return httpConnection;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用于替代Jsoup.connect(String url);
     * 并使用LoginInfo对象实现cookie自动管理
     *
     * @param url
     * @return
     */
    public HttpConnection connect(@NonNull String url, @NonNull LoginInfo loginInfo) {
        try {
            HttpConnection httpConnection = connect(url);
            //从代理类中取出实际对象
            HttpConnection beanFromProxy = AopTargetUtils.getTargetObject(httpConnection, HttpConnection.class);
            //将LoginInfo跟cookie放入实际对象中
            beanFromProxy.loginInfo = loginInfo;
            beanFromProxy.proxy((loginInfo != null && loginInfo.proxy != null) ? loginInfo.proxy : Proxy.NO_PROXY);
            beanFromProxy.cookies(loginInfo.cookies);
            if (initHttpConnection == null) {
                return httpConnection;
            }
            initHttpConnection.init(beanFromProxy);
            return httpConnection;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HttpConnection connect(@NonNull String url, @NonNull LoginInfo loginInfo, FollowProcess followProcess) {
        HttpConnection httpConnection = connect(url, loginInfo);
        if (httpConnection == null) {
            return null;
        }
        try {
            //从代理类中取出实际对象
            HttpConnection beanFromProxy = AopTargetUtils.getTargetObject(httpConnection, HttpConnection.class);
            beanFromProxy.followProcess = followProcess;
            return httpConnection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
