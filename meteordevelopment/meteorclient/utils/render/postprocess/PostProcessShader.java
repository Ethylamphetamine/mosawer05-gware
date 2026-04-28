/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gl.Framebuffer
 *  net.minecraft.client.gl.SimpleFramebuffer
 *  net.minecraft.client.render.OutlineVertexConsumerProvider
 *  net.minecraft.entity.Entity
 *  org.lwjgl.glfw.GLFW
 */
package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.PostProcessRenderer;
import meteordevelopment.meteorclient.renderer.Shader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.entity.Entity;
import org.lwjgl.glfw.GLFW;

public abstract class PostProcessShader {
    public OutlineVertexConsumerProvider vertexConsumerProvider;
    public Framebuffer framebuffer;
    protected Shader shader;

    public void init(String frag) {
        this.vertexConsumerProvider = new OutlineVertexConsumerProvider(MeteorClient.mc.getBufferBuilders().getEntityVertexConsumers());
        this.framebuffer = new SimpleFramebuffer(MeteorClient.mc.getWindow().getFramebufferWidth(), MeteorClient.mc.getWindow().getFramebufferHeight(), false, MinecraftClient.IS_SYSTEM_MAC);
        this.shader = new Shader("post-process/base.vert", "post-process/" + frag + ".frag");
    }

    protected abstract boolean shouldDraw();

    public abstract boolean shouldDraw(Entity var1);

    protected void preDraw() {
    }

    protected void postDraw() {
    }

    protected abstract void setUniforms();

    public void beginRender() {
        if (!this.shouldDraw()) {
            return;
        }
        this.framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        MeteorClient.mc.getFramebuffer().beginWrite(false);
    }

    public void endRender(Runnable draw) {
        if (!this.shouldDraw()) {
            return;
        }
        this.preDraw();
        draw.run();
        this.postDraw();
        MeteorClient.mc.getFramebuffer().beginWrite(false);
        GL.bindTexture(this.framebuffer.getColorAttachment(), 0);
        this.shader.bind();
        this.shader.set("u_Size", MeteorClient.mc.getWindow().getFramebufferWidth(), MeteorClient.mc.getWindow().getFramebufferHeight());
        this.shader.set("u_Texture", 0);
        this.shader.set("u_Time", GLFW.glfwGetTime());
        this.setUniforms();
        PostProcessRenderer.render();
    }

    public void onResized(int width, int height) {
        if (this.framebuffer == null) {
            return;
        }
        this.framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
    }
}

