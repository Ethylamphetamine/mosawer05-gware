/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.api.v0.IrisApi
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.shape.VoxelShapes
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import meteordevelopment.meteorclient.MixinPlugin;
import meteordevelopment.meteorclient.events.render.RenderBlockEntityEvent;
import meteordevelopment.meteorclient.events.world.AmbientOcclusionEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.WallHack;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class Xray
extends Module {
    private final SettingGroup sgGeneral;
    public static final List<Block> ORES = List.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS);
    private final Setting<List<Block>> blocks;
    public final Setting<Integer> opacity;
    private final Setting<Boolean> exposedOnly;

    public Xray() {
        super(Categories.Render, "xray", "Only renders specified blocks. Good for mining.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("Which blocks to show x-rayed.")).defaultValue(ORES)).onChanged(v -> {
            if (this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
        this.opacity = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("opacity")).description("The opacity for all other blocks.")).defaultValue(25)).range(0, 255).sliderMax(255).onChanged(onChanged -> {
            if (this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
        this.exposedOnly = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("exposed-only")).description("Show only exposed ores.")).defaultValue(false)).onChanged(onChanged -> {
            if (this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
    }

    @Override
    public void onActivate() {
        this.mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        this.mc.worldRenderer.reload();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        if (MixinPlugin.isSodiumPresent) {
            return theme.label("Warning: Due to Sodium in use, opacity is overridden to 0.");
        }
        if (MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) {
            return theme.label("Warning: Due to shaders in use, opacity is overridden to 0.");
        }
        return null;
    }

    @EventHandler
    private void onRenderBlockEntity(RenderBlockEntityEvent event) {
        if (this.isBlocked(event.blockEntity.getCachedState().getBlock(), event.blockEntity.getPos())) {
            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onAmbientOcclusion(AmbientOcclusionEvent event) {
        event.lightLevel = 1.0f;
    }

    public boolean modifyDrawSide(BlockState state, BlockView view, BlockPos pos, Direction facing, boolean returns) {
        if (!returns && !this.isBlocked(state.getBlock(), pos)) {
            BlockPos adjPos = pos.offset(facing);
            BlockState adjState = view.getBlockState(adjPos);
            return adjState.getCullingFace(view, adjPos, facing.getOpposite()) != VoxelShapes.fullCube() || adjState.getBlock() != state.getBlock() || BlockUtils.isExposed(adjPos);
        }
        return returns;
    }

    public boolean isBlocked(Block block, BlockPos blockPos) {
        return !this.blocks.get().contains(block) || this.exposedOnly.get() != false && blockPos != null && !BlockUtils.isExposed(blockPos);
    }

    public static int getAlpha(BlockState state, BlockPos pos) {
        WallHack wallHack = Modules.get().get(WallHack.class);
        Xray xray = Modules.get().get(Xray.class);
        if (wallHack.isActive() && wallHack.blocks.get().contains(state.getBlock())) {
            if (MixinPlugin.isSodiumPresent || MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse()) {
                return 0;
            }
            int alpha = xray.isActive() ? xray.opacity.get().intValue() : wallHack.opacity.get().intValue();
            return alpha;
        }
        if (xray.isActive() && !wallHack.isActive() && xray.isBlocked(state.getBlock(), pos)) {
            return MixinPlugin.isSodiumPresent || MixinPlugin.isIrisPresent && IrisApi.getInstance().isShaderPackInUse() ? 0 : xray.opacity.get();
        }
        return -1;
    }
}

