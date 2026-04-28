/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.FishingBobberEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FishingBobberEntity.class})
public abstract class FishingBobberEntityMixin {
    @WrapOperation(method={"handleStatus"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/projectile/FishingBobberEntity;pullHookedEntity(Lnet/minecraft/entity/Entity;)V")})
    private void preventFishingRodPull(FishingBobberEntity instance, Entity entity, Operation<Void> original) {
        Velocity velocity;
        if (!instance.getWorld().isClient || entity != MeteorClient.mc.player) {
            original.call(new Object[]{instance, entity});
        }
        if (!(velocity = Modules.get().get(Velocity.class)).isActive() || !velocity.fishing.get().booleanValue()) {
            original.call(new Object[]{instance, entity});
        }
    }
}

