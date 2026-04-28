/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.TextureUtil
 *  net.minecraft.entity.Entity
 *  net.minecraft.resource.Resource
 *  org.lwjgl.stb.STBImage
 *  org.lwjgl.system.MemoryStack
 */
package meteordevelopment.meteorclient.utils.render.postprocess;

import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.renderer.Texture;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Resource;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class ChamsShader
extends EntityShader {
    private static final String[] FILE_FORMATS = new String[]{"png", "jpg"};
    private static Texture IMAGE_TEX;
    private static Chams chams;

    public ChamsShader() {
        MeteorClient.EVENT_BUS.subscribe(ChamsShader.class);
    }

    @PostInit
    public static void load() {
        try {
            ByteBuffer data = null;
            for (String fileFormat : FILE_FORMATS) {
                Optional optional = MeteorClient.mc.getResourceManager().getResource(MeteorClient.identifier("textures/chams." + fileFormat));
                if (optional.isEmpty() || ((Resource)optional.get()).getInputStream() == null) continue;
                data = TextureUtil.readResource((InputStream)((Resource)optional.get()).getInputStream());
                break;
            }
            if (data == null) {
                return;
            }
            data.rewind();
            try (MemoryStack stack = MemoryStack.stackPush();){
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                STBImage.stbi_set_flip_vertically_on_load((boolean)true);
                ByteBuffer image = STBImage.stbi_load_from_memory((ByteBuffer)data, (IntBuffer)width, (IntBuffer)height, (IntBuffer)comp, (int)3);
                IMAGE_TEX = new Texture();
                IMAGE_TEX.upload(width.get(0), height.get(0), image, Texture.Format.RGB, Texture.Filter.Nearest, Texture.Filter.Nearest, false);
                STBImage.stbi_image_free((ByteBuffer)image);
                STBImage.stbi_set_flip_vertically_on_load((boolean)false);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    private static void onResourcePacksReloaded(ResourcePacksReloadedEvent event) {
        ChamsShader.load();
    }

    @Override
    protected void setUniforms() {
        this.shader.set("u_Color", ChamsShader.chams.shaderColor.get());
        if (chams.isShader() && ChamsShader.chams.shader.get() == Chams.Shader.Image && IMAGE_TEX != null && IMAGE_TEX.isValid()) {
            IMAGE_TEX.bind(1);
            this.shader.set("u_TextureI", 1);
        }
    }

    @Override
    protected boolean shouldDraw() {
        if (chams == null) {
            chams = Modules.get().get(Chams.class);
        }
        return chams.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        if (!this.shouldDraw()) {
            return false;
        }
        return ChamsShader.chams.entities.get().contains(entity.getType()) && (entity != MeteorClient.mc.player || ChamsShader.chams.ignoreSelfDepth.get() == false);
    }
}

