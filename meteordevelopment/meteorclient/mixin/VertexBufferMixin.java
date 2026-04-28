/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem$ShapeIndexBuffer
 *  net.minecraft.client.gl.VertexBuffer
 *  net.minecraft.client.render.BuiltBuffer$DrawParameters
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import meteordevelopment.meteorclient.mixin.ShapeIndexBufferAccessor;
import meteordevelopment.meteorclient.renderer.GL;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BuiltBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={VertexBuffer.class})
public abstract class VertexBufferMixin {
    @Shadow
    private int indexBufferId;

    @Inject(method={"uploadIndexBuffer(Lnet/minecraft/client/render/BuiltBuffer$DrawParameters;Ljava/nio/ByteBuffer;)Lcom/mojang/blaze3d/systems/RenderSystem$ShapeIndexBuffer;"}, at={@At(value="RETURN")})
    private void onConfigureIndexBuffer(BuiltBuffer.DrawParameters parameters, ByteBuffer indexBuffer, CallbackInfoReturnable<RenderSystem.ShapeIndexBuffer> info) {
        GL.CURRENT_IBO = info.getReturnValue() == null ? this.indexBufferId : ((ShapeIndexBufferAccessor)info.getReturnValue()).getId();
    }
}

