/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderResult
 *  io.netty.util.internal.ObjectUtil
 */
package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.util.internal.ObjectUtil;

public abstract class AbstractSocksMessage
implements SocksMessage {
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    public DecoderResult decoderResult() {
        return this.decoderResult;
    }

    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = (DecoderResult)ObjectUtil.checkNotNull((Object)decoderResult, (String)"decoderResult");
    }
}

