/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.texture.AbstractTexture
 *  net.minecraft.resource.ResourceManager
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL30C
 */
package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.resource.ResourceManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30C;

public class ByteTexture
extends AbstractTexture {
    public ByteTexture(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.upload(width, height, data, format, filterMin, filterMag));
        } else {
            this.upload(width, height, data, format, filterMin, filterMag);
        }
    }

    public ByteTexture(int width, int height, ByteBuffer buffer, Format format, Filter filterMin, Filter filterMag) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.upload(width, height, buffer, format, filterMin, filterMag));
        } else {
            this.upload(width, height, buffer, format, filterMin, filterMag);
        }
    }

    private void upload(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)data.length).put(data);
        this.upload(width, height, buffer, format, filterMin, filterMag);
    }

    private void upload(int width, int height, ByteBuffer buffer, Format format, Filter filterMin, Filter filterMag) {
        this.bindTexture();
        GL30C.glPixelStorei((int)3312, (int)0);
        GL30C.glPixelStorei((int)3313, (int)0);
        GL30C.glPixelStorei((int)3314, (int)0);
        GL30C.glPixelStorei((int)32878, (int)0);
        GL30C.glPixelStorei((int)3315, (int)0);
        GL30C.glPixelStorei((int)3316, (int)0);
        GL30C.glPixelStorei((int)32877, (int)0);
        GL30C.glPixelStorei((int)3317, (int)4);
        GL30C.glTexParameteri((int)3553, (int)10242, (int)10497);
        GL30C.glTexParameteri((int)3553, (int)10243, (int)10497);
        GL30C.glTexParameteri((int)3553, (int)10241, (int)filterMin.toOpenGL());
        GL30C.glTexParameteri((int)3553, (int)10240, (int)filterMag.toOpenGL());
        ((Buffer)buffer).rewind();
        GL30C.glTexImage2D((int)3553, (int)0, (int)format.toOpenGL(), (int)width, (int)height, (int)0, (int)format.toOpenGL(), (int)5121, (ByteBuffer)buffer);
    }

    public void load(ResourceManager manager) throws IOException {
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
        Linear;


        public int toOpenGL() {
            return this == Nearest ? 9728 : 9729;
        }
    }
}

