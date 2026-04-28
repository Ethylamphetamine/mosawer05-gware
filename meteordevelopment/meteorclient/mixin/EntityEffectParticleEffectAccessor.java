/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.particle.EntityEffectParticleEffect
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.particle.EntityEffectParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityEffectParticleEffect.class})
public interface EntityEffectParticleEffectAccessor {
    @Accessor
    public int getColor();
}

