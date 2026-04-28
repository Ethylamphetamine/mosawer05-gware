/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resource.language.I18n
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.potion.Potion
 *  net.minecraft.potion.Potions
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.screen.BrewingStandScreenHandler
 *  net.minecraft.screen.slot.Slot
 */
package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.PotionSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MyPotion;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AutoBrewer
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<MyPotion> potion;
    private int ingredientI;
    private boolean first;
    private int timer;

    public AutoBrewer() {
        super(Categories.World, "auto-brewer", "Automatically brews the specified potion.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.potion = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new PotionSetting.Builder().name("potion")).description("The type of potion to brew.")).defaultValue(MyPotion.Strength)).build());
    }

    @Override
    public void onActivate() {
        this.first = false;
    }

    public void onBrewingStandClose() {
        this.first = false;
    }

    public void tick(BrewingStandScreenHandler c) {
        ++this.timer;
        if (!this.first) {
            this.first = true;
            this.ingredientI = -2;
            this.timer = 0;
        }
        if (c.getBrewTime() != 0 || this.timer < 5) {
            return;
        }
        if (this.ingredientI == -2) {
            if (this.takePotions(c)) {
                return;
            }
            ++this.ingredientI;
            this.timer = 0;
        } else if (this.ingredientI == -1) {
            if (this.insertWaterBottles(c)) {
                return;
            }
            ++this.ingredientI;
            this.timer = 0;
        } else if (this.ingredientI < this.potion.get().ingredients.length) {
            if (this.checkFuel(c)) {
                return;
            }
            if (this.insertIngredient(c, this.potion.get().ingredients[this.ingredientI])) {
                return;
            }
            ++this.ingredientI;
            this.timer = 0;
        } else {
            this.ingredientI = -2;
            this.timer = 0;
        }
    }

    private boolean insertIngredient(BrewingStandScreenHandler c, Item ingredient) {
        int slot = -1;
        for (int slotI = 5; slotI < c.slots.size(); ++slotI) {
            if (((Slot)c.slots.get(slotI)).getStack().getItem() != ingredient) continue;
            slot = slotI;
            break;
        }
        if (slot == -1) {
            this.error("You do not have any %s left in your inventory... disabling.", I18n.translate((String)ingredient.getTranslationKey(), (Object[])new Object[0]));
            this.toggle();
            return true;
        }
        this.moveOneItem(c, slot, 3);
        return false;
    }

    private boolean checkFuel(BrewingStandScreenHandler c) {
        if (c.getFuel() == 0) {
            int slot = -1;
            for (int slotI = 5; slotI < c.slots.size(); ++slotI) {
                if (((Slot)c.slots.get(slotI)).getStack().getItem() != Items.BLAZE_POWDER) continue;
                slot = slotI;
                break;
            }
            if (slot == -1) {
                this.error("You do not have a sufficient amount of blaze powder to use as fuel for the brew... disabling.", new Object[0]);
                this.toggle();
                return true;
            }
            this.moveOneItem(c, slot, 4);
        }
        return false;
    }

    private void moveOneItem(BrewingStandScreenHandler c, int from, int to) {
        InvUtils.move().fromId(from).toId(to);
    }

    private boolean insertWaterBottles(BrewingStandScreenHandler c) {
        for (int i = 0; i < 3; ++i) {
            int slot = -1;
            for (int slotI = 5; slotI < c.slots.size(); ++slotI) {
                Potion potion;
                if (((Slot)c.slots.get(slotI)).getStack().getItem() != Items.POTION || (potion = (Potion)((RegistryEntry)((PotionContentsComponent)((Slot)c.slots.get(slotI)).getStack().get(DataComponentTypes.POTION_CONTENTS)).comp_2378().get()).comp_349()) != Potions.WATER.comp_349()) continue;
                slot = slotI;
                break;
            }
            if (slot == -1) {
                this.error("You do not have a sufficient amount of water bottles to complete this brew... disabling.", new Object[0]);
                this.toggle();
                return true;
            }
            InvUtils.move().fromId(slot).toId(i);
        }
        return false;
    }

    private boolean takePotions(BrewingStandScreenHandler c) {
        for (int i = 0; i < 3; ++i) {
            InvUtils.shiftClick().slotId(i);
            if (((Slot)c.slots.get(i)).getStack().isEmpty()) continue;
            this.error("You do not have a sufficient amount of inventory space... disabling.", new Object[0]);
            this.toggle();
            return true;
        }
        return false;
    }
}

