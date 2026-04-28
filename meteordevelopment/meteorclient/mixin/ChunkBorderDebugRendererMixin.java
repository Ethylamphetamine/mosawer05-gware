/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.render.debug.ChunkBorderDebugRenderer
 *  net.minecraft.util.math.ChunkPos
 *  net.minecraft.util.math.ChunkSectionPos
 *  net.minecraft.util.math.MathHelper
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.ChunkBorderDebugRenderer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ChunkBorderDebugRenderer.class})
public abstract class ChunkBorderDebugRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyExpressionValue(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getChunkPos()Lnet/minecraft/util/math/ChunkPos;")})
    private ChunkPos render$getChunkPos(ChunkPos chunkPos) {
        Freecam freecam = Modules.get().get(Freecam.class);
        if (!freecam.isActive()) {
            return chunkPos;
        }
        float delta = this.client.getRenderTickCounter().getTickDelta(true);
        return new ChunkPos(ChunkSectionPos.getSectionCoord((int)MathHelper.floor((double)freecam.getX(delta))), ChunkSectionPos.getSectionCoord((int)MathHelper.floor((double)freecam.getZ(delta))));
    }
}

