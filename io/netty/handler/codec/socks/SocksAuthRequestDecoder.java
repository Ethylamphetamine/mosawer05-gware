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
import io.netty.handler.codec.socks.SocksAuthRequest;
import io.netty.handler.codec.socks.SocksCommonUtils;
import io.netty.handler.codec.socks.SocksSubnegotiationVersion;
import java.util.List;

public class SocksAuthRequestDecoder
extends ReplayingDecoder<State> {
    private String username;

    public SocksAuthRequestDecoder() {
        super((Object)State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch ((State)((Object)this.state())) {
            case CHECK_PROTOCOL_VERSION: {
                if (byteBuf.readByte() != SocksSubnegotiationVersion.AUTH_PASSWORD.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                    break;
                }
                this.checkpoint((Object)State.READ_USERNAME);
            }
            case READ_USERNAME: {
                byte fieldLength = byteBuf.readByte();
                this.username = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                this.checkpoint((Object)State.READ_PASSWORD);
            }
            case READ_PASSWORD: {
                byte fieldLength = byteBuf.readByte();
                String password = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                out.add(new SocksAuthRequest(this.username, password));
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
        READ_USERNAME,
        READ_PASSWORD;

    }
}

