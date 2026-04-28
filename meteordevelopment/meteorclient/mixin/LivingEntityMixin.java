/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.damage.DamageSource
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.fluid.FluidState
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.Hand
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraFakeFly;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Slide;
import meteordevelopment.meteorclient.systems.modules.player.OffhandCrash;
import meteordevelopment.meteorclient.systems.modules.player.PotionSpoof;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityMixin
extends Entity {
    @Unique
    private boolean previousElytra = false;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method={"damage"}, at={@At(value="HEAD")})
    private void onDamageHead(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (Utils.canUpdate() && this.getWorld().isClient) {
            MeteorClient.EVENT_BUS.post(DamageEvent.get((LivingEntity)this, source));
        }
    }

    @ModifyReturnValue(method={"canWalkOnFluid"}, at={@At(value="RETURN")})
    private boolean onCanWalkOnFluid(boolean original, FluidState fluidState) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        CanWalkOnFluidEvent event = MeteorClient.EVENT_BUS.post(CanWalkOnFluidEvent.get(fluidState));
        return event.walkOnFluid;
    }

    @Inject(method={"spawnItemParticles"}, at={@At(value="HEAD")}, cancellable=true)
    private void spawnItemParticles(ItemStack stack, int count, CallbackInfo info) {
        NoRender noRender = Modules.get().get(NoRender.class);
        if (noRender.noEatParticles() && stack.getComponents().contains(DataComponentTypes.FOOD)) {
            info.cancel();
        }
    }

    @Inject(method={"onEquipStack"}, at={@At(value="HEAD")}, cancellable=true)
    private void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack, CallbackInfo info) {
        if (this == MeteorClient.mc.player && Modules.get().get(OffhandCrash.class).isAntiCrash()) {
            info.cancel();
        }
    }

    @ModifyArg(method={"swingHand(Lnet/minecraft/util/Hand;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;swingHand(Lnet/minecraft/util/Hand;Z)V"))
    private Hand setHand(Hand hand) {
        HandView handView = Modules.get().get(HandView.class);
        if (this == MeteorClient.mc.player && handView.isActive()) {
            if (handView.swingMode.get() == HandView.SwingMode.None) {
                return hand;
            }
            return handView.swingMode.get() == HandView.SwingMode.Offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
        }
        return hand;
    }

    @ModifyConstant(method={"getHandSwingDuration"}, constant={@Constant(intValue=6)})
    private int getHandSwingDuration(int constant) {
        if (this != MeteorClient.mc.player) {
            return constant;
        }
        return Modules.get().get(HandView.class).isActive() && MeteorClient.mc.options.getPerspective().isFirstPerson() ? Modules.get().get(HandView.class).swingSpeed.get() : constant;
    }

    @ModifyReturnValue(method={"isFallFlying"}, at={@At(value="RETURN")})
    private boolean isFallFlyingHook(boolean original) {
        if (this == MeteorClient.mc.player && Modules.get().get(ElytraFly.class).canPacketEfly()) {
            return true;
        }
        return original;
    }

    @Inject(method={"isFallFlying"}, at={@At(value="TAIL")}, cancellable=true)
    public void recastOnLand(CallbackInfoReturnable<Boolean> cir) {
        boolean elytra = (Boolean)cir.getReturnValue();
        ElytraFly elytraFly = Modules.get().get(ElytraFly.class);
        ElytraFakeFly fakeFly = Modules.get().get(ElytraFakeFly.class);
        if (this == MeteorClient.mc.player && fakeFly.isFlying()) {
            cir.setReturnValue((Object)false);
            return;
        }
        if (this.previousElytra && !elytra && elytraFly.isActive() && (elytraFly.flightMode.get() == ElytraFlightModes.Bounce || elytraFly.flightMode.get() == ElytraFlightModes.Slide)) {
            if (elytraFly.flightMode.get() == ElytraFlightModes.Bounce) {
                cir.setReturnValue((Object)Bounce.recastElytra(MeteorClient.mc.player));
            } else if (elytraFly.flightMode.get() == ElytraFlightModes.Slide) {
                cir.setReturnValue((Object)Slide.recastElytra(MeteorClient.mc.player));
            }
        }
        this.previousElytra = elytra;
    }

    @ModifyExpressionValue(method={"travel"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isFallFlying()Z")})
    private boolean overrideTravelIsFallFlying(boolean original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        ElytraFakeFly fakeFly = Modules.get().get(ElytraFakeFly.class);
        if (fakeFly.isFlying()) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method={"isInSwimmingPose"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isFallFlying()Z")})
    private boolean overrideIsInSwimmingPosIsFallFlying(boolean original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        ElytraFakeFly fakeFly = Modules.get().get(ElytraFakeFly.class);
        if (fakeFly.isFlying()) {
            return true;
        }
        return original;
    }

    @ModifyReturnValue(method={"hasStatusEffect"}, at={@At(value="RETURN")})
    private boolean hasStatusEffect(boolean original, RegistryEntry<StatusEffect> effect) {
        if (Modules.get().get(PotionSpoof.class).shouldBlock((StatusEffect)effect.comp_349())) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method={"jump"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getYaw()F")})
    private float modifyGetYaw(float original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        Sprint s = Modules.get().get(Sprint.class);
        if (!s.rageSprint() || !s.jumpFix.get().booleanValue()) {
            return original;
        }
        float forward = Math.signum(MeteorClient.mc.player.input.movementForward);
        float strafe = 90.0f * Math.signum(MeteorClient.mc.player.input.movementSideways);
        if (forward != 0.0f) {
            strafe *= forward * 0.5f;
        }
        original -= strafe;
        if (forward < 0.0f) {
            original -= 180.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method={"jump"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;isSprinting()Z")})
    private boolean modifyIsSprinting(boolean original) {
        if (this != MeteorClient.mc.player || !Modules.get().get(Sprint.class).rageSprint()) {
            return original;
        }
        return original && (Math.abs(MeteorClient.mc.player.input.movementForward) > 1.0E-5f || Math.abs(MeteorClient.mc.player.input.movementSideways) > 1.0E-5f);
    }

    @Inject(method={"takeKnockback(DDD)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void disableKnockback(double strength, double x, double z, CallbackInfo info) {
        if (this == MeteorClient.mc.player && Modules.get().get(Velocity.class).livingEntityKnockback.get().booleanValue()) {
            info.cancel();
        }
    }

    @Inject(method={"isClimbing"}, at={@At(value="HEAD")}, cancellable=true)
    private void overrideIsClimbing(CallbackInfoReturnable<Boolean> info) {
        if (this == MeteorClient.mc.player && Modules.get().get(NoSlow.class).climbing()) {
            info.cancel();
        }
    }
}

