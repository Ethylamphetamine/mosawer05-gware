/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.util.internal.ObjectUtil
 */
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.socks.SocksAuthStatus;
import io.netty.handler.codec.socks.SocksResponse;
import io.netty.handler.codec.socks.SocksResponseType;
import io.netty.handler.codec.socks.SocksSubnegotiationVersion;
import io.netty.util.internal.ObjectUtil;

public final class SocksAuthResponse
extends SocksResponse {
    private static final SocksSubnegotiationVersion SUBNEGOTIATION_VERSION = SocksSubnegotiationVersion.AUTH_PASSWORD;
    private final SocksAuthStatus authStatus;

    public SocksAuthResponse(SocksAuthStatus authStatus) {
        super(SocksResponseType.AUTH);
        this.authStatus = (SocksAuthStatus)((Object)ObjectUtil.checkNotNull((Object)((Object)authStatus), (String)"authStatus"));
    }

    public SocksAuthStatus authStatus() {
        return this.authStatus;
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte((int)SUBNEGOTIATION_VERSION.byteValue());
        byteBuf.writeByte((int)this.authStatus.byteValue());
    }
}

