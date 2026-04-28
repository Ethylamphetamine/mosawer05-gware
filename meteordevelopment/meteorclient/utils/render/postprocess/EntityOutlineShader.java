/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 */
package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import net.minecraft.entity.Entity;

public class EntityOutlineShader
extends EntityShader {
    private static ESP esp;

    public EntityOutlineShader() {
        this.init("outline");
    }

    @Override
    protected boolean shouldDraw() {
        if (esp == null) {
            esp = Modules.get().get(ESP.class);
        }
        return esp.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        if (!this.shouldDraw()) {
            return false;
        }
        return !esp.shouldSkip(entity);
    }

    @Override
    protected void setUniforms() {
        this.shader.set("u_Width", EntityOutlineShader.esp.outlineWidth.get());
        this.shader.set("u_FillOpacity", EntityOutlineShader.esp.fillOpacity.get());
        this.shader.set("u_ShapeMode", EntityOutlineShader.esp.shapeMode.get().ordinal());
        this.shader.set("u_GlowMultiplier", EntityOutlineShader.esp.glowMultiplier.get());
    }
}

