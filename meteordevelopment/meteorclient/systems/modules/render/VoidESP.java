/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Direction
 *  net.minecraft.world.chunk.Chunk
 *  net.minecraft.world.chunk.ChunkStatus
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public class VoidESP
extends Module {
    private static final Direction[] SIDES = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<Boolean> airOnly;
    private final Setting<Integer> horizontalRadius;
    private final Setting<Integer> holeHeight;
    private final Setting<Boolean> netherRoof;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final BlockPos.Mutable blockPos;
    private final Pool<Void> voidHolePool;
    private final List<Void> voidHoles;

    public VoidESP() {
        super(Categories.Render, "void-esp", "Renders holes in bedrock layers that lead to the void.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.airOnly = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("air-only")).description("Checks bedrock only for air blocks.")).defaultValue(false)).build());
        this.horizontalRadius = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("horizontal-radius")).description("Horizontal radius in which to search for holes.")).defaultValue(64)).min(0).sliderMax(256).build());
        this.holeHeight = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hole-height")).description("The minimum hole height to be rendered.")).defaultValue(1)).min(1).sliderRange(1, 5).build());
        this.netherRoof = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("nether-roof")).description("Check for holes in nether roof.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("fill-color")).description("The color that fills holes in the void.")).defaultValue(new SettingColor(225, 25, 25, 50)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The color to draw lines of holes to the void.")).defaultValue(new SettingColor(225, 25, 255)).build());
        this.blockPos = new BlockPos.Mutable();
        this.voidHolePool = new Pool<Void>(() -> new Void());
        this.voidHoles = new ArrayList<Void>();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.voidHoles.clear();
        if (PlayerUtils.getDimension() == Dimension.End) {
            return;
        }
        int px = this.mc.player.getBlockPos().getX();
        int pz = this.mc.player.getBlockPos().getZ();
        int radius = this.horizontalRadius.get();
        for (int x = px - radius; x <= px + radius; ++x) {
            for (int z = pz - radius; z <= pz + radius; ++z) {
                this.blockPos.set(x, this.mc.world.getBottomY(), z);
                if (this.isHole(this.blockPos, false)) {
                    this.voidHoles.add(this.voidHolePool.get().set(this.blockPos.set(x, this.mc.world.getBottomY(), z), false));
                }
                if (!this.netherRoof.get().booleanValue() || PlayerUtils.getDimension() != Dimension.Nether) continue;
                this.blockPos.set(x, 127, z);
                if (!this.isHole(this.blockPos, true)) continue;
                this.voidHoles.add(this.voidHolePool.get().set(this.blockPos.set(x, 127, z), true));
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        for (Void voidHole : this.voidHoles) {
            voidHole.render(event);
        }
    }

    private boolean isBlockWrong(BlockPos blockPos) {
        Chunk chunk = this.mc.world.getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk == null) {
            return true;
        }
        Block block = chunk.getBlockState(blockPos).getBlock();
        if (this.airOnly.get().booleanValue()) {
            return block != Blocks.AIR;
        }
        return block == Blocks.BEDROCK;
    }

    private boolean isHole(BlockPos.Mutable blockPos, boolean nether) {
        for (int i = 0; i < this.holeHeight.get(); ++i) {
            blockPos.setY(nether ? 127 - i : this.mc.world.getBottomY());
            if (!this.isBlockWrong((BlockPos)blockPos)) continue;
            return false;
        }
        return true;
    }

    private class Void {
        private int x;
        private int y;
        private int z;
        private int excludeDir;

        private Void() {
        }

        public Void set(BlockPos.Mutable blockPos, boolean nether) {
            this.x = blockPos.getX();
            this.y = blockPos.getY();
            this.z = blockPos.getZ();
            this.excludeDir = 0;
            for (Direction side : SIDES) {
                blockPos.set(this.x + side.getOffsetX(), this.y, this.z + side.getOffsetZ());
                if (!VoidESP.this.isHole(blockPos, nether)) continue;
                this.excludeDir |= Dir.get(side);
            }
            return this;
        }

        public void render(Render3DEvent event) {
            event.renderer.box(this.x, this.y, this.z, this.x + 1, this.y + 1, this.z + 1, VoidESP.this.sideColor.get(), VoidESP.this.lineColor.get(), VoidESP.this.shapeMode.get(), this.excludeDir);
        }
    }
}

