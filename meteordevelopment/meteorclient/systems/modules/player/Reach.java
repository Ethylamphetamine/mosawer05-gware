/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;

public class Reach
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> blockReach;
    private final Setting<Double> entityReach;

    public Reach() {
        super(Categories.Player, "reach", "Gives you super long arms.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blockReach = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("extra-block-reach")).description("The distance to add to your block reach.")).sliderMax(1.0).build());
        this.entityReach = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("extra-entity-reach")).description("The distance to add to your entity reach.")).sliderMax(1.0).build());
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        return theme.label("Note: on vanilla servers you may give yourself up to 4 blocks of additional reach for specific actions - interacting with block entities (chests, furnaces, etc.) or with vehicles. This does not work on paper servers.", (double)Utils.getWindowWidth() / 3.0);
    }

    public double blockReach() {
        return this.isActive() ? this.blockReach.get() : 0.0;
    }

    public double entityReach() {
        return this.isActive() ? this.entityReach.get() : 0.0;
    }
}

