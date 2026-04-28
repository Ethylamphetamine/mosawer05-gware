/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.ShapeContext
 *  net.minecraft.item.Item
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.managers;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockPlacementManager {
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private final Map<BlockPos, Long> placeCooldowns = new ConcurrentHashMap<BlockPos, Long>();
    private final Deque<Integer> burstHistory = new ConcurrentLinkedDeque<Integer>();
    private boolean locked = false;
    private boolean isOffhandBatch = false;
    private int packetsSent;
    private long lastSentPacketTimestamp = -1L;

    public BlockPlacementManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public boolean beginUseXp(Item item, float yaw, float pitch, boolean manualOffhand) {
        long currentTime = System.currentTimeMillis();
        this.placeCooldowns.values().removeIf(time -> currentTime - time > 1000L);
        if (!this.checkLimit(currentTime, false)) {
            return false;
        }
        if (this.locked) {
            return false;
        }
        if (!MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        PacketManager.INSTANCE.incrementGlobal();
        MeteorClient.ROTATION.snapAt(yaw, pitch);
        this.locked = true;
        return true;
    }

    public boolean useItem(Hand hand, float yaw, float pitch) {
        long currentTime = System.currentTimeMillis();
        if (!this.checkLimit(currentTime, true)) {
            return false;
        }
        MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, MeteorClient.mc.world.getPendingUpdateManager().incrementSequence().getSequence(), yaw, pitch));
        PacketManager.INSTANCE.incrementInteract();
        return true;
    }

    public void endUse() {
        if (!this.locked) {
            return;
        }
        this.locked = false;
        MeteorClient.SWAP.endSwap(true);
        PacketManager.INSTANCE.incrementGlobal();
    }

    public boolean interactItem(Hand hand) {
        return this.interactItem(hand, MeteorClient.mc.player.getYaw(), 90.0f);
    }

    public boolean interactItem(Hand hand, float yaw, float pitch) {
        Item item;
        Item item2 = item = hand == Hand.OFF_HAND ? MeteorClient.mc.player.getOffHandStack().getItem() : MeteorClient.mc.player.getMainHandStack().getItem();
        if (this.beginUseXp(item, yaw, pitch, false)) {
            boolean result = this.useItem(hand, yaw, pitch);
            this.endUse();
            return result;
        }
        return false;
    }

    public boolean beginPlacement(BlockPos position, BlockState state, Item item) {
        long currentTime = System.currentTimeMillis();
        this.placeCooldowns.values().removeIf(time -> currentTime - time > 1000L);
        if (!this.checkLimit(currentTime, false)) {
            return false;
        }
        if (this.locked) {
            return false;
        }
        if (!this.checkPlacement(item, position, state)) {
            return false;
        }
        if (!MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        PacketManager.INSTANCE.incrementGlobal();
        if (this.antiCheatConfig.blockPlaceAirPlace.get().booleanValue()) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
            this.isOffhandBatch = true;
        }
        this.locked = true;
        return true;
    }

    public boolean beginPlacement(List<BlockPos> positions, Item item) {
        long currentTime = System.currentTimeMillis();
        this.placeCooldowns.values().removeIf(time -> currentTime - time > 1000L);
        if (!this.checkLimit(currentTime, false)) {
            return false;
        }
        if (this.locked) {
            return false;
        }
        if (positions.stream().noneMatch(x -> this.checkPlacement(item, (BlockPos)x))) {
            return false;
        }
        if (!MeteorClient.SWAP.beginSwap(item, true)) {
            return false;
        }
        PacketManager.INSTANCE.incrementGlobal();
        if (this.antiCheatConfig.blockPlaceAirPlace.get().booleanValue()) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
            this.isOffhandBatch = true;
        }
        this.locked = true;
        return true;
    }

    public boolean placeBlock(Item item, BlockPos blockPos) {
        return this.placeBlock(item, blockPos, MeteorClient.mc.world.getBlockState(blockPos));
    }

    public boolean placeBlock(Item item, BlockPos blockPos, BlockState state) {
        Long lastPlaced;
        BlockPos neighbour;
        Direction dir;
        long currentTime = System.currentTimeMillis();
        if (!this.checkLimit(currentTime, false)) {
            return false;
        }
        if (!this.checkPlacement(item, blockPos, state)) {
            return false;
        }
        if (this.antiCheatConfig.blockPlaceAirPlace.get().booleanValue() && this.antiCheatConfig.forceAirPlace.get().booleanValue()) {
            dir = null;
            neighbour = blockPos;
        } else {
            dir = BlockUtils.getPlaceSide(blockPos);
            neighbour = dir == null ? blockPos : blockPos.offset(dir);
        }
        Vec3d hitPos = blockPos.toCenterPos();
        if (dir != null) {
            hitPos = hitPos.add((double)dir.getOffsetX() * 0.5, (double)dir.getOffsetY() * 0.5, (double)dir.getOffsetZ() * 0.5);
            if (this.antiCheatConfig.blockRotatePlace.get().booleanValue()) {
                MeteorClient.ROTATION.snapAt(hitPos);
            }
        }
        if ((lastPlaced = this.placeCooldowns.get(blockPos)) != null && (double)(currentTime - lastPlaced) < this.antiCheatConfig.blockPlacePerBlockCooldown.get() * 1000.0) {
            return false;
        }
        if (!this.checkLimit(currentTime, true)) {
            return false;
        }
        this.placeCooldowns.put(blockPos, currentTime);
        Hand placeHand = Hand.MAIN_HAND;
        boolean performSingleSwap = false;
        if (this.isOffhandBatch) {
            placeHand = Hand.OFF_HAND;
        } else {
            boolean grimAirPlaceSwap;
            boolean bl = grimAirPlaceSwap = this.antiCheatConfig.blockPlaceAirPlace.get() != false && (dir == null || this.antiCheatConfig.forceAirPlace.get() != false);
            if (grimAirPlaceSwap) {
                MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
                PacketManager.INSTANCE.incrementGlobal();
                placeHand = Hand.OFF_HAND;
                performSingleSwap = true;
            }
        }
        MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractBlockC2SPacket(placeHand, new BlockHitResult(hitPos, dir == null ? Direction.DOWN : dir.getOpposite(), neighbour, false), MeteorClient.mc.world.getPendingUpdateManager().incrementSequence().getSequence()));
        PacketManager.INSTANCE.incrementPlace();
        if (performSingleSwap) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
        }
        return true;
    }

    public boolean checkPlacement(Item item, BlockPos blockPos) {
        return this.checkPlacement(item, blockPos, MeteorClient.mc.world.getBlockState(blockPos));
    }

    public boolean checkPlacement(Item item, BlockPos blockPos, BlockState state) {
        long currentTime;
        if (!this.antiCheatConfig.blockPlaceAirPlace.get().booleanValue() && BlockPlacementManager.getPlaceOnDirection(blockPos) == null) {
            return false;
        }
        if (!state.isReplaceable()) {
            return false;
        }
        if (!World.isValid((BlockPos)blockPos)) {
            return false;
        }
        if (!MeteorClient.mc.world.canPlace(Block.getBlockFromItem((Item)item).getDefaultState(), blockPos, ShapeContext.absent())) {
            return false;
        }
        Long lastPlaced = this.placeCooldowns.get(blockPos);
        return lastPlaced == null || !((double)((currentTime = System.currentTimeMillis()) - lastPlaced) < this.antiCheatConfig.blockPlacePerBlockCooldown.get() * 1000.0);
    }

    public void endPlacement() {
        if (!this.locked) {
            return;
        }
        if (this.isOffhandBatch) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            PacketManager.INSTANCE.incrementGlobal();
            this.isOffhandBatch = false;
        }
        this.locked = false;
        MeteorClient.SWAP.endSwap(true);
        PacketManager.INSTANCE.incrementGlobal();
    }

    public void forceResetPlaceCooldown(BlockPos blockPos) {
        this.placeCooldowns.remove(blockPos);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        BlockUpdateS2CPacket packet;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof BlockUpdateS2CPacket && !(packet = (BlockUpdateS2CPacket)packet2).getState().isAir()) {
            BlockPos pos = packet.getPos();
            Long lastPlacedTime = this.placeCooldowns.get(pos);
            if (lastPlacedTime != null) {
                long currentTime = System.currentTimeMillis();
                double cooldownSettingSeconds = this.antiCheatConfig.blockPlacePerBlockCooldown.get();
                double timeElapsedSeconds = (double)(currentTime - lastPlacedTime) / 1000.0;
                if (timeElapsedSeconds >= cooldownSettingSeconds) {
                    this.placeCooldowns.remove(pos);
                }
            } else {
                this.placeCooldowns.remove(pos);
            }
        }
    }

    public static Direction getPlaceOnDirection(BlockPos pos) {
        if (pos == null) {
            return null;
        }
        Direction best = null;
        if (MeteorClient.mc.world != null && MeteorClient.mc.player != null) {
            double cDist = -1.0;
            for (Direction dir : Direction.values()) {
                double dist;
                if (MeteorClient.mc.world.getBlockState(pos.offset(dir)).isAir() || !((dist = BlockPlacementManager.getDistanceForDir(pos, dir)) >= 0.0) || !(cDist < 0.0) && !(dist < cDist)) continue;
                best = dir;
                cDist = dist;
            }
        }
        return best;
    }

    private static double getDistanceForDir(BlockPos pos, Direction dir) {
        if (MeteorClient.mc.player == null) {
            return 0.0;
        }
        Vec3d vec = new Vec3d((double)((float)pos.getX() + (float)dir.getOffsetX() / 2.0f), (double)((float)pos.getY() + (float)dir.getOffsetY() / 2.0f), (double)((float)pos.getZ() + (float)dir.getOffsetZ() / 2.0f));
        Vec3d dist = MeteorClient.mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);
        return dist.lengthSquared();
    }

    private boolean checkLimit(long timestamp, boolean incrementLimit) {
        boolean windowExpired;
        long packetLimitMs = this.antiCheatConfig.blockPacketLimit.get().intValue();
        double maxPackets = this.antiCheatConfig.blocksPerPacketLimit.get().intValue();
        int effectivePacketsSent = this.packetsSent;
        boolean bl = windowExpired = this.lastSentPacketTimestamp == -1L || timestamp - this.lastSentPacketTimestamp >= packetLimitMs;
        if (windowExpired) {
            effectivePacketsSent = 0;
        }
        if ((double)effectivePacketsSent >= maxPackets) {
            return false;
        }
        if (incrementLimit) {
            if (windowExpired) {
                if (this.packetsSent > 0) {
                    this.burstHistory.addFirst(this.packetsSent);
                    if (this.burstHistory.size() > 10) {
                        this.burstHistory.removeLast();
                    }
                }
                this.lastSentPacketTimestamp = timestamp;
                this.packetsSent = 0;
            }
            ++this.packetsSent;
        }
        return true;
    }

    public double getBurstProgress() {
        if (this.lastSentPacketTimestamp == -1L) {
            return 1.0;
        }
        long timeSinceStart = System.currentTimeMillis() - this.lastSentPacketTimestamp;
        double limit = this.antiCheatConfig.blockPacketLimit.get().intValue();
        return Math.min(1.0, Math.max(0.0, (double)timeSinceStart / limit));
    }

    public int getPacketsLeft() {
        long timeSinceStart;
        if (this.lastSentPacketTimestamp != -1L && (timeSinceStart = System.currentTimeMillis() - this.lastSentPacketTimestamp) >= (long)this.antiCheatConfig.blockPacketLimit.get().intValue()) {
            return Math.round(this.antiCheatConfig.blocksPerPacketLimit.get().intValue());
        }
        double max = this.antiCheatConfig.blocksPerPacketLimit.get().intValue();
        return (int)Math.max(0.0, max - (double)this.packetsSent);
    }

    public Object[] getBurstHistory() {
        return this.burstHistory.toArray();
    }
}

