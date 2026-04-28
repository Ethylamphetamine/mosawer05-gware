/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.Entity$RemovalReason
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class Surround
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgProtection;
    private final SettingGroup sgSelfTrap;
    private final SettingGroup sgExtend;
    private final SettingGroup sgRender;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> avoidHelpingOpponents;
    private final Setting<Boolean> ignoreRebreak;
    private final Setting<Boolean> protect;
    private final Setting<SwingMode> breakSwingMode;
    private final Setting<Boolean> protectOverrideBlockCooldown;
    private final Setting<Boolean> protectCrystalPlacements;
    private final Setting<Boolean> enemySilentMineReact;
    private final Setting<Boolean> predictRebreak;
    private final Setting<Boolean> selfTrapEnabled;
    private final Setting<SelfTrapMode> autoSelfTrapMode;
    private final Setting<Boolean> selfTrapHead;
    private final Setting<Boolean> extendEnabled;
    private final Setting<ExtendMode> extendMode;
    private final Setting<CrawlExtendMode> crawlExtendMode;
    private final Setting<Integer> obsidianSlot;
    private final Setting<Boolean> render;
    private final Setting<Double> fadeTime;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> skippedSideColor;
    private final Setting<SettingColor> skippedLineColor;
    private final Setting<Boolean> debugProtectShape;
    private List<BlockPos> placePoses;
    private Map<BlockPos, Long> renderLastPlacedBlock;
    private Map<BlockPos, Long> renderLastSkippedBlock;
    private long lastTimeOfCrystalNearHead;
    private long lastTimeOfCrystalNearFeet;
    private long lastTimeOfExtendCrystal;
    private long lastTimeOfCrawlExtendCrystal;
    private long lastAttackTime;
    private BlockPos lastExtendCrystalOffset;
    private BlockPos lastCrawlExtendCrystalOffset;
    private boolean currentFootBlockThreatened;
    private BlockPos lastPlacePos;
    private int lastPlaceTick;
    private BlockPos rebreakPosition;
    private int rebreakTick;
    private int ticks;

    public Surround() {
        super(Categories.Combat, "surround", "Surrounds you in blocks to prevent massive crystal damage.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgProtection = this.settings.createGroup("Protection");
        this.sgSelfTrap = this.settings.createGroup("Self Trap");
        this.sgExtend = this.settings.createGroup("Extend");
        this.sgRender = this.settings.createGroup("Render");
        this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat")).description("Pauses while eating.")).defaultValue(true)).build());
        this.avoidHelpingOpponents = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("avoid-helping-opponents")).description("Avoid placing blocks that directly help opponents surround themselves (heuristic).")).defaultValue(true)).build());
        this.ignoreRebreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-rebreak")).description("Prevents placing blocks on the position SilentMine is attempting to re-break.")).defaultValue(true)).build());
        this.protect = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("protect")).description("Attempts to break crystals around surround positions to prevent surround break.")).defaultValue(true)).build());
        this.breakSwingMode = this.sgProtection.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("break-swing")).description("How to swing when breaking crystals for protection.")).visible(this.protect::get)).defaultValue(SwingMode.None)).build());
        this.protectOverrideBlockCooldown = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("protect-override-block-cooldown")).description("Overrides the cooldown for block placements when you break a crystal. May result in more packet kicks")).visible(this.protect::get)).defaultValue(false)).build());
        this.protectCrystalPlacements = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("protect-instant-place")).description("Attempt to immediately place obsidian at the crystal position right after breaking the crystal.")).visible(this.protect::get)).defaultValue(true)).build());
        this.enemySilentMineReact = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enemy-silentmine-react")).description("React to enemy breaking (silent mining) blocks near you using BreakIndicators.")).defaultValue(true)).build());
        this.predictRebreak = this.sgProtection.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict-rebreak")).description("Predicts rebreaks. In the anti-cheat the block place cooldown should be set to x + ping in s = 0.05.")).defaultValue(true)).build());
        this.selfTrapEnabled = this.sgSelfTrap.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("self-trap")).description("Enables self trap")).defaultValue(true)).build());
        this.autoSelfTrapMode = this.sgSelfTrap.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("self-trap-mode")).description("When to build double high")).defaultValue(SelfTrapMode.Smart)).visible(this.selfTrapEnabled::get)).build());
        this.selfTrapHead = this.sgSelfTrap.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("self-trap-head")).description("Places a block above your head to prevent you from velo failing upwards")).visible(this.selfTrapEnabled::get)).defaultValue(true)).build());
        this.extendEnabled = this.sgExtend.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("extend")).description("Enables extend placing")).defaultValue(true)).build());
        this.extendMode = this.sgExtend.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("extend-mode")).description("When to place extend blocks")).defaultValue(ExtendMode.Smart)).visible(this.extendEnabled::get)).build());
        this.crawlExtendMode = this.sgExtend.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("crawl-extend-mode")).description("additional protection layer when crawling.")).defaultValue(CrawlExtendMode.Smart)).build());
        this.obsidianSlot = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("obsidian-slot")).description("The hotbar slot to move Obsidian to if not found in hotbar.")).defaultValue(9)).range(1, 9).sliderMin(1).sliderMax(9).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders a block overlay when you try to place obsidian.")).defaultValue(true)).build());
        this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fadeTime")).description("How many seconds it takes to fade.")).defaultValue(0.2).min(0.0).sliderMax(1.0).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(85, 0, 255, 40)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Lines)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Sides)).build());
        this.skippedSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("skipped-side-color")).description("Side color for skipped blocks.")).defaultValue(new SettingColor(255, 0, 0, 40)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Lines)).build());
        this.skippedLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("skipped-line-color")).description("Line color for skipped blocks.")).defaultValue(new SettingColor(255, 0, 0, 120)).visible(() -> this.render.get() != false && this.shapeMode.get() != ShapeMode.Sides)).build());
        this.debugProtectShape = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("debug-protect-shape")).description("Renders the crystal protect shape positions.")).defaultValue(false)).build());
        this.placePoses = new ArrayList<BlockPos>();
        this.renderLastPlacedBlock = new HashMap<BlockPos, Long>();
        this.renderLastSkippedBlock = new HashMap<BlockPos, Long>();
        this.lastTimeOfCrystalNearHead = 0L;
        this.lastTimeOfCrystalNearFeet = 0L;
        this.lastTimeOfExtendCrystal = 0L;
        this.lastTimeOfCrawlExtendCrystal = 0L;
        this.lastAttackTime = 0L;
        this.lastExtendCrystalOffset = null;
        this.lastCrawlExtendCrystalOffset = null;
        this.currentFootBlockThreatened = false;
        this.lastPlacePos = null;
        this.lastPlaceTick = 0;
        this.rebreakPosition = null;
        this.rebreakTick = 0;
        this.ticks = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult invObsidian;
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        FindItemResult hotbarObsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
        if (!hotbarObsidian.found() && (invObsidian = InvUtils.find(Items.OBSIDIAN)).found()) {
            InvUtils.move().from(invObsidian.slot()).toHotbar(this.obsidianSlot.get() - 1);
        }
        this.placePoses.clear();
        ArrayList<BlockPos> feetBlocks = new ArrayList<BlockPos>();
        ArrayList<BlockPos> selfTrapBlocks = new ArrayList<BlockPos>();
        ArrayList<BlockPos> extendBlocks = new ArrayList<BlockPos>();
        long currentTime = System.currentTimeMillis();
        if (this.mc.player.isCrawling() || this.mc.player.isFallFlying()) {
            boolean shouldPlaceCrawlExtend;
            boundingBox = this.mc.player.getBoundingBox().shrink(0.01, 0.0, 0.01);
            int currentY = this.mc.player.getBlockPos().getY();
            int minX = (int)Math.floor(boundingBox.minX);
            int maxX = (int)Math.floor(boundingBox.maxX);
            int minZ = (int)Math.floor(boundingBox.minZ);
            int maxZ = (int)Math.floor(boundingBox.maxZ);
            boolean is1x1 = minX == maxX && minZ == maxZ;
            boolean extendThreatDetected = false;
            if (this.crawlExtendMode.get() == CrawlExtendMode.Smart && is1x1) {
                Box detectionBox = this.mc.player.getBoundingBox().expand(2.0, 1.0, 2.0);
                EndCrystalEntity closestCrystal = null;
                double closestDistSq = Double.MAX_VALUE;
                for (Entity entity2 : this.mc.world.getEntitiesByClass(EndCrystalEntity.class, detectionBox, e -> true)) {
                    double distSq = entity2.squaredDistanceTo(this.mc.player.getPos());
                    if (!(distSq < closestDistSq)) continue;
                    closestDistSq = distSq;
                    closestCrystal = (EndCrystalEntity)entity2;
                }
                if (closestCrystal != null) {
                    this.lastCrawlExtendCrystalOffset = closestCrystal.getBlockPos().subtract((Vec3i)this.mc.player.getBlockPos());
                    this.lastTimeOfCrawlExtendCrystal = currentTime;
                    extendThreatDetected = true;
                }
            }
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockPos playerPos = new BlockPos(x, currentY, z);
                    List<BlockPos> criticalOffsets = List.of(new BlockPos(0, 1, 0), new BlockPos(0, -1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(0, 0, -1));
                    for (BlockPos offset : criticalOffsets) {
                        BlockPos targetPos = playerPos.add((Vec3i)offset);
                        if (!this.mc.world.getBlockState(targetPos).isReplaceable()) continue;
                        feetBlocks.add(targetPos);
                    }
                }
            }
            boolean bl = shouldPlaceCrawlExtend = is1x1 && this.crawlExtendMode.get() == CrawlExtendMode.Smart && extendThreatDetected && (double)(currentTime - this.lastTimeOfCrawlExtendCrystal) / 1000.0 < 1.0;
            if (shouldPlaceCrawlExtend) {
                BlockPos basePos = new BlockPos(minX, currentY, minZ);
                this.placeExtendBlocks(extendBlocks, basePos, this.lastCrawlExtendCrystalOffset);
            }
        } else {
            boundingBox = this.mc.player.getBoundingBox().shrink(0.01, 0.1, 0.01);
            int feetY = this.mc.player.getBlockPos().getY();
            SilentMine silentMine = Modules.get().get(SilentMine.class);
            int minX = (int)Math.floor(boundingBox.minX);
            int maxX = (int)Math.floor(boundingBox.maxX);
            int minZ = (int)Math.floor(boundingBox.minZ);
            int maxZ = (int)Math.floor(boundingBox.maxZ);
            boolean is1x1 = minX == maxX && minZ == maxZ;
            boolean extendThreatDetected = false;
            boolean threatFromAbove = false;
            boolean threatFromBelow = false;
            if (this.selfTrapEnabled.get().booleanValue() || this.extendEnabled.get().booleanValue() && this.extendMode.get() == ExtendMode.Smart) {
                Box feetBox;
                Box headBox = this.mc.player.getBoundingBox().expand(3.5, 0.5, 3.5).offset(0.0, 1.0, 0.0);
                if (EntityUtils.intersectsWithEntity(headBox, e -> e instanceof EndCrystalEntity)) {
                    threatFromAbove = true;
                    this.lastTimeOfCrystalNearHead = currentTime;
                }
                if (EntityUtils.intersectsWithEntity(feetBox = this.mc.player.getBoundingBox().expand(1.5, 0.0, 1.5).offset(0.0, -1.0, 0.0), e -> e instanceof EndCrystalEntity)) {
                    threatFromBelow = true;
                    this.lastTimeOfCrystalNearFeet = currentTime;
                }
            }
            if (this.extendMode.get() == ExtendMode.Smart && this.extendEnabled.get().booleanValue() && is1x1) {
                Box extendCheckBox = this.mc.player.getBoundingBox().expand(2.0, 1.0, 2.0);
                EndCrystalEntity closestCrystal = null;
                double closestDistSq = Double.MAX_VALUE;
                for (Entity entity3 : this.mc.world.getEntitiesByClass(EndCrystalEntity.class, extendCheckBox, e -> true)) {
                    double distSq;
                    if (entity3.getBlockPos().getY() > this.mc.player.getBlockPos().getY() || !((distSq = entity3.squaredDistanceTo(this.mc.player.getPos())) < closestDistSq)) continue;
                    closestDistSq = distSq;
                    closestCrystal = (EndCrystalEntity)entity3;
                }
                if (closestCrystal != null) {
                    this.lastExtendCrystalOffset = closestCrystal.getBlockPos().subtract((Vec3i)this.mc.player.getBlockPos());
                    this.lastTimeOfExtendCrystal = currentTime;
                    extendThreatDetected = true;
                }
            }
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    boolean skipRebreak;
                    BlockPos feetPos = new BlockPos(x, feetY, z);
                    for (int offsetX = -1; offsetX <= 1; ++offsetX) {
                        for (int offsetZ = -1; offsetZ <= 1; ++offsetZ) {
                            if (Math.abs(offsetX) + Math.abs(offsetZ) != 1) continue;
                            BlockPos adjacentPos = feetPos.add(offsetX, 0, offsetZ);
                            if (this.mc.world.getBlockState(adjacentPos).isReplaceable()) {
                                feetBlocks.add(adjacentPos);
                            }
                            if (this.autoSelfTrapMode.get() == SelfTrapMode.None || !this.selfTrapEnabled.get().booleanValue()) continue;
                            this.checkSmartDefenses(selfTrapBlocks, adjacentPos, threatFromAbove, threatFromBelow, currentTime);
                        }
                    }
                    BlockPos belowFeetPos = new BlockPos(x, feetY - 1, z);
                    BlockState belowFeetState = this.mc.world.getBlockState(belowFeetPos);
                    boolean bl = skipRebreak = this.ignoreRebreak.get() != false && (belowFeetPos.equals((Object)silentMine.getRebreakBlockPos()) || belowFeetPos.equals((Object)silentMine.getDelayedDestroyBlockPos()));
                    if (skipRebreak || !belowFeetState.isAir() && !belowFeetState.isReplaceable()) continue;
                    feetBlocks.add(belowFeetPos);
                }
            }
            if (this.extendEnabled.get().booleanValue() && is1x1) {
                boolean shouldPlaceExtend;
                boolean bl = shouldPlaceExtend = this.extendMode.get() == ExtendMode.Always || this.extendMode.get() == ExtendMode.Smart && extendThreatDetected && (double)(currentTime - this.lastTimeOfExtendCrystal) / 1000.0 < 1.0;
                if (shouldPlaceExtend) {
                    this.placeExtendBlocks(extendBlocks, this.mc.player.getBlockPos(), this.lastExtendCrystalOffset);
                }
            }
            if (this.selfTrapEnabled.get().booleanValue() && this.selfTrapHead.get().booleanValue() && !this.mc.player.isCrawling()) {
                selfTrapBlocks.add(this.mc.player.getBlockPos().offset(Direction.UP, 2));
            }
        }
        BreakIndicators breakIndicators = Modules.get().get(BreakIndicators.class);
        this.currentFootBlockThreatened = false;
        if (this.enemySilentMineReact.get().booleanValue() && breakIndicators != null && this.mc.player != null) {
            boolean is1x1Local;
            BlockPos myFeet = this.mc.player.getBlockPos();
            Box bbFeet = this.mc.player.getBoundingBox().shrink(0.01, 0.1, 0.01);
            int bbMinX = (int)Math.floor(bbFeet.minX);
            int bbMaxX = (int)Math.floor(bbFeet.maxX);
            int bbMinZ = (int)Math.floor(bbFeet.minZ);
            int bbMaxZ = (int)Math.floor(bbFeet.maxZ);
            boolean bl = is1x1Local = bbMinX == bbMaxX && bbMinZ == bbMaxZ;
            if (is1x1Local) {
                Map<BlockPos, BreakIndicators.BreakInfo> active = breakIndicators.getActiveBreaksSnapshot();
                for (BreakIndicators.BreakInfo info : active.values()) {
                    BlockPos[] targets;
                    int perpZ2;
                    int perpX2;
                    int perpZ1;
                    int perpX1;
                    if (info.player == null) continue;
                    int dx = info.pos.getX() - myFeet.getX();
                    int dy = info.pos.getY() - myFeet.getY();
                    int dz = info.pos.getZ() - myFeet.getZ();
                    if (dy != 0 || Math.abs(dx) + Math.abs(dz) != 1) continue;
                    this.currentFootBlockThreatened = true;
                    BlockPos adjacent = info.pos;
                    if (dx != 0) {
                        perpX1 = dx;
                        perpZ1 = 1;
                        perpX2 = dx;
                        perpZ2 = -1;
                    } else {
                        perpX1 = 1;
                        perpZ1 = dz;
                        perpX2 = -1;
                        perpZ2 = dz;
                    }
                    BlockPos aboveAdj = adjacent.up();
                    BlockPos belowAdj = adjacent.down();
                    BlockPos perp1 = myFeet.add(perpX1, 0, perpZ1);
                    BlockPos perp2 = myFeet.add(perpX2, 0, perpZ2);
                    BlockPos outward2 = myFeet.add(dx * 2, 0, dz * 2);
                    for (BlockPos t : targets = new BlockPos[]{aboveAdj, belowAdj, perp1, perp2, outward2}) {
                        if (t.equals((Object)myFeet) || !this.mc.world.getBlockState(t).isReplaceable()) continue;
                        extendBlocks.add(t);
                    }
                }
            }
        }
        this.placePoses.addAll(feetBlocks);
        this.placePoses.addAll(selfTrapBlocks);
        this.placePoses.addAll(extendBlocks);
        Set<BlockPos> protectPositions = this.getProtectPositions();
        ++this.ticks;
        if (this.rebreakPosition != null && this.ticks - this.rebreakTick > 5 && this.predictRebreak.get().booleanValue()) {
            this.rebreakPosition = null;
        }
        if (!this.pauseEat.get().booleanValue() || !this.mc.player.isUsingItem()) {
            if (this.protect.get().booleanValue()) {
                SilentMine silentMine = Modules.get().get(SilentMine.class);
                for (BlockPos protectPos : protectPositions) {
                    boolean skipRebreak;
                    BlockPos crystalPos;
                    Predicate<Entity> entityPredicate;
                    Box box = new Box((double)protectPos.getX(), (double)protectPos.getY(), (double)protectPos.getZ(), (double)(protectPos.getX() + 1), (double)(protectPos.getY() + 1), (double)(protectPos.getZ() + 1));
                    Entity blocking = this.mc.world.getOtherEntities(null, box, entityPredicate = entity -> entity instanceof EndCrystalEntity).stream().findFirst().orElse(null);
                    if (blocking == null || System.currentTimeMillis() - this.lastAttackTime < 50L || (crystalPos = blocking.getBlockPos()) == null || !this.isInsideProtectShape(crystalPos, protectPositions)) continue;
                    MeteorClient.ROTATION.requestRotation(blocking.getPos(), 11.0);
                    if (!MeteorClient.ROTATION.lookingAt(blocking.getBoundingBox()) && RotationManager.lastGround) {
                        MeteorClient.ROTATION.snapAt(blocking.getPos());
                    }
                    if (!MeteorClient.ROTATION.lookingAt(blocking.getBoundingBox())) continue;
                    this.mc.getNetworkHandler().sendPacket((Packet)PlayerInteractEntityC2SPacket.attack((Entity)blocking, (boolean)this.mc.player.isSneaking()));
                    if (this.breakSwingMode.get() == SwingMode.Client) {
                        this.mc.player.swingHand(Hand.MAIN_HAND);
                    } else if (this.breakSwingMode.get() == SwingMode.Packet) {
                        this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
                    }
                    blocking.setRemoved(Entity.RemovalReason.KILLED);
                    blocking.discard();
                    this.lastAttackTime = System.currentTimeMillis();
                    if (this.protectOverrideBlockCooldown.get().booleanValue()) {
                        MeteorClient.BLOCK.forceResetPlaceCooldown(protectPos);
                    }
                    if (!this.protectCrystalPlacements.get().booleanValue()) continue;
                    ArrayList<BlockPos> immediateTargets = new ArrayList<BlockPos>();
                    BlockPos rebreak = null;
                    BlockPos delayed = null;
                    if (silentMine != null && silentMine.isActive()) {
                        rebreak = silentMine.getRebreakBlockPos();
                        delayed = silentMine.getDelayedDestroyBlockPos();
                    }
                    BlockPos crystalPos2 = blocking.getBlockPos();
                    boolean bl = skipRebreak = this.ignoreRebreak.get() != false && (rebreak != null && crystalPos2.equals((Object)rebreak) || delayed != null && crystalPos2.equals((Object)delayed));
                    if (crystalPos2 != null && this.isInsideProtectShape(crystalPos2, protectPositions) && !this.isInsideEnemyHitboxShell(crystalPos2) && this.mc.world.getBlockState(crystalPos2).isReplaceable() && !skipRebreak) {
                        immediateTargets.add(crystalPos2);
                    }
                    if (immediateTargets.isEmpty() || !MeteorClient.BLOCK.beginPlacement(immediateTargets, Items.OBSIDIAN)) continue;
                    for (BlockPos t : immediateTargets) {
                        try {
                            if (this.ignoreRebreak.get().booleanValue() && (t.equals((Object)rebreak) || t.equals((Object)delayed))) continue;
                            if (MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, t)) {
                                this.renderLastPlacedBlock.put(t, System.currentTimeMillis());
                            }
                            MeteorClient.BLOCK.endPlacement();
                        }
                        catch (Exception exception) {}
                    }
                }
            }
            ArrayList<BlockPos> filteredPlacePoses = new ArrayList<BlockPos>();
            HashSet<BlockPos> skippedThisTick = new HashSet<BlockPos>();
            if (!this.avoidHelpingOpponents.get().booleanValue()) {
                filteredPlacePoses.addAll(this.placePoses);
            } else {
                HashSet<BlockPos> playerFootBlocks = new HashSet<BlockPos>();
                int playerFeetY = this.mc.player.getBlockPos().getY();
                boolean amIn1x1 = false;
                Box bb = this.mc.player.getBoundingBox().shrink(0.01, 0.1, 0.01);
                int minX = (int)Math.floor(bb.minX);
                int maxX = (int)Math.floor(bb.maxX);
                int minZ = (int)Math.floor(bb.minZ);
                int maxZ = (int)Math.floor(bb.maxZ);
                amIn1x1 = minX == maxX && minZ == maxZ;
                for (int x = minX; x <= maxX; ++x) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        playerFootBlocks.add(new BlockPos(x, playerFeetY, z));
                    }
                }
                boolean lowHealth = this.isLowHealth(10.0);
                for (BlockPos pos : this.placePoses) {
                    boolean helps = this.wouldHelpOpponent(pos);
                    if (!helps) {
                        filteredPlacePoses.add(pos);
                        continue;
                    }
                    boolean isNearMyFeet = this.isNearMyPerimeter(pos, playerFootBlocks);
                    if (this.isPlayerPhased() && !this.currentFootBlockThreatened) {
                        skippedThisTick.add(pos);
                        continue;
                    }
                    if (isNearMyFeet) {
                        filteredPlacePoses.add(pos);
                        continue;
                    }
                    skippedThisTick.add(pos);
                }
            }
            long now = System.currentTimeMillis();
            for (BlockPos p : skippedThisTick) {
                this.renderLastSkippedBlock.put(p, now);
            }
            if (MeteorClient.BLOCK.beginPlacement(filteredPlacePoses, Items.OBSIDIAN)) {
                filteredPlacePoses.forEach(blockPos -> {
                    boolean skipRebreak;
                    SilentMine sm = Modules.get().get(SilentMine.class);
                    boolean bl = skipRebreak = this.ignoreRebreak.get() != false && (blockPos.equals((Object)sm.getRebreakBlockPos()) || blockPos.equals((Object)sm.getLastDelayedDestroyBlockPos()));
                    if (!skipRebreak && MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, (BlockPos)blockPos)) {
                        if (this.predictRebreak.get().booleanValue()) {
                            if (blockPos.equals((Object)this.lastPlacePos) && this.ticks - this.lastPlaceTick <= 3) {
                                this.rebreakPosition = blockPos;
                                this.rebreakTick = this.ticks;
                            }
                            this.lastPlacePos = blockPos;
                            this.lastPlaceTick = this.ticks;
                        }
                        this.renderLastPlacedBlock.put((BlockPos)blockPos, currentTime);
                    }
                });
                MeteorClient.BLOCK.endPlacement();
            }
        }
        if (this.debugProtectShape.get().booleanValue() && this.mc.world != null && this.mc.player != null) {
            long nowDbg = System.currentTimeMillis();
            for (BlockPos p : protectPositions) {
                this.renderLastSkippedBlock.put(p, nowDbg);
            }
        }
    }

    private boolean isNearMyPerimeter(BlockPos candidatePos, Set<BlockPos> playerBlocks) {
        for (BlockPos playerBlock : playerBlocks) {
            if (!candidatePos.equals((Object)playerBlock.up()) && !candidatePos.equals((Object)playerBlock.down()) && !candidatePos.equals((Object)playerBlock.north()) && !candidatePos.equals((Object)playerBlock.south()) && !candidatePos.equals((Object)playerBlock.east()) && !candidatePos.equals((Object)playerBlock.west()) && !candidatePos.equals((Object)playerBlock)) continue;
            return true;
        }
        return false;
    }

    private void checkSmartDefenses(List<BlockPos> placePoses, BlockPos adjacentPos, boolean threatFromAbove, boolean threatFromBelow, long currentTime) {
        boolean shouldBuildDown;
        BlockPos facePlacePos;
        boolean shouldBuildUp;
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        boolean bl = shouldBuildUp = this.autoSelfTrapMode.get() == SelfTrapMode.Always || this.autoSelfTrapMode.get() == SelfTrapMode.Smart && threatFromAbove && (double)(currentTime - this.lastTimeOfCrystalNearHead) / 1000.0 < 1.0;
        if (shouldBuildUp && this.mc.world.getBlockState(facePlacePos = adjacentPos.up()).isReplaceable()) {
            placePoses.add(facePlacePos);
        }
        boolean bl2 = shouldBuildDown = this.autoSelfTrapMode.get() == SelfTrapMode.Always || this.autoSelfTrapMode.get() == SelfTrapMode.Smart && threatFromAbove && (double)(currentTime - this.lastTimeOfCrystalNearFeet) / 1000.0 < 1.0;
        if (shouldBuildDown) {
            BlockPos feetFloor;
            BlockPos belowPos = adjacentPos.down();
            if (this.mc.world.getBlockState(belowPos).isReplaceable()) {
                placePoses.add(belowPos);
            }
            if (this.mc.world.getBlockState(feetFloor = this.mc.player.getBlockPos().down(2)).isReplaceable()) {
                placePoses.add(feetFloor);
            }
        }
    }

    private void placeExtendBlocks(List<BlockPos> placePoses, BlockPos feetPos, BlockPos crystalOffset) {
        if (crystalOffset == null || this.mc.world == null) {
            return;
        }
        int normDx = Integer.signum(crystalOffset.getX());
        int normDz = Integer.signum(crystalOffset.getZ());
        boolean isDiagonal = normDx != 0 && normDz != 0;
        boolean isCardinal = normDx != 0 ^ normDz != 0;
        if (isDiagonal) {
            BlockPos straight2;
            BlockPos straight1;
            BlockPos diagonalBlock = feetPos.add(normDx, 0, normDz);
            if (this.mc.world.getBlockState(diagonalBlock).isReplaceable()) {
                placePoses.add(diagonalBlock);
            }
            if (this.mc.world.getBlockState(straight1 = feetPos.add(normDx * 2, 0, 0)).isReplaceable()) {
                placePoses.add(straight1);
            }
            if (this.mc.world.getBlockState(straight2 = feetPos.add(0, 0, normDz * 2)).isReplaceable()) {
                placePoses.add(straight2);
            }
        } else if (isCardinal) {
            BlockPos straightBlock;
            BlockPos diagonal2;
            BlockPos diagonal1;
            if (normDx != 0) {
                diagonal1 = feetPos.add(normDx, 0, 1);
                diagonal2 = feetPos.add(normDx, 0, -1);
                straightBlock = feetPos.add(normDx * 2, 0, 0);
            } else {
                diagonal1 = feetPos.add(1, 0, normDz);
                diagonal2 = feetPos.add(-1, 0, normDz);
                straightBlock = feetPos.add(0, 0, normDz * 2);
            }
            if (this.mc.world.getBlockState(diagonal1).isReplaceable()) {
                placePoses.add(diagonal1);
            }
            if (this.mc.world.getBlockState(diagonal2).isReplaceable()) {
                placePoses.add(diagonal2);
            }
            if (this.mc.world.getBlockState(straightBlock).isReplaceable()) {
                placePoses.add(straightBlock);
            }
        }
    }

    private boolean isCrystalBlock(BlockPos blockPos) {
        BlockState blockState = this.mc.world.getBlockState(blockPos);
        return blockState.isOf(Blocks.OBSIDIAN) || blockState.isOf(Blocks.BEDROCK);
    }

    private boolean wouldHelpOpponent(BlockPos candidatePos) {
        if (!this.avoidHelpingOpponents.get().booleanValue()) {
            return false;
        }
        if (this.mc.world == null || this.mc.player == null) {
            return false;
        }
        BlockPos cand = candidatePos;
        for (Entity e : this.mc.world.getPlayers()) {
            int z;
            int x;
            int startZ;
            int startX;
            List<Object> checkSet;
            boolean oppIn2x2;
            BlockPos oppFeet;
            double horiz;
            if (e == this.mc.player || !(e instanceof PlayerEntity)) continue;
            PlayerEntity p = (PlayerEntity)e;
            if (Friends.get().isFriend(p) || (horiz = Math.sqrt(Math.pow((oppFeet = p.getBlockPos()).getX() - this.mc.player.getBlockPos().getX(), 2.0) + Math.pow(oppFeet.getZ() - this.mc.player.getBlockPos().getZ(), 2.0))) > 5.0) continue;
            Box pbb = p.getBoundingBox().shrink(0.01, 0.1, 0.01);
            int minX = (int)Math.floor(pbb.minX);
            int maxX = (int)Math.floor(pbb.maxX);
            int minZ = (int)Math.floor(pbb.minZ);
            int maxZ = (int)Math.floor(pbb.maxZ);
            int sizeX = maxX - minX + 1;
            int sizeZ = maxZ - minZ + 1;
            boolean oppIn1x1 = sizeX == 1 && sizeZ == 1;
            boolean oppIn2x1 = sizeX == 2 && sizeZ == 1 || sizeX == 1 && sizeZ == 2;
            boolean bl = oppIn2x2 = sizeX == 2 && sizeZ == 2;
            if (this.isEntityPhased((Entity)p)) continue;
            if (oppIn1x1) {
                checkSet = List.of(oppFeet.north(), oppFeet.south(), oppFeet.east(), oppFeet.west());
                if (checkSet.contains(cand)) {
                    return true;
                }
                if (cand.equals((Object)oppFeet.down()) || cand.equals((Object)oppFeet)) {
                    return true;
                }
            }
            if (oppIn2x1) {
                checkSet = new ArrayList();
                startX = minX;
                startZ = minZ;
                for (x = startX; x < startX + sizeX; ++x) {
                    checkSet.add(new BlockPos(x, oppFeet.getY(), startZ - 1));
                    checkSet.add(new BlockPos(x, oppFeet.getY(), startZ + sizeZ));
                }
                for (z = startZ; z < startZ + sizeZ; ++z) {
                    checkSet.add(new BlockPos(startX - 1, oppFeet.getY(), z));
                    checkSet.add(new BlockPos(startX + sizeX, oppFeet.getY(), z));
                }
                if (checkSet.contains(cand)) {
                    return true;
                }
            }
            if (!oppIn2x2) continue;
            checkSet = new ArrayList();
            startX = minX;
            startZ = minZ;
            for (x = startX; x < startX + 2; ++x) {
                checkSet.add(new BlockPos(x, oppFeet.getY(), startZ - 1));
                checkSet.add(new BlockPos(x, oppFeet.getY(), startZ + 2));
            }
            for (z = startZ; z < startZ + 2; ++z) {
                checkSet.add(new BlockPos(startX - 1, oppFeet.getY(), z));
                checkSet.add(new BlockPos(startX + 2, oppFeet.getY(), z));
            }
            if (!checkSet.contains(cand)) continue;
            return true;
        }
        return false;
    }

    private boolean isEntityPhased(Entity e) {
        if (e == null || this.mc.world == null) {
            return false;
        }
        BlockPos pos = e.getBlockPos();
        return !this.mc.world.getBlockState(pos).isReplaceable();
    }

    private boolean isInsideEnemyHitboxShell(BlockPos candidatePos) {
        if (this.mc.world == null || this.mc.player == null) {
            return false;
        }
        double cx = (double)candidatePos.getX() + 0.5;
        double cy = (double)candidatePos.getY() + 0.5;
        double cz = (double)candidatePos.getZ() + 0.5;
        for (Entity e : this.mc.world.getPlayers()) {
            Box bb;
            Box expanded;
            PlayerEntity enemy;
            if (!(e instanceof PlayerEntity) || (enemy = (PlayerEntity)e) == this.mc.player || Friends.get().isFriend(enemy) || !(expanded = (bb = enemy.getBoundingBox()).expand(2.0)).contains(cx, cy, cz)) continue;
            return true;
        }
        return false;
    }

    private Set<BlockPos> getStandingFeetOffsets() {
        HashSet<BlockPos> s = new HashSet<BlockPos>();
        s.add(new BlockPos(0, 0, 0));
        s.add(new BlockPos(1, 0, 0));
        s.add(new BlockPos(2, 0, 0));
        s.add(new BlockPos(3, 0, 0));
        s.add(new BlockPos(-1, 0, 0));
        s.add(new BlockPos(-2, 0, 0));
        s.add(new BlockPos(-3, 0, 0));
        s.add(new BlockPos(0, 0, 1));
        s.add(new BlockPos(0, 0, 2));
        s.add(new BlockPos(0, 0, 3));
        s.add(new BlockPos(0, 0, -1));
        s.add(new BlockPos(0, 0, -2));
        s.add(new BlockPos(0, 0, -3));
        s.add(new BlockPos(1, 0, 1));
        s.add(new BlockPos(2, 0, 1));
        s.add(new BlockPos(1, 0, 2));
        s.add(new BlockPos(1, 0, -1));
        s.add(new BlockPos(2, 0, -1));
        s.add(new BlockPos(1, 0, -2));
        s.add(new BlockPos(-1, 0, 1));
        s.add(new BlockPos(-2, 0, 1));
        s.add(new BlockPos(-1, 0, 2));
        s.add(new BlockPos(-1, 0, -1));
        s.add(new BlockPos(-2, 0, -1));
        s.add(new BlockPos(-1, 0, -2));
        return s;
    }

    private Set<BlockPos> getStandingHeadOffsets() {
        HashSet<BlockPos> s = new HashSet<BlockPos>();
        s.add(new BlockPos(0, 1, 0));
        s.add(new BlockPos(1, 1, 0));
        s.add(new BlockPos(-1, 1, 0));
        s.add(new BlockPos(2, 1, 0));
        s.add(new BlockPos(-2, 1, 0));
        s.add(new BlockPos(0, 1, 1));
        s.add(new BlockPos(0, 1, -1));
        s.add(new BlockPos(0, 1, 2));
        s.add(new BlockPos(0, 1, -2));
        s.add(new BlockPos(1, 1, 1));
        s.add(new BlockPos(1, 1, -1));
        s.add(new BlockPos(-1, 1, 1));
        s.add(new BlockPos(-1, 1, -1));
        return s;
    }

    private Set<BlockPos> getStandingAboveHeadOffsets() {
        HashSet<BlockPos> s = new HashSet<BlockPos>();
        s.add(new BlockPos(0, 2, 0));
        s.add(new BlockPos(0, 3, 0));
        s.add(new BlockPos(1, 2, 0));
        s.add(new BlockPos(-1, 2, 0));
        s.add(new BlockPos(0, 2, 1));
        s.add(new BlockPos(0, 2, -1));
        return s;
    }

    private Set<BlockPos> getStandingBelowFeetOffsets() {
        HashSet<BlockPos> s = new HashSet<BlockPos>();
        s.add(new BlockPos(0, -1, 0));
        s.add(new BlockPos(1, -1, 0));
        s.add(new BlockPos(-1, -1, 0));
        s.add(new BlockPos(2, -1, 0));
        s.add(new BlockPos(2, -1, 1));
        s.add(new BlockPos(2, -1, -1));
        s.add(new BlockPos(-2, -1, 0));
        s.add(new BlockPos(-2, -1, 1));
        s.add(new BlockPos(-2, -1, -1));
        s.add(new BlockPos(0, -1, 1));
        s.add(new BlockPos(0, -1, -1));
        s.add(new BlockPos(0, -1, 2));
        s.add(new BlockPos(1, -1, 2));
        s.add(new BlockPos(-1, -1, 2));
        s.add(new BlockPos(0, -1, -2));
        s.add(new BlockPos(1, -1, -2));
        s.add(new BlockPos(-1, -1, -2));
        s.add(new BlockPos(1, -1, 1));
        s.add(new BlockPos(1, -1, -1));
        s.add(new BlockPos(-1, -1, 1));
        s.add(new BlockPos(-1, -1, -1));
        s.add(new BlockPos(0, -2, 0));
        return s;
    }

    private Set<BlockPos> getCrawlingBodyOffsets() {
        return this.getStandingFeetOffsets();
    }

    private Set<BlockPos> getCrawlingBelowAndAboveOffsets() {
        HashSet<BlockPos> s = new HashSet<BlockPos>();
        s.add(new BlockPos(0, -1, 0));
        s.add(new BlockPos(1, -1, 0));
        s.add(new BlockPos(-1, -1, 0));
        s.add(new BlockPos(0, -1, 1));
        s.add(new BlockPos(0, -1, -1));
        s.add(new BlockPos(0, -2, 0));
        s.add(new BlockPos(1, -1, 1));
        s.add(new BlockPos(1, -1, -1));
        s.add(new BlockPos(-1, -1, 1));
        s.add(new BlockPos(-1, -1, -1));
        s.add(new BlockPos(2, -1, 0));
        s.add(new BlockPos(2, -1, 1));
        s.add(new BlockPos(2, -1, -1));
        s.add(new BlockPos(-2, -1, 0));
        s.add(new BlockPos(-2, -1, 1));
        s.add(new BlockPos(-2, -1, -1));
        s.add(new BlockPos(0, 2, 0));
        s.add(new BlockPos(0, 1, 0));
        s.add(new BlockPos(1, 1, 0));
        s.add(new BlockPos(0, 1, 1));
        s.add(new BlockPos(-1, 1, 0));
        s.add(new BlockPos(0, 1, -1));
        s.add(new BlockPos(0, -1, 2));
        s.add(new BlockPos(1, -1, 2));
        s.add(new BlockPos(-1, -1, 2));
        s.add(new BlockPos(0, -1, -2));
        s.add(new BlockPos(1, -1, -2));
        s.add(new BlockPos(-1, -1, -2));
        return s;
    }

    private Set<BlockPos> getProtectPositions() {
        HashSet<BlockPos> result = new HashSet<BlockPos>();
        if (this.mc.player == null) {
            return result;
        }
        BlockPos base = this.mc.player.getBlockPos();
        if (this.mc.player.isCrawling() || this.mc.player.isFallFlying()) {
            for (BlockPos off : this.getCrawlingBodyOffsets()) {
                result.add(base.add((Vec3i)off));
            }
            for (BlockPos off : this.getCrawlingBelowAndAboveOffsets()) {
                result.add(base.add((Vec3i)off));
            }
        } else {
            for (BlockPos off : this.getStandingFeetOffsets()) {
                result.add(base.add((Vec3i)off));
            }
            for (BlockPos off : this.getStandingHeadOffsets()) {
                result.add(base.add((Vec3i)off));
            }
            for (BlockPos off : this.getStandingAboveHeadOffsets()) {
                result.add(base.add((Vec3i)off));
            }
            for (BlockPos off : this.getStandingBelowFeetOffsets()) {
                result.add(base.add((Vec3i)off));
            }
        }
        return result;
    }

    private boolean isInsideProtectShape(BlockPos pos, Set<BlockPos> protectPositions) {
        return protectPositions.contains(pos);
    }

    private boolean isPlayerPhased() {
        if (this.mc.player == null || this.mc.world == null) {
            return false;
        }
        int playerFeetY = this.mc.player.getBlockPos().getY();
        Box bb = this.mc.player.getBoundingBox().shrink(0.01, 0.1, 0.01);
        int minX = (int)Math.floor(bb.minX);
        int maxX = (int)Math.floor(bb.maxX);
        int minZ = (int)Math.floor(bb.minZ);
        int maxZ = (int)Math.floor(bb.maxZ);
        int validSolidBlocks = 0;
        SilentMine silentMine = Modules.get().get(SilentMine.class);
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                BlockPos footBlock = new BlockPos(x, playerFeetY, z);
                if (this.mc.world.getBlockState(footBlock).isReplaceable() || silentMine != null && silentMine.isActive() && (footBlock.equals((Object)silentMine.getRebreakBlockPos()) || footBlock.equals((Object)silentMine.getDelayedDestroyBlockPos()) && silentMine.getDelayedDestroyProgress() > 0.5)) continue;
                ++validSolidBlocks;
            }
        }
        return validSolidBlocks >= 1;
    }

    private boolean isLowHealth(double threshold) {
        if (this.mc.player == null) {
            return false;
        }
        return (double)this.mc.player.getHealth() < threshold;
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.render.get().booleanValue()) {
            this.draw(event);
        }
    }

    private void draw(Render3DEvent event) {
        Color fadedLineColor;
        Color fadedSideColor;
        double timeCompletion;
        double time;
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<BlockPos, Long> entry : this.renderLastPlacedBlock.entrySet()) {
            if ((double)(currentTime - entry.getValue()) > this.fadeTime.get() * 1000.0) continue;
            time = (double)(currentTime - entry.getValue()) / 1000.0;
            timeCompletion = time / this.fadeTime.get();
            fadedSideColor = this.sideColor.get().copy().a((int)((double)this.sideColor.get().a * (1.0 - timeCompletion)));
            fadedLineColor = this.lineColor.get().copy().a((int)((double)this.lineColor.get().a * (1.0 - timeCompletion)));
            event.renderer.box(entry.getKey(), fadedSideColor, fadedLineColor, this.shapeMode.get(), 0);
        }
        for (Map.Entry<BlockPos, Long> entry : this.renderLastSkippedBlock.entrySet()) {
            if ((double)(currentTime - entry.getValue()) > this.fadeTime.get() * 1000.0) continue;
            time = (double)(currentTime - entry.getValue()) / 1000.0;
            timeCompletion = time / this.fadeTime.get();
            fadedSideColor = this.skippedSideColor.get().copy().a((int)((double)this.skippedSideColor.get().a * (1.0 - timeCompletion)));
            fadedLineColor = this.skippedLineColor.get().copy().a((int)((double)this.skippedLineColor.get().a * (1.0 - timeCompletion)));
            event.renderer.box(entry.getKey(), fadedSideColor, fadedLineColor, this.shapeMode.get(), 0);
        }
    }

    public static enum SwingMode {
        None,
        Client,
        Packet;

    }

    public static enum SelfTrapMode {
        None,
        Smart,
        Always;

    }

    public static enum ExtendMode {
        None,
        Smart,
        Always;

    }

    public static enum CrawlExtendMode {
        None,
        Smart;

    }

    public static enum RotationMode {
        None,
        Silent,
        Real;

    }
}

