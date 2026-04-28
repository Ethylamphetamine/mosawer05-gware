/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerAbilities
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvent
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.player.ClipAtLedgeEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerJumpEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTravelEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.SoundBlocker;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.player.Reach;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerEntity.class})
public abstract class PlayerEntityMixin
extends LivingEntity {
    @Shadow
    public abstract PlayerAbilities getAbilities();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method={"clipAtLedge"}, at={@At(value="HEAD")}, cancellable=true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        if (!this.getWorld().isClient) {
            return;
        }
        ClipAtLedgeEvent event = MeteorClient.EVENT_BUS.post(ClipAtLedgeEvent.get());
        if (event.isSet()) {
            info.setReturnValue((Object)event.isClip());
        }
    }

    @Inject(method={"dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"}, at={@At(value="HEAD")}, cancellable=true)
    private void onDropItem(ItemStack stack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> info) {
        if (this.getWorld().isClient && !stack.isEmpty() && MeteorClient.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled()) {
            info.cancel();
        }
    }

    @ModifyReturnValue(method={"getBlockBreakingSpeed"}, at={@At(value="RETURN")})
    public float onGetBlockBreakingSpeed(float breakSpeed, BlockState block) {
        if (!this.getWorld().isClient) {
            return breakSpeed;
        }
        SpeedMine speedMine = Modules.get().get(SpeedMine.class);
        if (!speedMine.isActive() || speedMine.mode.get() != SpeedMine.Mode.Normal || !speedMine.filter(block.getBlock())) {
            return breakSpeed;
        }
        float breakSpeedMod = (float)((double)breakSpeed * speedMine.modifier.get());
        HitResult hitResult = MeteorClient.mc.crosshairTarget;
        if (hitResult instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)hitResult;
            BlockPos pos = bhr.getBlockPos();
            if (speedMine.modifier.get() < 1.0 || BlockUtils.canInstaBreak(pos, breakSpeed) == BlockUtils.canInstaBreak(pos, breakSpeedMod)) {
                return breakSpeedMod;
            }
            return 0.9f / BlockUtils.calcBlockBreakingDelta2(pos, 1.0f);
        }
        return breakSpeed;
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")}, cancellable=true)
    public void dontJump(CallbackInfo info) {
        if (!this.getWorld().isClient) {
            return;
        }
        Anchor module = Modules.get().get(Anchor.class);
        if (module.isActive() && module.cancelJump) {
            info.cancel();
        } else if (Modules.get().get(Scaffold.class).towering()) {
            info.cancel();
        }
    }

    @ModifyReturnValue(method={"getMovementSpeed"}, at={@At(value="RETURN")})
    private float onGetMovementSpeed(float original) {
        if (!this.getWorld().isClient) {
            return original;
        }
        if (!Modules.get().get(NoSlow.class).slowness()) {
            return original;
        }
        float walkSpeed = this.getAbilities().getWalkSpeed();
        if (original < walkSpeed) {
            if (this.isSprinting()) {
                return (float)((double)walkSpeed * 1.300000011920929);
            }
            return walkSpeed;
        }
        return original;
    }

    @Inject(method={"getOffGroundSpeed"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetOffGroundSpeed(CallbackInfoReturnable<Float> info) {
        if (!this.getWorld().isClient) {
            return;
        }
        float speed = Modules.get().get(Flight.class).getOffGroundSpeed();
        if (speed != -1.0f) {
            info.setReturnValue((Object)Float.valueOf(speed));
        }
    }

    @WrapWithCondition(method={"attack"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")})
    private boolean keepSprint$setVelocity(PlayerEntity instance, Vec3d vec3d) {
        return Modules.get().get(Sprint.class).stopSprinting();
    }

    @WrapWithCondition(method={"attack"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V")})
    private boolean keepSprint$setSprinting(PlayerEntity instance, boolean b) {
        return Modules.get().get(Sprint.class).stopSprinting();
    }

    @ModifyReturnValue(method={"getBlockInteractionRange"}, at={@At(value="RETURN")})
    private double modifyBlockInteractionRange(double original) {
        return Math.max(0.0, original + Modules.get().get(Reach.class).blockReach());
    }

    @ModifyReturnValue(method={"getEntityInteractionRange"}, at={@At(value="RETURN")})
    private double modifyEntityInteractionRange(double original) {
        return Math.max(0.0, original + Modules.get().get(Reach.class).entityReach());
    }

    @Inject(method={"jump"}, at={@At(value="HEAD")})
    private void onJumpPre(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new PlayerJumpEvent.Pre());
    }

    @Inject(method={"jump"}, at={@At(value="RETURN")})
    private void onJumpPost(CallbackInfo ci) {
        MeteorClient.EVENT_BUS.post(new PlayerJumpEvent.Post());
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTravelPre(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)this;
        if (player != MeteorClient.mc.player) {
            return;
        }
        PlayerTravelEvent.Pre event = new PlayerTravelEvent.Pre();
        MeteorClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            PlayerTravelEvent.Post forcedPostEvent = new PlayerTravelEvent.Post();
            MeteorClient.EVENT_BUS.post(forcedPostEvent);
        }
    }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    private void onTravelPost(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)this;
        if (player != MeteorClient.mc.player) {
            return;
        }
        PlayerTravelEvent.Post event = new PlayerTravelEvent.Post();
        MeteorClient.EVENT_BUS.post(event);
    }

    @Redirect(method={"attack"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void poseNotCollide(World instance, PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        SoundBlocker soundBlocker = Modules.get().get(SoundBlocker.class);
        if (soundBlocker.isActive()) {
            instance.playSound(except, x, y, z, sound, category, (float)((double)volume * soundBlocker.getCrystalHitVolume()), pitch);
            return;
        }
        instance.playSound(except, x, y, z, sound, category, volume, pitch);
    }
}

