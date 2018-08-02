package me.kagura;

import com.sun.istack.NotNull;
import me.kagura.config.BeanConfig;
import me.kagura.util.AopTargetUtils;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.Proxy;

public class HttpConnection {

    protected LoginInfo loginInfo;
    //解析器
    protected FollowProcess followProcess;
    //默认重试次数
    protected int retryCount = 3;

    @Autowired
    private BeanConfig beanConfig;
    @Autowired(required = false)
    private InitHttpConnection initHttpConnection;

    public Connection connect(@NotNull String url) {
        try {
            Connection connection = beanConfig.getHttpConnection()
                    .url(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true);
            if (initHttpConnection == null) {
                return connection;
            }
            initHttpConnection.init(connection);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection connect(@NotNull String url, @NotNull LoginInfo loginInfo) {
        try {
            Connection connection = connect(url);
            if (connection == null) {
                return null;
            }
            //从代理类中取出实际对象
            HttpConnection beanFromProxy = AopTargetUtils.getTargetObject(connection, HttpConnection.class);
            //将LoginInfo跟cookie放入实际对象中
            beanFromProxy.loginInfo = loginInfo;
            Connection conn = (Connection) beanFromProxy;
            conn.proxy((loginInfo != null && loginInfo.Proxy() != null) ? loginInfo.Proxy() : Proxy.NO_PROXY);
            conn.cookies(loginInfo.cookies);
            return conn;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection connect(@NotNull String url, @NotNull LoginInfo loginInfo, FollowProcess followProcess) {
        Connection connection = connect(url, loginInfo);
        if (connection == null) {
            return null;
        }
        try {
            //从代理类中取出实际对象
            HttpConnection beanFromProxy = AopTargetUtils.getTargetObject(connection, HttpConnection.class);
            beanFromProxy.followProcess = followProcess;
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
