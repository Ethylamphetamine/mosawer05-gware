/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gl.VertexBuffer
 *  net.minecraft.client.render.BufferRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BufferRenderer.class})
public interface BufferRendererAccessor {
    @Accessor(value="currentVertexBuffer")
    public static void setCurrentVertexBuffer(VertexBuffer vertexBuffer) {
    }
}

