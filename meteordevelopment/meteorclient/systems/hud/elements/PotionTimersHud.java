/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffectUtil
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;

public class PotionTimersHud
extends HudElement {
    public static final HudElementInfo<PotionTimersHud> INFO = new HudElementInfo<PotionTimersHud>(Hud.GROUP, "potion-timers", "Displays active potion effects with timers.", PotionTimersHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private final Setting<List<StatusEffect>> hiddenEffects;
    private final Setting<Boolean> showAmbient;
    private final Setting<ColorMode> colorMode;
    private final Setting<SettingColor> flatColor;
    private final Setting<Double> rainbowSpeed;
    private final Setting<Double> rainbowSpread;
    private final Setting<Double> rainbowSaturation;
    private final Setting<Double> rainbowBrightness;
    private final Setting<Boolean> shadow;
    private final Setting<Alignment> alignment;
    private final Setting<Integer> border;
    private final Setting<Boolean> customScale;
    private final Setting<Double> scale;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;
    private final List<Pair<StatusEffectInstance, String>> texts;
    private double rainbowHue;

    public PotionTimersHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.hiddenEffects = this.sgGeneral.add(((StatusEffectListSetting.Builder)((StatusEffectListSetting.Builder)new StatusEffectListSetting.Builder().name("hidden-effects")).description("Which effects not to show in the list.")).build());
        this.showAmbient = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-ambient")).description("Whether to show ambient effects like from beacons and conduits.")).defaultValue(true)).build());
        this.colorMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("color-mode")).description("What color to use for effects.")).defaultValue(ColorMode.Effect)).build());
        this.flatColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("flat-color")).description("Color for flat color mode.")).defaultValue(new SettingColor(225, 25, 25)).visible(() -> this.colorMode.get() == ColorMode.Flat)).build());
        this.rainbowSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-speed")).description("Rainbow speed of rainbow color mode.")).defaultValue(0.05).sliderMin(0.01).sliderMax(0.2).decimalPlaces(4).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.rainbowSpread = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-spread")).description("Rainbow spread of rainbow color mode.")).defaultValue(0.01).sliderMin(0.001).sliderMax(0.05).decimalPlaces(4).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.rainbowSaturation = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-saturation")).description("Saturation of rainbow color mode.")).defaultValue(1.0).sliderRange(0.0, 1.0).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.rainbowBrightness = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-brightness")).description("Brightness of rainbow color mode.")).defaultValue(1.0).sliderRange(0.0, 1.0).visible(() -> this.colorMode.get() == ColorMode.Rainbow)).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow behind text.")).defaultValue(true)).build());
        this.alignment = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("alignment")).description("Horizontal alignment.")).defaultValue(Alignment.Auto)).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(0)).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(1.0).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.texts = new ArrayList<Pair<StatusEffectInstance, String>>();
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
        if (MeteorClient.mc.player == null || this.isInEditor() && this.hasNoVisibleEffects()) {
            this.setSize(renderer.textWidth("Potion Timers 0:00", this.shadow.get(), this.getScale()), renderer.textHeight(this.shadow.get(), this.getScale()));
            return;
        }
        double width = 0.0;
        double height = 0.0;
        this.texts.clear();
        for (StatusEffectInstance statusEffectInstance : MeteorClient.mc.player.getStatusEffects()) {
            if (this.hiddenEffects.get().contains(statusEffectInstance.getEffectType().comp_349()) || !this.showAmbient.get().booleanValue() && statusEffectInstance.isAmbient()) continue;
            String text = this.getString(statusEffectInstance);
            this.texts.add((Pair<StatusEffectInstance, String>)new ObjectObjectImmutablePair((Object)statusEffectInstance, (Object)text));
            width = Math.max(width, renderer.textWidth(text, this.shadow.get(), this.getScale()));
            height += renderer.textHeight(this.shadow.get(), this.getScale());
        }
        this.setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x + this.border.get();
        double y = this.y + this.border.get();
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
        if (MeteorClient.mc.player == null || this.isInEditor() && this.hasNoVisibleEffects()) {
            renderer.text("Potion Timers 0:00", x, y, meteordevelopment.meteorclient.utils.render.color.Color.WHITE, this.shadow.get(), this.getScale());
            return;
        }
        this.rainbowHue += this.rainbowSpeed.get() * renderer.delta;
        if (this.rainbowHue > 1.0) {
            this.rainbowHue -= 1.0;
        } else if (this.rainbowHue < -1.0) {
            this.rainbowHue += 1.0;
        }
        double localRainbowHue = this.rainbowHue;
        for (Pair<StatusEffectInstance, String> potionEffectEntry : this.texts) {
            SettingColor color = switch (this.colorMode.get().ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    int c = ((StatusEffect)((StatusEffectInstance)potionEffectEntry.left()).getEffectType().comp_349()).getColor();
                    yield new meteordevelopment.meteorclient.utils.render.color.Color(c).a(255);
                }
                case 1 -> {
                    this.flatColor.get().update();
                    yield this.flatColor.get();
                }
                case 2 -> {
                    int c = Color.HSBtoRGB((float)(localRainbowHue += this.rainbowSpread.get().doubleValue()), this.rainbowSaturation.get().floatValue(), this.rainbowBrightness.get().floatValue());
                    yield new meteordevelopment.meteorclient.utils.render.color.Color(c);
                }
            };
            String text = (String)potionEffectEntry.right();
            renderer.text(text, x + this.alignX(renderer.textWidth(text, this.shadow.get(), this.getScale()), this.alignment.get()), y, color, this.shadow.get(), this.getScale());
            y += renderer.textHeight(this.shadow.get(), this.getScale());
        }
    }

    private String getString(StatusEffectInstance statusEffectInstance) {
        return String.format("%s %d (%s)", Names.get((StatusEffect)statusEffectInstance.getEffectType().comp_349()), statusEffectInstance.getAmplifier() + 1, StatusEffectUtil.getDurationText((StatusEffectInstance)statusEffectInstance, (float)1.0f, (float)MeteorClient.mc.world.getTickManager().getTickRate()).getString());
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : -1.0;
    }

    private boolean hasNoVisibleEffects() {
        for (StatusEffectInstance statusEffectInstance : MeteorClient.mc.player.getStatusEffects()) {
            if (this.hiddenEffects.get().contains(statusEffectInstance.getEffectType().comp_349()) || !this.showAmbient.get().booleanValue() && statusEffectInstance.isAmbient()) continue;
            return false;
        }
        return true;
    }

    public static enum ColorMode {
        Effect,
        Flat,
        Rainbow;

    }
}

