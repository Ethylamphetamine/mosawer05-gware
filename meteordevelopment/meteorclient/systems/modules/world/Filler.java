/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.item.Item
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class Filler
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<FillerMode> mode;
    private final Setting<List<Block>> blocks;
    private final Setting<HorizontalDirection> horizontalDirection;
    private final Setting<PlaneDirection> planeDirection;
    private final Setting<Integer> planeValue;
    private final Setting<Integer> planeThickness;
    private final Setting<Double> fadeTime;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final BlockPos.Mutable mutablePos;
    private Map<BlockPos, Long> renderLastPlacedBlock;

    public Filler() {
        super(Categories.World, "filler", "Places blocks to piss of NSO.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("What mode to use.")).defaultValue(FillerMode.Below)).build());
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Which blocks to use.")).defaultValue(Blocks.OBSIDIAN).visible(() -> this.mode.get() != FillerMode.Litematica)).build());
        this.horizontalDirection = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("horizontal-direction")).description("What direction to fill in horizontally.")).defaultValue(HorizontalDirection.East)).visible(() -> this.mode.get() == FillerMode.Horizontal || this.mode.get() == FillerMode.HorizontalSwim)).build());
        this.planeDirection = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("plane-direction")).description("What axis to put the plane on.")).defaultValue(PlaneDirection.X)).visible(() -> this.mode.get() == FillerMode.Plane)).build());
        this.planeValue = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("plane-value")).description("The value for the axis on the plane. Think Direction = X, value = -39 to mean place on X = -39.")).defaultValue(-39)).noSlider().visible(() -> this.mode.get() == FillerMode.Plane)).build());
        this.planeThickness = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("plane-thickness")).description("How thick to build the plane. Useful for building walls.")).min(1).sliderMax(4).defaultValue(1)).visible(() -> this.mode.get() == FillerMode.Plane)).build());
        this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fade-time")).description("How many seconds it takes to fade.")).defaultValue(0.2).min(0.0).sliderMax(1.0).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(85, 0, 255, 40)).visible(() -> this.shapeMode.get() != ShapeMode.Lines)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 60)).visible(() -> this.shapeMode.get() != ShapeMode.Sides)).build());
        this.mutablePos = new BlockPos.Mutable();
        this.renderLastPlacedBlock = new HashMap<BlockPos, Long>();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (!this.mc.player.isSpectator() && this.mode.get() != FillerMode.Litematica) {
            List<BlockPos> placePoses = this.getPoses();
            placePoses.sort((x, y) -> Double.compare(x.getSquaredDistance((Position)this.mc.player.getPos()), y.getSquaredDistance((Position)this.mc.player.getPos())));
            this.placeBlockBatch(placePoses);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        long currentTime = System.currentTimeMillis();
        this.renderLastPlacedBlock.entrySet().removeIf(entry -> {
            if (!((double)(currentTime - (Long)entry.getValue()) > this.fadeTime.get() * 1000.0)) {
                double time = (double)(currentTime - (Long)entry.getValue()) / 1000.0;
                double timeCompletion = time / this.fadeTime.get();
                Color fadedSideColor = this.sideColor.get().copy().a((int)((double)this.sideColor.get().a * (1.0 - timeCompletion)));
                Color fadedLineColor = this.lineColor.get().copy().a((int)((double)this.lineColor.get().a * (1.0 - timeCompletion)));
                event.renderer.box((BlockPos)entry.getKey(), fadedSideColor, fadedLineColor, this.shapeMode.get(), 0);
                return false;
            }
            return true;
        });
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
            for (BlockPos pos : toPlace) {
                if (!MeteorClient.BLOCK.placeBlock(item, pos)) continue;
                this.renderLastPlacedBlock.put(pos.toImmutable(), System.currentTimeMillis());
            }
            MeteorClient.BLOCK.endPlacement();
            return;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    private List<BlockPos> getPoses() {
        ArrayList<BlockPos> placePoses = new ArrayList<BlockPos>();
        if (this.mc.player == null || this.mc.world == null) {
            return placePoses;
        }
        int r = 5;
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        int x = -r;
        block6: while (x <= r) {
            int y = -r;
            while (true) {
                if (y <= r) {
                } else {
                    ++x;
                    continue block6;
                }
                block8: for (int z = -r; z <= r; ++z) {
                    this.mutablePos.set(ex + x, ey + y, ez + z);
                    switch (this.mode.get().ordinal()) {
                        case 0: {
                            if (this.mutablePos.getY() < this.mc.player.getBlockY()) break;
                            continue block8;
                        }
                        case 1: {
                            if (this.directionCheck((BlockPos)this.mutablePos)) break;
                            continue block8;
                        }
                        case 2: {
                            if (this.mutablePos.getY() != this.mc.player.getBlockY() || this.directionCheck((BlockPos)this.mutablePos)) break;
                            continue block8;
                        }
                        case 3: {
                            if (!this.planeCheck((BlockPos)this.mutablePos)) continue block8;
                        }
                    }
                    if (!this.inPlaceRange((BlockPos)this.mutablePos)) continue;
                    placePoses.add(this.mutablePos.toImmutable());
                }
                ++y;
            }
            break;
        }
        return placePoses;
    }

    private boolean directionCheck(BlockPos blockPos) {
        if (this.mc.player == null) {
            return false;
        }
        switch (this.horizontalDirection.get().ordinal()) {
            case 0: {
                if (blockPos.getZ() < this.mc.player.getBlockZ()) break;
                return false;
            }
            case 1: {
                if (blockPos.getZ() > this.mc.player.getBlockZ()) break;
                return false;
            }
            case 2: {
                if (blockPos.getX() > this.mc.player.getBlockX()) break;
                return false;
            }
            case 3: {
                if (blockPos.getX() < this.mc.player.getBlockX()) break;
                return false;
            }
        }
        return true;
    }

    private boolean planeCheck(BlockPos blockPos) {
        int blockValue = 0;
        switch (this.planeDirection.get().ordinal()) {
            case 0: {
                blockValue = blockPos.getX();
                break;
            }
            case 1: {
                blockValue = blockPos.getY();
                break;
            }
            case 2: {
                blockValue = blockPos.getZ();
            }
        }
        return Math.abs(this.planeValue.get() - blockValue) <= this.planeThickness.get() - 1;
    }

    private boolean inPlaceRange(BlockPos blockPos) {
        if (this.mc.player == null) {
            return false;
        }
        Vec3d from = this.mc.player.getEyePos();
        return blockPos.toCenterPos().squaredDistanceTo(from) <= 26.009999999999998;
    }

    private static enum FillerMode {
        Below,
        Horizontal,
        HorizontalSwim,
        Plane,
        Litematica;

    }

    public static enum HorizontalDirection {
        North,
        South,
        East,
        West;

    }

    public static enum PlaneDirection {
        X,
        Y,
        Z;

    }
}

