/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractType
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerEntityAccessor;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class Sprint
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    public final Setting<Boolean> jumpFix;
    private final Setting<Boolean> keepSprint;
    private final Setting<Boolean> unsprintOnHit;
    private final Setting<Boolean> unsprintInWater;

    public Sprint() {
        super(Categories.Movement, "sprint", "Automatically sprints.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("speed-mode")).description("What mode of sprinting.")).defaultValue(Mode.Strict)).build());
        this.jumpFix = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("jump-fix")).description("Whether to correct jumping directions.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Rage)).build());
        this.keepSprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("keep-sprint")).description("Whether to keep sprinting after attacking an entity.")).defaultValue(false)).build());
        this.unsprintOnHit = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unsprint-on-hit")).description("Whether to stop sprinting when attacking, to ensure you get crits and sweep attacks.")).defaultValue(false)).build());
        this.unsprintInWater = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unsprint-in-water")).description("Whether to stop sprinting when in water.")).defaultValue(true)).build());
    }

    @Override
    public void onDeactivate() {
        this.mc.player.setSprinting(false);
    }

    @EventHandler
    private void onTickMovement(TickEvent.Post event) {
        if (this.shouldSprint()) {
            this.mc.player.setSprinting(true);
        }
    }

    @EventHandler(priority=100)
    private void onPacketSend(PacketEvent.Send event) {
        IPlayerInteractEntityC2SPacket packet;
        Packet<?> packet2;
        if (!this.unsprintOnHit.get().booleanValue() || !((packet2 = event.packet) instanceof IPlayerInteractEntityC2SPacket) || (packet = (IPlayerInteractEntityC2SPacket)packet2).getType() != PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            return;
        }
        this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        this.mc.player.setSprinting(false);
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        IPlayerInteractEntityC2SPacket packet;
        if (!this.unsprintOnHit.get().booleanValue() || !this.keepSprint.get().booleanValue()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (!(packet2 instanceof IPlayerInteractEntityC2SPacket) || (packet = (IPlayerInteractEntityC2SPacket)packet2).getType() != PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            return;
        }
        if (this.shouldSprint() && !this.mc.player.isSprinting()) {
            this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            this.mc.player.setSprinting(true);
        }
    }

    public boolean shouldSprint() {
        if (this.unsprintInWater.get().booleanValue() && (this.mc.player.isTouchingWater() || this.mc.player.isSubmergedInWater())) {
            return false;
        }
        if (RotationManager.hasPersistentRotation()) {
            return false;
        }
        boolean strictSprint = !(!(this.mc.player.forwardSpeed > 1.0E-5f) || !((ClientPlayerEntityAccessor)this.mc.player).invokeCanSprint() || this.mc.player.horizontalCollision && !this.mc.player.collidedSoftly || this.mc.player.isTouchingWater() && !this.mc.player.isSubmergedInWater());
        return !(!this.isActive() || this.mode.get() != Mode.Rage && !strictSprint || this.mc.currentScreen != null && Modules.get().get(GUIMove.class).sprint.get() == false);
    }

    public boolean rageSprint() {
        return this.isActive() && this.mode.get() == Mode.Rage;
    }

    public boolean stopSprinting() {
        return !this.isActive() || this.keepSprint.get() == false;
    }

    public static enum Mode {
        Strict,
        Rage;

    }
}

