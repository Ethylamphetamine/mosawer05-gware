/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.EncoderException
 *  io.netty.handler.codec.MessageToByteEncoder
 *  io.netty.util.internal.ObjectUtil
 *  io.netty.util.internal.StringUtil
 */
package io.netty.handler.codec.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressEncoder;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

@ChannelHandler.Sharable
public class Socks5ServerEncoder
extends MessageToByteEncoder<Socks5Message> {
    public static final Socks5ServerEncoder DEFAULT = new Socks5ServerEncoder(Socks5AddressEncoder.DEFAULT);
    private final Socks5AddressEncoder addressEncoder;

    protected Socks5ServerEncoder() {
        this(Socks5AddressEncoder.DEFAULT);
    }

    public Socks5ServerEncoder(Socks5AddressEncoder addressEncoder) {
        this.addressEncoder = (Socks5AddressEncoder)ObjectUtil.checkNotNull((Object)addressEncoder, (String)"addressEncoder");
    }

    protected final Socks5AddressEncoder addressEncoder() {
        return this.addressEncoder;
    }

    protected void encode(ChannelHandlerContext ctx, Socks5Message msg, ByteBuf out) throws Exception {
        if (msg instanceof Socks5InitialResponse) {
            Socks5ServerEncoder.encodeAuthMethodResponse((Socks5InitialResponse)msg, out);
        } else if (msg instanceof Socks5PasswordAuthResponse) {
            Socks5ServerEncoder.encodePasswordAuthResponse((Socks5PasswordAuthResponse)msg, out);
        } else if (msg instanceof Socks5CommandResponse) {
            this.encodeCommandResponse((Socks5CommandResponse)msg, out);
        } else {
            throw new EncoderException("unsupported message type: " + StringUtil.simpleClassName((Object)msg));
        }
    }

    private static void encodeAuthMethodResponse(Socks5InitialResponse msg, ByteBuf out) {
        out.writeByte((int)msg.version().byteValue());
        out.writeByte((int)msg.authMethod().byteValue());
    }

    private static void encodePasswordAuthResponse(Socks5PasswordAuthResponse msg, ByteBuf out) {
        out.writeByte(1);
        out.writeByte((int)msg.status().byteValue());
    }

    private void encodeCommandResponse(Socks5CommandResponse msg, ByteBuf out) throws Exception {
        out.writeByte((int)msg.version().byteValue());
        out.writeByte((int)msg.status().byteValue());
        out.writeByte(0);
        Socks5AddressType bndAddrType = msg.bndAddrType();
        out.writeByte((int)bndAddrType.byteValue());
        this.addressEncoder.encodeAddress(bndAddrType, msg.bndAddr(), out);
        out.writeShort(msg.bndPort());
    }
}

