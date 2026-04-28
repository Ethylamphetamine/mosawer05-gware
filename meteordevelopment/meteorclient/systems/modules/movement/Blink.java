/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.joml.Vector3d;

public class Blink
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> renderOriginal;
    private final Setting<Keybind> cancelBlink;
    private final List<PlayerMoveC2SPacket> packets;
    private FakePlayerEntity model;
    private final Vector3d start;
    private boolean cancelled;
    private int timer;

    public Blink() {
        super(Categories.Movement, "blink", "Allows you to essentially teleport while suspending motion updates.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.renderOriginal = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-original")).description("Renders your player model at the original position.")).defaultValue(true)).build());
        this.cancelBlink = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("cancel-blink")).description("Cancels sending packets and sends you back to your original position.")).defaultValue(Keybind.none())).action(() -> {
            this.cancelled = true;
            if (this.isActive()) {
                this.toggle();
            }
        }).build());
        this.packets = new ArrayList<PlayerMoveC2SPacket>();
        this.start = new Vector3d();
        this.cancelled = false;
        this.timer = 0;
    }

    @Override
    public void onActivate() {
        if (this.renderOriginal.get().booleanValue()) {
            this.model = new FakePlayerEntity((PlayerEntity)this.mc.player, this.mc.player.getGameProfile().getName(), 20.0f, true);
            this.model.doNotPush = true;
            this.model.hideWhenInsideCamera = true;
            this.model.spawn();
        }
        Utils.set(this.start, this.mc.player.getPos());
    }

    @Override
    public void onDeactivate() {
        this.dumpPackets(!this.cancelled);
        if (this.cancelled) {
            this.mc.player.setPos(this.start.x, this.start.y, this.start.z);
        }
        this.cancelled = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ++this.timer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        PlayerMoveC2SPacket prev;
        Packet<?> packet = event.packet;
        if (!(packet instanceof PlayerMoveC2SPacket)) {
            return;
        }
        PlayerMoveC2SPacket p = (PlayerMoveC2SPacket)packet;
        event.cancel();
        PlayerMoveC2SPacket playerMoveC2SPacket = prev = this.packets.isEmpty() ? null : this.packets.getLast();
        if (prev != null && p.isOnGround() == prev.isOnGround() && p.getYaw(-1.0f) == prev.getYaw(-1.0f) && p.getPitch(-1.0f) == prev.getPitch(-1.0f) && p.getX(-1.0) == prev.getX(-1.0) && p.getY(-1.0) == prev.getY(-1.0) && p.getZ(-1.0) == prev.getZ(-1.0)) {
            return;
        }
        List<PlayerMoveC2SPacket> list = this.packets;
        synchronized (list) {
            this.packets.add(p);
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%.1f", Float.valueOf((float)this.timer / 20.0f));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dumpPackets(boolean send) {
        List<PlayerMoveC2SPacket> list = this.packets;
        synchronized (list) {
            if (send) {
                this.packets.forEach(arg_0 -> ((ClientPlayNetworkHandler)this.mc.player.networkHandler).sendPacket(arg_0));
            }
            this.packets.clear();
        }
        if (this.model != null) {
            this.model.despawn();
            this.model = null;
        }
        this.timer = 0;
    }
}

