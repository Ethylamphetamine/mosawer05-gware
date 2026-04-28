/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.Registries
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

public class StatusEffectListSettingScreen
extends RegistryListSettingScreen<StatusEffect> {
    public StatusEffectListSettingScreen(GuiTheme theme, Setting<List<StatusEffect>> setting) {
        super(theme, "Select Effects", setting, (Collection)setting.get(), Registries.STATUS_EFFECT);
    }

    @Override
    protected WWidget getValueWidget(StatusEffect value) {
        return this.theme.itemWithLabel(this.getPotionStack(value), this.getValueName(value));
    }

    @Override
    protected String getValueName(StatusEffect value) {
        return Names.get(value);
    }

    private ItemStack getPotionStack(StatusEffect effect) {
        ItemStack potion = Items.POTION.getDefaultStack();
        potion.set(DataComponentTypes.POTION_CONTENTS, (Object)new PotionContentsComponent(((PotionContentsComponent)potion.get(DataComponentTypes.POTION_CONTENTS)).comp_2378(), Optional.of(effect.getColor()), ((PotionContentsComponent)potion.get(DataComponentTypes.POTION_CONTENTS)).comp_2380()));
        return potion;
    }
}

