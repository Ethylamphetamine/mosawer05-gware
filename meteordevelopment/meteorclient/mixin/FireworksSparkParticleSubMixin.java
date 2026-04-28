/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.FireworksSparkParticle$Explosion
 *  net.minecraft.client.particle.FireworksSparkParticle$Flash
 *  net.minecraft.client.render.Camera
 *  net.minecraft.client.render.VertexConsumer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.client.particle.FireworksSparkParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FireworksSparkParticle.Explosion.class, FireworksSparkParticle.Flash.class})
public abstract class FireworksSparkParticleSubMixin {
    @Inject(method={"buildGeometry"}, at={@At(value="HEAD")}, cancellable=true)
    private void buildExplosionGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta, CallbackInfo info) {
        if (Modules.get().get(NoRender.class).noFireworkExplosions()) {
            info.cancel();
        }
    }
}

