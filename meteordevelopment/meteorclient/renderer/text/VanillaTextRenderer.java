/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.font.TextRenderer$TextLayerType
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.util.BufferAllocator
 *  net.minecraft.client.util.math.MatrixStack
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 */
package meteordevelopment.meteorclient.renderer.text;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

public class VanillaTextRenderer
implements TextRenderer {
    public static final VanillaTextRenderer INSTANCE = new VanillaTextRenderer();
    private final BufferAllocator buffer = new BufferAllocator(2048);
    private final VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate((BufferAllocator)this.buffer);
    private final MatrixStack matrices = new MatrixStack();
    private final Matrix4f emptyMatrix = new Matrix4f();
    public double scale = 2.0;
    public boolean scaleIndividually;
    private boolean building;
    private double alpha = 1.0;

    private VanillaTextRenderer() {
    }

    @Override
    public void setAlpha(double a) {
        this.alpha = a;
    }

    @Override
    public double getWidth(String text, int length, boolean shadow) {
        if (text.isEmpty()) {
            return 0.0;
        }
        if (length != text.length()) {
            text = text.substring(0, length);
        }
        return (double)(MeteorClient.mc.textRenderer.getWidth(text) + (shadow ? 1 : 0)) * this.scale;
    }

    @Override
    public double getHeight(boolean shadow) {
        Objects.requireNonNull(MeteorClient.mc.textRenderer);
        return (double)(9 + (shadow ? 1 : 0)) * this.scale;
    }

    @Override
    public void begin(double scale, boolean scaleOnly, boolean big) {
        if (this.building) {
            throw new RuntimeException("VanillaTextRenderer.begin() called twice");
        }
        this.scale = scale * 2.0;
        this.building = true;
    }

    @Override
    public double render(String text, double x, double y, Color color, boolean shadow) {
        boolean wasBuilding = this.building;
        if (!wasBuilding) {
            this.begin();
        }
        x += 0.5 * this.scale;
        y += 0.5 * this.scale;
        int preA = color.a;
        color.a = (int)((double)(color.a / 255) * this.alpha * 255.0);
        Matrix4f matrix = this.emptyMatrix;
        if (this.scaleIndividually) {
            this.matrices.push();
            this.matrices.scale((float)this.scale, (float)this.scale, 1.0f);
            matrix = this.matrices.peek().getPositionMatrix();
        }
        double x2 = MeteorClient.mc.textRenderer.draw(text, (float)(x / this.scale), (float)(y / this.scale), color.getPacked(), shadow, matrix, (VertexConsumerProvider)this.immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
        if (this.scaleIndividually) {
            this.matrices.pop();
        }
        color.a = preA;
        if (!wasBuilding) {
            this.end();
        }
        return (x2 - 1.0) * this.scale;
    }

    @Override
    public boolean isBuilding() {
        return this.building;
    }

    @Override
    public void end(MatrixStack matrices) {
        if (!this.building) {
            throw new RuntimeException("VanillaTextRenderer.end() called without calling begin()");
        }
        Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
        RenderSystem.disableDepthTest();
        matrixStack.pushMatrix();
        if (matrices != null) {
            matrixStack.mul((Matrix4fc)matrices.peek().getPositionMatrix());
        }
        if (!this.scaleIndividually) {
            matrixStack.scale((float)this.scale, (float)this.scale, 1.0f);
        }
        RenderSystem.applyModelViewMatrix();
        this.immediate.draw();
        matrixStack.popMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.applyModelViewMatrix();
        this.scale = 2.0;
        this.building = false;
    }
}

