/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractBlock$AbstractBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerEntityAccessor;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Flight
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAntiKick;
    private final Setting<Mode> mode;
    private final Setting<Double> speed;
    private final Setting<Boolean> verticalSpeedMatch;
    private final Setting<Boolean> noSneak;
    private final Setting<AntiKickMode> antiKickMode;
    private final Setting<Integer> delay;
    private final Setting<Integer> offTime;
    private int delayLeft;
    private int offLeft;
    private boolean flip;
    private float lastYaw;
    private double lastPacketY;

    public Flight() {
        super(Categories.Movement, "flight", "FLYYYY! No Fall is recommended with this module.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAntiKick = this.settings.createGroup("Anti Kick");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode for Flight.")).defaultValue(Mode.Abilities)).onChanged(mode -> {
            if (!this.isActive() || !Utils.canUpdate()) {
                return;
            }
            this.abilitiesOff();
        })).build());
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("Your speed when flying.")).defaultValue(0.1).min(0.0).build());
        this.verticalSpeedMatch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("vertical-speed-match")).description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio.")).defaultValue(false)).build());
        this.noSneak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-sneak")).description("Prevents you from sneaking while flying.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Velocity)).build());
        this.antiKickMode = this.sgAntiKick.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode for anti kick.")).defaultValue(AntiKickMode.Packet)).build());
        this.delay = this.sgAntiKick.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The amount of delay, in ticks, between flying down a bit and return to original position")).defaultValue(20)).min(1).sliderMax(200).build());
        this.offTime = this.sgAntiKick.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("off-time")).description("The amount of delay, in milliseconds, to fly down a bit to reset floating ticks.")).defaultValue(1)).min(1).sliderRange(1, 20).build());
        this.delayLeft = this.delay.get();
        this.offLeft = this.offTime.get();
        this.lastPacketY = Double.MAX_VALUE;
    }

    @Override
    public void onActivate() {
        if (this.mode.get() == Mode.Abilities && !this.mc.player.isSpectator()) {
            this.mc.player.getAbilities().flying = true;
            if (this.mc.player.getAbilities().creativeMode) {
                return;
            }
            this.mc.player.getAbilities().allowFlying = true;
        }
    }

    @Override
    public void onDeactivate() {
        if (this.mode.get() == Mode.Abilities && !this.mc.player.isSpectator()) {
            this.abilitiesOff();
        }
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        float currentYaw = this.mc.player.getYaw();
        if (this.mc.player.fallDistance >= 3.0f && currentYaw == this.lastYaw && this.mc.player.getVelocity().length() < 0.003) {
            this.mc.player.setYaw(currentYaw + (float)(this.flip ? 1 : -1));
            this.flip = !this.flip;
        }
        this.lastYaw = currentYaw;
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (this.delayLeft > 0) {
            --this.delayLeft;
        }
        if (this.offLeft <= 0 && this.delayLeft <= 0) {
            this.delayLeft = this.delay.get();
            this.offLeft = this.offTime.get();
            if (this.antiKickMode.get() == AntiKickMode.Packet) {
                ((ClientPlayerEntityAccessor)this.mc.player).setTicksSinceLastPositionPacketSent(20);
            }
        } else if (this.delayLeft <= 0) {
            boolean shouldReturn = false;
            if (this.antiKickMode.get() == AntiKickMode.Normal) {
                if (this.mode.get() == Mode.Abilities) {
                    this.abilitiesOff();
                    shouldReturn = true;
                }
            } else if (this.antiKickMode.get() == AntiKickMode.Packet && this.offLeft == this.offTime.get()) {
                ((ClientPlayerEntityAccessor)this.mc.player).setTicksSinceLastPositionPacketSent(20);
            }
            --this.offLeft;
            if (shouldReturn) {
                return;
            }
        }
        if (this.mc.player.getYaw() != this.lastYaw) {
            this.mc.player.setYaw(this.lastYaw);
        }
        switch (this.mode.get().ordinal()) {
            case 1: {
                this.mc.player.getAbilities().flying = false;
                this.mc.player.setVelocity(0.0, 0.0, 0.0);
                Vec3d playerVelocity = this.mc.player.getVelocity();
                if (this.mc.options.jumpKey.isPressed()) {
                    playerVelocity = playerVelocity.add(0.0, this.speed.get() * (double)(this.verticalSpeedMatch.get() != false ? 10.0f : 5.0f), 0.0);
                }
                if (this.mc.options.sneakKey.isPressed()) {
                    playerVelocity = playerVelocity.subtract(0.0, this.speed.get() * (double)(this.verticalSpeedMatch.get() != false ? 10.0f : 5.0f), 0.0);
                }
                this.mc.player.setVelocity(playerVelocity);
                if (!this.noSneak.get().booleanValue()) break;
                this.mc.player.setOnGround(false);
                break;
            }
            case 0: {
                if (this.mc.player.isSpectator()) {
                    return;
                }
                this.mc.player.getAbilities().setFlySpeed(this.speed.get().floatValue());
                this.mc.player.getAbilities().flying = true;
                if (this.mc.player.getAbilities().creativeMode) {
                    return;
                }
                this.mc.player.getAbilities().allowFlying = true;
            }
        }
    }

    private void antiKickPacket(PlayerMoveC2SPacket packet, double currentY) {
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE && this.shouldFlyDown(currentY, this.lastPacketY) && this.isEntityOnAir((Entity)this.mc.player)) {
            ((PlayerMoveC2SPacketAccessor)packet).setY(this.lastPacketY - 0.0313);
        } else {
            this.lastPacketY = currentY;
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        PlayerMoveC2SPacket packet;
        block6: {
            block5: {
                Packet<?> packet2 = event.packet;
                if (!(packet2 instanceof PlayerMoveC2SPacket)) break block5;
                packet = (PlayerMoveC2SPacket)packet2;
                if (this.antiKickMode.get() == AntiKickMode.Packet) break block6;
            }
            return;
        }
        double currentY = packet.getY(Double.MAX_VALUE);
        if (currentY != Double.MAX_VALUE) {
            this.antiKickPacket(packet, currentY);
        } else {
            Object fullPacket = packet.changesLook() ? new PlayerMoveC2SPacket.Full(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), packet.getYaw(0.0f), packet.getPitch(0.0f), packet.isOnGround()) : new PlayerMoveC2SPacket.PositionAndOnGround(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), packet.isOnGround());
            event.cancel();
            this.antiKickPacket((PlayerMoveC2SPacket)fullPacket, this.mc.player.getY());
            this.mc.getNetworkHandler().sendPacket((Packet)fullPacket);
        }
    }

    private boolean shouldFlyDown(double currentY, double lastY) {
        if (currentY >= lastY) {
            return true;
        }
        return lastY - currentY < 0.0313;
    }

    private void abilitiesOff() {
        this.mc.player.getAbilities().flying = false;
        this.mc.player.getAbilities().setFlySpeed(0.05f);
        if (this.mc.player.getAbilities().creativeMode) {
            return;
        }
        this.mc.player.getAbilities().allowFlying = false;
    }

    private boolean isEntityOnAir(Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    public float getOffGroundSpeed() {
        if (!this.isActive() || this.mode.get() != Mode.Velocity) {
            return -1.0f;
        }
        return this.speed.get().floatValue() * (this.mc.player.isSprinting() ? 15.0f : 10.0f);
    }

    public boolean noSneak() {
        return this.isActive() && this.mode.get() == Mode.Velocity && this.noSneak.get() != false;
    }

    public static enum Mode {
        Abilities,
        Velocity;

    }

    public static enum AntiKickMode {
        Normal,
        Packet,
        None;

    }
}

