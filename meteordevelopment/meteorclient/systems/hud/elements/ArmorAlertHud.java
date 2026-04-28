/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArmorAlertHud
extends HudElement {
    public static final HudElementInfo<ArmorAlertHud> INFO = new HudElementInfo<ArmorAlertHud>(Hud.GROUP, "armor-alert", "Displays a warning when your armor durability is low.", ArmorAlertHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Setting<Double> threshold;
    private final Setting<SettingColor> color;
    private final Setting<Boolean> shadow;
    private final List<String> warnings;

    public ArmorAlertHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the text.")).defaultValue(2.0).min(1.0).max(10.0).sliderRange(1.0, 10.0).build());
        this.threshold = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("threshold-percent")).description("The durability percentage at which the warning appears.")).defaultValue(20.0).min(1.0).max(100.0).sliderRange(1.0, 100.0).build());
        this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The color of the warning text.")).defaultValue(new SettingColor(255, 20, 20)).build());
        this.shadow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("shadow")).description("Renders shadow for the text.")).defaultValue(true)).build());
        this.warnings = new ArrayList<String>();
    }

    @Override
    public void render(HudRenderer renderer) {
        this.warnings.clear();
        double scale = this.scale.get();
        TextRenderer.get().begin(scale, false, this.shadow.get());
        String textToRender = null;
        if (this.isInEditor()) {
            textToRender = "Armor durability low!";
        } else if (MeteorClient.mc.player != null) {
            for (int i = 0; i < 4; ++i) {
                int damage;
                int maxDamage;
                double durabilityPercent;
                ItemStack stack = MeteorClient.mc.player.getInventory().getArmorStack(i);
                if (stack.isEmpty() || !stack.isDamageable() || !((durabilityPercent = ((double)(maxDamage = stack.getMaxDamage()) - (double)(damage = stack.getDamage())) / (double)maxDamage * 100.0) <= this.threshold.get())) continue;
                String name = i == 2 && stack.isOf(Items.ELYTRA) ? "Elytra" : this.getPieceName(i);
                this.warnings.add(String.format("%s durability is low!", name));
            }
            if (this.warnings.size() == 1) {
                textToRender = this.warnings.get(0);
            } else if (this.warnings.size() > 1) {
                textToRender = "Armor durability low!";
            }
        }
        if (textToRender == null) {
            this.setSize(0.0, 0.0);
            TextRenderer.get().end();
            return;
        }
        double width = TextRenderer.get().getWidth(textToRender);
        double height = TextRenderer.get().getHeight();
        TextRenderer.get().render(textToRender, this.x, this.y, this.color.get());
        this.setSize(width, height);
        TextRenderer.get().end();
    }

    private String getPieceName(int i) {
        return switch (i) {
            case 0 -> "Boots";
            case 1 -> "Leggings";
            case 2 -> "Chestplate";
            case 3 -> "Helmet";
            default -> "Armor";
        };
    }
}

