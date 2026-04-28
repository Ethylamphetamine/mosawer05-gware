/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.TridentItem
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.TridentBoost;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={TridentItem.class})
public abstract class TridentItemMixin {
    @Inject(method={"onStoppedUsing"}, at={@At(value="HEAD")})
    private void onStoppedUsingHead(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        if (user == MeteorClient.mc.player) {
            Utils.isReleasingTrident = true;
        }
    }

    @Inject(method={"onStoppedUsing"}, at={@At(value="TAIL")})
    private void onStoppedUsingTail(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo info) {
        if (user == MeteorClient.mc.player) {
            Utils.isReleasingTrident = false;
        }
    }

    @ModifyArgs(method={"onStoppedUsing"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;addVelocity(DDD)V"))
    private void modifyVelocity(Args args) {
        TridentBoost tridentBoost = Modules.get().get(TridentBoost.class);
        args.set(0, (Object)((Double)args.get(0) * tridentBoost.getMultiplier()));
        args.set(1, (Object)((Double)args.get(1) * tridentBoost.getMultiplier()));
        args.set(2, (Object)((Double)args.get(2) * tridentBoost.getMultiplier()));
    }

    @ModifyExpressionValue(method={"use"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z")})
    private boolean isInWaterUse(boolean original) {
        TridentBoost tridentBoost = Modules.get().get(TridentBoost.class);
        return tridentBoost.allowOutOfWater() || original;
    }

    @ModifyExpressionValue(method={"onStoppedUsing"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;isTouchingWaterOrRain()Z")})
    private boolean isInWaterPostUse(boolean original) {
        TridentBoost tridentBoost = Modules.get().get(TridentBoost.class);
        return tridentBoost.allowOutOfWater() || original;
    }
}

