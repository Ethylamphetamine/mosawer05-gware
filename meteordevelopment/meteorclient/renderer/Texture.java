/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  org.lwjgl.BufferUtils
 */
package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.renderer.GL;
import org.lwjgl.BufferUtils;

public class Texture {
    public int width;
    public int height;
    private int id;
    private boolean valid;

    public Texture(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        if (RenderSystem.isOnRenderThread()) {
            this.upload(width, height, data, format, filterMin, filterMag);
        } else {
            RenderSystem.recordRenderCall(() -> this.upload(width, height, data, format, filterMin, filterMag));
        }
    }

    public Texture() {
    }

    protected void upload(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)data.length).put(data);
        this.upload(width, height, buffer, format, filterMin, filterMag, false);
    }

    public void upload(int width, int height, ByteBuffer buffer, Format format, Filter filterMin, Filter filterMag, boolean wrapClamp) {
        this.width = width;
        this.height = height;
        if (!this.valid) {
            this.id = GL.genTexture();
            this.valid = true;
        }
        this.bind();
        GL.defaultPixelStore();
        GL.textureParam(3553, 10242, wrapClamp ? 33071 : 10497);
        GL.textureParam(3553, 10243, wrapClamp ? 33071 : 10497);
        GL.textureParam(3553, 10241, filterMin.toOpenGL());
        GL.textureParam(3553, 10240, filterMag.toOpenGL());
        ((Buffer)buffer).rewind();
        GL.textureImage2D(3553, 0, format.toOpenGL(), width, height, 0, format.toOpenGL(), 5121, buffer);
        if (filterMin == Filter.LinearMipmapLinear || filterMag == Filter.LinearMipmapLinear) {
            GL.generateMipmap(3553);
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    public void bind(int slot) {
        GL.bindTexture(this.id, slot);
    }

    public void bind() {
        this.bind(0);
    }

    public void dispose() {
        GL.deleteTexture(this.id);
        this.valid = false;
    }

    public static enum Format {
        A,
        RGB,
        RGBA;


        public int toOpenGL() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 6403;
                case 1 -> 6407;
                case 2 -> 6408;
            };
        }
    }

    public static enum Filter {
        Nearest,
        Linear,
        LinearMipmapLinear;


        public int toOpenGL() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 9728;
                case 1 -> 9729;
                case 2 -> 9987;
            };
        }
    }
}

