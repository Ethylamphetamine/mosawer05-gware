/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.fluid.Fluid
 *  net.minecraft.registry.tag.TagKey
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={LivingEntity.class})
public interface LivingEntityAccessor {
    @Invoker(value="swimUpward")
    public void swimUpwards(TagKey<Fluid> var1);

    @Accessor(value="jumping")
    public boolean isJumping();

    @Accessor(value="jumpingCooldown")
    public int getJumpCooldown();

    @Accessor(value="jumpingCooldown")
    public void setJumpCooldown(int var1);
}

