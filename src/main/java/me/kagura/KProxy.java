package me.kagura;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

public class KProxy implements Serializable {
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
