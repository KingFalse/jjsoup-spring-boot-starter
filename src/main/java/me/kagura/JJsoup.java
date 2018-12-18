package me.kagura;

import com.sun.istack.NotNull;
import me.kagura.config.BeanConfig;
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

public abstract class JJsoup {

    protected LoginInfo loginInfo;
    //解析器
    protected FollowProcess followProcess;
    //默认重试次数
    protected int retryCount = 3;

    @Autowired
    private BeanConfig beanConfig;
    @Autowired(required = false)
    private InitConnection initConn;

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
            if (initConn == null) {
                return connection;
            }
            initConn.init(connection);
            return connection;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection connect(@NotNull String url, LoginInfo loginInfo, FollowProcess followProcess) {
        Connection connection = connect(url);
        if (connection == null) {
            return null;
        }
        if (loginInfo == null) {
            return connection;
        }
        JJsoup jjsoup = (JJsoup) connection;
        jjsoup.loginInfo = loginInfo;
        connection.proxy((loginInfo != null && loginInfo.Proxy() != null) ? loginInfo.Proxy() : Proxy.NO_PROXY);
        connection.cookies(loginInfo.cookies);
        if (followProcess == null) {
            return connection;
        }
        jjsoup.followProcess = followProcess;
        return connection;
    }

    public Connection connect(@NotNull String url, LoginInfo loginInfo) {
        return connect(url, loginInfo, null);
    }

}
