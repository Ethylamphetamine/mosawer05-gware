/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.utils.world;

import java.util.Arrays;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

public class TickRate {
    public static TickRate INSTANCE = new TickRate();
    private final float[] tickRates = new float[20];
    private int nextIndex = 0;
    private long timeLastTimeUpdate = -1L;
    private long timeGameJoined;

    private TickRate() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof WorldTimeUpdateS2CPacket) {
            long now = System.currentTimeMillis();
            float timeElapsed = (float)(now - this.timeLastTimeUpdate) / 1000.0f;
            this.tickRates[this.nextIndex] = MathHelper.clamp((float)(20.0f / timeElapsed), (float)0.0f, (float)20.0f);
            this.nextIndex = (this.nextIndex + 1) % this.tickRates.length;
            this.timeLastTimeUpdate = now;
        }
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        Arrays.fill(this.tickRates, 0.0f);
        this.nextIndex = 0;
        this.timeGameJoined = this.timeLastTimeUpdate = System.currentTimeMillis();
    }

    public float getTickRate() {
        if (!Utils.canUpdate()) {
            return 0.0f;
        }
        if (System.currentTimeMillis() - this.timeGameJoined < 4000L) {
            return 20.0f;
        }
        int numTicks = 0;
        float sumTickRates = 0.0f;
        for (float tickRate : this.tickRates) {
            if (!(tickRate > 0.0f)) continue;
            sumTickRates += tickRate;
            ++numTicks;
        }
        return sumTickRates / (float)numTicks;
    }

    public float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - this.timeGameJoined < 4000L) {
            return 0.0f;
        }
        return (float)(now - this.timeLastTimeUpdate) / 1000.0f;
    }
}

