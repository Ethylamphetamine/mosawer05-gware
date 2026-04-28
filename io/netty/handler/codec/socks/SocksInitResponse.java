/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.util.internal.ObjectUtil
 */
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksResponse;
import io.netty.handler.codec.socks.SocksResponseType;
import io.netty.util.internal.ObjectUtil;

public final class SocksInitResponse
extends SocksResponse {
    private final SocksAuthScheme authScheme;

    public SocksInitResponse(SocksAuthScheme authScheme) {
        super(SocksResponseType.INIT);
        this.authScheme = (SocksAuthScheme)((Object)ObjectUtil.checkNotNull((Object)((Object)authScheme), (String)"authScheme"));
    }

    public SocksAuthScheme authScheme() {
        return this.authScheme;
    }

    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeByte((int)this.protocolVersion().byteValue());
        byteBuf.writeByte((int)this.authScheme.byteValue());
    }
}

