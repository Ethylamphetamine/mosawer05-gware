/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.client.render.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityPose
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.MovementType
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.JumpVelocityMultiplierEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.UpdatePlayerVelocity;
import meteordevelopment.meteorclient.mixininterface.ICamera;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.Hitboxes;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraFakeFly;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.Jesus;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Entity.class})
public abstract class EntityMixin {
    @ModifyExpressionValue(method={"updateMovementInFluid"}, at={@At(value="INVOKE", target="Lnet/minecraft/fluid/FluidState;getVelocity(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/Vec3d;")})
    private Vec3d updateMovementInFluidFluidStateGetVelocity(Vec3d vec) {
        if (this != MeteorClient.mc.player) {
            return vec;
        }
        Velocity velocity = Modules.get().get(Velocity.class);
        if (velocity.isActive() && velocity.liquids.get().booleanValue()) {
            vec = vec.multiply(velocity.getHorizontal(velocity.liquidsHorizontal), velocity.getVertical(velocity.liquidsVertical), velocity.getHorizontal(velocity.liquidsHorizontal));
        }
        return vec;
    }

    @Inject(method={"isTouchingWater"}, at={@At(value="HEAD")}, cancellable=true)
    private void isTouchingWater(CallbackInfoReturnable<Boolean> info) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        if (Modules.get().get(Flight.class).isActive()) {
            info.setReturnValue((Object)false);
        }
        if (Modules.get().get(NoSlow.class).fluidDrag()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"isInLava"}, at={@At(value="HEAD")}, cancellable=true)
    private void isInLava(CallbackInfoReturnable<Boolean> info) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        if (Modules.get().get(Flight.class).isActive()) {
            info.setReturnValue((Object)false);
        }
        if (Modules.get().get(NoSlow.class).fluidDrag()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"onBubbleColumnSurfaceCollision"}, at={@At(value="HEAD")})
    private void onBubbleColumnSurfaceCollision(CallbackInfo info) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        Jesus jesus = Modules.get().get(Jesus.class);
        if (jesus.isActive()) {
            jesus.isInBubbleColumn = true;
        }
    }

    @Inject(method={"onBubbleColumnCollision"}, at={@At(value="HEAD")})
    private void onBubbleColumnCollision(CallbackInfo info) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        Jesus jesus = Modules.get().get(Jesus.class);
        if (jesus.isActive()) {
            jesus.isInBubbleColumn = true;
        }
    }

    @ModifyExpressionValue(method={"updateSwimming"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isSubmergedInWater()Z")})
    private boolean isSubmergedInWater(boolean submerged) {
        if (this != MeteorClient.mc.player) {
            return submerged;
        }
        if (Modules.get().get(NoSlow.class).fluidDrag()) {
            return false;
        }
        if (Modules.get().get(Flight.class).isActive()) {
            return false;
        }
        return submerged;
    }

    @ModifyArgs(method={"pushAwayFrom(Lnet/minecraft/entity/Entity;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    private void onPushAwayFrom(Args args, Entity entity) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (this == MeteorClient.mc.player && velocity.isActive() && velocity.entityPush.get().booleanValue()) {
            double multiplier = velocity.entityPushAmount.get();
            args.set(0, (Object)((Double)args.get(0) * multiplier));
            args.set(2, (Object)((Double)args.get(2) * multiplier));
        } else if (entity instanceof FakePlayerEntity) {
            FakePlayerEntity player = (FakePlayerEntity)entity;
            if (player.doNotPush) {
                args.set(0, (Object)0.0);
                args.set(2, (Object)0.0);
            }
        }
    }

    @ModifyReturnValue(method={"getJumpVelocityMultiplier"}, at={@At(value="RETURN")})
    private float onGetJumpVelocityMultiplier(float original) {
        if (this == MeteorClient.mc.player) {
            JumpVelocityMultiplierEvent event = MeteorClient.EVENT_BUS.post(JumpVelocityMultiplierEvent.get());
            return original * event.multiplier;
        }
        return original;
    }

    @Inject(method={"move"}, at={@At(value="HEAD")})
    private void onMove(MovementType type, Vec3d movement, CallbackInfo info) {
        if (this == MeteorClient.mc.player) {
            MeteorClient.EVENT_BUS.post(PlayerMoveEvent.get(type, movement));
        } else if (this instanceof LivingEntity) {
            MeteorClient.EVENT_BUS.post(LivingEntityMoveEvent.get((LivingEntity)this, movement));
        }
    }

    @Inject(method={"getTeamColorValue"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetTeamColorValue(CallbackInfoReturnable<Integer> info) {
        Color color;
        if (PostProcessShaders.rendering && (color = Modules.get().get(ESP.class).getSideColor((Entity)this)) != null) {
            info.setReturnValue((Object)color.getPacked());
        }
    }

    @Redirect(method={"getVelocityMultiplier"}, at=@At(value="INVOKE", target="Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;"))
    private Block getVelocityMultiplierGetBlockProxy(BlockState blockState) {
        if (this != MeteorClient.mc.player) {
            return blockState.getBlock();
        }
        if (blockState.getBlock() == Blocks.SOUL_SAND && Modules.get().get(NoSlow.class).soulSand()) {
            return Blocks.STONE;
        }
        if (blockState.getBlock() == Blocks.HONEY_BLOCK && Modules.get().get(NoSlow.class).honeyBlock()) {
            return Blocks.STONE;
        }
        return blockState.getBlock();
    }

    @ModifyReturnValue(method={"isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"}, at={@At(value="RETURN")})
    private boolean isInvisibleToCanceller(boolean original) {
        if (!Utils.canUpdate()) {
            return original;
        }
        ESP esp = Modules.get().get(ESP.class);
        if (Modules.get().get(NoRender.class).noInvisibility() || esp.isActive() && !esp.shouldSkip((Entity)this)) {
            return false;
        }
        return original;
    }

    @Inject(method={"isGlowing"}, at={@At(value="HEAD")}, cancellable=true)
    private void isGlowing(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(NoRender.class).noGlowing()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"getTargetingMargin"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetTargetingMargin(CallbackInfoReturnable<Float> info) {
        double v = Modules.get().get(Hitboxes.class).getEntityValue((Entity)this);
        if (v != 0.0) {
            info.setReturnValue((Object)Float.valueOf((float)v));
        }
    }

    @Inject(method={"isInvisibleTo"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> info) {
        if (player == null) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"getPose"}, at={@At(value="HEAD")}, cancellable=true)
    private void getPoseHook(CallbackInfoReturnable<EntityPose> info) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        if (Modules.get().get(ElytraFly.class).canPacketEfly()) {
            info.setReturnValue((Object)EntityPose.FALL_FLYING);
        }
    }

    @ModifyReturnValue(method={"getPose"}, at={@At(value="RETURN")})
    private EntityPose modifyGetPose(EntityPose original) {
        if (this != MeteorClient.mc.player) {
            return original;
        }
        ElytraFakeFly fakeFly = Modules.get().get(ElytraFakeFly.class);
        if (original == EntityPose.FALL_FLYING && fakeFly.isFlying()) {
            return EntityPose.STANDING;
        }
        if (original == EntityPose.CROUCHING && !MeteorClient.mc.player.isSneaking()) {
            return EntityPose.STANDING;
        }
        return original;
    }

    @ModifyReturnValue(method={"bypassesLandingEffects"}, at={@At(value="RETURN")})
    private boolean cancelBounce(boolean original) {
        return Modules.get().get(NoFall.class).cancelBounce() || original;
    }

    @Inject(method={"changeLookDirection"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (this != MeteorClient.mc.player) {
            return;
        }
        Freecam freecam = Modules.get().get(Freecam.class);
        FreeLook freeLook = Modules.get().get(FreeLook.class);
        if (freecam.isActive()) {
            freecam.changeLookDirection(cursorDeltaX * 0.15, cursorDeltaY * 0.15);
            ci.cancel();
        } else if (Modules.get().isActive(HighwayBuilder.class)) {
            Camera camera = MeteorClient.mc.gameRenderer.getCamera();
            ((ICamera)camera).setRot((double)camera.getYaw() + cursorDeltaX * 0.15, (double)camera.getPitch() + cursorDeltaY * 0.15);
            ci.cancel();
        } else if (freeLook.cameraMode()) {
            freeLook.cameraYaw += (float)(cursorDeltaX / (double)freeLook.sensitivity.get().floatValue());
            freeLook.cameraPitch += (float)(cursorDeltaY / (double)freeLook.sensitivity.get().floatValue());
            if (Math.abs(freeLook.cameraPitch) > 90.0f) {
                freeLook.cameraPitch = freeLook.cameraPitch > 0.0f ? 90.0f : -90.0f;
            }
            ci.cancel();
        }
    }

    @Inject(method={"updateVelocity"}, at={@At(value="HEAD")}, cancellable=true)
    public void updateVelocityHook(float speed, Vec3d movementInput, CallbackInfo ci) {
        if (this == MeteorClient.mc.player) {
            UpdatePlayerVelocity event = new UpdatePlayerVelocity(movementInput, speed, MeteorClient.mc.player.getYaw(), PlayerUtils.movementInputToVelocity(movementInput, speed, MeteorClient.mc.player.getYaw()));
            MeteorClient.EVENT_BUS.post(event);
            if (event.isCancelled()) {
                ci.cancel();
                MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().add(event.getVelocity()));
            }
        }
    }
}

