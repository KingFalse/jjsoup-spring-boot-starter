package me.kagura;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.*;

/**
 * 用于存放cookie跟一些参数
 */
public class LoginInfo implements Serializable {

    //任务ID，也可不单独定义，放入extras中亦可
    public final String key;
    //用于存放临时变量的线程安全的变量池
    public final Map<String, Object> extras = Collections.synchronizedNavigableMap(new TreeMap<>());
    //用于存放cookie的线程安全的cookie池
    public final Map<String, String> cookies = Collections.synchronizedNavigableMap(new TreeMap<>());
    //代理
    private KProxy kProxy;
    //实际代理
    private transient Proxy proxy;

    public LoginInfo() {
        this.key = UUID.randomUUID().toString();
    }

    public LoginInfo(String key) {
        this.key = key;
    }

    public LoginInfo(String key, String host, int port) {
        this.key = key;
        this.kProxy = new KProxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port));
    }

    public LoginInfo(String key, Proxy proxy) {
        this.key = key;
        this.kProxy = new KProxy(proxy.type(), proxy.address());
    }

    public LoginInfo Proxy(Proxy proxy) {
        this.kProxy = new KProxy(proxy.type(), proxy.address());
        this.proxy = proxy;
        return this;
    }

    public Proxy Proxy() {
        if (proxy == null && kProxy != null) {
            this.proxy = new Proxy(kProxy.type, kProxy.sa);
        }
        return this.proxy;
    }

    public <T> T getExtra(String key, Class<T> classOfT) {
        return (T) extras.get(key);
    }

    public String getExtra(String key) {
        return String.valueOf(extras.get(key));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginInfo loginInfo = (LoginInfo) o;
        return Objects.equals(key, loginInfo.key) &&
                Objects.equals(extras, loginInfo.extras) &&
                Objects.equals(cookies, loginInfo.cookies) &&
                Objects.equals(proxy, loginInfo.proxy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, extras, cookies, proxy);
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "key='" + key + '\'' +
                ", extras=" + extras +
                ", cookies=" + cookies +
                ", proxy=" + kProxy +
                '}';
    }
}

class KProxy implements Serializable {
    public Proxy.Type type;
    public SocketAddress sa;

    public KProxy() {
        type = Proxy.Type.DIRECT;
        sa = null;
    }

    public KProxy(Proxy.Type type, SocketAddress sa) {
        if ((type == Proxy.Type.DIRECT) || !(sa instanceof InetSocketAddress))
            throw new IllegalArgumentException("type " + type + " is not compatible with address " + sa);
        this.type = type;
        this.sa = sa;
    }
}
