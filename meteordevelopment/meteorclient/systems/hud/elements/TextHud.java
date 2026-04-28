/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.StarscriptError;

public class TextHud
extends HudElement {
    private static final Color WHITE = new Color();
    private final SettingGroup sgGeneral;
    private final SettingGroup sgShown;
    private final SettingGroup sgScale;
    private final SettingGroup sgBackground;
    private double originalWidth;
    private double originalHeight;
    private boolean needsCompile;
    private boolean recalculateSize;
    private int timer;
    public final Setting<String> text;
    public final Setting<Integer> updateDelay;
    public final Setting<Boolean> shadow;
    public final Setting<Integer> border;
    public final Setting<Shown> shown;
    public final Setting<String> condition;
    public final Setting<Boolean> customScale;
    public final Setting<Double> scale;
    public final Setting<Boolean> background;
    public final Setting<SettingColor> backgroundColor;
    private Script script;
    private Script conditionScript;
    private Section section;
    private boolean firstTick;
    private boolean empty;
    private boolean visible;

    public TextHud(HudElementInfo<TextHud> info) {
        super(info);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgShown = this.settings.createGroup("Shown");
        this.sgScale = this.settings.createGroup("Scale");
        this.sgBackground = this.settings.createGroup("Background");
        this.text = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("text")).description("Text to display with Starscript.")).defaultValue(MeteorClient.NAME)).onChanged(s -> this.recompile())).wide().renderer(StarscriptTextBoxRenderer.class).build());
        this.updateDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("update-delay")).description("Update delay in ticks")).defaultValue(4)).onChanged(integer -> {
            if (this.timer > integer) {
                this.timer = integer;
            }
        })).min(0).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow behind text.")).defaultValue(true)).onChanged(aBoolean -> {
            this.recalculateSize = true;
        })).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the text.")).defaultValue(0)).onChanged(integer -> super.setSize(this.originalWidth + (double)(integer * 2), this.originalHeight + (double)(integer * 2)))).build());
        this.shown = this.sgShown.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shown")).description("When this text element is shown.")).defaultValue(Shown.Always)).onChanged(s -> this.recompile())).build());
        this.condition = this.sgShown.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("condition")).description("Condition to check when shown is not Always.")).visible(() -> this.shown.get() != Shown.Always)).onChanged(s -> this.recompile())).renderer(StarscriptTextBoxRenderer.class).build());
        this.customScale = this.sgScale.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-scale")).description("Applies custom text scale rather than the global one.")).defaultValue(false)).onChanged(integer -> {
            this.recalculateSize = true;
        })).build());
        this.scale = this.sgScale.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Custom scale.")).visible(this.customScale::get)).defaultValue(1.0).onChanged(integer -> {
            this.recalculateSize = true;
        })).min(0.5).sliderRange(0.5, 3.0).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.firstTick = true;
        this.empty = false;
        this.needsCompile = true;
    }

    private void recompile() {
        this.firstTick = true;
        this.needsCompile = true;
    }

    @Override
    public void setSize(double width, double height) {
        this.originalWidth = width;
        this.originalHeight = height;
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    private void calculateSize(HudRenderer renderer) {
        String str;
        double width = 0.0;
        if (this.section != null && !(str = this.section.toString()).isBlank()) {
            width = renderer.textWidth(str, this.shadow.get(), this.getScale());
        }
        if (width != 0.0) {
            this.setSize(width, renderer.textHeight(this.shadow.get(), this.getScale()));
            this.empty = false;
        } else {
            this.setSize(100.0, renderer.textHeight(this.shadow.get(), this.getScale()));
            this.empty = true;
        }
    }

    @Override
    public void tick(HudRenderer renderer) {
        if (this.recalculateSize) {
            this.calculateSize(renderer);
            this.recalculateSize = false;
        }
        if (this.timer <= 0) {
            this.runTick(renderer);
            this.timer = this.updateDelay.get();
        } else {
            --this.timer;
        }
    }

    private void runTick(HudRenderer renderer) {
        if (this.needsCompile) {
            Parser.Result result = Parser.parse(this.text.get());
            if (result.hasErrors()) {
                this.script = null;
                this.section = new Section(0, result.errors.getFirst().toString());
                this.calculateSize(renderer);
            } else {
                this.script = Compiler.compile(result);
            }
            if (this.shown.get() != Shown.Always) {
                this.conditionScript = Compiler.compile(Parser.parse(this.condition.get()));
            }
            this.needsCompile = false;
        }
        try {
            if (this.script != null) {
                this.section = MeteorStarscript.ss.run(this.script);
                this.calculateSize(renderer);
            }
        }
        catch (StarscriptError error) {
            this.section = new Section(0, error.getMessage());
            this.calculateSize(renderer);
        }
        if (this.shown.get() != Shown.Always && this.conditionScript != null) {
            String text = MeteorStarscript.run(this.conditionScript);
            this.visible = text == null ? false : (this.shown.get() == Shown.WhenTrue ? text.equalsIgnoreCase("true") : text.equalsIgnoreCase("false"));
        }
        this.firstTick = false;
    }

    @Override
    public void render(HudRenderer renderer) {
        boolean visible;
        if (this.firstTick) {
            this.runTick(renderer);
        }
        boolean bl = visible = this.shown.get() == Shown.Always || this.visible;
        if ((this.empty || !visible) && this.isInEditor()) {
            renderer.line(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.GRAY);
            renderer.line(this.x, this.y + this.getHeight(), this.x + this.getWidth(), this.y, Color.GRAY);
        }
        if (this.section == null || !visible) {
            return;
        }
        double x = this.x + this.border.get();
        Section s = this.section;
        while (s != null) {
            x = renderer.text(s.text, x, this.y + this.border.get(), TextHud.getSectionColor(s.index), this.shadow.get(), this.getScale());
            s = s.next;
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    @Override
    public void onFontChanged() {
        this.recalculateSize = true;
    }

    private double getScale() {
        return this.customScale.get() != false ? this.scale.get() : -1.0;
    }

    public static Color getSectionColor(int i) {
        List<SettingColor> colors = Hud.get().textColors.get();
        return i >= 0 && i < colors.size() ? (Color)colors.get(i) : WHITE;
    }

    public static enum Shown {
        Always,
        WhenTrue,
        WhenFalse;


        public String toString() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Always";
                case 1 -> "When True";
                case 2 -> "When False";
            };
        }
    }
}

