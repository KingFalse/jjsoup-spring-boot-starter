package me.kagura;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * 用于存放cookie跟一些参数
 */
public class LoginInfo {

    //任务ID，也可不单独定义，放入extras中亦可
    public final String traceID;
    //用于存放临时变量的线程安全的变量池
    public final Map<String, Object> extras = Collections.synchronizedNavigableMap(new TreeMap<>());
    //用于存放cookie的线程安全的cookie池
    public final Map<String, String> cookies = Collections.synchronizedNavigableMap(new TreeMap<>());
    //代理
    public Proxy proxy;

    public LoginInfo() {
        this.traceID = UUID.randomUUID().toString();
    }

    public LoginInfo(String traceID) {
        this.traceID = traceID;
    }

    public LoginInfo(String traceID, String host, int port) {
        this.traceID = traceID;
        this.proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port));
    }

    public LoginInfo(String traceID, Proxy proxy) {
        this.traceID = traceID;
        this.proxy = proxy;
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
        return Objects.equals(traceID, loginInfo.traceID) &&
                Objects.equals(extras, loginInfo.extras) &&
                Objects.equals(cookies, loginInfo.cookies) &&
                Objects.equals(proxy, loginInfo.proxy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(traceID, extras, cookies, proxy);
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "traceID='" + traceID + '\'' +
                ", extras=" + extras +
                ", cookies=" + cookies +
                ", proxy=" + proxy +
                '}';
    }
}
