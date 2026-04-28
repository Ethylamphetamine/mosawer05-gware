/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ReplayingDecoder
 */
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksCommonUtils;
import io.netty.handler.codec.socks.SocksInitResponse;
import io.netty.handler.codec.socks.SocksProtocolVersion;
import java.util.List;

public class SocksInitResponseDecoder
extends ReplayingDecoder<State> {
    public SocksInitResponseDecoder() {
        super((Object)State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch ((State)((Object)this.state())) {
            case CHECK_PROTOCOL_VERSION: {
                if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_RESPONSE);
                    break;
                }
                this.checkpoint((Object)State.READ_PREFERRED_AUTH_TYPE);
            }
            case READ_PREFERRED_AUTH_TYPE: {
                SocksAuthScheme authScheme = SocksAuthScheme.valueOf(byteBuf.readByte());
                out.add(new SocksInitResponse(authScheme));
                break;
            }
            default: {
                throw new Error();
            }
        }
        ctx.pipeline().remove((ChannelHandler)this);
    }

    public static enum State {
        CHECK_PROTOCOL_VERSION,
        READ_PREFERRED_AUTH_TYPE;

    }
}

