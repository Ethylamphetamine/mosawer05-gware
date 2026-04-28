/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.ReplayingDecoder
 *  io.netty.util.NetUtil
 */
package io.netty.handler.codec.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksCommonUtils;
import io.netty.handler.codec.socks.SocksProtocolVersion;
import io.netty.util.NetUtil;
import java.util.List;

public class SocksCmdRequestDecoder
extends ReplayingDecoder<State> {
    private SocksCmdType cmdType;
    private SocksAddressType addressType;

    public SocksCmdRequestDecoder() {
        super((Object)State.CHECK_PROTOCOL_VERSION);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        block0 : switch ((State)((Object)this.state())) {
            case CHECK_PROTOCOL_VERSION: {
                if (byteBuf.readByte() != SocksProtocolVersion.SOCKS5.byteValue()) {
                    out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                    break;
                }
                this.checkpoint((Object)State.READ_CMD_HEADER);
            }
            case READ_CMD_HEADER: {
                this.cmdType = SocksCmdType.valueOf(byteBuf.readByte());
                byteBuf.skipBytes(1);
                this.addressType = SocksAddressType.valueOf(byteBuf.readByte());
                this.checkpoint((Object)State.READ_CMD_ADDRESS);
            }
            case READ_CMD_ADDRESS: {
                switch (this.addressType) {
                    case IPv4: {
                        String host = NetUtil.intToIpAddress((int)byteBuf.readInt());
                        int port = byteBuf.readUnsignedShort();
                        out.add(new SocksCmdRequest(this.cmdType, this.addressType, host, port));
                        break block0;
                    }
                    case DOMAIN: {
                        byte fieldLength = byteBuf.readByte();
                        String host = SocksCommonUtils.readUsAscii(byteBuf, fieldLength);
                        int port = byteBuf.readUnsignedShort();
                        out.add(new SocksCmdRequest(this.cmdType, this.addressType, host, port));
                        break block0;
                    }
                    case IPv6: {
                        byte[] bytes = new byte[16];
                        byteBuf.readBytes(bytes);
                        String host = SocksCommonUtils.ipv6toStr(bytes);
                        int port = byteBuf.readUnsignedShort();
                        out.add(new SocksCmdRequest(this.cmdType, this.addressType, host, port));
                        break block0;
                    }
                    case UNKNOWN: {
                        out.add(SocksCommonUtils.UNKNOWN_SOCKS_REQUEST);
                        break block0;
                    }
                }
                throw new Error();
            }
            default: {
                throw new Error();
            }
        }
        ctx.pipeline().remove((ChannelHandler)this);
    }

    public static enum State {
        CHECK_PROTOCOL_VERSION,
        READ_CMD_HEADER,
        READ_CMD_ADDRESS;

    }
}

