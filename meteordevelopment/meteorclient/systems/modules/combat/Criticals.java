/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.MaceItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMace;
    private final Setting<Mode> mode;
    private final Setting<Boolean> ka;
    private final Setting<Boolean> mace;
    private final Setting<Double> extraHeight;
    private PlayerInteractEntityC2SPacket attackPacket;
    private HandSwingC2SPacket swingPacket;
    private boolean sendPackets;
    private int sendTimer;

    public Criticals() {
        super(Categories.Combat, "criticals", "Performs critical attacks when you hit your target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMace = this.settings.createGroup("Mace");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode on how Criticals will function.")).defaultValue(Mode.Packet)).build());
        this.ka = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-killaura")).description("Only performs crits when using killaura.")).defaultValue(false)).visible(() -> this.mode.get() != Mode.None)).build());
        this.mace = this.sgMace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smash-attack")).description("Will always perform smash attacks when using a mace.")).defaultValue(true)).build());
        this.extraHeight = this.sgMace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("additional-height")).description("The amount of additional height to spoof. More height means more damage.")).defaultValue(0.0).min(0.0).sliderRange(0.0, 100.0).visible(this.mace::get)).build());
    }

    @Override
    public void onActivate() {
        this.attackPacket = null;
        this.swingPacket = null;
        this.sendPackets = false;
        this.sendTimer = 0;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        IPlayerInteractEntityC2SPacket packet;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof IPlayerInteractEntityC2SPacket && (packet = (IPlayerInteractEntityC2SPacket)packet2).getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            if (this.mace.get().booleanValue() && this.mc.player.getMainHandStack().getItem() instanceof MaceItem) {
                if (this.mc.player.isFallFlying()) {
                    return;
                }
                this.sendPacket(0.0);
                this.sendPacket(1.501 + this.extraHeight.get());
                this.sendPacket(0.0);
            } else {
                if (this.skipCrit()) {
                    return;
                }
                Entity entity = packet.getEntity();
                if (!(entity instanceof LivingEntity) || entity != Modules.get().get(KillAura.class).getTarget() && this.ka.get().booleanValue()) {
                    return;
                }
                switch (this.mode.get().ordinal()) {
                    case 1: {
                        this.sendPacket(0.0625);
                        this.sendPacket(0.0);
                        break;
                    }
                    case 2: {
                        this.sendPacket(0.11);
                        this.sendPacket(0.1100013579);
                        this.sendPacket(1.3579E-6);
                        break;
                    }
                    case 3: 
                    case 4: {
                        if (this.sendPackets) break;
                        this.sendPackets = true;
                        this.sendTimer = this.mode.get() == Mode.Jump ? 6 : 4;
                        this.attackPacket = (PlayerInteractEntityC2SPacket)event.packet;
                        if (this.mode.get() == Mode.Jump) {
                            this.mc.player.jump();
                        } else {
                            ((IVec3d)this.mc.player.getVelocity()).setY(0.25);
                        }
                        event.cancel();
                    }
                }
            }
        } else if (event.packet instanceof HandSwingC2SPacket && this.mode.get() != Mode.Packet) {
            if (this.skipCrit()) {
                return;
            }
            if (this.sendPackets && this.swingPacket == null) {
                this.swingPacket = (HandSwingC2SPacket)event.packet;
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.sendPackets) {
            if (this.sendTimer <= 0) {
                this.sendPackets = false;
                if (this.attackPacket == null || this.swingPacket == null) {
                    return;
                }
                this.mc.getNetworkHandler().sendPacket((Packet)this.attackPacket);
                this.mc.getNetworkHandler().sendPacket((Packet)this.swingPacket);
                this.attackPacket = null;
                this.swingPacket = null;
            } else {
                --this.sendTimer;
            }
        }
    }

    private void sendPacket(double height) {
        double x = this.mc.player.getX();
        double y = this.mc.player.getY();
        double z = this.mc.player.getZ();
        PlayerMoveC2SPacket.PositionAndOnGround packet = new PlayerMoveC2SPacket.PositionAndOnGround(x, y + height, z, false);
        ((IPlayerMoveC2SPacket)packet).setTag(1337);
        this.mc.player.networkHandler.sendPacket((Packet)packet);
    }

    private boolean skipCrit() {
        return !this.mc.player.isOnGround() || this.mc.player.isSubmergedInWater() || this.mc.player.isInLava() || this.mc.player.isClimbing();
    }

    @Override
    public String getInfoString() {
        return this.mode.get().name();
    }

    public static enum Mode {
        None,
        Packet,
        Bypass,
        Jump,
        MiniJump;

    }
}

