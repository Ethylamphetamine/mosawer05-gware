/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.fluid.Fluid
 *  net.minecraft.fluid.Fluids
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.registry.tag.FluidTags
 *  net.minecraft.registry.tag.TagKey
 *  net.minecraft.state.property.Properties
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.util.shape.VoxelShapes
 *  net.minecraft.world.GameMode
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import com.google.common.collect.Streams;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.entity.player.CanWalkOnFluidEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.LivingEntityAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.GameMode;

public class Jesus
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWater;
    private final SettingGroup sgLava;
    private final Setting<Boolean> powderSnow;
    private final Setting<Mode> waterMode;
    private final Setting<Boolean> dipIfBurning;
    private final Setting<Boolean> dipOnSneakWater;
    private final Setting<Boolean> dipOnFallWater;
    private final Setting<Integer> dipFallHeightWater;
    private final Setting<Mode> lavaMode;
    private final Setting<Boolean> dipIfFireResistant;
    private final Setting<Boolean> dipOnSneakLava;
    private final Setting<Boolean> dipOnFallLava;
    private final Setting<Integer> dipFallHeightLava;
    private final BlockPos.Mutable blockPos;
    private int tickTimer;
    private int packetTimer;
    private boolean prePathManagerWalkOnWater;
    private boolean prePathManagerWalkOnLava;
    public boolean isInBubbleColumn;

    public Jesus() {
        super(Categories.Movement, "jesus", "Walk on liquids and powder snow like Jesus.");
        this.sgGeneral = this.settings.createGroup("General");
        this.sgWater = this.settings.createGroup("Water");
        this.sgLava = this.settings.createGroup("Lava");
        this.powderSnow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("powder-snow")).description("Walk on powder snow.")).defaultValue(true)).build());
        this.waterMode = this.sgWater.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("How to treat the water.")).defaultValue(Mode.Solid)).build());
        this.dipIfBurning = this.sgWater.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-if-burning")).description("Lets you go into the water when you are burning.")).defaultValue(true)).visible(() -> this.waterMode.get() == Mode.Solid)).build());
        this.dipOnSneakWater = this.sgWater.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-sneak")).description("Lets you go into the water when your sneak key is held.")).defaultValue(true)).visible(() -> this.waterMode.get() == Mode.Solid)).build());
        this.dipOnFallWater = this.sgWater.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-fall")).description("Lets you go into the water when you fall over a certain height.")).defaultValue(true)).visible(() -> this.waterMode.get() == Mode.Solid)).build());
        this.dipFallHeightWater = this.sgWater.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dip-fall-height")).description("The fall height at which you will go into the water.")).defaultValue(4)).range(1, 255).sliderRange(3, 20).visible(() -> this.waterMode.get() == Mode.Solid && this.dipOnFallWater.get() != false)).build());
        this.lavaMode = this.sgLava.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("How to treat the lava.")).defaultValue(Mode.Solid)).build());
        this.dipIfFireResistant = this.sgLava.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-if-resistant")).description("Lets you go into the lava if you have Fire Resistance effect.")).defaultValue(true)).visible(() -> this.lavaMode.get() == Mode.Solid)).build());
        this.dipOnSneakLava = this.sgLava.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-sneak")).description("Lets you go into the lava when your sneak key is held.")).defaultValue(true)).visible(() -> this.lavaMode.get() == Mode.Solid)).build());
        this.dipOnFallLava = this.sgLava.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dip-on-fall")).description("Lets you go into the lava when you fall over a certain height.")).defaultValue(true)).visible(() -> this.lavaMode.get() == Mode.Solid)).build());
        this.dipFallHeightLava = this.sgLava.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("dip-fall-height")).description("The fall height at which you will go into the lava.")).defaultValue(4)).range(1, 255).sliderRange(3, 20).visible(() -> this.lavaMode.get() == Mode.Solid && this.dipOnFallLava.get() != false)).build());
        this.blockPos = new BlockPos.Mutable();
        this.tickTimer = 10;
        this.packetTimer = 0;
        this.isInBubbleColumn = false;
    }

    @Override
    public void onActivate() {
        this.prePathManagerWalkOnWater = PathManagers.get().getSettings().getWalkOnWater().get();
        this.prePathManagerWalkOnLava = PathManagers.get().getSettings().getWalkOnLava().get();
        PathManagers.get().getSettings().getWalkOnWater().set(this.waterMode.get() == Mode.Solid);
        PathManagers.get().getSettings().getWalkOnLava().set(this.lavaMode.get() == Mode.Solid);
    }

    @Override
    public void onDeactivate() {
        PathManagers.get().getSettings().getWalkOnWater().set(this.prePathManagerWalkOnWater);
        PathManagers.get().getSettings().getWalkOnLava().set(this.prePathManagerWalkOnLava);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean bubbleColumn = this.isInBubbleColumn;
        this.isInBubbleColumn = false;
        if (this.waterMode.get() == Mode.Bob && this.mc.player.isTouchingWater() || this.lavaMode.get() == Mode.Bob && this.mc.player.isInLava()) {
            double fluidHeight = this.mc.player.isInLava() ? this.mc.player.getFluidHeight(FluidTags.LAVA) : this.mc.player.getFluidHeight(FluidTags.WATER);
            double swimHeight = this.mc.player.getSwimHeight();
            if (this.mc.player.isTouchingWater() && fluidHeight > swimHeight) {
                ((LivingEntityAccessor)this.mc.player).swimUpwards((TagKey<Fluid>)FluidTags.WATER);
            } else if (this.mc.player.isOnGround() && fluidHeight <= swimHeight && ((LivingEntityAccessor)this.mc.player).getJumpCooldown() == 0) {
                this.mc.player.jump();
                ((LivingEntityAccessor)this.mc.player).setJumpCooldown(10);
            } else {
                ((LivingEntityAccessor)this.mc.player).swimUpwards((TagKey<Fluid>)FluidTags.LAVA);
            }
        }
        if (this.mc.player.isTouchingWater() && !this.waterShouldBeSolid()) {
            return;
        }
        if (this.mc.player.isInSwimmingPose()) {
            return;
        }
        if (this.mc.player.isInLava() && !this.lavaShouldBeSolid()) {
            return;
        }
        if (bubbleColumn) {
            if (this.mc.options.jumpKey.isPressed() && this.mc.player.getVelocity().getY() < 0.11) {
                ((IVec3d)this.mc.player.getVelocity()).setY(0.11);
            }
            return;
        }
        if (this.mc.player.isTouchingWater() || this.mc.player.isInLava()) {
            ((IVec3d)this.mc.player.getVelocity()).setY(0.11);
            this.tickTimer = 0;
            return;
        }
        BlockState blockBelowState = this.mc.world.getBlockState(this.mc.player.getBlockPos().down());
        boolean waterLogger = false;
        try {
            waterLogger = (Boolean)blockBelowState.get((Property)Properties.WATERLOGGED);
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.tickTimer == 0) {
            ((IVec3d)this.mc.player.getVelocity()).setY(0.3);
        } else if (this.tickTimer == 1 && (blockBelowState == Blocks.WATER.getDefaultState() || blockBelowState == Blocks.LAVA.getDefaultState() || waterLogger)) {
            ((IVec3d)this.mc.player.getVelocity()).setY(0.0);
        }
        ++this.tickTimer;
    }

    @EventHandler
    private void onCanWalkOnFluid(CanWalkOnFluidEvent event) {
        if (this.mc.player != null && this.mc.player.isInSwimmingPose()) {
            return;
        }
        if ((event.fluidState.getFluid() == Fluids.WATER || event.fluidState.getFluid() == Fluids.FLOWING_WATER) && this.waterShouldBeSolid()) {
            event.walkOnFluid = true;
        } else if ((event.fluidState.getFluid() == Fluids.LAVA || event.fluidState.getFluid() == Fluids.FLOWING_LAVA) && this.lavaShouldBeSolid()) {
            event.walkOnFluid = true;
        }
    }

    @EventHandler
    private void onFluidCollisionShape(CollisionShapeEvent event) {
        if (event.state.getFluidState().isEmpty()) {
            return;
        }
        if (event.state.getBlock() == Blocks.WATER | event.state.getFluidState().getFluid() == Fluids.WATER && !this.mc.player.isTouchingWater() && this.waterShouldBeSolid() && (double)event.pos.getY() <= this.mc.player.getY() - 1.0) {
            event.shape = VoxelShapes.fullCube();
        } else if (event.state.getBlock() == Blocks.LAVA && !this.mc.player.isInLava() && this.lavaShouldBeSolid() && (!this.lavaIsSafe() || (double)event.pos.getY() <= this.mc.player.getY() - 1.0)) {
            event.shape = VoxelShapes.fullCube();
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        Packet<?> packet = event.packet;
        if (!(packet instanceof PlayerMoveC2SPacket)) {
            return;
        }
        PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket)packet;
        if (this.mc.player.isTouchingWater() && !this.waterShouldBeSolid()) {
            return;
        }
        if (this.mc.player.isInLava() && !this.lavaShouldBeSolid()) {
            return;
        }
        if (!(packet2 instanceof PlayerMoveC2SPacket.PositionAndOnGround) && !(packet2 instanceof PlayerMoveC2SPacket.Full)) {
            return;
        }
        if (this.mc.player.isTouchingWater() || this.mc.player.isInLava() || this.mc.player.fallDistance > 3.0f || !this.isOverLiquid()) {
            return;
        }
        if (this.mc.player.input.movementForward == 0.0f && this.mc.player.input.movementSideways == 0.0f) {
            event.cancel();
            return;
        }
        if (this.packetTimer++ < 4) {
            return;
        }
        this.packetTimer = 0;
        event.cancel();
        double x = packet2.getX(0.0);
        double y = packet2.getY(0.0) + 0.05;
        double z = packet2.getZ(0.0);
        Object newPacket = packet2 instanceof PlayerMoveC2SPacket.PositionAndOnGround ? new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true) : new PlayerMoveC2SPacket.Full(x, y, z, packet2.getYaw(0.0f), packet2.getPitch(0.0f), true);
        this.mc.getNetworkHandler().getConnection().send((Packet)newPacket);
    }

    private boolean waterShouldBeSolid() {
        EntityType vehicle;
        if (EntityUtils.getGameMode((PlayerEntity)this.mc.player) == GameMode.SPECTATOR || this.mc.player.getAbilities().flying) {
            return false;
        }
        if (this.mc.player.getVehicle() != null && ((vehicle = this.mc.player.getVehicle().getType()) == EntityType.BOAT || vehicle == EntityType.CHEST_BOAT)) {
            return false;
        }
        if (Modules.get().get(Flight.class).isActive()) {
            return false;
        }
        if (this.dipIfBurning.get().booleanValue() && this.mc.player.isOnFire()) {
            return false;
        }
        if (this.dipOnSneakWater.get().booleanValue() && this.mc.options.sneakKey.isPressed()) {
            return false;
        }
        if (this.dipOnFallWater.get().booleanValue() && this.mc.player.fallDistance > (float)this.dipFallHeightWater.get().intValue()) {
            return false;
        }
        return this.waterMode.get() == Mode.Solid;
    }

    private boolean lavaShouldBeSolid() {
        if (EntityUtils.getGameMode((PlayerEntity)this.mc.player) == GameMode.SPECTATOR || this.mc.player.getAbilities().flying) {
            return false;
        }
        if (!this.lavaIsSafe() && this.lavaMode.get() == Mode.Solid) {
            return true;
        }
        if (this.dipOnSneakLava.get().booleanValue() && this.mc.options.sneakKey.isPressed()) {
            return false;
        }
        if (this.dipOnFallLava.get().booleanValue() && this.mc.player.fallDistance > (float)this.dipFallHeightLava.get().intValue()) {
            return false;
        }
        return this.lavaMode.get() == Mode.Solid;
    }

    private boolean lavaIsSafe() {
        if (!this.dipIfFireResistant.get().booleanValue()) {
            return false;
        }
        return this.mc.player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) && (double)this.mc.player.getStatusEffect(StatusEffects.FIRE_RESISTANCE).getDuration() > 300.0 * this.mc.player.getAttributeValue(EntityAttributes.GENERIC_BURNING_TIME);
    }

    private boolean isOverLiquid() {
        boolean foundLiquid = false;
        boolean foundSolid = false;
        List blockCollisions = Streams.stream((Iterable)this.mc.world.getBlockCollisions((Entity)this.mc.player, this.mc.player.getBoundingBox().offset(0.0, -0.5, 0.0))).map(VoxelShape::getBoundingBox).collect(Collectors.toCollection(ArrayList::new));
        for (Box bb : blockCollisions) {
            this.blockPos.set(MathHelper.lerp((double)0.5, (double)bb.minX, (double)bb.maxX), MathHelper.lerp((double)0.5, (double)bb.minY, (double)bb.maxY), MathHelper.lerp((double)0.5, (double)bb.minZ, (double)bb.maxZ));
            BlockState blockState = this.mc.world.getBlockState((BlockPos)this.blockPos);
            if (blockState.getBlock() == Blocks.WATER | blockState.getFluidState().getFluid() == Fluids.WATER || blockState.getBlock() == Blocks.LAVA) {
                foundLiquid = true;
                continue;
            }
            if (blockState.isAir()) continue;
            foundSolid = true;
        }
        return foundLiquid && !foundSolid;
    }

    public boolean canWalkOnPowderSnow() {
        return this.isActive() && this.powderSnow.get() != false;
    }

    public static enum Mode {
        Solid,
        Bob,
        Ignore;

    }
}

