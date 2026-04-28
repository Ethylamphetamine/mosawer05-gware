/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.TextureUtil
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.resource.Resource
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.stb.STBImage
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.utils.network.Http;
import net.minecraft.resource.Resource;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class PlayerHeadTexture
extends Texture {
    private boolean needsRotate;

    public PlayerHeadTexture(String url) {
        int j;
        int y;
        int x;
        BufferedImage skin;
        try {
            skin = ImageIO.read(Http.get(url).sendInputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        byte[] head = new byte[192];
        int[] pixel = new int[4];
        int i = 0;
        for (x = 8; x < 16; ++x) {
            for (y = 8; y < 16; ++y) {
                skin.getData().getPixel(x, y, pixel);
                for (j = 0; j < 3; ++j) {
                    head[i] = (byte)pixel[j];
                    ++i;
                }
            }
        }
        i = 0;
        for (x = 40; x < 48; ++x) {
            for (y = 8; y < 16; ++y) {
                skin.getData().getPixel(x, y, pixel);
                if (pixel[3] != 0) {
                    for (j = 0; j < 3; ++j) {
                        head[i] = (byte)pixel[j];
                        ++i;
                    }
                    continue;
                }
                i += 3;
            }
        }
        this.upload(BufferUtils.createByteBuffer((int)head.length).put(head));
        this.needsRotate = true;
    }

    public PlayerHeadTexture() {
        try (InputStream inputStream = ((Resource)MeteorClient.mc.getResourceManager().getResource(MeteorClient.identifier("textures/steve.png")).get()).getInputStream();){
            ByteBuffer data = TextureUtil.readResource((InputStream)inputStream);
            data.rewind();
            try (MemoryStack stack = MemoryStack.stackPush();){
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                ByteBuffer image = STBImage.stbi_load_from_memory((ByteBuffer)data, (IntBuffer)width, (IntBuffer)height, (IntBuffer)comp, (int)3);
                this.upload(image);
                STBImage.stbi_image_free((ByteBuffer)image);
            }
            MemoryUtil.memFree((Buffer)data);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upload(ByteBuffer data) {
        Runnable action = () -> this.upload(8, 8, data, Texture.Format.RGB, Texture.Filter.Nearest, Texture.Filter.Nearest, false);
        if (RenderSystem.isOnRenderThread()) {
            action.run();
        } else {
            RenderSystem.recordRenderCall(action::run);
        }
    }

    public boolean needsRotate() {
        return this.needsRotate;
    }
}

