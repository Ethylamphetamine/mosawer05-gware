/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufUtil
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.MessageToByteEncoder
 *  io.netty.util.NetUtil
 */
package io.netty.handler.codec.socksx.v4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.util.NetUtil;

@ChannelHandler.Sharable
public final class Socks4ClientEncoder
extends MessageToByteEncoder<Socks4CommandRequest> {
    public static final Socks4ClientEncoder INSTANCE = new Socks4ClientEncoder();
    private static final byte[] IPv4_DOMAIN_MARKER = new byte[]{0, 0, 0, 1};

    private Socks4ClientEncoder() {
    }

    protected void encode(ChannelHandlerContext ctx, Socks4CommandRequest msg, ByteBuf out) throws Exception {
        out.writeByte((int)msg.version().byteValue());
        out.writeByte((int)msg.type().byteValue());
        out.writeShort(msg.dstPort());
        if (NetUtil.isValidIpV4Address((String)msg.dstAddr())) {
            out.writeBytes(NetUtil.createByteArrayFromIpAddressString((String)msg.dstAddr()));
            ByteBufUtil.writeAscii((ByteBuf)out, (CharSequence)msg.userId());
            out.writeByte(0);
        } else {
            out.writeBytes(IPv4_DOMAIN_MARKER);
            ByteBufUtil.writeAscii((ByteBuf)out, (CharSequence)msg.userId());
            out.writeByte(0);
            ByteBufUtil.writeAscii((ByteBuf)out, (CharSequence)msg.dstAddr());
            out.writeByte(0);
        }
    }
}

