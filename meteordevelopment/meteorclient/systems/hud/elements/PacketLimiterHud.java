/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class PacketLimiterHud
extends HudElement {
    public static final HudElementInfo<PacketLimiterHud> INFO = new HudElementInfo<PacketLimiterHud>(Hud.GROUP, "packet-limiter", "Displays 2b2t packet limiter status.", PacketLimiterHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<Alignment> alignment;
    private final Setting<Integer> border;
    private final Setting<Boolean> shadow;
    private final Setting<SettingColor> labelColor;
    private final Setting<SettingColor> valueColor;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final AntiCheatConfig acConfig;
    private final List<LineData> lines;

    public PacketLimiterHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.alignment = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("alignment")).description("Horizontal alignment.")).defaultValue(Alignment.Auto)).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(0)).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow for the text.")).defaultValue(true)).build());
        this.labelColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("label-color")).description("The color of the packet limit labels.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.valueColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("value-color")).description("The color of the packet limit values.")).defaultValue(new SettingColor(200, 200, 200)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.acConfig = AntiCheatConfig.get();
        this.lines = new ArrayList<LineData>();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    @Override
    protected double alignX(double width, Alignment alignment) {
        return this.box.alignX(this.getWidth() - this.border.get() * 2, width, alignment);
    }

    @Override
    public void tick(HudRenderer renderer) {
        if (!this.acConfig.packetLimiter.get().booleanValue() && !this.isInEditor()) {
            this.setSize(0.0, 0.0);
            this.lines.clear();
            return;
        }
        this.lines.clear();
        this.lines.add(new LineData("Click: ", this.getHudValue("click")));
        this.lines.add(new LineData("Interact: ", this.getHudValue("interact")));
        this.lines.add(new LineData("Global: ", this.getHudValue("global")));
        double width = 0.0;
        double height = 0.0;
        for (LineData line : this.lines) {
            double lineWidth = renderer.textWidth(line.label + line.value, this.shadow.get(), this.getScale());
            width = Math.max(width, lineWidth);
            height += renderer.textHeight(this.shadow.get(), this.getScale());
        }
        this.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (this.lines.isEmpty()) {
            return;
        }
        double x = this.x + this.border.get();
        double y = this.y + this.border.get();
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
        for (LineData line : this.lines) {
            String fullText = line.label + line.value;
            double lineWidth = renderer.textWidth(fullText, this.shadow.get(), this.getScale());
            double lineX = x + this.alignX(lineWidth, this.alignment.get());
            renderer.text(line.label, lineX, y, this.labelColor.get(), this.shadow.get(), this.getScale());
            double labelWidth = renderer.textWidth(line.label, this.shadow.get(), this.getScale());
            renderer.text(line.value, lineX + labelWidth, y, this.valueColor.get(), this.shadow.get(), this.getScale());
            y += renderer.textHeight(this.shadow.get(), this.getScale());
        }
    }

    private String getHudValue(String type) {
        if (this.isInEditor()) {
            switch (type) {
                case "click": {
                    return "10/79";
                }
                case "interact": {
                    return "100/220";
                }
                case "global": {
                    return "123/1249";
                }
            }
            return "0/0";
        }
        switch (type) {
            case "click": {
                return String.format("%d/%d", PacketManager.INSTANCE.getClickPPS(), this.acConfig.clickLimiter.get());
            }
            case "interact": {
                return String.format("%d/%d", PacketManager.INSTANCE.getInteractPPS(), this.acConfig.interactLimiter.get());
            }
            case "global": {
                return String.format("%d/%d", PacketManager.INSTANCE.getGlobalPPS(), this.acConfig.globalLimiter.get());
            }
        }
        return "0/0";
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : -1.0;
    }

    private static class LineData {
        public String label;
        public String value;

        public LineData(String label, String value) {
            this.label = label;
            this.value = value;
        }
    }
}

