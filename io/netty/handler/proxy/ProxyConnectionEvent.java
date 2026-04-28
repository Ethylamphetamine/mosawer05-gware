/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.ObjectUtil
 *  io.netty.util.internal.StringUtil
 */
package io.netty.handler.proxy;

import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import java.net.SocketAddress;

public final class ProxyConnectionEvent {
    private final String protocol;
    private final String authScheme;
    private final SocketAddress proxyAddress;
    private final SocketAddress destinationAddress;
    private String strVal;

    public ProxyConnectionEvent(String protocol, String authScheme, SocketAddress proxyAddress, SocketAddress destinationAddress) {
        this.protocol = (String)ObjectUtil.checkNotNull((Object)protocol, (String)"protocol");
        this.authScheme = (String)ObjectUtil.checkNotNull((Object)authScheme, (String)"authScheme");
        this.proxyAddress = (SocketAddress)ObjectUtil.checkNotNull((Object)proxyAddress, (String)"proxyAddress");
        this.destinationAddress = (SocketAddress)ObjectUtil.checkNotNull((Object)destinationAddress, (String)"destinationAddress");
    }

    public String protocol() {
        return this.protocol;
    }

    public String authScheme() {
        return this.authScheme;
    }

    public <T extends SocketAddress> T proxyAddress() {
        return (T)this.proxyAddress;
    }

    public <T extends SocketAddress> T destinationAddress() {
        return (T)this.destinationAddress;
    }

    public String toString() {
        if (this.strVal != null) {
            return this.strVal;
        }
        StringBuilder buf = new StringBuilder(128).append(StringUtil.simpleClassName((Object)this)).append('(').append(this.protocol).append(", ").append(this.authScheme).append(", ").append(this.proxyAddress).append(" => ").append(this.destinationAddress).append(')');
        this.strVal = buf.toString();
        return this.strVal;
    }
}

