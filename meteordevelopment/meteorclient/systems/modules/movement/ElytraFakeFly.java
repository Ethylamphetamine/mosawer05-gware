/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.network.packet.s2c.play.PositionFlag
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public class ElytraFakeFly
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBoost;
    private final Setting<Mode> mode;
    public final Setting<Double> fireworkDelay;
    public final Setting<Double> horizontalSpeed;
    public final Setting<Double> verticalSpeed;
    public final Setting<Double> accelTime;
    public final Setting<Boolean> sprintToBoost;
    public final Setting<Double> sprintToBoostMaxSpeed;
    public final Setting<Double> boostAccelTime;
    private int fireworkTicksLeft;
    private boolean needsFirework;
    private Vec3d lastMovement;
    private Vec3d currentVelocity;
    private long timeOfLastRubberband;
    private Vec3d lastRubberband;

    public ElytraFakeFly() {
        super(Categories.Movement, "elytra-fakefly", "Gives you more control over your elytra but funnier.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBoost = this.settings.createGroup("Boost");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Determines how to fake fly")).defaultValue(Mode.Chestplate)).build());
        this.fireworkDelay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("firework-delay")).description("Length of a firework.")).defaultValue(2.1).min(0.0).max(5.0).build());
        this.horizontalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("horizontal-speed")).description("Controls how fast will you go horizontally.")).defaultValue(50.0).min(0.0).max(100.0).build());
        this.verticalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vertical-speed")).description("Controls how fast will you go veritcally.")).defaultValue(30.0).min(0.0).max(100.0).build());
        this.accelTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("accel-time")).description("Controls how fast will you accelerate and decelerate in second")).defaultValue(0.25).min(0.01).max(2.0).build());
        this.sprintToBoost = this.sgBoost.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sprint-to-boost")).description("Allows you to hold sprint to go extra fast")).defaultValue(true)).build());
        this.sprintToBoostMaxSpeed = this.sgBoost.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("boost-max-speed")).description("Controls how fast will can go at maximum boost speed")).defaultValue(100.0).min(50.0).sliderMax(300.0).build());
        this.boostAccelTime = this.sgBoost.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("boost-accel-time")).description("Conbtrols how fast you will accelerate and decelerate when boosting")).defaultValue(0.5).min(0.01).sliderMax(2.0).build());
        this.fireworkTicksLeft = 0;
        this.needsFirework = false;
        this.lastMovement = Vec3d.ZERO;
        this.currentVelocity = Vec3d.ZERO;
        this.timeOfLastRubberband = 0L;
        this.lastRubberband = Vec3d.ZERO;
    }

    @Override
    public void onActivate() {
        this.needsFirework = this.getIsUsingFirework();
        this.currentVelocity = this.mc.player.getVelocity();
        this.mc.player.jump();
        this.mc.player.setOnGround(false);
    }

    @Override
    public void onDeactivate() {
        PlayerUtils.silentSwapEquipChestplate();
        this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        this.mc.player.setSneaking(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean isUsingFirework = this.getIsUsingFirework();
        if (!isUsingFirework && !InvUtils.find(Items.FIREWORK_ROCKET).found()) {
            return;
        }
        Vec3d desiredVelocity = new Vec3d(0.0, 0.0, 0.0);
        double yaw = Math.toRadians(this.mc.player.getYaw());
        double pitch = Math.toRadians(this.mc.player.getPitch());
        Vec3d direction = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch)).normalize();
        if (this.mc.options.forwardKey.isPressed()) {
            desiredVelocity = desiredVelocity.add(direction.multiply(this.getHorizontalSpeed() / 20.0, 0.0, this.getHorizontalSpeed() / 20.0));
        }
        if (this.mc.options.backKey.isPressed()) {
            desiredVelocity = desiredVelocity.add(direction.multiply(-this.getHorizontalSpeed() / 20.0, 0.0, -this.getHorizontalSpeed() / 20.0));
        }
        if (this.mc.options.leftKey.isPressed()) {
            desiredVelocity = desiredVelocity.add(direction.multiply(this.getHorizontalSpeed() / 20.0, 0.0, this.getHorizontalSpeed() / 20.0).rotateY(1.5707964f));
        }
        if (this.mc.options.rightKey.isPressed()) {
            desiredVelocity = desiredVelocity.add(direction.multiply(this.getHorizontalSpeed() / 20.0, 0.0, this.getHorizontalSpeed() / 20.0).rotateY(-1.5707964f));
        }
        if (this.mc.options.jumpKey.isPressed()) {
            desiredVelocity = desiredVelocity.add(0.0, this.verticalSpeed.get() / 20.0, 0.0);
        }
        if (this.mc.options.sneakKey.isPressed()) {
            desiredVelocity = desiredVelocity.add(0.0, -this.verticalSpeed.get().doubleValue() / 20.0, 0.0);
        }
        this.currentVelocity = new Vec3d(this.mc.player.getVelocity().x, this.currentVelocity.y, this.mc.player.getVelocity().z);
        Vec3d velocityDifference = desiredVelocity.subtract(this.currentVelocity);
        double maxDelta = this.getHorizontalSpeed() / 20.0 / (this.getHorizontalAccelTime() * 20.0);
        if (velocityDifference.lengthSquared() > maxDelta * maxDelta) {
            velocityDifference = velocityDifference.normalize().multiply(maxDelta);
        }
        this.currentVelocity = this.currentVelocity.add(velocityDifference);
        Box boundingBox = this.mc.player.getBoundingBox();
        double playerFeetY = boundingBox.minY;
        Box groundBox = new Box(boundingBox.minX, playerFeetY - 0.1, boundingBox.minZ, boundingBox.maxX, playerFeetY, boundingBox.maxZ);
        for (BlockPos pos : BlockPos.iterate((int)((int)Math.floor(groundBox.minX)), (int)((int)Math.floor(groundBox.minY)), (int)((int)Math.floor(groundBox.minZ)), (int)((int)Math.floor(groundBox.maxX)), (int)((int)Math.floor(groundBox.maxY)), (int)((int)Math.floor(groundBox.maxZ)))) {
            double blockTopY;
            double distanceToBlock;
            BlockState blockState = this.mc.world.getBlockState(pos);
            if (!blockState.isSolidBlock((BlockView)this.mc.world, pos) || !((distanceToBlock = playerFeetY - (blockTopY = (double)pos.getY() + 1.0)) >= 0.0) || !(distanceToBlock < 0.1) || !(this.currentVelocity.y < 0.0)) continue;
            this.currentVelocity = new Vec3d(this.currentVelocity.x, 0.1, this.currentVelocity.z);
        }
        if (this.fireworkTicksLeft < (int)(this.fireworkDelay.get() * 20.0) - 3 && this.fireworkTicksLeft > 3 && !isUsingFirework) {
            this.fireworkTicksLeft = 0;
        }
        PlayerUtils.silentSwapEquipElytra();
        this.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        if (this.fireworkTicksLeft <= 0) {
            this.needsFirework = true;
        }
        if (this.needsFirework && this.currentVelocity.length() > 1.0E-7) {
            this.useFirework();
            this.needsFirework = false;
        }
        if (this.fireworkTicksLeft >= 0) {
            --this.fireworkTicksLeft;
        }
        if (this.mode.get() == Mode.Chestplate) {
            PlayerUtils.silentSwapEquipChestplate();
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (!this.isActive()) {
            return;
        }
        if (!this.getIsUsingFirework() && !InvUtils.find(Items.FIREWORK_ROCKET).found()) {
            return;
        }
        if (this.lastMovement == null) {
            this.lastMovement = event.movement;
        }
        Vec3d newMovement = this.currentVelocity;
        this.mc.player.setVelocity(newMovement);
        ((IVec3d)event.movement).set(newMovement.x, newMovement.y, newMovement.z);
        this.lastMovement = newMovement;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
    }

    @EventHandler
    public void onPacketReceive(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet2 = (PlayerPositionLookS2CPacket)packet;
            if (packet2.getFlags().contains(PositionFlag.X)) {
                this.currentVelocity = new Vec3d(packet2.getX(), this.currentVelocity.y, this.currentVelocity.z);
            }
            if (packet2.getFlags().contains(PositionFlag.Y)) {
                this.currentVelocity = new Vec3d(this.currentVelocity.x, packet2.getY(), this.currentVelocity.z);
            }
            if (packet2.getFlags().contains(PositionFlag.Z)) {
                this.currentVelocity = new Vec3d(this.currentVelocity.x, this.currentVelocity.y, packet2.getZ());
            }
            if (!(packet2.getFlags().contains(PositionFlag.X) || packet2.getFlags().contains(PositionFlag.Y) || packet2.getFlags().contains(PositionFlag.Z))) {
                if (System.currentTimeMillis() - this.timeOfLastRubberband < 100L) {
                    this.currentVelocity = new Vec3d(packet2.getX(), packet2.getY(), packet2.getZ()).subtract(this.lastRubberband);
                }
                this.timeOfLastRubberband = System.currentTimeMillis();
                this.lastRubberband = new Vec3d(packet2.getX(), packet2.getY(), packet2.getZ());
            }
        }
    }

    private boolean getIsUsingFirework() {
        boolean usingFirework = false;
        for (Entity entity : this.mc.world.getEntities()) {
            FireworkRocketEntity firework;
            if (!(entity instanceof FireworkRocketEntity) || (firework = (FireworkRocketEntity)entity).getOwner() == null || !firework.getOwner().equals((Object)this.mc.player)) continue;
            usingFirework = true;
        }
        return usingFirework;
    }

    public boolean isFlying() {
        return this.isActive();
    }

    private void useFirework() {
        this.fireworkTicksLeft = (int)(this.fireworkDelay.get() * 20.0);
        int hotbarSilentSwapSlot = -1;
        int inventorySilentSwapSlot = -1;
        FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
        if (!itemResult.found()) {
            FindItemResult invResult = InvUtils.find(Items.FIREWORK_ROCKET);
            if (!invResult.found()) {
                return;
            }
            FindItemResult hotbarSlotToSwapToResult = InvUtils.findInHotbar(x -> x.getItem() != Items.TOTEM_OF_UNDYING);
            inventorySilentSwapSlot = invResult.slot();
            hotbarSilentSwapSlot = hotbarSlotToSwapToResult.found() ? hotbarSlotToSwapToResult.slot() : 0;
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, inventorySilentSwapSlot, hotbarSilentSwapSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
            itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
        }
        if (!itemResult.found()) {
            return;
        }
        if (itemResult.isOffhand()) {
            this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.OFF_HAND);
            this.mc.player.swingHand(Hand.OFF_HAND);
        } else {
            InvUtils.swap(itemResult.slot(), true);
            this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
            this.mc.player.swingHand(Hand.MAIN_HAND);
            InvUtils.swapBack();
        }
        if (inventorySilentSwapSlot != -1 && hotbarSilentSwapSlot != -1) {
            this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, inventorySilentSwapSlot, hotbarSilentSwapSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
        }
    }

    private double getHorizontalSpeed() {
        if (this.mc.options.sprintKey.isPressed()) {
            double horizontalVelocity = this.currentVelocity.horizontalLength();
            return Math.clamp(horizontalVelocity * 1.3 * 20.0, this.horizontalSpeed.get(), this.sprintToBoostMaxSpeed.get());
        }
        return this.horizontalSpeed.get();
    }

    private double getHorizontalAccelTime() {
        if (this.currentVelocity.horizontalLength() > this.horizontalSpeed.get()) {
            return this.boostAccelTime.get();
        }
        return this.accelTime.get();
    }

    public static enum Mode {
        Chestplate,
        Elytra;

    }
}

