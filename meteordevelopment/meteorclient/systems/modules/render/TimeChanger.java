/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class TimeChanger
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> time;
    long oldTime;

    public TimeChanger() {
        super(Categories.Render, "time-changer", "Makes you able to set a custom time.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.time = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("time")).description("The specified time to be set.")).defaultValue(0.0).sliderRange(-20000.0, 20000.0).build());
    }

    @Override
    public void onActivate() {
        this.oldTime = this.mc.world.getTime();
    }

    @Override
    public void onDeactivate() {
        this.mc.world.setTimeOfDay(this.oldTime);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof WorldTimeUpdateS2CPacket) {
            this.oldTime = ((WorldTimeUpdateS2CPacket)event.packet).getTime();
            event.cancel();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.mc.world.setTimeOfDay(this.time.get().longValue());
    }
}

