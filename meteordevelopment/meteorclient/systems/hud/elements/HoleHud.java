/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.WorldRendererAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class HoleHud
extends HudElement {
    public static final HudElementInfo<HoleHud> INFO = new HudElementInfo<HoleHud>(Hud.GROUP, "hole", "Displays information about the hole you are standing in.", HoleHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBackground;
    public final Setting<List<Block>> safe;
    private final Setting<Double> scale;
    private final Setting<Integer> border;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final Color BG_COLOR;
    private final Color OL_COLOR;

    public HoleHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBackground = this.settings.createGroup("Background");
        this.safe = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("safe-blocks")).description("Which blocks to consider safe.")).defaultValue(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.CRYING_OBSIDIAN, Blocks.NETHERITE_BLOCK).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale.")).defaultValue(2.0).onChanged(aDouble -> this.calculateSize())).min(1.0).sliderRange(1.0, 5.0).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(0)).onChanged(integer -> this.calculateSize())).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.BG_COLOR = new Color(255, 25, 25, 100);
        this.OL_COLOR = new Color(255, 25, 25, 255);
        this.calculateSize();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    private void calculateSize() {
        this.setSize(48.0 * this.scale.get(), 48.0 * this.scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            double x = this.x + this.border.get();
            double y = this.y + this.border.get();
            this.drawBlock(renderer, this.get(Facing.Left), x, y + 16.0 * this.scale.get());
            this.drawBlock(renderer, this.get(Facing.Front), x + 16.0 * this.scale.get(), y);
            this.drawBlock(renderer, this.get(Facing.Right), x + 32.0 * this.scale.get(), y + 16.0 * this.scale.get());
            this.drawBlock(renderer, this.get(Facing.Back), x + 16.0 * this.scale.get(), y + 32.0 * this.scale.get());
        });
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    private Direction get(Facing dir) {
        if (this.isInEditor()) {
            return Direction.DOWN;
        }
        return Direction.fromRotation((double)MathHelper.wrapDegrees((float)(MeteorClient.mc.player.getYaw() + (float)dir.offset)));
    }

    private void drawBlock(HudRenderer renderer, Direction dir, double x, double y) {
        Block block;
        Block block2 = block = dir == Direction.DOWN ? Blocks.OBSIDIAN : MeteorClient.mc.world.getBlockState(MeteorClient.mc.player.getBlockPos().offset(dir)).getBlock();
        if (!this.safe.get().contains(block)) {
            return;
        }
        renderer.item(block.asItem().getDefaultStack(), (int)x, (int)y, this.scale.get().floatValue(), false);
        if (dir == Direction.DOWN) {
            return;
        }
        ((WorldRendererAccessor)MeteorClient.mc.worldRenderer).getBlockBreakingInfos().values().forEach(info -> {
            if (info.getPos().equals((Object)MeteorClient.mc.player.getBlockPos().offset(dir))) {
                this.renderBreaking(renderer, x, y, (float)info.getStage() / 9.0f);
            }
        });
    }

    private void renderBreaking(HudRenderer renderer, double x, double y, double percent) {
        renderer.quad(x, y, 16.0 * percent * this.scale.get(), 16.0 * this.scale.get(), this.BG_COLOR);
        renderer.quad(x, y, 16.0 * this.scale.get(), 1.0 * this.scale.get(), this.OL_COLOR);
        renderer.quad(x, y + 15.0 * this.scale.get(), 16.0 * this.scale.get(), 1.0 * this.scale.get(), this.OL_COLOR);
        renderer.quad(x, y, 1.0 * this.scale.get(), 16.0 * this.scale.get(), this.OL_COLOR);
        renderer.quad(x + 15.0 * this.scale.get(), y, 1.0 * this.scale.get(), 16.0 * this.scale.get(), this.OL_COLOR);
    }

    private static enum Facing {
        Left(-90),
        Right(90),
        Front(0),
        Back(180);

        public final int offset;

        private Facing(int offset) {
            this.offset = offset;
        }
    }
}

