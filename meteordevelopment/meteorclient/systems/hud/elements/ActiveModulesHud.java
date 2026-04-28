/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ActiveModulesHud
extends HudElement {
    public static final HudElementInfo<ActiveModulesHud> INFO = new HudElementInfo<ActiveModulesHud>(Hud.GROUP, "active-modules", "Displays your active modules.", ActiveModulesHud::new);
    private static final meteordevelopment.meteorclient.utils.render.color.Color WHITE = new meteordevelopment.meteorclient.utils.render.color.Color();
    private final SettingGroup sgGeneral;
    private final Setting<List<Module>> shownModules;
    private final Setting<Sort> sort;
    private final Setting<Boolean> activeInfo;
    private final Setting<SettingColor> moduleInfoColor;
    private final Setting<ColorMode> colorMode;
    private final Setting<SettingColor> flatColor;
    private final Setting<Boolean> shadow;
    private final Setting<Alignment> alignment;
    private final Setting<Boolean> outlines;
    private final Setting<Integer> outlineWidth;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Double> rainbowSpeed;
    private final Setting<Double> rainbowSpread;
    private final Setting<Double> rainbowSaturation;
    private final Setting<Double> rainbowBrightness;
    private final List<Module> modules;
    private final meteordevelopment.meteorclient.utils.render.color.Color rainbow;
    private double rainbowHue1;
    private double rainbowHue2;
    private double prevX;
    private double prevTextLength;
    private meteordevelopment.meteorclient.utils.render.color.Color prevColor;

    public ActiveModulesHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.shownModules = this.sgGeneral.add(((ModuleListSetting.Builder)((ModuleListSetting.Builder)new ModuleListSetting.Builder().name("visible-modules")).description("Which modules to show in the list")).build());
        this.sort = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("sort")).description("How to sort active modules.")).defaultValue(Sort.Biggest)).build());
        this.activeInfo = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("additional-info")).description("Shows additional info from the module next to the name in the active modules list.")).defaultValue(true)).build());
        this.moduleInfoColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("module-info-color")).description("Color of module info text.")).defaultValue(new SettingColor(175, 175, 175)).visible(this.activeInfo::get)).build());
        this.colorMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("color-mode")).description("What color to use for active modules.")).defaultValue(ColorMode.Rainbow)).build());
        this.flatColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("flat-color")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).visible(() -> this.colorMode.get() == ColorMode.Flat)).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow behind text.")).defaultValue(true)).build());
        this.alignment = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("alignment")).description("Horizontal alignment.")).defaultValue(Alignment.Auto)).build());
        this.outlines = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("outlines")).description("Whether or not to render outlines")).defaultValue(false)).build());
        this.outlineWidth = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("outline-width")).description("Outline width")).defaultValue(2)).min(1).sliderMin(1).visible(this.outlines::get)).build());
        this.customScale = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).build());
        this.rainbowSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-speed")).description("Rainbow speed of rainbow color mode.")).defaultValue(0.05).sliderMin(0.01).sliderMax(0.2).decimalPlaces(4).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.rainbowSpread = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-spread")).description("Rainbow spread of rainbow color mode.")).defaultValue(0.01).sliderMin(0.001).sliderMax(0.05).decimalPlaces(4).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.rainbowSaturation = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-saturation")).defaultValue(1.0).sliderRange(0.0, 1.0).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.rainbowBrightness = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-brightness")).defaultValue(1.0).sliderRange(0.0, 1.0).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.modules = new ArrayList<Module>();
        this.rainbow = new meteordevelopment.meteorclient.utils.render.color.Color(255, 255, 255);
        this.prevColor = new meteordevelopment.meteorclient.utils.render.color.Color();
    }

    @Override
    public void tick(HudRenderer renderer) {
        this.modules.clear();
        for (Module module : Modules.get().getActive()) {
            if (!this.shownModules.get().contains(module)) continue;
            this.modules.add(module);
        }
        if (this.modules.isEmpty()) {
            if (this.isInEditor()) {
                this.setSize(renderer.textWidth("Active Modules", this.shadow.get(), this.getScale()), renderer.textHeight(this.shadow.get(), this.getScale()));
            }
            return;
        }
        this.modules.sort((e1, e2) -> switch (this.sort.get().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> e1.title.compareTo(e2.title);
            case 1 -> Double.compare(this.getModuleWidth(renderer, (Module)e2), this.getModuleWidth(renderer, (Module)e1));
            case 2 -> Double.compare(this.getModuleWidth(renderer, (Module)e1), this.getModuleWidth(renderer, (Module)e2));
        });
        double width = 0.0;
        double height = 0.0;
        for (int i = 0; i < this.modules.size(); ++i) {
            Module module = this.modules.get(i);
            width = Math.max(width, this.getModuleWidth(renderer, module));
            height += renderer.textHeight(this.shadow.get(), this.getScale());
            if (i <= 0) continue;
            height += 2.0;
        }
        this.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        if (this.modules.isEmpty()) {
            if (this.isInEditor()) {
                renderer.text("Active Modules", x, y, WHITE, this.shadow.get(), this.getScale());
            }
            return;
        }
        this.rainbowHue1 += this.rainbowSpeed.get() * renderer.delta;
        if (this.rainbowHue1 > 1.0) {
            this.rainbowHue1 -= 1.0;
        } else if (this.rainbowHue1 < -1.0) {
            this.rainbowHue1 += 1.0;
        }
        this.rainbowHue2 = this.rainbowHue1;
        this.prevX = x;
        for (int i = 0; i < this.modules.size(); ++i) {
            double offset = this.alignX(this.getModuleWidth(renderer, this.modules.get(i)), this.alignment.get());
            this.renderModule(renderer, this.modules, i, x + offset, y);
            this.prevX = x + offset;
            y += 2.0 + renderer.textHeight(this.shadow.get(), this.getScale());
        }
    }

    private void renderModule(HudRenderer renderer, List<Module> modules, int index, double x, double y) {
        String info;
        Module module = modules.get(index);
        meteordevelopment.meteorclient.utils.render.color.Color color = this.flatColor.get();
        switch (this.colorMode.get().ordinal()) {
            case 1: {
                color = module.color;
                break;
            }
            case 2: {
                this.rainbowHue2 += this.rainbowSpread.get().doubleValue();
                int c = Color.HSBtoRGB((float)this.rainbowHue2, this.rainbowSaturation.get().floatValue(), this.rainbowBrightness.get().floatValue());
                this.rainbow.r = meteordevelopment.meteorclient.utils.render.color.Color.toRGBAR(c);
                this.rainbow.g = meteordevelopment.meteorclient.utils.render.color.Color.toRGBAG(c);
                this.rainbow.b = meteordevelopment.meteorclient.utils.render.color.Color.toRGBAB(c);
                color = this.rainbow;
                break;
            }
        }
        renderer.text(module.title, x, y, color, this.shadow.get(), this.getScale());
        double emptySpace = renderer.textWidth(" ", this.shadow.get(), this.getScale());
        double textHeight = renderer.textHeight(this.shadow.get(), this.getScale());
        double textLength = renderer.textWidth(module.title, this.shadow.get(), this.getScale());
        if (this.activeInfo.get().booleanValue() && (info = module.getInfoString()) != null) {
            renderer.text(info, x + emptySpace + textLength, y, this.moduleInfoColor.get(), this.shadow.get(), this.getScale());
            textLength += emptySpace + renderer.textWidth(info, this.shadow.get(), this.getScale());
        }
        if (this.outlines.get().booleanValue()) {
            if (index == 0) {
                renderer.quad(x - 2.0 - (double)this.outlineWidth.get().intValue(), y - 2.0, this.outlineWidth.get().intValue(), textHeight + 4.0, this.prevColor, this.prevColor, color, color);
                renderer.quad(x + textLength + 2.0, y - 2.0, this.outlineWidth.get().intValue(), textHeight + 4.0, this.prevColor, this.prevColor, color, color);
                renderer.quad(x - 2.0 - (double)this.outlineWidth.get().intValue(), y - 2.0 - (double)this.outlineWidth.get().intValue(), textLength + 4.0 + (double)(this.outlineWidth.get() * 2), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                if (index == modules.size() - 1) {
                    renderer.quad(x - 2.0 - (double)this.outlineWidth.get().intValue(), y + textHeight + 2.0, textLength + 4.0 + (double)(this.outlineWidth.get() * 2), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                }
            } else if (index == modules.size() - 1) {
                renderer.quad(x - 2.0 - (double)this.outlineWidth.get().intValue(), y, this.outlineWidth.get().intValue(), textHeight + 2.0 + (double)this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                renderer.quad(x + textLength + 2.0, y, this.outlineWidth.get().intValue(), textHeight + 2.0 + (double)this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                renderer.quad(x - 2.0 - (double)this.outlineWidth.get().intValue(), y + textHeight + 2.0, textLength + 4.0 + (double)(this.outlineWidth.get() * 2), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
            }
            if (index > 0) {
                if (index < modules.size() - 1) {
                    renderer.quad(x - 2.0 - (double)this.outlineWidth.get().intValue(), y, this.outlineWidth.get().intValue(), textHeight + 2.0, this.prevColor, this.prevColor, color, color);
                    renderer.quad(x + textLength + 2.0, y, this.outlineWidth.get().intValue(), textHeight + 2.0, this.prevColor, this.prevColor, color, color);
                }
                renderer.quad(Math.min(this.prevX, x) - 2.0 - (double)this.outlineWidth.get().intValue(), Math.max(this.prevX, x) == x ? y : y - (double)this.outlineWidth.get().intValue(), Math.max(this.prevX, x) - 2.0 - (Math.min(this.prevX, x) - 2.0 - (double)this.outlineWidth.get().intValue()), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
                renderer.quad(Math.min(this.prevX + this.prevTextLength, x + textLength) + 2.0, Math.min(this.prevX + this.prevTextLength, x + textLength) == x + textLength ? y : y - (double)this.outlineWidth.get().intValue(), Math.max(this.prevX + this.prevTextLength, x + textLength) + 2.0 + (double)this.outlineWidth.get().intValue() - (Math.min(this.prevX + this.prevTextLength, x + textLength) + 2.0), this.outlineWidth.get().intValue(), this.prevColor, this.prevColor, color, color);
            }
        }
        this.prevTextLength = textLength;
        this.prevColor = color;
    }

    private double getModuleWidth(HudRenderer renderer, Module module) {
        String info;
        double width = renderer.textWidth(module.title, this.shadow.get(), this.getScale());
        if (this.activeInfo.get().booleanValue() && (info = module.getInfoString()) != null) {
            width += renderer.textWidth(" ", this.shadow.get(), this.getScale()) + renderer.textWidth(info, this.shadow.get(), this.getScale());
        }
        return width;
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : -1.0;
    }

    public static enum Sort {
        Alphabetical,
        Biggest,
        Smallest;

    }

    public static enum ColorMode {
        Flat,
        Random,
        Rainbow;

    }
}

