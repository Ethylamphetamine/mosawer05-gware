/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.render.WorldRenderer
 */
package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.WorldRenderer;

public abstract class EntityShader
extends PostProcessShader {
    private Framebuffer prevBuffer;

    @Override
    protected void preDraw() {
        WorldRenderer worldRenderer = MeteorClient.mc.worldRenderer;
        WorldRendererAccessor wra = (WorldRendererAccessor)worldRenderer;
        this.prevBuffer = worldRenderer.getEntityOutlinesFramebuffer();
        wra.setEntityOutlinesFramebuffer(this.framebuffer);
    }

    @Override
    protected void postDraw() {
        if (this.prevBuffer == null) {
            return;
        }
        WorldRenderer worldRenderer = MeteorClient.mc.worldRenderer;
        WorldRendererAccessor wra = (WorldRendererAccessor)worldRenderer;
        wra.setEntityOutlinesFramebuffer(this.prevBuffer);
        this.prevBuffer = null;
    }

    public void endRender() {
        this.endRender(() -> this.vertexConsumerProvider.draw());
    }
}

