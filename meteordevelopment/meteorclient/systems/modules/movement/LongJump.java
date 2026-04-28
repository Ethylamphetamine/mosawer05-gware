/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
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
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class LongJump
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<JumpMode> jumpMode;
    private final Setting<Double> vanillaBoostFactor;
    private final Setting<Double> burstInitialSpeed;
    private final Setting<Double> burstBoostFactor;
    private final Setting<Boolean> onlyOnGround;
    private final Setting<Boolean> onJump;
    private final Setting<Double> glideMultiplier;
    public final Setting<Double> timer;
    private final Setting<Boolean> autoDisable;
    private final Setting<Boolean> disableOnRubberband;
    private int stage;
    private double moveSpeed;
    private boolean jumping;
    private int airTicks;
    private int groundTicks;
    private boolean jumped;

    public LongJump() {
        super(Categories.Movement, "long-jump", "Allows you to jump further than normal.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.jumpMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The method of jumping.")).defaultValue(JumpMode.Vanilla)).build());
        this.vanillaBoostFactor = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vanilla-boost-factor")).description("The amount by which to boost the jump.")).visible(() -> this.jumpMode.get() == JumpMode.Vanilla)).defaultValue(1.261).min(0.0).sliderMax(5.0).build());
        this.burstInitialSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("burst-initial-speed")).description("The initial speed of the runup.")).visible(() -> this.jumpMode.get() == JumpMode.Burst)).defaultValue(6.0).min(0.0).sliderMax(20.0).build());
        this.burstBoostFactor = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("burst-boost-factor")).description("The amount by which to boost the jump.")).visible(() -> this.jumpMode.get() == JumpMode.Burst)).defaultValue(2.149).min(0.0).sliderMax(20.0).build());
        this.onlyOnGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Only performs the jump if you are on the ground.")).visible(() -> this.jumpMode.get() == JumpMode.Burst)).defaultValue(true)).build());
        this.onJump = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("on-jump")).description("Whether the player needs to jump first or not.")).visible(() -> this.jumpMode.get() == JumpMode.Burst)).defaultValue(false)).build());
        this.glideMultiplier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("glide-multiplier")).description("The amount by to multiply the glide velocity.")).visible(() -> this.jumpMode.get() == JumpMode.Glide)).defaultValue(1.0).min(0.0).sliderMax(5.0).build());
        this.timer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("timer")).description("Timer override.")).defaultValue(1.0).min(0.01).sliderMin(0.01).build());
        this.autoDisable = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-disable")).description("Automatically disabled the module after jumping.")).visible(() -> this.jumpMode.get() != JumpMode.Vanilla)).defaultValue(true)).build());
        this.disableOnRubberband = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-on-rubberband")).description("Disables the module when you get lagged back.")).defaultValue(true)).build());
        this.jumping = false;
        this.jumped = false;
    }

    @Override
    public void onActivate() {
        this.stage = 0;
        this.jumping = false;
        this.airTicks = 0;
        this.groundTicks = -5;
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(1.0);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket && this.disableOnRubberband.get().booleanValue()) {
            this.info("Rubberband detected! Disabling...", new Object[0]);
            this.toggle();
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (this.timer.get() != 1.0) {
            Modules.get().get(Timer.class).setOverride(PlayerUtils.isMoving() ? this.timer.get() : 1.0);
        }
        switch (this.jumpMode.get().ordinal()) {
            case 0: {
                if (!PlayerUtils.isMoving() || !this.mc.options.jumpKey.isPressed()) break;
                double dir = this.getDir();
                double xDir = Math.cos(Math.toRadians(dir + 90.0));
                double zDir = Math.sin(Math.toRadians(dir + 90.0));
                if (!this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, this.mc.player.getVelocity().y, 0.0)) || this.mc.player.verticalCollision) {
                    ((IVec3d)event.movement).setXZ(xDir * (double)0.29f, zDir * (double)0.29f);
                }
                if (event.movement.getY() != 0.33319999363422365) break;
                ((IVec3d)event.movement).setXZ(xDir * this.vanillaBoostFactor.get(), zDir * this.vanillaBoostFactor.get());
                break;
            }
            case 1: {
                if (this.stage != 0 && !this.mc.player.isOnGround() && this.autoDisable.get().booleanValue()) {
                    this.jumping = true;
                }
                if (this.jumping && this.mc.player.getY() - (double)((int)this.mc.player.getY()) < 0.01) {
                    this.jumping = false;
                    this.toggle();
                    this.info("Disabling after jump.", new Object[0]);
                }
                if (this.onlyOnGround.get().booleanValue() && !this.mc.player.isOnGround() && this.stage == 0) {
                    return;
                }
                double xDist = this.mc.player.getX() - this.mc.player.prevX;
                double zDist = this.mc.player.getZ() - this.mc.player.prevZ;
                double lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                if (!PlayerUtils.isMoving() || this.onJump.get().booleanValue() && !this.mc.options.jumpKey.isPressed() || this.mc.player.isInLava() || this.mc.player.isTouchingWater()) break;
                if (this.stage == 0) {
                    this.moveSpeed = this.getMoveSpeed() * this.burstInitialSpeed.get();
                } else if (this.stage == 1) {
                    ((IVec3d)event.movement).setY(0.42);
                    this.moveSpeed *= this.burstBoostFactor.get().doubleValue();
                } else if (this.stage == 2) {
                    double difference = lastDist - this.getMoveSpeed();
                    this.moveSpeed = lastDist - difference;
                } else {
                    this.moveSpeed = lastDist - lastDist / 159.0;
                }
                this.moveSpeed = Math.max(this.getMoveSpeed(), this.moveSpeed);
                this.setMoveSpeed(event, this.moveSpeed);
                if (!(this.mc.player.verticalCollision || this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, this.mc.player.getVelocity().y, 0.0)) || this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, -0.4, 0.0)))) {
                    ((IVec3d)event.movement).setY(-0.001);
                }
                ++this.stage;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Utils.canUpdate() && this.jumpMode.get() == JumpMode.Glide) {
            if (!PlayerUtils.isMoving()) {
                return;
            }
            float yaw = this.mc.player.getYaw() + 90.0f;
            double forward = this.mc.player.forwardSpeed != 0.0f ? (this.mc.player.forwardSpeed > 0.0f ? 1 : -1) : 0;
            float[] motion = new float[]{0.4206065f, 0.4179245f, 0.41525924f, 0.41261f, 0.409978f, 0.407361f, 0.404761f, 0.402178f, 0.399611f, 0.39706f, 0.394525f, 0.392f, 0.3894f, 0.38644f, 0.383655f, 0.381105f, 0.37867f, 0.37625f, 0.37384f, 0.37145f, 0.369f, 0.3666f, 0.3642f, 0.3618f, 0.35945f, 0.357f, 0.354f, 0.351f, 0.348f, 0.345f, 0.342f, 0.339f, 0.336f, 0.333f, 0.33f, 0.327f, 0.324f, 0.321f, 0.318f, 0.315f, 0.312f, 0.309f, 0.307f, 0.305f, 0.303f, 0.3f, 0.297f, 0.295f, 0.293f, 0.291f, 0.289f, 0.287f, 0.285f, 0.283f, 0.281f, 0.279f, 0.277f, 0.275f, 0.273f, 0.271f, 0.269f, 0.267f, 0.265f, 0.263f, 0.261f, 0.259f, 0.257f, 0.255f, 0.253f, 0.251f, 0.249f, 0.247f, 0.245f, 0.243f, 0.241f, 0.239f, 0.237f};
            float[] glide = new float[]{0.3425f, 0.5445f, 0.65425f, 0.685f, 0.675f, 0.2f, 0.895f, 0.719f, 0.76f};
            double cos = Math.cos(Math.toRadians(yaw));
            double sin = Math.sin(Math.toRadians(yaw));
            if (!this.mc.player.verticalCollision && !this.mc.player.isOnGround()) {
                this.jumped = true;
                ++this.airTicks;
                this.groundTicks = -5;
                double velocityY = this.mc.player.getVelocity().y;
                if (this.airTicks - 6 >= 0 && this.airTicks - 6 < glide.length) {
                    this.updateY(velocityY * (double)glide[this.airTicks - 6] * this.glideMultiplier.get());
                }
                if (velocityY < -0.2 && velocityY > -0.24) {
                    this.updateY(velocityY * 0.7 * this.glideMultiplier.get());
                } else if (velocityY < -0.25 && velocityY > -0.32) {
                    this.updateY(velocityY * 0.8 * this.glideMultiplier.get());
                } else if (velocityY < -0.35 && velocityY > -0.8) {
                    this.updateY(velocityY * 0.98 * this.glideMultiplier.get());
                }
                if (this.airTicks - 1 >= 0 && this.airTicks - 1 < motion.length) {
                    this.mc.player.setVelocity(forward * (double)motion[this.airTicks - 1] * 3.0 * cos * this.glideMultiplier.get(), this.mc.player.getVelocity().y, forward * (double)motion[this.airTicks - 1] * 3.0 * sin * this.glideMultiplier.get());
                } else {
                    this.mc.player.setVelocity(0.0, this.mc.player.getVelocity().y, 0.0);
                }
            } else {
                if (this.autoDisable.get().booleanValue() && this.jumped) {
                    this.jumped = false;
                    this.toggle();
                    this.info("Disabling after jump.", new Object[0]);
                }
                this.airTicks = 0;
                ++this.groundTicks;
                if (this.groundTicks <= 2) {
                    this.mc.player.setVelocity(forward * (double)0.01f * cos * this.glideMultiplier.get(), this.mc.player.getVelocity().y, forward * (double)0.01f * sin * this.glideMultiplier.get());
                } else {
                    this.mc.player.setVelocity(forward * (double)0.3f * cos * this.glideMultiplier.get(), (double)0.424f, forward * (double)0.3f * sin * this.glideMultiplier.get());
                }
            }
        }
    }

    private void updateY(double amount) {
        this.mc.player.setVelocity(this.mc.player.getVelocity().x, amount, this.mc.player.getVelocity().z);
    }

    private double getDir() {
        double dir = 0.0;
        if (Utils.canUpdate()) {
            dir = this.mc.player.getYaw() + (float)(this.mc.player.forwardSpeed < 0.0f ? 180 : 0);
            if (this.mc.player.sidewaysSpeed > 0.0f) {
                dir += (double)(-90.0f * (this.mc.player.forwardSpeed < 0.0f ? -0.5f : (this.mc.player.forwardSpeed > 0.0f ? 0.5f : 1.0f)));
            } else if (this.mc.player.sidewaysSpeed < 0.0f) {
                dir += (double)(90.0f * (this.mc.player.forwardSpeed < 0.0f ? -0.5f : (this.mc.player.forwardSpeed > 0.0f ? 0.5f : 1.0f)));
            }
        }
        return dir;
    }

    private double getMoveSpeed() {
        double base = 0.2873;
        if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            base *= 1.0 + 0.2 * (double)(this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1);
        }
        return base;
    }

    private void setMoveSpeed(PlayerMoveEvent event, double speed) {
        double forward = this.mc.player.forwardSpeed;
        double strafe = this.mc.player.sidewaysSpeed;
        float yaw = this.mc.player.getYaw();
        if (!PlayerUtils.isMoving()) {
            ((IVec3d)event.movement).setXZ(0.0, 0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }
            }
            strafe = 0.0;
            if (forward > 0.0) {
                forward = 1.0;
            } else if (forward < 0.0) {
                forward = -1.0;
            }
        }
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        ((IVec3d)event.movement).setXZ(forward * speed * cos + strafe * speed * sin, forward * speed * sin + strafe * speed * cos);
    }

    public static enum JumpMode {
        Vanilla,
        Burst,
        Glide;

    }
}

