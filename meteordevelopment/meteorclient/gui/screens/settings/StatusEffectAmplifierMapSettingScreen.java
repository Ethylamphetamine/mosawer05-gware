/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WIntEdit;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.StringUtils;

public class StatusEffectAmplifierMapSettingScreen
extends WindowScreen {
    private final Setting<Reference2IntMap<StatusEffect>> setting;
    private WTable table;
    private String filterText = "";

    public StatusEffectAmplifierMapSettingScreen(GuiTheme theme, Setting<Reference2IntMap<StatusEffect>> setting) {
        super(theme, "Modify Amplifiers");
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = this.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            this.filterText = filter.get().trim();
            this.table.clear();
            this.initTable();
        };
        this.table = this.add(this.theme.table()).expandX().widget();
        this.initTable();
    }

    private void initTable() {
        ArrayList<StatusEffect> statusEffects = new ArrayList<StatusEffect>((Collection<StatusEffect>)this.setting.get().keySet());
        statusEffects.sort(Comparator.comparing(Names::get));
        for (StatusEffect statusEffect : statusEffects) {
            String name = Names.get(statusEffect);
            if (!StringUtils.containsIgnoreCase((CharSequence)name, (CharSequence)this.filterText)) continue;
            this.table.add(this.theme.itemWithLabel(this.getPotionStack(statusEffect), name)).expandCellX();
            WIntEdit level = this.theme.intEdit(this.setting.get().getInt((Object)statusEffect), 0, Integer.MAX_VALUE, true);
            level.action = () -> {
                this.setting.get().put((Object)statusEffect, level.get());
                this.setting.onChanged();
            };
            this.table.add(level).minWidth(50.0);
            this.table.row();
        }
    }

    private ItemStack getPotionStack(StatusEffect effect) {
        ItemStack potion = Items.POTION.getDefaultStack();
        potion.set(DataComponentTypes.POTION_CONTENTS, (Object)new PotionContentsComponent(((PotionContentsComponent)potion.get(DataComponentTypes.POTION_CONTENTS)).comp_2378(), Optional.of(effect.getColor()), ((PotionContentsComponent)potion.get(DataComponentTypes.POTION_CONTENTS)).comp_2380()));
        return potion;
    }
}

