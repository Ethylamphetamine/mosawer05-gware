/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.system.MemoryUtil
 */
package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.DrawMode;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Shader;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

public class Mesh {
    public boolean depthTest = false;
    public double alpha = 1.0;
    private final DrawMode drawMode;
    private final int primitiveVerticesSize;
    private final int vao;
    private final int vbo;
    private final int ibo;
    private ByteBuffer vertices;
    private long verticesPointerStart;
    private long verticesPointer;
    private ByteBuffer indices;
    private long indicesPointer;
    private int vertexI;
    private int indicesCount;
    private boolean building;
    private boolean rendering3D;
    private double cameraX;
    private double cameraZ;
    private boolean beganRendering;

    public Mesh(DrawMode drawMode, Attrib ... attributes) {
        int stride = 0;
        for (Attrib attribute : attributes) {
            stride += attribute.size;
        }
        this.drawMode = drawMode;
        this.primitiveVerticesSize = stride * drawMode.indicesCount;
        this.vertices = BufferUtils.createByteBuffer((int)(this.primitiveVerticesSize * 256 * 4));
        this.verticesPointerStart = MemoryUtil.memAddress0((Buffer)this.vertices);
        this.indices = BufferUtils.createByteBuffer((int)(drawMode.indicesCount * 512 * 4));
        this.indicesPointer = MemoryUtil.memAddress0((Buffer)this.indices);
        this.vao = GL.genVertexArray();
        GL.bindVertexArray(this.vao);
        this.vbo = GL.genBuffer();
        GL.bindVertexBuffer(this.vbo);
        this.ibo = GL.genBuffer();
        GL.bindIndexBuffer(this.ibo);
        int offset = 0;
        for (int i = 0; i < attributes.length; ++i) {
            Attrib attrib = attributes[i];
            GL.enableVertexAttribute(i);
            GL.vertexAttribute(i, attrib.count, attrib.getType(), attrib.normalized, stride, offset);
            offset += attrib.size;
        }
        GL.bindVertexArray(0);
        GL.bindVertexBuffer(0);
        GL.bindIndexBuffer(0);
    }

    public void destroy() {
        GL.deleteBuffer(this.ibo);
        GL.deleteBuffer(this.vbo);
        GL.deleteVertexArray(this.vao);
    }

    public void begin() {
        if (this.building) {
            throw new IllegalStateException("Mesh.begin() called while already building.");
        }
        this.verticesPointer = this.verticesPointerStart;
        this.vertexI = 0;
        this.indicesCount = 0;
        this.building = true;
        this.rendering3D = Utils.rendering3D;
        if (this.rendering3D) {
            Vec3d camera = MeteorClient.mc.gameRenderer.getCamera().getPos();
            this.cameraX = camera.x;
            this.cameraZ = camera.z;
        } else {
            this.cameraX = 0.0;
            this.cameraZ = 0.0;
        }
    }

    public Mesh vec3(double x, double y, double z) {
        long p = this.verticesPointer;
        MemoryUtil.memPutFloat((long)p, (float)((float)(x - this.cameraX)));
        MemoryUtil.memPutFloat((long)(p + 4L), (float)((float)y));
        MemoryUtil.memPutFloat((long)(p + 8L), (float)((float)(z - this.cameraZ)));
        this.verticesPointer += 12L;
        return this;
    }

    public Mesh vec2(double x, double y) {
        long p = this.verticesPointer;
        MemoryUtil.memPutFloat((long)p, (float)((float)x));
        MemoryUtil.memPutFloat((long)(p + 4L), (float)((float)y));
        this.verticesPointer += 8L;
        return this;
    }

    public Mesh color(Color c) {
        long p = this.verticesPointer;
        MemoryUtil.memPutByte((long)p, (byte)((byte)c.r));
        MemoryUtil.memPutByte((long)(p + 1L), (byte)((byte)c.g));
        MemoryUtil.memPutByte((long)(p + 2L), (byte)((byte)c.b));
        MemoryUtil.memPutByte((long)(p + 3L), (byte)((byte)((float)c.a * (float)this.alpha)));
        this.verticesPointer += 4L;
        return this;
    }

    public int next() {
        return this.vertexI++;
    }

    public void line(int i1, int i2) {
        long p = this.indicesPointer + (long)this.indicesCount * 4L;
        MemoryUtil.memPutInt((long)p, (int)i1);
        MemoryUtil.memPutInt((long)(p + 4L), (int)i2);
        this.indicesCount += 2;
        this.growIfNeeded();
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = this.indicesPointer + (long)this.indicesCount * 4L;
        MemoryUtil.memPutInt((long)p, (int)i1);
        MemoryUtil.memPutInt((long)(p + 4L), (int)i2);
        MemoryUtil.memPutInt((long)(p + 8L), (int)i3);
        MemoryUtil.memPutInt((long)(p + 12L), (int)i3);
        MemoryUtil.memPutInt((long)(p + 16L), (int)i4);
        MemoryUtil.memPutInt((long)(p + 20L), (int)i1);
        this.indicesCount += 6;
        this.growIfNeeded();
    }

