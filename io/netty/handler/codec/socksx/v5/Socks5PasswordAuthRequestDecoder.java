/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.DecoderResult
 *  io.netty.handler.codec.ReplayingDecoder
 *  io.netty.util.CharsetUtil
 */
package io.netty.handler.codec.socksx.v5;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import io.netty.util.CharsetUtil;
import java.util.List;

public class Socks5PasswordAuthRequestDecoder
extends ReplayingDecoder<State> {
    public Socks5PasswordAuthRequestDecoder() {
        super((Object)State.INIT);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            switch ((State)((Object)this.state())) {
                case INIT: {
                    int startOffset = in.readerIndex();
                    byte version = in.getByte(startOffset);
                    if (version != 1) {
                        throw new DecoderException("unsupported subnegotiation version: " + version + " (expected: 1)");
                    }
                    short usernameLength = in.getUnsignedByte(startOffset + 1);
                    short passwordLength = in.getUnsignedByte(startOffset + 2 + usernameLength);
                    int totalLength = usernameLength + passwordLength + 3;
                    in.skipBytes(totalLength);
                    out.add(new DefaultSocks5PasswordAuthRequest(in.toString(startOffset + 2, (int)usernameLength, CharsetUtil.US_ASCII), in.toString(startOffset + 3 + usernameLength, (int)passwordLength, CharsetUtil.US_ASCII)));
                    this.checkpoint((Object)State.SUCCESS);
                }
                case SUCCESS: {
                    int readableBytes = this.actualReadableBytes();
                    if (readableBytes <= 0) break;
                    out.add(in.readRetainedSlice(readableBytes));
                    break;
                }
                case FAILURE: {
                    in.skipBytes(this.actualReadableBytes());
                }
            }
        }
        catch (Exception e) {
            this.fail(out, e);
        }
    }

    private void fail(List<Object> out, Exception cause) {
        if (!(cause instanceof DecoderException)) {
            cause = new DecoderException((Throwable)cause);
        }
        this.checkpoint((Object)State.FAILURE);
        DefaultSocks5PasswordAuthRequest m = new DefaultSocks5PasswordAuthRequest("", "");
        m.setDecoderResult(DecoderResult.failure((Throwable)cause));
        out.add(m);
    }

    public static enum State {
        INIT,
        SUCCESS,
        FAILURE;

    }
}

