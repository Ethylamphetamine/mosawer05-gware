/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ShapeContext
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Axis
 *  net.minecraft.util.math.Direction$Type
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public class AutoTrap
extends Module {
    private static final double TERMINAL_VELOCITY = -3.92;
    private static final double DRAG_XZ_FLY = 0.99;
    private static final double DRAG_Y_FLY = 0.98;
    private static final double ALIGN_D = 1.5;
    private static final double ALIGN_E = 0.01;
    private static final double LOOK_PUSH = 0.1;
    private static final int BOOST_DURATION_TICKS = 40;
    private static final double ELYTRA_GRAVITY = -0.04;
    private boolean buildInterceptNext = true;
    private long lastPlaceTime = 0L;
    Set<BlockPos> placePoses;
    private final Map<UUID, Integer> boostingTicks = new HashMap<UUID, Integer>();
    private BlockPos lockedCenter = null;
    private Direction.Axis lockedPrimary = null;
    private Direction.Axis lockedSecondary = null;
    private Direction.Axis lockedTertiary = null;
    private int lockedPrimaryDir = 0;
    private int lockedSecondaryDir = 0;
    private int lockedTertiaryDir = 0;
    private int trapClosingTicks = -1;
    private UUID lockedTargetUUID = null;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgPrediction = this.settings.createGroup("Prediction");
    private final SettingGroup sgRender = this.settings.createGroup("Render");
    private final Setting<List<Block>> blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("Which blocks to use.")).defaultValue(Blocks.OBSIDIAN).build());
    private final Setting<Integer> slot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("slot")).description("The hotbar slot to move blocks to if not found in hotbar.")).defaultValue(9)).range(1, 9).sliderMin(1).sliderMax(9).build());
    private final Setting<SortPriority> priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.ClosestAngle)).build());
    private final Setting<Boolean> pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat")).description("Pauses while eating.")).defaultValue(true)).build());
    private final Setting<Boolean> prediction = this.sgPrediction.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("prediction")).description("Places blocks where the player will be in the future.")).defaultValue(true)).build());
    private final Setting<Integer> predictionTicks = this.sgPrediction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("prediction-ticks")).description("The number of ticks to calculate movement into the future.")).defaultValue(3)).min(0).sliderMax(20).build());
    private final Setting<Integer> elytraPredictionTicks = this.sgPrediction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("elytra-prediction-ticks")).description("The number of ticks to calculate movement into the future")).defaultValue(3)).min(0).sliderMax(20).build());
    private final Setting<Boolean> render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed.")).defaultValue(true)).build());
    private final Setting<ShapeMode> shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
    private final Setting<SettingColor> sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232, 10)).build());
    private final Setting<SettingColor> lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232)).build());
    private final Setting<SettingColor> placementColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("placement-color")).description("Color for blocks the trap will place.")).defaultValue(new SettingColor(0, 200, 255, 35)).build());
    private final Setting<SettingColor> predictColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("predict-color")).description("Color for predicted/lead trap positions.")).defaultValue(new SettingColor(255, 140, 0, 35)).build());
    private PlayerEntity target;
    private final Setting<Boolean> trapSwimmers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("trap-swimmers")).description("Trap swimming players tightly.")).defaultValue(true)).build());
    private final Setting<Boolean> trapElytra = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("trap-elytra")).description("Trap players flying with elytra tightly.")).defaultValue(true)).build());
    private final Setting<Boolean> faceFirst = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("face-first")).description("Place the first block in front of target's face.")).defaultValue(true)).build());
    private final Setting<Integer> faceLeadBlocks = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("face-lead-blocks")).description("How many blocks forward from their face to place first.")).defaultValue(2)).min(0).sliderMax(6).visible(this.faceFirst::get)).build());
    private final List<BlockPos> posesToRender = new ArrayList<BlockPos>();

    public AutoTrap() {
        super(Categories.Combat, "auto-trap", "Traps people in a box to prevent them from moving.");
    }

    private void resetElytraTrap() {
        this.buildInterceptNext = true;
        this.lockedCenter = null;
        this.lockedPrimary = null;
        this.lockedSecondary = null;
        this.lockedTertiary = null;
        this.lockedPrimaryDir = 0;
        this.lockedSecondaryDir = 0;
        this.lockedTertiaryDir = 0;
        this.trapClosingTicks = -1;
        this.lockedTargetUUID = null;
    }

    @Override
    public void onActivate() {
        this.target = null;
        this.posesToRender.clear();
        this.resetElytraTrap();
        this.lastPlaceTime = System.currentTimeMillis();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        PlaySoundS2CPacket packet;
        if (this.mc.world == null || this.mc.world.getPlayers().isEmpty()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof PlaySoundS2CPacket && (packet = (PlaySoundS2CPacket)packet2).getSound().comp_349() == SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH) {
            Vec3d soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (!(player.getPos().squaredDistanceTo(soundPos) < 9.0)) continue;
                this.boostingTicks.put(player.getUuid(), 40);
                break;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Vec3d predictedPoint;
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        boolean foundInHotbar = false;
        for (Block block : this.blocks.get()) {
            if (!InvUtils.findInHotbar(block.asItem()).found()) continue;
            foundInHotbar = true;
            break;
        }
        if (!foundInHotbar) {
            for (Block block : this.blocks.get()) {
                FindItemResult invResult = InvUtils.find(block.asItem());
                if (!invResult.found()) continue;
                InvUtils.move().from(invResult.slot()).toHotbar(this.slot.get() - 1);
                break;
            }
        }
        this.posesToRender.clear();
        if (this.trapClosingTicks > 0) {
            --this.trapClosingTicks;
        }
        if (this.target == null || TargetUtils.isBadTarget(this.target, 14.0)) {
            this.target = TargetUtils.getPlayerTarget(14.0, this.priority.get());
            this.resetElytraTrap();
            if (this.target == null) {
                return;
            }
        }
        if (this.lockedTargetUUID != null && !this.lockedTargetUUID.equals(this.target.getUuid())) {
            this.resetElytraTrap();
        }
        if (this.trapElytra.get().booleanValue() && !this.target.isFallFlying() && this.lockedTargetUUID != null) {
            this.resetElytraTrap();
        }
        if (this.pauseEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        if (this.trapElytra.get().booleanValue() && this.target.isFallFlying()) {
            if (!this.buildInterceptNext && System.currentTimeMillis() - this.lastPlaceTime > 500L) {
                this.resetElytraTrap();
            }
            this.placePoses = this.getElytraTrapPoses(this.target);
        } else {
            this.placePoses = this.trapSwimmers.get() != false && this.target.isSwimming() ? this.getSwimmingTrapPoses(this.target) : (this.target.isCrawling() ? this.getCrawlingTrapPoses(this.target) : this.getStandingBlockPoses());
        }
        ArrayList<BlockPos> reachablePoses = new ArrayList<BlockPos>();
        Vec3d eyePos = this.mc.player.getEyePos();
        for (BlockPos pos : this.placePoses) {
            if (!(eyePos.distanceTo(Vec3d.ofCenter((Vec3i)pos)) <= 5.1)) continue;
            reachablePoses.add(pos);
        }
        if (reachablePoses.isEmpty()) {
            return;
        }
        this.posesToRender.addAll(reachablePoses);
        if (!this.buildInterceptNext) {
            predictedPoint = this.target.getPos();
        } else if (this.prediction.get().booleanValue()) {
            UUID id = this.target.getUuid();
            predictedPoint = this.target.isFallFlying() ? (this.boostingTicks.containsKey(id) ? this.simulateBoostedElytraFuturePos(this.target, this.elytraPredictionTicks.get()) : this.simulateElytraFuturePos(this.target, this.elytraPredictionTicks.get())) : this.predictPositionOnGround(this.target);
        } else {
            predictedPoint = this.target.getEyePos();
        }
        Vec3d point = predictedPoint;
        reachablePoses.sort(Comparator.comparingDouble(x -> x.getSquaredDistance((Position)point)));
        BlockPos facePriorityPos = null;
        if (this.faceFirst.get().booleanValue()) {
            Direction lookDir = this.target.getHorizontalFacing();
            BlockPos basePos = this.target.getBlockPos();
            if (this.target.isFallFlying() && this.prediction.get().booleanValue()) {
                UUID id = this.target.getUuid();
                Vec3d pred = this.boostingTicks.containsKey(id) ? this.simulateBoostedElytraFuturePos(this.target, this.elytraPredictionTicks.get()) : this.simulateElytraFuturePos(this.target, this.elytraPredictionTicks.get());
                basePos = BlockPos.ofFloored((Position)pred);
            }
            facePriorityPos = basePos.up().offset(lookDir, this.faceLeadBlocks.get().intValue());
            reachablePoses.remove(facePriorityPos);
            reachablePoses.add(0, facePriorityPos);
        }
        if (this.mc.player.getEyePos().distanceTo(predictedPoint) > 3.5 && this.target.isFallFlying()) {
            if (this.buildInterceptNext) {
                this.resetElytraTrap();
            }
            return;
        }
        if (this.target.isFallFlying()) {
            this.buildInterceptNext = false;
        }
        this.placeBlockBatch(reachablePoses);
    }

    private void placeBlockBatch(List<BlockPos> placeablePoses) {
        if (placeablePoses.isEmpty()) {
            return;
        }
        ArrayList<BlockPos> toPlace = new ArrayList<BlockPos>();
        for (BlockPos pos : placeablePoses) {
            BlockState state = this.mc.world.getBlockState(pos);
            if (!state.isReplaceable()) continue;
            toPlace.add(pos);
        }
        if (toPlace.isEmpty()) {
            return;
        }
        for (Block block : this.blocks.get()) {
            Item item = block.asItem();
            FindItemResult hotbarResult = InvUtils.findInHotbar(item);
            if (!hotbarResult.found() || !MeteorClient.BLOCK.beginPlacement(toPlace, item)) continue;
            this.lastPlaceTime = System.currentTimeMillis();
            for (BlockPos pos : toPlace) {
                MeteorClient.BLOCK.placeBlock(item, pos);
            }
            MeteorClient.BLOCK.endPlacement();
            return;
        }
    }

    private Set<BlockPos> getStandingBlockPoses() {
        Vec3d centerPos = this.prediction.get() != false ? this.predictPositionOnGround(this.target) : this.target.getPos();
        LinkedHashSet<BlockPos> placePoses = new LinkedHashSet<BlockPos>();
        double width = this.target.getWidth();
        int feetY = BlockPos.ofFloored((Position)centerPos).getY();
        Box feetBox = new Box(centerPos.x - width / 2.0, centerPos.y, centerPos.z - width / 2.0, centerPos.x + width / 2.0, centerPos.y + 0.1, centerPos.z + width / 2.0);
        for (BlockPos pos : BlockPos.iterate((int)((int)Math.floor(feetBox.minX)), (int)((int)Math.floor(feetBox.minY)), (int)((int)Math.floor(feetBox.minZ)), (int)((int)Math.floor(feetBox.maxX)), (int)((int)Math.floor(feetBox.maxY)), (int)((int)Math.floor(feetBox.maxZ)))) {
            for (int y = -1; y < 3; ++y) {
                if (pos.getY() + y == feetY) continue;
                if (y < 2) {
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        placePoses.add(pos.add(0, y, 0).offset(dir));
                    }
                }
                placePoses.add(pos.add(0, y, 0));
            }
        }
        return placePoses;
    }

    private Set<BlockPos> getCrawlingTrapPoses(PlayerEntity player) {
        Vec3d centerPos = this.prediction.get() != false ? this.predictPositionOnGround(player) : player.getPos();
        BlockPos centerBlockPos = BlockPos.ofFloored((Position)centerPos);
        LinkedHashSet<BlockPos> placePoses = new LinkedHashSet<BlockPos>();
        double width = player.getWidth();
        double height = player.getHeight();
        Box boundingBox = new Box(centerPos.x - width / 2.0, centerPos.y, centerPos.z - width / 2.0, centerPos.x + width / 2.0, centerPos.y + height, centerPos.z + width / 2.0).shrink(0.05, 0.1, 0.05);
        double feetY = centerPos.y;
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        for (BlockPos pos : BlockPos.iterate((int)((int)Math.floor(feetBox.minX)), (int)((int)Math.floor(feetBox.minY)), (int)((int)Math.floor(feetBox.minZ)), (int)((int)Math.floor(feetBox.maxX)), (int)((int)Math.floor(feetBox.maxY)), (int)((int)Math.floor(feetBox.maxZ)))) {
            for (int offsetX = -1; offsetX <= 1; ++offsetX) {
                for (int offsetZ = -1; offsetZ <= 1; ++offsetZ) {
                    BlockPos adjacentPos;
                    if (Math.abs(offsetX) + Math.abs(offsetZ) != 1 || !this.mc.world.getBlockState(adjacentPos = pos.add(offsetX, 0, offsetZ)).isAir()) continue;
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos actualPos = adjacentPos.offset(dir);
                        boolean isBadPos = false;
                        for (Direction dir2 : Direction.Type.HORIZONTAL) {
                            BlockPos playerAdjacent = centerBlockPos.offset(dir2);
                            if (!playerAdjacent.equals((Object)actualPos)) continue;
                            isBadPos = true;
                            break;
                        }
                        if (isBadPos || actualPos.equals((Object)pos)) continue;
                        placePoses.add(actualPos);
                    }
                }
            }
            placePoses.add(pos.up());
            placePoses.add(pos.down());
        }
        return placePoses;
    }

    private Set<BlockPos> getSwimmingTrapPoses(PlayerEntity player) {
        if (this.mc.world == null || player == null) {
            return new LinkedHashSet<BlockPos>();
        }
        Vec3d centerPos = this.prediction.get() != false ? this.predictPositionOnGround(player) : player.getPos();
        LinkedHashSet<BlockPos> poses = new LinkedHashSet<BlockPos>();
        double width = player.getWidth();
        double height = player.getHeight();
        Box boundingBox = new Box(centerPos.x - width / 2.0, centerPos.y, centerPos.z - width / 2.0, centerPos.x + width / 2.0, centerPos.y + height, centerPos.z + width / 2.0).shrink(0.05, 0.1, 0.05);
        int minX = (int)Math.floor(boundingBox.minX - 1.0);
        int maxX = (int)Math.ceil(boundingBox.maxX);
        int minY = (int)Math.floor(boundingBox.minY - 1.0);
        int maxY = (int)Math.ceil(boundingBox.maxY);
        int minZ = (int)Math.floor(boundingBox.minZ - 1.0);
        int maxZ = (int)Math.ceil(boundingBox.maxZ);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos pos;
                    boolean isInside;
                    boolean bl = isInside = (double)x >= boundingBox.minX && (double)x < boundingBox.maxX && (double)y >= boundingBox.minY && (double)y < boundingBox.maxY && (double)z >= boundingBox.minZ && (double)z < boundingBox.maxZ;
                    if (isInside || !BlockUtils.canPlace(pos = new BlockPos(x, y, z))) continue;
                    poses.add(pos);
                }
            }
        }
        return poses;
    }

    private Set<BlockPos> getElytraTrapPoses(PlayerEntity player) {
        LinkedHashSet<BlockPos> poses = new LinkedHashSet<BlockPos>();
        if (this.buildInterceptNext) {
            int i;
            UUID id;
            Vec3d pred = this.prediction.get().booleanValue() ? (this.boostingTicks.containsKey(id = player.getUuid()) ? this.simulateBoostedElytraFuturePos(player, this.elytraPredictionTicks.get()) : this.simulateElytraFuturePos(player, this.elytraPredictionTicks.get())) : player.getPos();
            this.lockedCenter = BlockPos.ofFloored((Position)pred);
            this.lockedTargetUUID = player.getUuid();
            Vec3d targetVel = this.target.getVelocity();
            Map<Direction.Axis, Double> axisStrength = Stream.of(Direction.Axis.values()).collect(Collectors.toMap(axis -> axis, axis -> Math.abs(targetVel.getComponentAlongAxis(axis))));
            List sortedAxes = axisStrength.entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).map(Map.Entry::getKey).collect(Collectors.toList());
            this.lockedPrimary = (Direction.Axis)sortedAxes.get(0);
            this.lockedSecondary = (Direction.Axis)sortedAxes.get(1);
            this.lockedTertiary = (Direction.Axis)sortedAxes.get(2);
            this.lockedPrimaryDir = (int)Math.signum(targetVel.getComponentAlongAxis(this.lockedPrimary));
            if (this.lockedPrimaryDir == 0) {
                this.lockedPrimaryDir = 1;
            }
            this.lockedSecondaryDir = (int)Math.signum(targetVel.getComponentAlongAxis(this.lockedSecondary));
            if (this.lockedSecondaryDir == 0) {
                this.lockedSecondaryDir = 1;
            }
            this.lockedTertiaryDir = (int)Math.signum(targetVel.getComponentAlongAxis(this.lockedTertiary));
            if (this.lockedTertiaryDir == 0) {
                this.lockedTertiaryDir = 1;
            }
            for (i = 0; i < 2; ++i) {
                for (int j = 0; j < 2; ++j) {
                    poses.add(this.lockedCenter.offset(this.lockedPrimary, this.lockedPrimaryDir).offset(this.lockedSecondary, i * this.lockedSecondaryDir).offset(this.lockedTertiary, j * this.lockedTertiaryDir));
                }
            }
            for (i = 0; i < 2; ++i) {
                poses.add(this.lockedCenter.offset(this.lockedSecondary, 2 * this.lockedSecondaryDir).offset(this.lockedTertiary, i * this.lockedTertiaryDir));
                poses.add(this.lockedCenter.offset(this.lockedTertiary, 2 * this.lockedTertiaryDir).offset(this.lockedSecondary, i * this.lockedSecondaryDir));
            }
        } else {
            Box boundingBox = player.getBoundingBox().shrink(0.01, 0.1, 0.01);
            int feetY = player.getBlockPos().getY();
            int minX = (int)Math.floor(boundingBox.minX);
            int maxX = (int)Math.floor(boundingBox.maxX);
            int minZ = (int)Math.floor(boundingBox.minZ);
            int maxZ = (int)Math.floor(boundingBox.maxZ);
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos aboveHeadPos;
                    BlockState aboveHeadState;
                    BlockPos feetPos = new BlockPos(x, feetY, z);
                    for (int offsetX = -1; offsetX <= 1; ++offsetX) {
                        for (int offsetZ = -1; offsetZ <= 1; ++offsetZ) {
                            BlockPos headPos;
                            BlockState headState;
                            if (Math.abs(offsetX) + Math.abs(offsetZ) != 1) continue;
                            BlockPos adjacentPos = feetPos.add(offsetX, 0, offsetZ);
                            BlockState adjacentState = this.mc.world.getBlockState(adjacentPos);
                            if (adjacentState.isAir() || adjacentState.isReplaceable()) {
                                poses.add(adjacentPos);
                            }
                            if (!(headState = this.mc.world.getBlockState(headPos = adjacentPos.up())).isAir() && !headState.isReplaceable()) continue;
                            poses.add(headPos);
                        }
                    }
                    BlockPos belowFeetPos = feetPos.down();
                    BlockState belowFeetState = this.mc.world.getBlockState(belowFeetPos);
                    if (belowFeetState.isAir() || belowFeetState.isReplaceable()) {
                        poses.add(belowFeetPos);
                    }
                    if (!(aboveHeadState = this.mc.world.getBlockState(aboveHeadPos = feetPos.up(2))).isAir() && !aboveHeadState.isReplaceable()) continue;
                    poses.add(aboveHeadPos);
                }
            }
        }
        return poses;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        Vec3d predictedPoint;
        if (!this.render.get().booleanValue() || this.target == null) {
            return;
        }
        if (!this.buildInterceptNext) {
            predictedPoint = this.target.getPos();
        } else if (this.prediction.get().booleanValue()) {
            UUID id = this.target.getUuid();
            predictedPoint = this.target.isFallFlying() ? (this.boostingTicks.containsKey(id) ? this.simulateBoostedElytraFuturePos(this.target, this.elytraPredictionTicks.get()) : this.simulateElytraFuturePos(this.target, this.elytraPredictionTicks.get())) : this.predictPositionOnGround(this.target);
        } else {
            predictedPoint = this.target.getEyePos();
        }
        if (predictedPoint != null) {
            event.renderer.box(Box.of((Vec3d)predictedPoint, (double)0.1, (double)0.1, (double)0.1), (Color)this.predictColor.get(), (Color)this.predictColor.get(), ShapeMode.Both, 0);
        }
        if (this.posesToRender.isEmpty()) {
            return;
        }
        for (BlockPos pos : this.posesToRender) {
            if (!BlockUtils.canPlace(pos, true)) continue;
            event.renderer.box(pos, (Color)this.placementColor.get(), (Color)this.placementColor.get(), this.shapeMode.get(), 0);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }

    private Vec3d simulateElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        float pitchRad = (float)Math.toRadians(player.getPitch());
        Vec3d look = player.getRotationVector();
        double cos = Math.cos(pitchRad);
        for (int i = 0; i < ticks; ++i) {
            Vec3d vNext;
            pos = pos.add(vel);
            double horizSpeed = Math.hypot(vel.x, vel.z);
            double len = vel.length();
            double liftFactor = cos * cos * Math.min(1.0, len / 0.4);
            double vy = vel.y + (-0.04 + liftFactor * 0.06);
            if (vel.y < 0.0 && horizSpeed > 0.0) {
                vy += -0.1 * vel.y * liftFactor;
            }
            Vec3d vAfterLift = new Vec3d(vel.x, vy, vel.z);
            Vec3d align = new Vec3d(look.x * 0.1 + (look.x * 1.5 - vAfterLift.x) * 0.01, look.y * 0.1 + (look.y * 1.5 - vAfterLift.y) * 0.01, look.z * 0.1 + (look.z * 1.5 - vAfterLift.z) * 0.01);
            Vec3d vAligned = vAfterLift.add(align);
            vel = vNext = new Vec3d(vAligned.x * 0.99, vAligned.y * 0.98, vAligned.z * 0.99);
        }
        return pos;
    }

    private Vec3d simulateBoostedElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        double BOOST_DRAG = 0.991;
        for (int i = 0; i < ticks; ++i) {
            pos = pos.add(vel);
            double vy = vel.y;
            if (vy < -3.92) {
                vy = -3.92;
            }
            vel = new Vec3d(vel.x * 0.991, vel.y, vel.z * 0.991);
        }
        return pos;
    }

    private double getGroundLevel(Vec3d position) {
        Vec3d rayStart = new Vec3d(position.x, position.y, position.z);
        Vec3d rayEnd = new Vec3d(position.x, position.y - 256.0, position.z);
        BlockHitResult hitResult = this.mc.world.raycast(new RaycastContext(rayStart, rayEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (ShapeContext)null));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult.getPos().y;
        }
        return 0.0;
    }

    public Vec3d predictPositionOnGround(PlayerEntity player) {
        if (player == null || this.isTrapped(player)) {
            return player.getPos();
        }
        Vec3d pos = player.getPos();
        double vX = player.getX() - player.prevX;
        double vY = player.getY() - player.prevY;
        double vZ = player.getZ() - player.prevZ;
        Vec3d velocity = new Vec3d(vX, vY, vZ);
        boolean onGround = player.isOnGround();
        if (onGround && vY <= 0.05) {
            velocity = new Vec3d(velocity.x, 0.0, velocity.z);
        }
        int ticks = this.predictionTicks.get();
        for (int i = 0; i < ticks; ++i) {
            Vec3d rayEnd;
            if (!onGround && velocity.y != 0.0) {
                velocity = velocity.add(0.0, -0.08, 0.0);
                velocity = velocity.multiply(0.98, 0.98, 0.98);
            }
            Vec3d nextPos = pos.add(velocity);
            Vec3d rayStart = pos.add(0.0, 0.5, 0.0);
            BlockHitResult result = this.mc.world.raycast(new RaycastContext(rayStart, rayEnd = nextPos.add(0.0, 0.5, 0.0), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)player));
            if (result.getType() == HitResult.Type.BLOCK) {
                if (velocity.y != 0.0 && result.getSide() == Direction.UP) {
                    return new Vec3d(nextPos.x, (double)(result.getBlockPos().getY() + 1), nextPos.z);
                }
                return result.getPos();
            }
            if (velocity.y != 0.0 && this.mc.world.getBlockState(BlockPos.ofFloored((Position)nextPos)).isSolid()) {
                return new Vec3d(nextPos.x, Math.floor(nextPos.y) + 1.0, nextPos.z);
            }
            pos = nextPos;
            if (velocity.lengthSquared() < 0.001) break;
        }
        return pos;
    }

    private boolean isTrapped(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        if (player.isSwimming()) {
            return false;
        }
        BlockPos pos = player.getBlockPos();
        if (this.mc.world.getBlockState(pos.down()).isReplaceable()) {
            return false;
        }
        for (Direction dir : Direction.Type.HORIZONTAL) {
            if (!this.mc.world.getBlockState(pos.up().offset(dir)).isReplaceable()) continue;
            return false;
        }
        return !this.mc.world.getBlockState(pos.up(2)).isReplaceable();
    }

    public static enum BottomMode {
        Single,
        Platform,
        Full,
        None;

    }

    public static enum TopMode {
        Full,
        Top,
        Face,
        None;

    }
}

