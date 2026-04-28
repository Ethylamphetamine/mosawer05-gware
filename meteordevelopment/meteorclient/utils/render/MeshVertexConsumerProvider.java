/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.RenderLayer
 *  net.minecraft.client.render.VertexConsumer
 */
package meteordevelopment.meteorclient.utils.render;

import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.utils.render.IVertexConsumerProvider;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;

public class MeshVertexConsumerProvider
implements IVertexConsumerProvider {
    private final MeshVertexConsumer vertexConsumer;

    public MeshVertexConsumerProvider(Mesh mesh) {
        this.vertexConsumer = new MeshVertexConsumer(mesh);
    }

    public VertexConsumer getBuffer(RenderLayer layer) {
        return this.vertexConsumer;
    }

    public void setColor(Color color) {
        this.vertexConsumer.fixedColor(color.r, color.g, color.b, color.a);
    }

    @Override
    public void setOffset(int offsetX, int offsetY, int offsetZ) {
        this.vertexConsumer.setOffset(offsetX, offsetY, offsetZ);
    }

    public static class MeshVertexConsumer
    implements VertexConsumer {
        private final Mesh mesh;
        private int offsetX;
        private int offsetY;
        private int offsetZ;
        private final double[] xs = new double[4];
        private final double[] ys = new double[4];
        private final double[] zs = new double[4];
        private final Color color = new Color();
        private int i;

        public MeshVertexConsumer(Mesh mesh) {
            this.mesh = mesh;
        }

        public void setOffset(int offsetX, int offsetY, int offsetZ) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
        }

        public VertexConsumer vertex(float x, float y, float z) {
            this.xs[this.i] = (double)this.offsetX + (double)x;
            this.ys[this.i] = (double)this.offsetY + (double)y;
            this.zs[this.i] = (double)this.offsetZ + (double)z;
            if (++this.i >= 4) {
                this.mesh.quad(this.mesh.vec3(this.xs[0], this.ys[0], this.zs[0]).color(this.color).next(), this.mesh.vec3(this.xs[1], this.ys[1], this.zs[1]).color(this.color).next(), this.mesh.vec3(this.xs[2], this.ys[2], this.zs[2]).color(this.color).next(), this.mesh.vec3(this.xs[3], this.ys[3], this.zs[3]).color(this.color).next());
                this.i = 0;
            }
            return this;
        }

        public VertexConsumer color(int red, int green, int blue, int alpha) {
            return this;
        }

        public VertexConsumer texture(float u, float v) {
            return this;
        }

        public VertexConsumer overlay(int u, int v) {
            return this;
        }

        public VertexConsumer light(int u, int v) {
            return this;
        }

        public VertexConsumer normal(float x, float y, float z) {
            return null;
        }

        public void fixedColor(int red, int green, int blue, int alpha) {
            this.color.set(red, green, blue, alpha);
        }
    }
}

