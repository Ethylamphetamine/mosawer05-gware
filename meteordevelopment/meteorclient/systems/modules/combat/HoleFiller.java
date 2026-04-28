/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.TntEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class HoleFiller
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSmart;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<Integer> searchRadius;
    private final Setting<Double> placeRange;
    private final Setting<Boolean> doubles;
    private final Setting<Boolean> rotate;
    private final Setting<Integer> placeDelay;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Boolean> smart;
    public final Setting<Keybind> forceFill;
    private final Setting<Boolean> predict;
    private final Setting<Boolean> ignoreSafe;
    private final Setting<Boolean> onlyMoving;
    private final Setting<Double> targetRange;
    private final Setting<Double> feetRange;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<SettingColor> nextSideColor;
    private final Setting<SettingColor> nextLineColor;
    private final List<PlayerEntity> targets;
    private final List<Hole> holes;
    private final BlockPos.Mutable testPos;
    private final Box box;
    private int timer;

    public HoleFiller() {
        super(Categories.Combat, "hole-filler", "Fills holes with specified blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSmart = this.settings.createGroup("Smart");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Which blocks can be used to fill holes.")).defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.COBWEB).build());
        this.searchRadius = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("search-radius")).description("Horizontal radius in which to search for holes.")).defaultValue(5)).min(0).sliderMax(6).build());
        this.placeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("How far away from the player you can place a block.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.doubles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("doubles")).description("Fills double holes.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically rotates towards the holes being filled.")).defaultValue(false)).build());
        this.placeDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The ticks delay between placement.")).defaultValue(1)).min(0).build());
        this.blocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-tick")).description("How many blocks to place in one tick.")).defaultValue(3)).min(1).build());
        this.smart = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart")).description("Take more factors into account before filling a hole.")).defaultValue(true)).build());
        this.forceFill = this.sgSmart.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("force-fill")).description("Fills all holes around you regardless of target checks.")).defaultValue(Keybind.none())).visible(this.smart::get)).build());
        this.predict = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict")).description("Predict target movement to account for ping.")).defaultValue(true)).visible(this.smart::get)).build());
        this.ignoreSafe = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-safe")).description("Ignore players in safe holes.")).defaultValue(true)).visible(this.smart::get)).build());
        this.onlyMoving = this.sgSmart.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-moving")).description("Ignore players if they're standing still.")).defaultValue(true)).visible(this.smart::get)).build());
        this.targetRange = this.sgSmart.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("How far away to target players.")).defaultValue(7.0).min(0.0).sliderMin(1.0).sliderMax(10.0).visible(this.smart::get)).build());
        this.feetRange = this.sgSmart.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("feet-range")).description("How far from a hole a player's feet must be to fill it.")).defaultValue(1.5).min(0.0).sliderMax(4.0).visible(this.smart::get)).build());
        this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swing the player's hand when placing.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders an overlay where blocks will be placed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the target block rendering.")).defaultValue(new SettingColor(197, 137, 232)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.nextSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("next-side-color")).description("The side color of the next block to be placed.")).defaultValue(new SettingColor(227, 196, 245, 10)).visible(() -> this.render.get() != false && this.shapeMode.get().sides())).build());
        this.nextLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("next-line-color")).description("The line color of the next block to be placed.")).defaultValue(new SettingColor(227, 196, 245)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.targets = new ArrayList<PlayerEntity>();
        this.holes = new ArrayList<Hole>();
        this.testPos = new BlockPos.Mutable();
        this.box = new Box(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.smart.get().booleanValue()) {
            this.setTargets();
        }
        this.holes.clear();
        FindItemResult block = InvUtils.findInHotbar(itemStack -> this.blocks.get().contains(Block.getBlockFromItem((Item)itemStack.getItem())));
        if (!block.found()) {
            return;
        }
        BlockIterator.register(this.searchRadius.get(), this.searchRadius.get(), (blockPos, blockState) -> {
            if (!this.validHole((BlockPos)blockPos)) {
                return;
            }
            int bedrock = 0;
            int obsidian = 0;
            Direction air = null;
            for (Direction direction : Direction.values()) {
                if (direction == Direction.UP) continue;
                BlockState state = this.mc.world.getBlockState(blockPos.offset(direction));
                if (state.getBlock() == Blocks.BEDROCK) {
                    ++bedrock;
                } else if (state.getBlock() == Blocks.OBSIDIAN) {
                    ++obsidian;
                } else {
                    if (direction == Direction.DOWN) {
                        return;
                    }
                    if (this.validHole(blockPos.offset(direction)) && air == null) {
                        for (Direction dir : Direction.values()) {
                            if (dir == direction.getOpposite() || dir == Direction.UP) continue;
                            BlockState blockState1 = this.mc.world.getBlockState(blockPos.offset(direction).offset(dir));
                            if (blockState1.getBlock() == Blocks.BEDROCK) {
                                ++bedrock;
                                continue;
                            }
                            if (blockState1.getBlock() == Blocks.OBSIDIAN) {
                                ++obsidian;
                                continue;
                            }
                            return;
                        }
                        air = direction;
                    }
                }
                if (obsidian + bedrock == 5 && air == null) {
                    this.holes.add(new Hole((BlockPos)blockPos, 0));
                    continue;
                }
                if (obsidian + bedrock != 8 || !this.doubles.get().booleanValue() || air == null) continue;
                this.holes.add(new Hole((BlockPos)blockPos, Dir.get(air)));
            }
        });
        BlockIterator.after(() -> {
            if (this.timer > 0 || this.holes.isEmpty()) {
                return;
            }
            int bpt = 0;
            for (Hole hole : this.holes) {
                if (bpt >= this.blocksPerTick.get() || !BlockUtils.place((BlockPos)hole.blockPos, block, this.rotate.get(), 10, this.swing.get(), true)) continue;
                ++bpt;
            }
            this.timer = this.placeDelay.get();
        });
        --this.timer;
    }

    @EventHandler(priority=100)
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue() || this.holes.isEmpty()) {
            return;
        }
        for (Hole hole : this.holes) {
            boolean isNext = false;
            for (int i = 0; i < this.holes.size(); ++i) {
                if (!this.holes.get(i).equals(hole) || i >= this.blocksPerTick.get()) continue;
                isNext = true;
            }
            Color side = isNext ? (Color)this.nextSideColor.get() : (Color)this.sideColor.get();
            Color line = isNext ? (Color)this.nextLineColor.get() : (Color)this.lineColor.get();
            event.renderer.box((BlockPos)hole.blockPos, side, line, this.shapeMode.get(), (int)hole.exclude);
        }
    }

    private boolean validHole(BlockPos pos) {
        this.testPos.set((Vec3i)pos);
        if (this.mc.player.getBlockPos().equals((Object)this.testPos)) {
            return false;
        }
        if (this.distance((PlayerEntity)this.mc.player, (BlockPos)this.testPos, false) > this.placeRange.get()) {
            return false;
        }
        if (this.mc.world.getBlockState((BlockPos)this.testPos).getBlock() == Blocks.COBWEB) {
            return false;
        }
        if (((AbstractBlockAccessor)this.mc.world.getBlockState((BlockPos)this.testPos).getBlock()).isCollidable()) {
            return false;
        }
        this.testPos.add(0, 1, 0);
        if (((AbstractBlockAccessor)this.mc.world.getBlockState((BlockPos)this.testPos).getBlock()).isCollidable()) {
            return false;
        }
        this.testPos.add(0, -1, 0);
        ((IBox)this.box).set(pos);
        if (!this.mc.world.getOtherEntities(null, this.box, entity -> entity instanceof PlayerEntity || entity instanceof TntEntity || entity instanceof EndCrystalEntity).isEmpty()) {
            return false;
        }
        if (!this.smart.get().booleanValue() || this.forceFill.get().isPressed()) {
            return true;
        }
        return this.targets.stream().anyMatch(target -> target.getY() > (double)this.testPos.getY() && this.distance((PlayerEntity)target, (BlockPos)this.testPos, true) < this.feetRange.get());
    }

    private void setTargets() {
        this.targets.clear();
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (player.squaredDistanceTo((Entity)this.mc.player) > Math.pow(this.targetRange.get(), 2.0) || player.isCreative() || player == this.mc.player || player.isDead() || !Friends.get().shouldAttack(player) || this.ignoreSafe.get().booleanValue() && this.isSurrounded(player) || this.onlyMoving.get().booleanValue() && (player.getX() - player.prevX != 0.0 || player.getY() - player.prevY != 0.0 || player.getZ() - player.prevZ != 0.0)) continue;
            this.targets.add(player);
        }
    }

    private boolean isSurrounded(PlayerEntity target) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            this.testPos.set((Vec3i)target.getBlockPos().offset(dir));
            Block block = this.mc.world.getBlockState((BlockPos)this.testPos).getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK) continue;
            return false;
        }
        return true;
    }

    private double distance(PlayerEntity player, BlockPos pos, boolean feet) {
        Vec3d testVec = player.getPos();
        if (!feet) {
            testVec.add(0.0, (double)player.getEyeHeight(this.mc.player.getPose()), 0.0);
        } else if (this.predict.get().booleanValue()) {
            testVec.add(player.getX() - player.prevX, player.getY() - player.prevY, player.getZ() - player.prevZ);
        }
        double i = testVec.x - ((double)pos.getX() + 0.5);
        double j = testVec.y - ((double)pos.getY() + (feet ? 1.0 : 0.5));
        double k = testVec.z - ((double)pos.getZ() + 0.5);
        return Math.sqrt(i * i + j * j + k * k);
    }

    private static class Hole {
        private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
        private final byte exclude;

        public Hole(BlockPos blockPos, byte exclude) {
            this.blockPos.set((Vec3i)blockPos);
            this.exclude = exclude;
        }
    }
}

