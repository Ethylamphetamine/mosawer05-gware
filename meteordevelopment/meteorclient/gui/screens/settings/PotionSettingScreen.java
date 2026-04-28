/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resource.language.I18n
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.PotionSetting;
import meteordevelopment.meteorclient.utils.misc.MyPotion;
import net.minecraft.client.resource.language.I18n;

public class PotionSettingScreen
extends WindowScreen {
    private final PotionSetting setting;

    public PotionSettingScreen(GuiTheme theme, PotionSetting setting) {
        super(theme, "Select Potion");
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTable table = this.add(this.theme.table()).expandX().widget();
        for (MyPotion potion : MyPotion.values()) {
            table.add(this.theme.itemWithLabel(potion.potion, I18n.translate((String)potion.potion.getTranslationKey(), (Object[])new Object[0])));
            WButton select = table.add(this.theme.button("Select")).widget();
            select.action = () -> {
                this.setting.set(potion);
                this.close();
            };
            table.row();
        }
    }
}

