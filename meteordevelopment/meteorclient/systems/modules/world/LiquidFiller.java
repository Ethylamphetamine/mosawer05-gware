/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.fluid.Fluid
 *  net.minecraft.fluid.Fluids
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class LiquidFiller
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgWhitelist;
    private final Setting<PlaceIn> placeInLiquids;
    private final Setting<Shape> shape;
    private final Setting<Double> range;
    private final Setting<Integer> delay;
    private final Setting<Integer> maxBlocksPerTick;
    private final Setting<SortMode> sortMode;
    private final Setting<Boolean> rotate;
    private final Setting<ListMode> listMode;
    private final Setting<List<Block>> whitelist;
    private final Setting<List<Block>> blacklist;
    private final List<BlockPos.Mutable> blocks;
    private int timer;

    public LiquidFiller() {
        super(Categories.World, "liquid-filler", "Places blocks inside of liquid source blocks within range of you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgWhitelist = this.settings.createGroup("Whitelist");
        this.placeInLiquids = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("place-in")).description("What type of liquids to place in.")).defaultValue(PlaceIn.Both)).build());
        this.shape = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape")).description("The shape of placing algorithm.")).defaultValue(Shape.Sphere)).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The place range.")).defaultValue(4.0).min(0.0).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("Delay between actions in ticks.")).defaultValue(0)).min(0).build());
        this.maxBlocksPerTick = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-blocks-per-tick")).description("Maximum blocks to try to place per tick.")).defaultValue(1)).min(1).sliderRange(1, 10).build());
        this.sortMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("sort-mode")).description("The blocks you want to place first.")).defaultValue(SortMode.Closest)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically rotates towards the space targeted for filling.")).defaultValue(true)).build());
        this.listMode = this.sgWhitelist.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("list-mode")).description("Selection mode.")).defaultValue(ListMode.Whitelist)).build());
        this.whitelist = this.sgWhitelist.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("The allowed blocks that it will use to fill up the liquid.")).defaultValue(Blocks.DIRT, Blocks.COBBLESTONE, Blocks.STONE, Blocks.NETHERRACK, Blocks.DIORITE, Blocks.GRANITE, Blocks.ANDESITE).visible(() -> this.listMode.get() == ListMode.Whitelist)).build());
        this.blacklist = this.sgWhitelist.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blacklist")).description("The denied blocks that it not will use to fill up the liquid.")).visible(() -> this.listMode.get() == ListMode.Blacklist)).build());
        this.blocks = new ArrayList<BlockPos.Mutable>();
    }

    @Override
    public void onActivate() {
        this.timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult item;
        if (this.timer < this.delay.get()) {
            ++this.timer;
            return;
        }
        this.timer = 0;
        double pX = this.mc.player.getX();
        double pY = this.mc.player.getY();
        double pZ = this.mc.player.getZ();
        double rangeSq = Math.pow(this.range.get(), 2.0);
        if (this.shape.get() == Shape.UniformCube) {
            this.range.set(Double.valueOf(Math.round(this.range.get())));
        }
        if (!(item = this.listMode.get() == ListMode.Whitelist ? InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem && this.whitelist.get().contains(Block.getBlockFromItem((Item)itemStack.getItem()))) : InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem && !this.blacklist.get().contains(Block.getBlockFromItem((Item)itemStack.getItem())))).found()) {
            return;
        }
        BlockIterator.register((int)Math.ceil(this.range.get() + 1.0), (int)Math.ceil(this.range.get()), (blockPos, blockState) -> {
            boolean toofarUniformCube;
            boolean toofarSphere = Utils.squaredDistance(pX, pY, pZ, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) > rangeSq;
            boolean bl = toofarUniformCube = LiquidFiller.maxDist(Math.floor(pX), Math.floor(pY), Math.floor(pZ), blockPos.getX(), blockPos.getY(), blockPos.getZ()) >= this.range.get();
            if (toofarSphere && this.shape.get() == Shape.Sphere || toofarUniformCube && this.shape.get() == Shape.UniformCube) {
                return;
            }
            Fluid fluid = blockState.getFluidState().getFluid();
            if (this.placeInLiquids.get() == PlaceIn.Both && fluid != Fluids.WATER && fluid != Fluids.LAVA || this.placeInLiquids.get() == PlaceIn.Water && fluid != Fluids.WATER || this.placeInLiquids.get() == PlaceIn.Lava && fluid != Fluids.LAVA) {
                return;
            }
            if (!BlockUtils.canPlace(blockPos)) {
                return;
            }
            this.blocks.add(blockPos.mutableCopy());
        });
        BlockIterator.after(() -> {
            if (this.sortMode.get() == SortMode.TopDown || this.sortMode.get() == SortMode.BottomUp) {
                this.blocks.sort(Comparator.comparingDouble(value -> value.getY() * (this.sortMode.get() == SortMode.BottomUp ? 1 : -1)));
            } else if (this.sortMode.get() != SortMode.None) {
                this.blocks.sort(Comparator.comparingDouble(value -> Utils.squaredDistance(pX, pY, pZ, (double)value.getX() + 0.5, (double)value.getY() + 0.5, (double)value.getZ() + 0.5) * (double)(this.sortMode.get() == SortMode.Closest ? 1 : -1)));
            }
            int count = 0;
            for (BlockPos blockPos : this.blocks) {
                if (count >= this.maxBlocksPerTick.get()) break;
                BlockUtils.place(blockPos, item, this.rotate.get(), 0, true);
                ++count;
            }
            this.blocks.clear();
        });
    }

    private static double maxDist(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = Math.ceil(Math.abs(x2 - x1));
        double dY = Math.ceil(Math.abs(y2 - y1));
        double dZ = Math.ceil(Math.abs(z2 - z1));
        return Math.max(Math.max(dX, dY), dZ);
    }

    public static enum PlaceIn {
        Both,
        Water,
        Lava;

    }

    public static enum Shape {
        Sphere,
        UniformCube;

    }

    public static enum SortMode {
        None,
        Closest,
        Furthest,
        TopDown,
        BottomUp;

    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}

