/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
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
import net.minecraft.util.math.MathHelper;

public class BlockLimiterHud
extends HudElement {
    public static final HudElementInfo<BlockLimiterHud> INFO = new HudElementInfo<BlockLimiterHud>(Hud.GROUP, "block-limiter-hud", "Visualizes the 9-block burst limit.", BlockLimiterHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgStyle;
    private final Setting<Double> scale;
    private final Setting<Integer> historyLength;
    private final Setting<Integer> border;
    private final Setting<SettingColor> activeColor;
    private final Setting<SettingColor> usedColor;
    private final Setting<SettingColor> historyColor;
    private final Setting<SettingColor> historyBgColor;
    private final Setting<SettingColor> barColor;
    private final Setting<SettingColor> backgroundColor;
    private final Setting<Boolean> background;
    private static final double BOX_WIDTH_BASE = 6.0;
    private static final double BOX_HEIGHT_BASE = 4.0;
    private static final double GAP_BASE = 1.0;
    private static final double BAR_HEIGHT_BASE = 2.0;
    private static final double BAR_GAP_BASE = 2.0;
    private static final double HISTORY_GAP_BASE = 2.0;

    public BlockLimiterHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgStyle = this.settings.createGroup("Style");
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the hud element.")).defaultValue(2.0).min(1.0).sliderMax(10.0).build());
        this.historyLength = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("history-length")).description("How many past bursts to show.")).defaultValue(5)).range(0, 5).sliderMax(10).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(2)).build());
        this.activeColor = this.sgStyle.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ready-color")).description("Color of available placement tokens.")).defaultValue(new SettingColor(0, 255, 110, 255)).build());
        this.usedColor = this.sgStyle.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("used-color")).description("Color of used/empty tokens.")).defaultValue(new SettingColor(40, 40, 40, 150)).build());
        this.historyColor = this.sgStyle.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("history-color")).description("Color of past burst bars.")).defaultValue(new SettingColor(0, 200, 255, 150)).build());
        this.historyBgColor = this.sgStyle.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("history-bg-color")).description("Background color for missing packets in history.")).defaultValue(new SettingColor(30, 30, 30, 100)).build());
        this.barColor = this.sgStyle.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("timer-bar-color")).description("Color of the reload timer bar.")).defaultValue(new SettingColor(0, 150, 255, 255)).build());
        this.backgroundColor = this.sgStyle.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color of the background.")).defaultValue(new SettingColor(20, 20, 20, 100)).build());
        this.background = this.sgStyle.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays the background.")).defaultValue(true)).build());
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    @Override
    public void render(HudRenderer renderer) {
        double s = this.scale.get();
        double boxW = 6.0 * s;
        double boxH = 4.0 * s;
        double gap = 1.0 * s;
        double barH = 2.0 * s;
        double barGap = 2.0 * s;
        double histGap = 2.0 * s;
        int maxTokens = 9;
        int currentTokens = 9;
        double reloadProgress = 1.0;
        Object[] history = new Object[]{};
        if (MeteorClient.mc.player != null) {
            try {
                currentTokens = MeteorClient.BLOCK.getPacketsLeft();
                reloadProgress = MeteorClient.BLOCK.getBurstProgress();
                history = MeteorClient.BLOCK.getBurstHistory();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        currentTokens = MathHelper.clamp((int)currentTokens, (int)0, (int)maxTokens);
        reloadProgress = MathHelper.clamp((double)reloadProgress, (double)0.0, (double)1.0);
        double contentWidth = (double)maxTokens * boxW + (double)(maxTokens - 1) * gap;
        double mainHeight = boxH + barGap + barH;
        int histCount = Math.min(this.historyLength.get(), history.length);
        double historyHeight = (double)histCount * (boxH + histGap);
        if (histCount > 0) {
            historyHeight += histGap;
        }
        double totalHeight = mainHeight + historyHeight;
        this.setSize(contentWidth, totalHeight);
        double startX = this.x + this.border.get();
        double bottomY = (double)(this.y + this.border.get()) + historyHeight;
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
        for (int h = 0; h < histCount; ++h) {
            int count = (Integer)history[h];
            double rowY = bottomY - (double)(h + 1) * (boxH + histGap) - histGap;
            double opacityFactor = 1.0 - (double)h / (double)(histCount + 1);
            Color histFilled = this.historyColor.get().copy().a((int)((double)this.historyColor.get().a * opacityFactor));
            Color histEmpty = this.historyBgColor.get().copy().a((int)((double)this.historyBgColor.get().a * opacityFactor));
            for (int i = 0; i < maxTokens; ++i) {
                double tokenX = startX + (double)i * (boxW + gap);
                renderer.quad(tokenX, rowY, boxW, boxH, i < count ? histFilled : histEmpty);
            }
        }
        for (int i = 0; i < maxTokens; ++i) {
            double tokenX = startX + (double)i * (boxW + gap);
            boolean isActive = i < currentTokens;
            Color color = isActive ? (Color)this.activeColor.get() : (Color)this.usedColor.get();
            renderer.quad(tokenX, bottomY, boxW, boxH, color);
        }
        double barWidth = contentWidth * reloadProgress;
        if (barWidth > 0.0) {
            renderer.quad(startX, bottomY + boxH + barGap, barWidth, barH, this.barColor.get());
        }
    }
}

