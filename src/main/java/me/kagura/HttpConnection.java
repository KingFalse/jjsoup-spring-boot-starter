package me.kagura;

import com.sun.istack.NotNull;
import me.kagura.config.BeanConfig;
import me.kagura.util.AopTargetUtils;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public abstract class HttpConnection {

    protected LoginInfo loginInfo;
    //解析器
    protected FollowProcess followProcess;
    //默认重试次数
    protected int retryCount = 3;

    @Autowired
    private BeanConfig beanConfig;
    @Autowired(required = false)
    private InitHttpConnection initHttpConnection;

    private static synchronized SSLSocketFactory initUnSecureTSL() throws IOException {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
            }

            public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        // Install the all-trusting trust manager
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Can't create unsecure trust manager");
        }

    }

    public Connection connect(@NotNull String url) {
        try {
            Connection connection = (Connection) beanConfig.getHttpConnection();
            connection.url(url)
                    .sslSocketFactory(initUnSecureTSL())
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
            ;
            if (initHttpConnection == null) {
                return connection;
            }
            return initHttpConnection.init(connection);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
            return connection;
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
