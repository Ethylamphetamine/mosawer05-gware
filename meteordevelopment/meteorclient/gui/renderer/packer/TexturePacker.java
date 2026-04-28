/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.TextureUtil
 *  net.minecraft.resource.Resource
 *  net.minecraft.util.Identifier
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.stb.STBImage
 *  org.lwjgl.stb.STBImageResize
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package meteordevelopment.meteorclient.gui.renderer.packer;

import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.renderer.packer.TextureRegion;
import meteordevelopment.meteorclient.utils.render.ByteTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class TexturePacker {
    private static final int maxWidth = 2048;
    private final List<Image> images = new ArrayList<Image>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public GuiTexture add(Identifier id) {
        try {
            InputStream in = ((Resource)MeteorClient.mc.getResourceManager().getResource(id).get()).getInputStream();
            GuiTexture texture = new GuiTexture();
            try (MemoryStack stack = MemoryStack.stackPush();){
                ByteBuffer rawImageBuffer = null;
                try {
                    rawImageBuffer = TextureUtil.readResource((InputStream)in);
                    ((Buffer)rawImageBuffer).rewind();
                    IntBuffer w = stack.mallocInt(1);
                    IntBuffer h = stack.mallocInt(1);
                    IntBuffer ignored = stack.mallocInt(1);
                    ByteBuffer imageBuffer = STBImage.stbi_load_from_memory((ByteBuffer)rawImageBuffer, (IntBuffer)w, (IntBuffer)h, (IntBuffer)ignored, (int)4);
                    int width = w.get(0);
                    int height = h.get(0);
                    TextureRegion region = new TextureRegion(width, height);
                    texture.add(region);
                    this.images.add(new Image(imageBuffer, region, width, height, true));
                    if (width > 20) {
                        this.addResized(texture, imageBuffer, width, height, 20);
                    }
                    if (width > 32) {
                        this.addResized(texture, imageBuffer, width, height, 32);
                    }
                    if (width > 48) {
                        this.addResized(texture, imageBuffer, width, height, 48);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    MemoryUtil.memFree((Buffer)rawImageBuffer);
                }
            }
            return texture;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addResized(GuiTexture texture, ByteBuffer srcImageBuffer, int srcWidth, int srcHeight, int width) {
        double scaleFactor = (double)width / (double)srcWidth;
        int height = (int)((double)srcHeight * scaleFactor);
        ByteBuffer imageBuffer = BufferUtils.createByteBuffer((int)(width * height * 4));
        STBImageResize.stbir_resize_uint8((ByteBuffer)srcImageBuffer, (int)srcWidth, (int)srcHeight, (int)0, (ByteBuffer)imageBuffer, (int)width, (int)height, (int)0, (int)4);
        TextureRegion region = new TextureRegion(width, height);
        texture.add(region);
        this.images.add(new Image(imageBuffer, region, width, height, false));
    }

    public ByteTexture pack() {
        int width = 0;
        int height = 0;
        int rowWidth = 0;
        int rowHeight = 0;
        for (Image image : this.images) {
            if (rowWidth + image.width > 2048) {
                width = Math.max(width, rowWidth);
                height += rowHeight;
                rowWidth = 0;
                rowHeight = 0;
            }
            image.x = 1 + rowWidth;
            image.y = 1 + height;
            rowWidth += 1 + image.width + 1;
            rowHeight = Math.max(rowHeight, 1 + image.height + 1);
        }
        width = Math.max(width, rowWidth);
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)(width * (height += rowHeight) * 4));
        for (Image image : this.images) {
            byte[] row = new byte[image.width * 4];
            for (int i = 0; i < image.height; ++i) {
                ((Buffer)image.buffer).position(i * row.length);
                image.buffer.get(row);
                ((Buffer)buffer).position(((image.y + i) * width + image.x) * 4);
                buffer.put(row);
            }
            ((Buffer)image.buffer).rewind();
            image.free();
            image.region.x1 = (double)image.x / (double)width;
            image.region.y1 = (double)image.y / (double)height;
            image.region.x2 = (double)(image.x + image.width) / (double)width;
            image.region.y2 = (double)(image.y + image.height) / (double)height;
        }
        ((Buffer)buffer).rewind();
        return new ByteTexture(width, height, buffer, ByteTexture.Format.RGBA, ByteTexture.Filter.Linear, ByteTexture.Filter.Linear);
    }

    private static class Image {
        public final ByteBuffer buffer;
        public final TextureRegion region;
        public final int width;
        public final int height;
        public int x;
        public int y;
        private final boolean stb;

        public Image(ByteBuffer buffer, TextureRegion region, int width, int height, boolean stb) {
            this.buffer = buffer;
            this.region = region;
            this.width = width;
            this.height = height;
            this.stb = stb;
        }

        public void free() {
            if (this.stb) {
                STBImage.stbi_image_free((ByteBuffer)this.buffer);
            }
        }
    }
}

