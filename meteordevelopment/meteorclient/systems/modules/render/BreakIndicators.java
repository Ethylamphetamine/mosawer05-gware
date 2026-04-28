/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class BreakIndicators
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> useDoubleminePrediction;
    private final Setting<Double> rebreakCompletionAmount;
    private final Setting<Double> completionAmount;
    private final Setting<Double> removeCompletionAmount;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Queue<BlockBreak> _breakPackets;
    private final Map<BlockPos, BlockBreak> breakStartTimes;
    private final Map<BlockPos, BlockBreak> predictedDoublemine;

    public BreakIndicators() {
        super(Categories.Render, "break-indicators", "Renders the progress of a block being broken.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.useDoubleminePrediction = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-doublemine-predicition")).description("Does some fancy stuff to make indicators more accurate.")).defaultValue(false)).build());
        this.rebreakCompletionAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rebreak-completion-amount")).description("Determines how fast rendering increases of a suspected rebreak block. Smaller is faster.")).defaultValue(0.7).min(0.0).sliderMax(1.5).build());
        this.completionAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("full-completion-amount")).description("Determines how fast rendering increases. Smaller is faster.")).defaultValue(1.0).min(0.0).sliderMax(1.5).build());
        this.removeCompletionAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("force-remove-completion-amount")).description("Determines how long it takes to forcibly remove a block from being rendered.")).defaultValue(1.3).min(0.0).sliderMax(1.5).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Doesn't render blocks that friends are breaking.")).defaultValue(false)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("do-render")).description("Renders the blocks in queue to be broken.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the rendering.")).defaultValue(new SettingColor(255, 0, 80, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the rendering.")).defaultValue(new SettingColor(255, 255, 255, 40)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this._breakPackets = new ConcurrentLinkedQueue<BlockBreak>();
        this.breakStartTimes = new HashMap<BlockPos, BlockBreak>();
        this.predictedDoublemine = new HashMap<BlockPos, BlockBreak>();
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        Packet<?> packet = event.packet;
        if (packet instanceof BlockBreakingProgressS2CPacket) {
            BlockBreakingProgressS2CPacket packet2 = (BlockBreakingProgressS2CPacket)packet;
            Entity entity = this.mc.world.getEntityById(packet2.getEntityId());
            this._breakPackets.add(new BlockBreak(packet2.getPos(), RenderUtils.getCurrentGameTickCalculated(), entity));
        }
    }

    public boolean isBlockBeingBroken(BlockPos blockPos) {
        return this.breakStartTimes.containsKey(blockPos);
    }

    public boolean isBeingDoublemined(BlockPos blockPos) {
        return this.predictedDoublemine.containsKey(blockPos);
    }

    public PlayerEntity getPlayerDoubleminingBlock(BlockPos blockPos) {
        BlockBreak bb = this.predictedDoublemine.get(blockPos);
        return bb != null && bb.entity instanceof PlayerEntity ? (PlayerEntity)bb.entity : null;
    }

    public Map<BlockPos, BreakInfo> getActiveBreaksSnapshot() {
        HashMap<BlockPos, BreakInfo> out = new HashMap<BlockPos, BreakInfo>();
        double now = RenderUtils.getCurrentGameTickCalculated();
        Map<BlockPos, BlockBreak> source = this.useDoubleminePrediction.get() != false ? this.predictedDoublemine : this.breakStartTimes;
        for (Map.Entry<BlockPos, BlockBreak> e : source.entrySet()) {
            BlockBreak bb = e.getValue();
            double prog = Math.min(1.0, bb.getBreakProgress(now));
            PlayerEntity pl = bb.entity instanceof PlayerEntity ? (PlayerEntity)bb.entity : null;
            out.put(e.getKey(), new BreakInfo(e.getKey(), pl, prog, bb.isRebreak));
        }
        return Collections.unmodifiableMap(out);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Map.Entry<BlockPos, BlockBreak> entry;
        Object playerBreakingBlocks;
        double currentGameTickCalculated = RenderUtils.getCurrentGameTickCalculated();
        while (!this._breakPackets.isEmpty()) {
            BlockBreak breakEvent = this._breakPackets.remove();
            if (breakEvent.entity instanceof PlayerEntity && (playerBreakingBlocks = this.breakStartTimes.values().stream().filter(x -> x.entity == breakEvent.entity && !x.blockPos.equals((Object)breakEvent.blockPos)).sorted(Comparator.comparingDouble(blockBreak -> blockBreak.startTick)).toList()).size() >= 2) {
                this.predictedDoublemine.remove(playerBreakingBlocks.getLast().blockPos);
            }
            if (!this.breakStartTimes.containsKey(breakEvent.blockPos)) {
                this.breakStartTimes.put(breakEvent.blockPos, breakEvent);
            }
            if (this.predictedDoublemine.containsKey(breakEvent.blockPos)) continue;
            this.predictedDoublemine.put(breakEvent.blockPos, breakEvent);
        }
        Iterator<Map.Entry<BlockPos, BlockBreak>> iterator = this.breakStartTimes.entrySet().iterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (!this.mc.world.getBlockState(entry.getKey()).isAir() && !(entry.getValue().getBreakProgress(currentGameTickCalculated) > this.removeCompletionAmount.get()) && BlockUtils.canBreak((BlockPos)entry.getKey())) continue;
            iterator.remove();
        }
        iterator = this.predictedDoublemine.entrySet().iterator();
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (!this.mc.world.getBlockState(entry.getKey()).isAir() && !(((BlockBreak)entry.getValue()).getBreakProgress(currentGameTickCalculated) > this.removeCompletionAmount.get()) && BlockUtils.canBreak((BlockPos)entry.getKey())) continue;
            iterator.remove();
        }
        if (this.useDoubleminePrediction.get().booleanValue()) {
            for (Map.Entry<BlockPos, BlockBreak> entry2 : this.predictedDoublemine.entrySet()) {
                if (this.ignoreFriends.get().booleanValue() && (var8_7 = entry2.getValue().entity) instanceof PlayerEntity) {
                    player = (PlayerEntity)var8_7;
                    if (Friends.get().isFriend(player)) continue;
                }
                entry2.getValue().renderBlock(event, currentGameTickCalculated);
            }
        } else {
            for (Map.Entry<BlockPos, BlockBreak> entry2 : this.breakStartTimes.entrySet()) {
                if (this.ignoreFriends.get().booleanValue() && (var8_7 = entry2.getValue().entity) instanceof PlayerEntity) {
                    player = (PlayerEntity)var8_7;
                    if (Friends.get().isFriend(player)) continue;
                }
                entry2.getValue().renderBlock(event, currentGameTickCalculated);
            }
        }
        playerBreakingBlocks = this.predictedDoublemine.values().stream().sorted(Comparator.comparingDouble(blockBreak -> blockBreak.startTick)).filter(blockBreak -> blockBreak.entity instanceof PlayerEntity).collect(Collectors.groupingBy(blockBreak -> (PlayerEntity)blockBreak.entity, Collectors.toList()));
        for (Map.Entry entry3 : playerBreakingBlocks.entrySet()) {
            ((List)entry3.getValue()).forEach(x -> {
                x.isRebreak = false;
            });
            if (((List)entry3.getValue()).size() < 2) continue;
            ((BlockBreak)((List)entry3.getValue()).getLast()).isRebreak = true;
        }
    }

    private class BlockBreak {
        public BlockPos blockPos;
        public double startTick;
        public Entity entity;
        public boolean isRebreak = false;

        public BlockBreak(BlockPos blockPos, double startTick, Entity entity) {
            this.blockPos = blockPos;
            this.startTick = startTick;
            this.entity = entity;
        }

        public void renderBlock(Render3DEvent event, double currentTick) {
            VoxelShape shape = ((BreakIndicators)BreakIndicators.this).mc.world.getBlockState(this.blockPos).getOutlineShape((BlockView)((BreakIndicators)BreakIndicators.this).mc.world, this.blockPos);
            if (shape == null || shape.isEmpty()) {
                event.renderer.box(this.blockPos, (Color)BreakIndicators.this.sideColor.get(), (Color)BreakIndicators.this.lineColor.get(), BreakIndicators.this.shapeMode.get(), 0);
                return;
            }
            Box orig = shape.getBoundingBox();
            double completion = this.isRebreak ? BreakIndicators.this.rebreakCompletionAmount.get() : BreakIndicators.this.completionAmount.get();
            double shrinkFactor = Math.clamp(1.0 - this.getBreakProgress(currentTick) * (1.0 / completion), 0.0, 1.0);
            BlockPos pos = this.blockPos;
            Box box = orig.shrink(orig.getLengthX() * shrinkFactor, orig.getLengthY() * shrinkFactor, orig.getLengthZ() * shrinkFactor);
            double xShrink = orig.getLengthX() * shrinkFactor / 2.0;
            double yShrink = orig.getLengthY() * shrinkFactor / 2.0;
            double zShrink = orig.getLengthZ() * shrinkFactor / 2.0;
            double x1 = (double)pos.getX() + box.minX + xShrink;
            double y1 = (double)pos.getY() + box.minY + yShrink;
            double z1 = (double)pos.getZ() + box.minZ + zShrink;
            double x2 = (double)pos.getX() + box.maxX + xShrink;
            double y2 = (double)pos.getY() + box.maxY + yShrink;
            double z2 = (double)pos.getZ() + box.maxZ + zShrink;
            Color color = BreakIndicators.this.sideColor.get();
            event.renderer.box(x1, y1, z1, x2, y2, z2, color, BreakIndicators.this.lineColor.get(), BreakIndicators.this.shapeMode.get(), 0);
        }

        private double getBreakProgress(double currentTick) {
            BlockState state = ((BreakIndicators)BreakIndicators.this).mc.world.getBlockState(this.blockPos);
            FindItemResult slot = InvUtils.findFastestToolHotbar(state);
            double breakingSpeed = BlockUtils.getBlockBreakingSpeed(slot.found() ? slot.slot() : ((BreakIndicators)BreakIndicators.this).mc.player.getInventory().selectedSlot, state, true);
            return BlockUtils.getBreakDelta(breakingSpeed, state) * (currentTick - this.startTick);
        }
    }

    public static class BreakInfo {
        public final BlockPos pos;
        public final PlayerEntity player;
        public final double progress;
        public final boolean isRebreak;

        public BreakInfo(BlockPos pos, PlayerEntity player, double progress, boolean isRebreak) {
            this.pos = pos;
            this.player = player;
            this.progress = progress;
            this.isRebreak = isRebreak;
        }
    }
}

