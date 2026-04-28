/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

public class PacketSaver
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> grimRubberbandResponse;

    public PacketSaver() {
        super(Categories.Misc, "packet-saver", "Stops the client from sending unnecessary packets. Helps with packet kicks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.grimRubberbandResponse = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-grim-rubberband")).description("Stops the client from responding to Grim rubberband packets")).defaultValue(true)).build());
    }

    @EventHandler(priority=201)
    private void onPacketSend(PacketEvent.Send event) {
        TeleportConfirmC2SPacket packet;
        Packet<?> packet2;
        if (this.grimRubberbandResponse.get().booleanValue() && (packet2 = event.packet) instanceof TeleportConfirmC2SPacket && (packet = (TeleportConfirmC2SPacket)packet2).getTeleportId() < 0) {
            event.cancel();
        }
    }
}

