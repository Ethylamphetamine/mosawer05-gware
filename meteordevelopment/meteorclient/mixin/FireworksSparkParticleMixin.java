/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.minecraft.client.particle.FireworksSparkParticle$Explosion
 *  net.minecraft.client.particle.FireworksSparkParticle$FireworkParticle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.LocalCapture
 */
package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.particle.FireworksSparkParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value={FireworksSparkParticle.FireworkParticle.class})
public abstract class FireworksSparkParticleMixin {
    @Inject(method={"addExplosionParticle"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/particle/FireworksSparkParticle$Explosion;setTrail(Z)V")}, cancellable=true, locals=LocalCapture.CAPTURE_FAILSOFT)
    private void onAddExplosion(double x, double y, double z, double velocityX, double velocityY, double velocityZ, IntList colors, IntList targetColors, boolean trail, boolean flicker, CallbackInfo info, FireworksSparkParticle.Explosion explosion) {
        if (explosion == null) {
            info.cancel();
        }
    }
}