    public void triangle(int i1, int i2, int i3) {
        long p = this.indicesPointer + (long)this.indicesCount * 4L;
        MemoryUtil.memPutInt((long)p, (int)i1);
        MemoryUtil.memPutInt((long)(p + 4L), (int)i2);
        MemoryUtil.memPutInt((long)(p + 8L), (int)i3);
        this.indicesCount += 3;
        this.growIfNeeded();
    }

    public void growIfNeeded() {
        if ((this.vertexI + 1) * this.primitiveVerticesSize >= this.vertices.capacity()) {
            int offset = this.getVerticesOffset();
            int newSize = this.vertices.capacity() * 2;
            if (newSize % this.primitiveVerticesSize != 0) {
                newSize += newSize % this.primitiveVerticesSize;
            }
            ByteBuffer newVertices = BufferUtils.createByteBuffer((int)newSize);
            MemoryUtil.memCopy((long)MemoryUtil.memAddress0((Buffer)this.vertices), (long)MemoryUtil.memAddress0((Buffer)newVertices), (long)offset);
            this.vertices = newVertices;
            this.verticesPointerStart = MemoryUtil.memAddress0((Buffer)this.vertices);
            this.verticesPointer = this.verticesPointerStart + (long)offset;
        }
        if (this.indicesCount * 4 >= this.indices.capacity()) {
            int newSize = this.indices.capacity() * 2;
            if (newSize % this.drawMode.indicesCount != 0) {
                newSize += newSize % (this.drawMode.indicesCount * 4);
            }
            ByteBuffer newIndices = BufferUtils.createByteBuffer((int)newSize);
            MemoryUtil.memCopy((long)MemoryUtil.memAddress0((Buffer)this.indices), (long)MemoryUtil.memAddress0((Buffer)newIndices), (long)((long)this.indicesCount * 4L));
            this.indices = newIndices;
            this.indicesPointer = MemoryUtil.memAddress0((Buffer)this.indices);
        }
    }

    public void end() {
        if (!this.building) {
            throw new IllegalStateException("Mesh.end() called while not building.");
        }
        if (this.indicesCount > 0) {
            GL.bindVertexBuffer(this.vbo);
            GL.bufferData(34962, this.vertices.limit(this.getVerticesOffset()), 35048);
            GL.bindVertexBuffer(0);
            GL.bindIndexBuffer(this.ibo);
            GL.bufferData(34963, this.indices.limit(this.indicesCount * 4), 35048);
            GL.bindIndexBuffer(0);
        }
        this.building = false;
    }

    public void beginRender(MatrixStack matrices) {
        GL.saveState();
        if (this.depthTest) {
            GL.enableDepth();
        } else {
            GL.disableDepth();
        }
        GL.enableBlend();
        GL.disableCull();
        GL.enableLineSmooth();
        if (this.rendering3D) {
            Matrix4fStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.pushMatrix();
            if (matrices != null) {
                matrixStack.mul((Matrix4fc)matrices.peek().getPositionMatrix());
            }
            Vec3d cameraPos = MeteorClient.mc.gameRenderer.getCamera().getPos();
            matrixStack.translate(0.0f, (float)(-cameraPos.y), 0.0f);
        }
        this.beganRendering = true;
    }

    public void render(MatrixStack matrices) {
        if (this.building) {
            this.end();
        }
        if (this.indicesCount > 0) {
            boolean wasBeganRendering = this.beganRendering;
            if (!wasBeganRendering) {
                this.beginRender(matrices);
            }
            this.beforeRender();
            Shader.BOUND.setDefaults();
            GL.bindVertexArray(this.vao);
            GL.drawElements(this.drawMode.getGL(), this.indicesCount, 5125);
            GL.bindVertexArray(0);
            if (!wasBeganRendering) {
                this.endRender();
            }
        }
    }

    public void endRender() {
        if (this.rendering3D) {
            RenderSystem.getModelViewStack().popMatrix();
        }
        GL.restoreState();
        this.beganRendering = false;
    }

    public boolean isBuilding() {
        return this.building;
    }

    protected void beforeRender() {
    }

    private int getVerticesOffset() {
        return (int)(this.verticesPointer - this.verticesPointerStart);
    }

    public static enum Attrib {
        Float(1, 4, false),
        Vec2(2, 4, false),
        Vec3(3, 4, false),
        Color(4, 1, true);

        public final int count;
        public final int size;
        public final boolean normalized;

        private Attrib(int count, int componentSize, boolean normalized) {
            this.count = count;
            this.size = count * componentSize;
            this.normalized = normalized;
        }

        public int getType() {
            return this == Color ? 5121 : 5126;
        }
    }
}

