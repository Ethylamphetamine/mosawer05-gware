/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader;
import net.minecraft.entity.Entity;

public class StorageOutlineShader
extends PostProcessShader {
    private static StorageESP storageESP;

    public StorageOutlineShader() {
        this.init("outline");
    }

    @Override
    protected void preDraw() {
        this.framebuffer.clear(false);
        this.framebuffer.beginWrite(false);
    }

    @Override
    protected boolean shouldDraw() {
        if (storageESP == null) {
            storageESP = Modules.get().get(StorageESP.class);
        }
        return storageESP.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        return true;
    }

    @Override
    protected void setUniforms() {
        this.shader.set("u_Width", StorageOutlineShader.storageESP.outlineWidth.get());
        this.shader.set("u_FillOpacity", (double)StorageOutlineShader.storageESP.fillOpacity.get().intValue() / 255.0);
        this.shader.set("u_ShapeMode", StorageOutlineShader.storageESP.shapeMode.get().ordinal());
        this.shader.set("u_GlowMultiplier", StorageOutlineShader.storageESP.glowMultiplier.get());
    }
}

