/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.EntityVelocityUpdateS2CPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class Velocity
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Boolean> knockback;
    public final Setting<Mode> mode;
    public final Setting<Boolean> knockbackPhaseOnly;
    public final Setting<Boolean> knockbackPhaseInAir;
    public final Setting<Boolean> wallsGroundOnly;
    public final Setting<Boolean> wallsTrapped;
    public final Setting<Double> knockbackHorizontal;
    public final Setting<Double> knockbackVertical;
    public final Setting<Boolean> explosions;
    public final Setting<Boolean> explosionsPhased;
    public final Setting<Double> explosionsHorizontal;
    public final Setting<Double> explosionsVertical;
    public final Setting<Boolean> liquids;
    public final Setting<Double> liquidsHorizontal;
    public final Setting<Double> liquidsVertical;
    public final Setting<Boolean> entityPush;
    public final Setting<Double> entityPushAmount;
    public final Setting<Boolean> blocks;
    public final Setting<Boolean> sinking;
    public final Setting<Boolean> fishing;
    public final Setting<Boolean> livingEntityKnockback;

    public Velocity() {
        super(Categories.Movement, "velocity", "Prevents you from being moved by external forces.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.knockback = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("knockback")).description("Modifies the amount of knockback you take from attacks.")).defaultValue(true)).build());
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("How to apply knockback.")).defaultValue(Mode.Normal)).visible(this.knockback::get)).build());
        this.knockbackPhaseOnly = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("knockback-phase-only")).description("Only modifies knockback when phased into a wall.")).defaultValue(true)).visible(() -> this.knockback.get() != false && this.mode.get() == Mode.Normal)).build());
        this.knockbackPhaseInAir = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("knockback-phase-disable-in-air")).description("Doesn't modify knockback in a phase when you're in the air (like jumping).")).defaultValue(true)).visible(() -> this.knockback.get() != false && this.mode.get() == Mode.Normal && this.knockbackPhaseOnly.get() != false)).build());
        this.wallsGroundOnly = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("walls-ground-only")).description("Only apply velocity changes while on ground in Walls mode.")).defaultValue(false)).visible(() -> this.knockback.get() != false && this.mode.get() == Mode.Walls)).build());
        this.wallsTrapped = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("walls-trapped")).description("Apply velocity changes when your head is trapped (in a block) in Walls mode.")).defaultValue(false)).visible(() -> this.knockback.get() != false && this.mode.get() == Mode.Walls)).build());
        this.knockbackHorizontal = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("knockback-horizontal")).description("How much horizontal knockback you will take.")).defaultValue(0.0).sliderMax(1.0).visible(this.knockback::get)).build());
        this.knockbackVertical = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("knockback-vertical")).description("How much vertical knockback you will take.")).defaultValue(0.0).sliderMax(1.0).visible(this.knockback::get)).build());
        this.explosions = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("explosions")).description("Modifies your knockback from explosions.")).defaultValue(true)).build());
        this.explosionsPhased = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("explosions-phased-only")).description("Only modifies explosion velocity when phased.")).defaultValue(true)).visible(this.explosions::get)).build());
        this.explosionsHorizontal = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("explosions-horizontal")).description("How much velocity you will take from explosions horizontally.")).defaultValue(0.0).sliderMax(1.0).visible(this.explosions::get)).build());
        this.explosionsVertical = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("explosions-vertical")).description("How much velocity you will take from explosions vertically.")).defaultValue(0.0).sliderMax(1.0).visible(this.explosions::get)).build());
        this.liquids = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("liquids")).description("Modifies the amount you are pushed by flowing liquids.")).defaultValue(true)).build());
        this.liquidsHorizontal = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("liquids-horizontal")).description("How much velocity you will take from liquids horizontally.")).defaultValue(0.0).sliderMax(1.0).visible(this.liquids::get)).build());
        this.liquidsVertical = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("liquids-vertical")).description("How much velocity you will take from liquids vertically.")).defaultValue(0.0).sliderMax(1.0).visible(this.liquids::get)).build());
        this.entityPush = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("entity-push")).description("Modifies the amount you are pushed by entities.")).defaultValue(true)).build());
        this.entityPushAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("entity-push-amount")).description("How much you will be pushed.")).defaultValue(0.0).sliderMax(1.0).visible(this.entityPush::get)).build());
        this.blocks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blocks")).description("Prevents you from being pushed out of blocks.")).defaultValue(true)).build());
        this.sinking = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sinking")).description("Prevents you from sinking in liquids.")).defaultValue(false)).build());
        this.fishing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fishing")).description("Prevents you from being pulled by fishing rods.")).defaultValue(false)).build());
        this.livingEntityKnockback = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("living-entity-knockback")).description("Prevents you from being moved by knockback.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.sinking.get().booleanValue() && !this.mc.options.jumpKey.isPressed() && !this.mc.options.sneakKey.isPressed() && (this.mc.player.isTouchingWater() || this.mc.player.isInLava()) && this.mc.player.getVelocity().y < 0.0) {
            ((IVec3d)this.mc.player.getVelocity()).setY(0.0);
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        EntityVelocityUpdateS2CPacket packet;
        Packet<?> packet2;
        if (this.knockback.get().booleanValue() && (packet2 = event.packet) instanceof EntityVelocityUpdateS2CPacket && (packet = (EntityVelocityUpdateS2CPacket)packet2).getEntityId() == this.mc.player.getId()) {
            if (this.mode.get() == Mode.Normal) {
                if (this.knockbackPhaseOnly.get().booleanValue()) {
                    if (this.knockbackPhaseInAir.get().booleanValue() && !RotationManager.lastGround) {
                        return;
                    }
                    if (!PlayerUtils.isPlayerPhased()) {
                        return;
                    }
                }
            } else if (this.mode.get() == Mode.Walls) {
                if (this.wallsGroundOnly.get().booleanValue() && !RotationManager.lastGround) {
                    return;
                }
                boolean phased = PlayerUtils.isPlayerPhased();
                if (!(phased || this.wallsTrapped.get().booleanValue() && this.isHeadTrapped())) {
                    return;
                }
            }
            double velX = (packet.getVelocityX() / 8000.0 - this.mc.player.getVelocity().x) * this.knockbackHorizontal.get();
            double velY = (packet.getVelocityY() / 8000.0 - this.mc.player.getVelocity().y) * this.knockbackVertical.get();
            double velZ = (packet.getVelocityZ() / 8000.0 - this.mc.player.getVelocity().z) * this.knockbackHorizontal.get();
            ((EntityVelocityUpdateS2CPacketAccessor)packet).setX((int)(velX * 8000.0 + this.mc.player.getVelocity().x * 8000.0));
            ((EntityVelocityUpdateS2CPacketAccessor)packet).setY((int)(velY * 8000.0 + this.mc.player.getVelocity().y * 8000.0));
            ((EntityVelocityUpdateS2CPacketAccessor)packet).setZ((int)(velZ * 8000.0 + this.mc.player.getVelocity().z * 8000.0));
        }
    }

    public double getHorizontal(Setting<Double> setting) {
        if (!this.isActive()) {
            return 1.0;
        }
        if (setting == this.explosionsHorizontal && this.explosions.get().booleanValue() && this.explosionsPhased.get().booleanValue() && !PlayerUtils.isPlayerPhased()) {
            return 1.0;
        }
        return setting.get();
    }

    public double getVertical(Setting<Double> setting) {
        if (!this.isActive()) {
            return 1.0;
        }
        if (setting == this.explosionsVertical && this.explosions.get().booleanValue() && this.explosionsPhased.get().booleanValue() && !PlayerUtils.isPlayerPhased()) {
            return 1.0;
        }
        return setting.get();
    }

    private boolean isHeadTrapped() {
        if (this.mc.player != null && this.mc.world != null) {
            int up = this.mc.player.isSneaking() ? 1 : 2;
            BlockPos headPos = this.mc.player.getBlockPos().up(up);
            return !this.mc.world.getBlockState(headPos).isAir();
        }
        return false;
    }

    public static enum Mode {
        Normal,
        Walls;

    }
}

