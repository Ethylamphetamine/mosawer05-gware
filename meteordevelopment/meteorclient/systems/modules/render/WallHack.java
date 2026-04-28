/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.irisshaders.iris.api.v0.IrisApi
 *  net.minecraft.block.Block
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import meteordevelopment.meteorclient.MixinPlugin;
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
import meteordevelopment.orbit.EventHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.block.Block;

public class WallHack
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Integer> opacity;
    public final Setting<List<Block>> blocks;
    public final Setting<Boolean> occludeChunks;

    public WallHack() {
        super(Categories.Render, "wall-hack", "Makes blocks translucent.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.opacity = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("opacity")).description("The opacity for rendered blocks.")).defaultValue(0)).range(0, 255).sliderMax(255).onChanged(onChanged -> {
            if (this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("What blocks should be targeted for Wall Hack.")).defaultValue(new Block[0]).onChanged(onChanged -> {
            if (this.isActive()) {
                this.mc.worldRenderer.reload();
            }
        })).build());
        this.occludeChunks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("occlude-chunks")).description("Whether caves should occlude underground (may look wonky when on).")).defaultValue(false)).build());
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
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        if (!this.occludeChunks.get().booleanValue()) {
            event.cancel();
        }
    }
}

