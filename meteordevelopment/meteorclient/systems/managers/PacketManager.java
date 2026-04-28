/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket
 *  net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
 *  net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket
 */
package meteordevelopment.meteorclient.systems.managers;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.managers.PacketPriority;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

public class PacketManager {
    public static final PacketManager INSTANCE = new PacketManager();
    private final Deque<Long> manualGlobalQueue = new ConcurrentLinkedDeque<Long>();
    private final Deque<Long> manualInteractQueue = new ConcurrentLinkedDeque<Long>();
    private final Deque<Long> manualClickQueue = new ConcurrentLinkedDeque<Long>();
    private final Deque<Long> realGlobalQueue = new ConcurrentLinkedDeque<Long>();
    private final Deque<Long> realInteractQueue = new ConcurrentLinkedDeque<Long>();
    private final Deque<Long> realClickQueue = new ConcurrentLinkedDeque<Long>();

    private PacketManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void incrementGlobal() {
        this.track(this.manualGlobalQueue);
    }

    public void incrementInteract() {
        this.track(this.manualGlobalQueue);
        this.track(this.manualInteractQueue);
    }

    public void incrementClick() {
        this.track(this.manualGlobalQueue);
        this.track(this.manualClickQueue);
    }

    public void incrementPlace() {
        this.track(this.manualGlobalQueue);
        this.track(this.manualInteractQueue);
    }

    private void track(Deque<Long> queue) {
        queue.addLast(System.currentTimeMillis());
    }

    public boolean isClickAllowed() {
        AntiCheatConfig config = AntiCheatConfig.get();
        if (config == null || !config.packetLimiter.get().booleanValue()) {
            return true;
        }
        return this.getClickPPS() < config.clickLimiter.get();
    }

    @EventHandler(priority=200)
    private void onSend(PacketEvent.Send event) {
        AntiCheatConfig config = AntiCheatConfig.get();
        if (config == null || !config.packetLimiter.get().booleanValue()) {
            return;
        }
        if (this.isMovementOrKeepAlive(event.packet)) {
            return;
        }
        int currentGlobalPPS = this.getGlobalPPS();
        if (currentGlobalPPS >= config.globalLimiter.get()) {
            if (!this.isHighPriority(event.packet, config.globalLimiter.get(), currentGlobalPPS)) {
                event.cancel();
                return;
            }
        } else if (this.isClickPacket(event.packet)) {
            if (this.getClickPPS() >= config.clickLimiter.get()) {
                event.cancel();
                return;
            }
        } else if (this.isInteractPacket(event.packet) && this.getInteractPPS() >= config.interactLimiter.get()) {
            event.cancel();
            return;
        }
        long now = System.currentTimeMillis();
        this.realGlobalQueue.addLast(now);
        if (event.packet instanceof PlayerInteractBlockC2SPacket) {
            this.realInteractQueue.addLast(now);
        } else if (this.isClickPacket(event.packet)) {
            this.realClickQueue.addLast(now);
        } else if (this.isInteractPacket(event.packet)) {
            this.realInteractQueue.addLast(now);
        }
    }

    private boolean isHighPriority(Packet<?> packet, int limit, int current) {
        return false;
    }

    private boolean isMovementOrKeepAlive(Packet<?> packet) {
        return packet instanceof PlayerMoveC2SPacket || packet instanceof KeepAliveC2SPacket || packet instanceof TeleportConfirmC2SPacket;
    }

    private boolean isInteractPacket(Packet<?> packet) {
        return packet instanceof PlayerInteractEntityC2SPacket || packet instanceof PlayerInteractItemC2SPacket || packet instanceof HandSwingC2SPacket || packet instanceof PlayerActionC2SPacket;
    }

    private boolean isClickPacket(Packet<?> packet) {
        return packet instanceof ClickSlotC2SPacket || packet instanceof ButtonClickC2SPacket || packet instanceof CreativeInventoryActionC2SPacket || packet instanceof PickFromInventoryC2SPacket;
    }

    public int getLoadPercentage() {
        AntiCheatConfig config = AntiCheatConfig.get();
        if (config == null) {
            return 0;
        }
        double globalLoad = (double)this.getGlobalPPS() / (double)config.globalLimiter.get().intValue();
        double interactLoad = (double)this.getInteractPPS() / (double)config.interactLimiter.get().intValue();
        double max = Math.max(globalLoad, interactLoad);
        return (int)Math.min(100.0, max * 100.0);
    }

    public boolean shouldThrottle(PacketPriority priority) {
        int load = this.getLoadPercentage();
        if (priority == PacketPriority.HIGH) {
            return false;
        }
        if (priority == PacketPriority.MEDIUM) {
            return load >= 85;
        }
        if (priority == PacketPriority.LOW) {
            return load >= 75;
        }
        return false;
    }

    public int getGlobalPPS() {
        this.cleanQueue(this.manualGlobalQueue, 4000L);
        this.cleanQueue(this.realGlobalQueue, 4000L);
        return Math.max(this.manualGlobalQueue.size(), this.realGlobalQueue.size());
    }

    public int getInteractPPS() {
        this.cleanQueue(this.manualInteractQueue, 1000L);
        this.cleanQueue(this.realInteractQueue, 1000L);
        return Math.max(this.manualInteractQueue.size(), this.realInteractQueue.size());
    }

    public int getClickPPS() {
        this.cleanQueue(this.manualClickQueue, 4000L);
        this.cleanQueue(this.realClickQueue, 4000L);
        return Math.max(this.manualClickQueue.size(), this.realClickQueue.size());
    }

    private void cleanQueue(Deque<Long> queue, long duration) {
        long now = System.currentTimeMillis();
        while (!queue.isEmpty() && now - queue.peekFirst() > duration) {
            queue.pollFirst();
        }
    }
}

