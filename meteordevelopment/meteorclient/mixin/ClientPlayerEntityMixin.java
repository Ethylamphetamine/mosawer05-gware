/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.input.Input
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.client.util.ClientPlayerTickable
 *  net.minecraft.client.world.ClientWorld
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.damage.DamageSource
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.VehicleMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTickMovementEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.GrimDisabler;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.player.Portals;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin
extends AbstractClientPlayerEntity {
    @Shadow
    public Input input;
    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;
    @Shadow
    @Final
    private List<ClientPlayerTickable> tickables;
    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    private double lastX;
    @Shadow
    private double lastBaseY;
    @Shadow
    private double lastZ;
    @Shadow
    private float lastYaw;
    @Shadow
    private float lastPitch;
    @Shadow
    private boolean lastOnGround;
    @Shadow
    private boolean lastSneaking;
    @Shadow
    private int ticksSinceLastPositionPacketSent;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"dropSelectedItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.EVENT_BUS.post(DropItemsEvent.get(this.getMainHandStack())).isCancelled()) {
            info.setReturnValue((Object)false);
        }
    }

    @Redirect(method={"tickNausea"}, at=@At(value="FIELD", target="Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"))
    private Screen updateNauseaGetCurrentScreenProxy(MinecraftClient client) {
        if (Modules.get().isActive(Portals.class)) {
            return null;
        }
        return client.currentScreen;
    }

    @ModifyExpressionValue(method={"tickMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z")})
    private boolean redirectUsingItem(boolean isUsingItem) {
        if (Modules.get().get(NoSlow.class).items()) {
            return false;
        }
        return isUsingItem;
    }

    @Inject(method={"isSneaking"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsSneaking(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(Scaffold.class).scaffolding()) {
            info.setReturnValue((Object)false);
        }
        if (Modules.get().get(Flight.class).noSneak()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"shouldSlowDown"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShouldSlowDown(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(NoSlow.class).sneaking()) {
            info.setReturnValue((Object)this.isCrawling());
        }
        if (this.isCrawling() && Modules.get().get(NoSlow.class).crawling()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"pushOutOfBlocks"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPushOutOfBlocks(double x, double d, CallbackInfo info) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (velocity.isActive() && velocity.blocks.get().booleanValue()) {
            info.cancel();
        }
    }

    @Inject(method={"damage"}, at={@At(value="HEAD")})
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (Utils.canUpdate() && this.getWorld().isClient && this.canTakeDamage()) {
            MeteorClient.EVENT_BUS.post(DamageEvent.get((LivingEntity)this, source));
        }
    }

    @ModifyExpressionValue(method={"canSprint"}, at={@At(value="CONSTANT", args={"floatValue=6.0f"})})
    private float onHunger(float constant) {
        if (Modules.get().get(NoSlow.class).hunger()) {
            return -1.0f;
        }
        return constant;
    }

    @ModifyExpressionValue(method={"sendMovementPackets"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z")})
    private boolean isSneaking(boolean sneaking) {
        return Modules.get().get(Sneak.class).doPacket() || Modules.get().get(NoSlow.class).airStrict() || sneaking;
    }

    @Inject(method={"tickMovement"}, at={@At(value="HEAD")})
    private void preTickMovement(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(PlayerTickMovementEvent.get());
    }

    @ModifyExpressionValue(method={"canStartSprinting"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;isWalking()Z")})
    private boolean modifyIsWalking(boolean original) {
        if (!Modules.get().get(Sprint.class).rageSprint()) {
            return original;
        }
        float forwards = Math.abs(this.input.movementSideways);
        float sideways = Math.abs(this.input.movementForward);
        return this.isSubmergedInWater() ? forwards > 1.0E-5f || sideways > 1.0E-5f : (double)forwards > 0.8 || (double)sideways > 0.8;
    }

    @ModifyExpressionValue(method={"tickMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/input/Input;hasForwardMovement()Z")})
    private boolean modifyMovement(boolean original) {
        if (!Modules.get().get(Sprint.class).rageSprint()) {
            return original;
        }
        return Math.abs(this.input.movementSideways) > 1.0E-5f || Math.abs(this.input.movementForward) > 1.0E-5f;
    }

    @WrapWithCondition(method={"tickMovement"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;setSprinting(Z)V", ordinal=3)})
    private boolean wrapSetSprinting(ClientPlayerEntity instance, boolean b) {
        return !Modules.get().get(Sprint.class).rageSprint();
    }

    @Shadow
    private void sendSprintingPacket() {
    }

    @Shadow
    private void sendMovementPackets() {
    }

    @Shadow
    protected boolean isCamera() {
        return false;
    }

    @Shadow
    public abstract float getPitch(float var1);

    @Inject(method={"sendMovementPackets"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendMovementPacketsOverwrite(CallbackInfo ci) {
        ci.cancel();
        this.sendSprintingPacket();
        if (this.isSneaking() != this.lastSneaking) {
            ClientCommandC2SPacket.Mode mode = this.isSneaking() ? ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY : ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY;
            this.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)this, mode));
            this.lastSneaking = this.isSneaking();
        }
        if (this.isCamera()) {
            SendMovementPacketsEvent.Pre updateEvent = new SendMovementPacketsEvent.Pre();
            MeteorClient.EVENT_BUS.post(updateEvent);
            double d = this.getX() - this.lastX;
            double e = this.getY() - this.lastBaseY;
            double f = this.getZ() - this.lastZ;
            float yaw = this.getYaw();
            float pitch = this.getPitch();
            SendMovementPacketsEvent.Rotation movementPacketsEvent = new SendMovementPacketsEvent.Rotation(yaw, pitch);
            MeteorClient.EVENT_BUS.post(movementPacketsEvent);
            yaw = movementPacketsEvent.yaw;
            pitch = movementPacketsEvent.pitch;
            MeteorClient.ROTATION.rotationYaw = yaw;
            MeteorClient.ROTATION.rotationPitch = pitch;
            double deltaYaw = yaw - MeteorClient.ROTATION.lastYaw;
            double deltaPitch = pitch - MeteorClient.ROTATION.lastPitch;
            ++this.ticksSinceLastPositionPacketSent;
            boolean positionChanged = MathHelper.squaredMagnitude((double)d, (double)e, (double)f) > MathHelper.square((double)2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean rotationChanged = deltaYaw != 0.0 || deltaPitch != 0.0;
            float sendYaw = yaw;
            boolean forceFull = movementPacketsEvent.forceFull;
            if (rotationChanged && movementPacketsEvent.forceFullOnRotate) {
                forceFull = true;
            }
            if (Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
                sendYaw = ClientPlayerEntityMixin.encodeDegrees(yaw, 100000);
                RotationManager.sendDisablerPacket = true;
                RotationManager.lastActualYaw = yaw;
            }
            if (this.hasVehicle()) {
                Vec3d vec3d = this.getVelocity();
                this.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(vec3d.x, -999.0, vec3d.z, sendYaw, pitch, this.isOnGround()));
                positionChanged = false;
            } else if (forceFull || positionChanged && rotationChanged) {
                this.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.Full(this.getX(), this.getY(), this.getZ(), sendYaw, pitch, this.isOnGround()));
            } else if (positionChanged) {
                this.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(this.getX(), this.getY(), this.getZ(), this.isOnGround()));
            } else if (rotationChanged) {
                this.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(sendYaw, pitch, this.isOnGround()));
            } else if (this.lastOnGround != this.isOnGround()) {
                this.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.OnGroundOnly(this.isOnGround()));
            }
            if (positionChanged) {
                this.lastX = this.getX();
                this.lastBaseY = this.getY();
                this.lastZ = this.getZ();
                this.ticksSinceLastPositionPacketSent = 0;
            }
            if (rotationChanged) {
                this.lastYaw = yaw;
                this.lastPitch = pitch;
            }
            this.lastOnGround = this.isOnGround();
            this.autoJumpEnabled = (Boolean)MeteorClient.mc.options.getAutoJump().getValue();
        }
        MeteorClient.EVENT_BUS.post(new SendMovementPacketsEvent.Post());
    }

    @Inject(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal=2)}, cancellable=true)
    private void beforeSendVehicleMovePacket(CallbackInfo ci) {
        VehicleMoveEvent event = MeteorClient.EVENT_BUS.post(VehicleMoveEvent.get(new VehicleMoveC2SPacket(MeteorClient.mc.player.getRootVehicle()), MeteorClient.mc.player.getRootVehicle()));
        if (event.packet != null) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)event.packet);
        }
        ci.cancel();
    }

    private static float encodeDegrees(float degrees, int multiplier) {
        return degrees + (float)multiplier * 360.0f;
    }
}

