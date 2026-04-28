/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.util.internal.ObjectUtil
 */
package io.netty.handler.codec.socks;

import io.netty.handler.codec.socks.SocksMessage;
import io.netty.handler.codec.socks.SocksMessageType;
import io.netty.handler.codec.socks.SocksResponseType;
import io.netty.util.internal.ObjectUtil;

public abstract class SocksResponse
extends SocksMessage {
    private final SocksResponseType responseType;

    protected SocksResponse(SocksResponseType responseType) {
        super(SocksMessageType.RESPONSE);
        this.responseType = (SocksResponseType)((Object)ObjectUtil.checkNotNull((Object)((Object)responseType), (String)"responseType"));
    }

    public SocksResponseType responseType() {
        return this.responseType;
    }
}

