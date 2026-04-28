/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class WelcomerHud
extends HudElement {
    public static final HudElementInfo<WelcomerHud> INFO = new HudElementInfo<Object>(Hud.GROUP, "welcomer", "Displays a welcome message with your name.", WelcomerHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<String> format;
    private final Setting<SettingColor> color;
    private final Setting<Double> scale;
    private final Setting<Boolean> shadow;

    public WelcomerHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.format = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("format")).description("The format of the welcome message. Use {player} for your name.")).defaultValue("Hello {player} :^)")).build());
        this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("Color of the text.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Scale of the text.")).defaultValue(1.0).min(0.5).max(10.0).sliderRange(1.0, 10.0).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow behind the text.")).defaultValue(true)).build());
    }

    @Override
    public void render(HudRenderer renderer) {
        double scale = this.scale.get();
        TextRenderer.get().begin(scale, false, this.shadow.get());
        String text = this.getDisplayText();
        double width = TextRenderer.get().getWidth(text);
        double height = TextRenderer.get().getHeight();
        TextRenderer.get().render(text, this.x, this.y, this.color.get());
        this.setSize(width, height);
        TextRenderer.get().end();
    }

    private String getDisplayText() {
        String name = "Player";
        if (MeteorClient.mc.player != null) {
            name = MeteorClient.mc.player.getName().getString();
        }
        return this.format.get().replace("{player}", name);
    }
}

