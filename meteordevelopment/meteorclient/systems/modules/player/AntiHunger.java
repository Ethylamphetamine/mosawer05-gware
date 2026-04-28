/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> sprint;
    private final Setting<Boolean> onGround;
    private boolean lastOnGround;
    private boolean ignorePacket;

    public AntiHunger() {
        super(Categories.Player, "anti-hunger", "Reduces (does NOT remove) hunger consumption.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sprint")).description("Spoofs sprinting packets.")).defaultValue(true)).build());
        this.onGround = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("on-ground")).description("Spoofs the onGround flag.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.lastOnGround = this.mc.player.isOnGround();
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        ClientCommandC2SPacket packet;
        if (this.ignorePacket && event.packet instanceof PlayerMoveC2SPacket) {
            this.ignorePacket = false;
            return;
        }
        if (this.mc.player.hasVehicle() || this.mc.player.isTouchingWater() || this.mc.player.isSubmergedInWater()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof ClientCommandC2SPacket) {
            packet = (ClientCommandC2SPacket)packet2;
            if (this.sprint.get().booleanValue() && packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
                event.cancel();
            }
        }
        if ((packet2 = event.packet) instanceof PlayerMoveC2SPacket) {
            packet = (PlayerMoveC2SPacket)packet2;
            if (this.onGround.get().booleanValue() && this.mc.player.isOnGround() && (double)this.mc.player.fallDistance <= 0.0 && !this.mc.interactionManager.isBreakingBlock()) {
                ((PlayerMoveC2SPacketAccessor)packet).setOnGround(false);
            }
        }
    }

    @EventHandler
    private void onTick(SendMovementPacketsEvent.Pre event) {
        if (this.mc.player.isOnGround() && !this.lastOnGround && this.onGround.get().booleanValue()) {
            this.ignorePacket = true;
        }
        this.lastOnGround = this.mc.player.isOnGround();
    }
}

