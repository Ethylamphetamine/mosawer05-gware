/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Type
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.meteor.SilentMineFinishedEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoMine
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final double INVALID_SCORE = -1000.0;
    private final Setting<Double> range;
    private final Setting<SortPriority> targetPriority;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<ExtendBreakMode> extendBreakMode;
    private final Setting<AntiSwimMode> antiSwim;
    private final Setting<AntiSurroundMode> antiSurroundMode;
    private final Setting<Boolean> pauseEatCrystals;
    private final Setting<Boolean> antiSurroundInnerSnap;
    private final Setting<Boolean> antiSurroundOuterSnap;
    private final Setting<Double> antiSurroundOuterCooldown;
    private final Setting<Boolean> renderDebugScores;
    private SilentMine silentMine;
    private PlayerEntity targetPlayer;
    private CityBlock target1;
    private CityBlock target2;
    private BlockPos ignorePos;
    private long lastOuterPlaceTime;
    private boolean isTerrainFight;

    public AutoMine() {
        super(Categories.Combat, "auto-mine", "Automatically mines blocks. Requires SilentMine to work.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.INVALID_SCORE = -1000.0;
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Max range to target")).defaultValue(6.5).min(0.0).sliderMax(7.0).build());
        this.targetPriority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to choose the target")).defaultValue(SortPriority.ClosestAngle)).build());
        this.ignoreNakeds = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Ignore players with no items.")).defaultValue(true)).build());
        this.extendBreakMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("extend-break-mode")).description("How to mine outside of their surround to place crystals better")).defaultValue(ExtendBreakMode.None)).build());
        this.antiSwim = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("anti-swim-mode")).description("Starts mining your head block when the enemy starts mining your feet")).defaultValue(AntiSwimMode.OnMineAndSwim)).build());
        this.antiSurroundMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("anti-surround-mode")).description("Places crystals in places to prevent surround")).defaultValue(AntiSurroundMode.Auto)).build());
        this.pauseEatCrystals = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat-crystals")).description("Pauses crystal placement while eating.")).defaultValue(true)).visible(() -> this.antiSurroundMode.get() != AntiSurroundMode.None)).build());
        this.antiSurroundInnerSnap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-surround-inner-snap")).description("Instantly snaps the camera when it needs to for inner place")).defaultValue(true)).visible(() -> this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Inner)).build());
        this.antiSurroundOuterSnap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-surround-outer-snap")).description("Instantly snaps the camera when it needs to for outer place")).defaultValue(true)).visible(() -> this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Outer)).build());
        this.antiSurroundOuterCooldown = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("anti-surround-outer-cooldown")).description("Time to wait between placing crystals")).defaultValue(0.1).min(0.0).sliderMax(1.0).visible(() -> this.antiSurroundMode.get() == AntiSurroundMode.Auto || this.antiSurroundMode.get() == AntiSurroundMode.Outer)).build());
        this.renderDebugScores = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-debug-scores")).description("Renders scores and their blocks.")).defaultValue(false)).build());
        this.silentMine = null;
        this.targetPlayer = null;
        this.target1 = null;
        this.target2 = null;
        this.ignorePos = null;
        this.lastOuterPlaceTime = 0L;
        this.isTerrainFight = false;
        this.silentMine = Modules.get().get(SilentMine.class);
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (this.silentMine == null) {
            this.silentMine = Modules.get().get(SilentMine.class);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.silentMine == null) {
            this.silentMine = Modules.get().get(SilentMine.class);
        }
    }

    @EventHandler
    private void onSilentMineFinishedPre(SilentMineFinishedEvent.Pre event) {
        this.handleMineFinished(event.getBlockPos());
    }

    @EventHandler
    private void onSilentMineFinishedPost(SilentMineFinishedEvent.Post event) {
        this.handleMineFinished(event.getBlockPos());
    }

    private void handleMineFinished(BlockPos minedBlockPos) {
        BlockPos playerSurroundBlock;
        if (this.targetPlayer == null) {
            return;
        }
        if (this.pauseEatCrystals.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        BlockState targetFeetState = this.mc.world.getBlockState(this.targetPlayer.getBlockPos());
        if (targetFeetState.isOf(Blocks.BEDROCK)) {
            return;
        }
        AntiSurroundMode mode = this.antiSurroundMode.get();
        if (mode == AntiSurroundMode.None) {
            return;
        }
        if (mode == AntiSurroundMode.Auto || mode == AntiSurroundMode.Outer) {
            for (Direction dir : Direction.HORIZONTAL) {
                boolean outerSpeedCheck;
                playerSurroundBlock = this.targetPlayer.getBlockPos().offset(dir);
                if (!minedBlockPos.equals((Object)playerSurroundBlock)) continue;
                boolean bl = outerSpeedCheck = (double)(System.currentTimeMillis() - this.lastOuterPlaceTime) > this.antiSurroundOuterCooldown.get() * 1000.0;
                if (!outerSpeedCheck) {
                    return;
                }
                Box blockHitbox = new Box(playerSurroundBlock);
                for (Direction d : Direction.values()) {
                    Box crystalPlaceHitbox;
                    BlockState downState;
                    BlockPos candidatePos = minedBlockPos.offset(d);
                    if (!this.mc.world.isAir(candidatePos) || !(downState = this.mc.world.getBlockState(candidatePos.down())).isOf(Blocks.OBSIDIAN) && !downState.isOf(Blocks.BEDROCK) || EntityUtils.intersectsWithEntity(crystalPlaceHitbox = new Box((double)candidatePos.getX(), (double)candidatePos.getY(), (double)candidatePos.getZ(), (double)(candidatePos.getX() + 1), (double)(candidatePos.getY() + 2), (double)(candidatePos.getZ() + 1)), entity -> !entity.isSpectator())) continue;
                    Vec3d crystalPos = new Vec3d((double)candidatePos.getX() + 0.5, (double)candidatePos.getY(), (double)candidatePos.getZ() + 0.5);
                    Box crystalHitbox = new Box(crystalPos.x - 1.0, crystalPos.y, crystalPos.z - 1.0, crystalPos.x + 1.0, crystalPos.y + 2.0, crystalPos.z + 1.0);
                    if (!crystalHitbox.intersects(blockHitbox)) continue;
                    Modules.get().get(AutoCrystal.class).preplaceCrystal(candidatePos, this.antiSurroundOuterSnap.get());
                    this.lastOuterPlaceTime = System.currentTimeMillis();
                    return;
                }
            }
        }
        if (mode == AntiSurroundMode.Auto || mode == AntiSurroundMode.Inner) {
            for (Direction dir : Direction.HORIZONTAL) {
                playerSurroundBlock = this.targetPlayer.getBlockPos().offset(dir);
                if (!playerSurroundBlock.equals((Object)minedBlockPos)) continue;
                Modules.get().get(AutoCrystal.class).preplaceCrystal(playerSurroundBlock, this.antiSurroundInnerSnap.get());
            }
        }
    }

    private void update() {
        BreakIndicators breakIndicators;
        if (this.silentMine == null) {
            this.silentMine = Modules.get().get(SilentMine.class);
        }
        BlockState selfFeetBlock = this.mc.world.getBlockState(this.mc.player.getBlockPos());
        BlockState selfHeadBlock = this.mc.world.getBlockState(this.mc.player.getBlockPos().up());
        boolean shouldBreakSelfHeadBlock = BlockUtils.canBreak(this.mc.player.getBlockPos().up(), selfHeadBlock) && (selfHeadBlock.isOf(Blocks.OBSIDIAN) || selfHeadBlock.isOf(Blocks.CRYING_OBSIDIAN));
        boolean prioHead = false;
        if (this.antiSwim.get() == AntiSwimMode.Always && shouldBreakSelfHeadBlock) {
            this.silentMine.silentBreakBlock(this.mc.player.getBlockPos().up(), 10.0);
            prioHead = true;
        }
        if (this.antiSwim.get() == AntiSwimMode.OnMineAndSwim && this.mc.player.isCrawling() && shouldBreakSelfHeadBlock) {
            this.silentMine.silentBreakBlock(this.mc.player.getBlockPos().up(), 30.0);
            prioHead = true;
        }
        if ((this.antiSwim.get() == AntiSwimMode.OnMine || this.antiSwim.get() == AntiSwimMode.OnMineAndSwim) && (breakIndicators = Modules.get().get(BreakIndicators.class)).isBlockBeingBroken(this.mc.player.getBlockPos()) && shouldBreakSelfHeadBlock) {
            this.silentMine.silentBreakBlock(this.mc.player.getBlockPos().up(), 20.0);
            prioHead = true;
        }
        this.targetPlayer = (PlayerEntity)TargetUtils.get(entity -> {
            if (entity.equals((Object)this.mc.player) || entity.equals((Object)this.mc.cameraEntity)) {
                return false;
            }
            if (!(entity instanceof PlayerEntity)) {
                return false;
            }
            PlayerEntity player = (PlayerEntity)entity;
            if (!player.isAlive() || player.isDead()) {
                return false;
            }
            if (player.isCreative()) {
                return false;
            }
            if (!Friends.get().shouldAttack(player)) {
                return false;
            }
            if (entity.getPos().distanceTo(this.mc.player.getEyePos()) > this.range.get()) {
                return false;
            }
            return this.ignoreNakeds.get() == false || !((ItemStack)player.getInventory().armor.get(0)).isEmpty() || !((ItemStack)player.getInventory().armor.get(1)).isEmpty() || !((ItemStack)player.getInventory().armor.get(2)).isEmpty() || !((ItemStack)player.getInventory().armor.get(3)).isEmpty();
        }, this.targetPriority.get());
        if (this.targetPlayer == null) {
            return;
        }
        if (this.silentMine.hasDelayedDestroy() && selfHeadBlock.getBlock().equals(Blocks.OBSIDIAN) && selfFeetBlock.isAir() && this.silentMine.getRebreakBlockPos() == this.mc.player.getBlockPos().up()) {
            return;
        }
        if (prioHead) {
            return;
        }
        this.findTargetBlocks();
        int blocksToSend = 0;
        if (!this.silentMine.hasDelayedDestroy()) {
            ++blocksToSend;
        }
        if (!this.silentMine.hasRebreakBlock() || this.silentMine.canRebreakRebreakBlock()) {
            ++blocksToSend;
        }
        if (blocksToSend == 0) {
            return;
        }
        LinkedList<BlockPos> targetBlocks = new LinkedList<BlockPos>();
        if (this.target1 != null && !this.silentMine.alreadyBreaking(this.target1.blockPos)) {
            targetBlocks.add(this.target1.blockPos);
        }
        if (this.target2 != null && !this.silentMine.alreadyBreaking(this.target2.blockPos)) {
            targetBlocks.add(this.target2.blockPos);
        }
        for (int sent = 0; !targetBlocks.isEmpty() && sent < blocksToSend; ++sent) {
            this.silentMine.silentBreakBlock((BlockPos)targetBlocks.remove(), 20.0);
        }
    }

    private void findTargetBlocks() {
        if (this.silentMine != null) {
            this.silentMine.stopRebreakPromotion = false;
        }
        CityBlock bestResult = this.findCityBlock(null);
        this.target1 = null;
        this.target2 = null;
        this.ignorePos = null;
        if (bestResult != null) {
            if (bestResult.type == CheckPosType.TerrainBase) {
                this.target1 = bestResult;
                this.ignorePos = this.target1.blockPos;
                this.target2 = new CityBlock(this);
                this.target2.blockPos = bestResult.blockPos.up();
                this.target2.score = bestResult.score - 10.0;
                this.target2.type = CheckPosType.Surround;
            } else {
                this.target1 = bestResult;
                this.ignorePos = this.target1.blockPos;
                this.target2 = this.findCityBlock(this.target1 != null ? this.target1.blockPos : null);
            }
        }
    }

    private List<BlockPos> getEntityFeetBlocks(PlayerEntity player) {
        ArrayList<BlockPos> feetBlocks = new ArrayList<BlockPos>();
        Box bb = player.getBoundingBox().shrink(0.01, 0.0, 0.01);
        int minX = (int)Math.floor(bb.minX);
        int maxX = (int)Math.floor(bb.maxX);
        int minZ = (int)Math.floor(bb.minZ);
        int maxZ = (int)Math.floor(bb.maxZ);
        int y = player.getBlockPos().getY();
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                feetBlocks.add(new BlockPos(x, y, z));
            }
        }
        return feetBlocks;
    }

    private CityBlock findCityBlock(BlockPos exclude) {
        if (this.targetPlayer == null) {
            return null;
        }
        boolean set = false;
        CityBlock bestBlock = new CityBlock(this);
        bestBlock.score = -1000.0;
        HashSet<CheckPos> checkPos = new HashSet<CheckPos>();
        List<BlockPos> feetBlocks = this.getEntityFeetBlocks(this.targetPlayer);
        boolean inBedrock = false;
        for (BlockPos feet : feetBlocks) {
            if (this.mc.world.getBlockState(feet).getBlock() != Blocks.BEDROCK) continue;
            inBedrock = true;
            break;
        }
        this.isTerrainFight = false;
        if (!inBedrock) {
            boolean hasAnyValidCrystalBase = false;
            HashSet<BlockPos> surroundCheck = new HashSet<BlockPos>();
            for (BlockPos feet : feetBlocks) {
                for (Direction dir2 : Direction.Type.HORIZONTAL) {
                    BlockPos neighbor = feet.offset(dir2);
                    if (feetBlocks.contains(neighbor)) continue;
                    surroundCheck.add(neighbor);
                }
            }
            for (BlockPos s : surroundCheck) {
                if (!this.isCrystalBlock(s.down())) continue;
                hasAnyValidCrystalBase = true;
                break;
            }
            boolean bl = this.isTerrainFight = !hasAnyValidCrystalBase;
        }
        if (inBedrock) {
            this.addBedrockCaseCheckPositions(checkPos, feetBlocks);
        } else {
            this.addNormalCaseCheckPositions(checkPos, feetBlocks);
        }
        for (CheckPos pos : checkPos) {
            BlockPos blockPos = pos.blockPos;
            if (blockPos.equals((Object)exclude)) continue;
            BlockState block = this.mc.world.getBlockState(blockPos);
            boolean isPosGoodRebreak = false;
            if (this.silentMine.canRebreakRebreakBlock() && blockPos.equals((Object)this.silentMine.getRebreakBlockPos())) {
                if (inBedrock) {
                    isPosGoodRebreak = true;
                } else {
                    boolean bl = isPosGoodRebreak = !blockPos.equals((Object)this.targetPlayer.getBlockPos()) && !feetBlocks.contains(blockPos) && Arrays.stream(Direction.HORIZONTAL).anyMatch(dir -> this.targetPlayer.getBlockPos().offset(dir).equals((Object)blockPos) && this.isCrystalBlock(this.targetPlayer.getBlockPos().offset(dir).down()));
                }
            }
            if (block.isAir() && !isPosGoodRebreak) continue;
            boolean isFeetBlock = feetBlocks.contains(blockPos);
            if (!BlockUtils.canBreak(blockPos, block) && !isPosGoodRebreak || !this.silentMine.inBreakRange(blockPos)) continue;
            double score = 0.0;
            score = inBedrock ? this.scoreBedrockCityBlock(pos) : this.scoreNormalCityBlock(pos);
            if (score == -1000.0) continue;
            if (isPosGoodRebreak) {
                AutoCrystal autoCrystal;
                double potentialDamage;
                boolean validForDamage = true;
                if (!inBedrock && (potentialDamage = (autoCrystal = Modules.get().get(AutoCrystal.class)).getDamageForPos(blockPos)) < 5.0) {
                    validForDamage = false;
                }
                if (validForDamage) {
                    if (this.silentMine.canRebreakRebreakBlock() && blockPos.equals((Object)this.silentMine.getRebreakBlockPos())) {
                        this.silentMine.stopRebreakPromotion = true;
                    }
                    score = inBedrock && block.isAir() ? (score += 10.0) : (score += 40.0);
                }
            } else {
                score -= this.getScorePenaltyForSync(pos.blockPos);
            }
            if (!(score > bestBlock.score)) continue;
            bestBlock.score = score;
            bestBlock.blockPos = blockPos;
            bestBlock.isFeetBlock = isFeetBlock;
            bestBlock.type = pos.type;
            set = true;
        }
        if (set) {
            return bestBlock;
        }
        return null;
    }

    private void addNormalCaseCheckPositions(Set<CheckPos> checkPos, List<BlockPos> feetBlocks) {
        for (BlockPos pos : feetBlocks) {
            checkPos.add(new CheckPos(this, pos, CheckPosType.Feet));
        }
        for (BlockPos pos : feetBlocks) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos surround = pos.offset(dir);
                if (feetBlocks.contains(surround)) continue;
                checkPos.add(new CheckPos(this, surround, CheckPosType.Surround));
                BlockPos base = surround.down();
                if (BlockUtils.canBreak(base, this.mc.world.getBlockState(base))) {
                    checkPos.add(new CheckPos(this, base, CheckPosType.TerrainBase));
                }
                switch (this.extendBreakMode.get().ordinal()) {
                    case 0: {
                        break;
                    }
                    case 1: {
                        checkPos.add(new CheckPos(this, surround.offset(dir), CheckPosType.Extend));
                        break;
                    }
                    case 2: {
                        Direction perpDir = this.getCornerPerpDir(dir);
                        if (perpDir == null) break;
                        checkPos.add(new CheckPos(this, surround.offset(perpDir), CheckPosType.Extend));
                    }
                }
            }
        }
    }

    private Direction getCornerPerpDir(Direction dir) {
        if (dir == Direction.NORTH) {
            return Direction.EAST;
        }
        if (dir == Direction.SOUTH) {
            return Direction.WEST;
        }
        if (dir == Direction.EAST) {
            return Direction.NORTH;
        }
        if (dir == Direction.WEST) {
            return Direction.SOUTH;
        }
        return null;
    }

    private void addBedrockCaseCheckPositions(Set<CheckPos> checkPos, List<BlockPos> feetBlocks) {
        boolean isCrawling = this.targetPlayer.isCrawling();
        for (BlockPos pos : feetBlocks) {
            BlockPos headPos = isCrawling ? pos : pos.up();
            BlockPos aboveHeadPos = headPos.up();
            boolean headMineable = BlockUtils.canBreak(headPos, this.mc.world.getBlockState(headPos));
            boolean aboveHeadMineable = BlockUtils.canBreak(aboveHeadPos, this.mc.world.getBlockState(aboveHeadPos));
            if (headMineable) {
                checkPos.add(new CheckPos(this, headPos, CheckPosType.FacePlace));
            } else if (aboveHeadMineable) {
                checkPos.add(new CheckPos(this, aboveHeadPos, CheckPosType.Head));
            }
            if (this.mc.world.getBlockState(pos.down()).getBlock() != Blocks.BEDROCK) {
                BlockPos center = pos.down();
                boolean plusNotFinished = false;
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockState state;
                    BlockPos surround = center.offset(dir);
                    if (!BlockUtils.canBreak(surround, state = this.mc.world.getBlockState(surround))) continue;
                    checkPos.add(new CheckPos(this, surround, CheckPosType.Below));
                    plusNotFinished = true;
                }
                if (!plusNotFinished && BlockUtils.canBreak(center, this.mc.world.getBlockState(center))) {
                    checkPos.add(new CheckPos(this, center, CheckPosType.Below));
                }
            }
            checkPos.add(new CheckPos(this, pos, CheckPosType.Surround));
            for (Direction dir : Direction.Type.HORIZONTAL) {
                if (feetBlocks.contains(pos.offset(dir))) continue;
                checkPos.add(new CheckPos(this, pos.offset(dir), CheckPosType.Surround));
            }
        }
    }

    private double scoreNormalCityBlock(CheckPos pos) {
        Vec3d toBlock;
        Vec3d moveDir;
        double dot;
        BlockPos blockPos = pos.blockPos;
        double score = 0.0;
        BlockState block = this.mc.world.getBlockState(blockPos);
        if (pos.type == CheckPosType.Feet) {
            BlockState headBlock = this.mc.world.getBlockState(blockPos.up());
            if (headBlock.getBlock().equals(Blocks.OBSIDIAN)) {
                score += 100.0;
            } else {
                if (block.getBlock() == Blocks.COBWEB) {
                    return -1000.0;
                }
                score += 50.0;
            }
        } else {
            BlockState selfHeadState = this.mc.world.getBlockState(this.mc.player.getBlockPos().up());
            if (blockPos.equals((Object)this.mc.player.getBlockPos()) && (selfHeadState.getBlock().equals(Blocks.OBSIDIAN) || selfHeadState.getBlock().equals(Blocks.BEDROCK))) {
                return -1000.0;
            }
            if (pos.type == CheckPosType.Surround) {
                if (this.mc.world.getBlockState(blockPos.down()).isAir()) {
                    score += 35.0;
                } else if (!this.isCrystalBlock(blockPos.down())) {
                    score += 10.0;
                } else {
                    score += 55.0;
                    boolean isPosAntiSurround = false;
                    for (Direction dir : Direction.Type.HORIZONTAL) {
                        BlockPos antiSurroundBlockPos;
                        if (!(this.targetPlayer.squaredDistanceTo(blockPos.toCenterPos()) < 4.0) || !this.getBlockStateIgnore(antiSurroundBlockPos = blockPos.offset(dir)).isAir() || !this.isCrystalBlock(antiSurroundBlockPos.down())) continue;
                        isPosAntiSurround = true;
                        break;
                    }
                    if (isPosAntiSurround) {
                        score += 25.0;
                    }
                }
            }
            if (pos.type == CheckPosType.Extend) {
                score += 20.0;
            }
            if (pos.type == CheckPosType.TerrainBase) {
                score = this.isTerrainFight ? (score += 30.0) : (score += 1.0);
            }
        }
        double d = this.targetPlayer.getPos().distanceTo(Vec3d.ofCenter((Vec3i)blockPos));
        score += 10.0 / d;
        Vec3d velocity = this.targetPlayer.getVelocity();
        if (velocity.horizontalLengthSquared() > 0.001 && (dot = (moveDir = new Vec3d(velocity.x, 0.0, velocity.z).normalize()).dotProduct(toBlock = Vec3d.ofCenter((Vec3i)blockPos).subtract(this.targetPlayer.getPos()).normalize())) > 0.0) {
            score += dot * 5.0;
        }
        return score;
    }

    private double scoreBedrockCityBlock(CheckPos pos) {
        BlockPos blockPos = pos.blockPos;
        double score = 0.0;
        if (blockPos.getY() == this.targetPlayer.getBlockY() + 2 || blockPos.getY() == this.targetPlayer.getBlockY() - 1) {
            score += 10.0;
        }
        Box boundingBox = this.targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = this.targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        if (BlockPos.stream((Box)feetBox).count() == 1L) {
            boolean canMineFaceBlock;
            boolean bl = canMineFaceBlock = this.mc.world.getBlockState(this.targetPlayer.getBlockPos().up()).getBlock() != Blocks.BEDROCK;
            if (canMineFaceBlock) {
                if (blockPos.equals((Object)this.targetPlayer.getBlockPos().up())) {
                    score += 20.0;
                } else {
                    boolean isSelfTrapBlock = false;
                    for (Direction dir : Direction.HORIZONTAL) {
                        if (!this.targetPlayer.getBlockPos().up().offset(dir).equals((Object)blockPos)) continue;
                        isSelfTrapBlock = true;
                        break;
                    }
                    if (isSelfTrapBlock) {
                        score += 7.5;
                    }
                }
            }
        }
        if (pos.type == CheckPosType.Below) {
            score = blockPos.equals((Object)this.targetPlayer.getBlockPos().down()) ? (score += 25.0) : (score += 30.0);
        }
        double d = this.targetPlayer.getPos().distanceTo(Vec3d.ofCenter((Vec3i)blockPos));
        return score += 10.0 / d;
    }

    private boolean isBlockInFeet(BlockPos blockPos) {
        Box boundingBox = this.targetPlayer.getBoundingBox().shrink(0.01, 0.1, 0.01);
        double feetY = this.targetPlayer.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        for (BlockPos pos : BlockPos.iterate((int)((int)Math.floor(feetBox.minX)), (int)((int)Math.floor(feetBox.minY)), (int)((int)Math.floor(feetBox.minZ)), (int)((int)Math.floor(feetBox.maxX)), (int)((int)Math.floor(feetBox.maxY)), (int)((int)Math.floor(feetBox.maxZ)))) {
            if (!blockPos.equals((Object)pos)) continue;
            return true;
        }
        return false;
    }

    private boolean isCrystalBlock(BlockPos blockPos) {
        BlockState blockState = this.mc.world.getBlockState(blockPos);
        return blockState.isOf(Blocks.OBSIDIAN) || blockState.isOf(Blocks.BEDROCK);
    }

    public boolean isTargetedPos(BlockPos blockPos) {
        return this.target1 != null && this.target1.blockPos.equals((Object)blockPos) || this.target2 != null && this.target2.blockPos.equals((Object)blockPos);
    }

    private BlockState getBlockStateIgnore(BlockPos blockPos) {
        if (blockPos == null) {
            return null;
        }
        if (blockPos.equals((Object)this.ignorePos)) {
            return Blocks.AIR.getDefaultState();
        }
        return this.mc.world.getBlockState(blockPos);
    }

    private double getScorePenaltyForSync(BlockPos blockPos) {
        BreakIndicators breakIndicators = Modules.get().get(BreakIndicators.class);
        if (breakIndicators.isBeingDoublemined(blockPos)) {
            return 12.0;
        }
        return 0.0;
    }

    public boolean isTargetingAnything() {
        return this.target1 != null && this.target2 != null;
    }

    private void render3d(Render3DEvent event) {
        if (this.targetPlayer == null) {
            return;
        }
        if (this.renderDebugScores.get().booleanValue()) {
            // empty if block
        }
    }

    @EventHandler
    private void onRender2d(Render2DEvent event) {
        if (this.targetPlayer == null) {
            return;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.silentMine.fromAutomine = true;
        this.update();
        this.render3d(event);
        this.silentMine.fromAutomine = false;
    }

    @Override
    public String getInfoString() {
        if (this.targetPlayer == null) {
            return null;
        }
        return String.format("%s", EntityUtils.getName((Entity)this.targetPlayer));
    }

    private static enum ExtendBreakMode {
        None,
        Long,
        Corner;

    }

    private static enum AntiSwimMode {
        None,
        Always,
        OnMine,
        OnMineAndSwim;

    }

    private static enum AntiSurroundMode {
        None,
        Inner,
        Outer,
        Auto;

    }

    private class CityBlock {
        public BlockPos blockPos;
        public double score;
        public boolean isFeetBlock = false;
        public CheckPosType type;

        private CityBlock(AutoMine autoMine) {
        }
    }

    public static enum CheckPosType {
        Feet,
        Surround,
        Extend,
        FacePlace,
        Head,
        Below,
        TerrainBase;

    }

    private class CheckPos {
        public final BlockPos blockPos;
        public final CheckPosType type;

        public CheckPos(AutoMine autoMine, BlockPos blockPos, CheckPosType type) {
            this.blockPos = blockPos;
            this.type = type;
        }

        public int hashCode() {
            return this.blockPos.hashCode();
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            CheckPos checkPos = (CheckPos)o;
            return this.blockPos.equals((Object)checkPos.blockPos);
        }
    }
}

