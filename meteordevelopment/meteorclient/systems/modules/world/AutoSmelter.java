/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.AbstractFurnaceBlockEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.recipe.RecipeType
 *  net.minecraft.recipe.input.RecipeInput
 *  net.minecraft.recipe.input.SingleStackRecipeInput
 *  net.minecraft.screen.AbstractFurnaceScreenHandler
 *  net.minecraft.screen.slot.Slot
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.mixininterface.IAbstractFurnaceScreenHandler;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;

public class AutoSmelter
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<Item>> fuelItems;
    private final Setting<List<Item>> smeltableItems;
    private final Setting<Boolean> disableWhenOutOfItems;
    private Map<Item, Integer> fuelTimeMap;

    public AutoSmelter() {
        super(Categories.World, "auto-smelter", "Automatically smelts items from your inventory");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fuelItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("fuel-items")).description("Items to use as fuel")).defaultValue(Items.COAL, Items.CHARCOAL).filter(this::fuelItemFilter).bypassFilterWhenSavingAndLoading().build());
        this.smeltableItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("smeltable-items")).description("Items to smelt")).defaultValue(Items.IRON_ORE, Items.GOLD_ORE, Items.COPPER_ORE, Items.RAW_IRON, Items.RAW_COPPER, Items.RAW_GOLD).filter(this::smeltableItemFilter).bypassFilterWhenSavingAndLoading().build());
        this.disableWhenOutOfItems = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-when-out-of-items")).description("Disable the module when you run out of items")).defaultValue(true)).build());
    }

    private boolean fuelItemFilter(Item item) {
        if (!Utils.canUpdate() && this.fuelTimeMap == null) {
            return false;
        }
        if (this.fuelTimeMap == null) {
            this.fuelTimeMap = AbstractFurnaceBlockEntity.createFuelTimeMap();
        }
        return this.fuelTimeMap.containsKey(item);
    }

    private boolean smeltableItemFilter(Item item) {
        return this.mc.world != null && this.mc.world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, (RecipeInput)new SingleStackRecipeInput(item.getDefaultStack()), (World)this.mc.world).isPresent();
    }

    public void tick(AbstractFurnaceScreenHandler c) {
        if (this.mc.player.age % 10 == 0) {
            return;
        }
        this.checkFuel(c);
        this.takeResults(c);
        this.insertItems(c);
    }

    private void insertItems(AbstractFurnaceScreenHandler c) {
        ItemStack inputItemStack = ((Slot)c.slots.getFirst()).getStack();
        if (!inputItemStack.isEmpty()) {
            return;
        }
        int slot = -1;
        for (int i = 3; i < c.slots.size(); ++i) {
            ItemStack item = ((Slot)c.slots.get(i)).getStack();
            if (!((IAbstractFurnaceScreenHandler)c).isItemSmeltable(item) || !this.smeltableItems.get().contains(item.getItem()) || !this.smeltableItemFilter(item.getItem())) continue;
            slot = i;
            break;
        }
        if (this.disableWhenOutOfItems.get().booleanValue() && slot == -1) {
            this.error("You do not have any items in your inventory that can be smelted. Disabling.", new Object[0]);
            this.toggle();
            return;
        }
        InvUtils.move().fromId(slot).toId(0);
    }

    private void checkFuel(AbstractFurnaceScreenHandler c) {
        ItemStack fuelStack = ((Slot)c.slots.get(1)).getStack();
        if (c.getFuelProgress() > 0.0f) {
            return;
        }
        if (!fuelStack.isEmpty()) {
            return;
        }
        int slot = -1;
        for (int i = 3; i < c.slots.size(); ++i) {
            ItemStack item = ((Slot)c.slots.get(i)).getStack();
            if (!this.fuelItems.get().contains(item.getItem()) || !this.fuelItemFilter(item.getItem())) continue;
            slot = i;
            break;
        }
        if (this.disableWhenOutOfItems.get().booleanValue() && slot == -1) {
            this.error("You do not have any fuel in your inventory. Disabling.", new Object[0]);
            this.toggle();
            return;
        }
        InvUtils.move().fromId(slot).toId(1);
    }

    private void takeResults(AbstractFurnaceScreenHandler c) {
        ItemStack resultStack = ((Slot)c.slots.get(2)).getStack();
        if (resultStack.isEmpty()) {
            return;
        }
        InvUtils.shiftClick().slotId(2);
        if (!resultStack.isEmpty()) {
            this.error("Your inventory is full. Disabling.", new Object[0]);
            this.toggle();
        }
    }
}

