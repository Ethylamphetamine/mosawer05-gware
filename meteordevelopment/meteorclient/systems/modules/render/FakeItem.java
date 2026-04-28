/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class FakeItem
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<HandMode> handMode;
    private final Setting<List<Item>> triggerItems;
    private final Setting<Item> fakeItem;
    private final Setting<Integer> stackSize;
    private final Setting<Boolean> requireHotbarPresence;
    private ItemStack cachedStack;

    public FakeItem() {
        super(Categories.Render, "fake-item", "Renders a configurable fake item when holding specific items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.handMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("hand")).description("Which hand should render the fake item.")).defaultValue(HandMode.MainHand)).build());
        this.triggerItems = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("trigger-items")).description("Items that trigger fake item rendering when held.")).defaultValue(List.of(Items.TOTEM_OF_UNDYING))).build());
        this.fakeItem = this.sgGeneral.add(((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("fake-item")).description("The item to render when holding selected trigger items.")).defaultValue(Items.END_CRYSTAL)).onChanged(item -> this.rebuildCachedStack())).build());
        this.stackSize = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("stack-size")).description("Stack size to display for the fake item.")).defaultValue(64)).min(1).max(64).sliderRange(1, 64).onChanged(integer -> this.rebuildCachedStack())).build());
        this.requireHotbarPresence = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("require-hotbar-item")).description("Require the fake item to be present in your hotbar before spoofing.")).defaultValue(true)).build());
        this.rebuildCachedStack();
    }

    @Override
    public void onActivate() {
        this.rebuildCachedStack();
    }

    public ItemStack getRenderStack(Hand hand, ClientPlayerEntity player, ItemStack current) {
        if (!this.isActive() || player == null) {
            return ItemStack.EMPTY;
        }
        if (!this.handMatches(hand)) {
            return ItemStack.EMPTY;
        }
        if (!this.triggerItems.get().contains(current.getItem())) {
            return ItemStack.EMPTY;
        }
        if (this.requireHotbarPresence.get().booleanValue() && !this.hasItemInHotbar(player, this.fakeItem.get())) {
            return ItemStack.EMPTY;
        }
        if (this.cachedStack.isEmpty() || this.cachedStack.getItem() != this.fakeItem.get() || this.cachedStack.getCount() != MathHelper.clamp((int)this.stackSize.get(), (int)1, (int)64)) {
            this.rebuildCachedStack();
        }
        return this.cachedStack;
    }

    private boolean handMatches(Hand hand) {
        return switch (this.handMode.get().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (hand == Hand.MAIN_HAND) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (hand == Hand.OFF_HAND) {
                    yield true;
                }
                yield false;
            }
            case 2 -> true;
        };
    }

    private boolean hasItemInHotbar(ClientPlayerEntity player, Item item) {
        for (int i = 0; i < 9; ++i) {
            if (!player.getInventory().getStack(i).isOf(item)) continue;
            return true;
        }
        return false;
    }

    private void rebuildCachedStack() {
        this.cachedStack = new ItemStack((ItemConvertible)this.fakeItem.get(), MathHelper.clamp((int)this.stackSize.get(), (int)1, (int)64));
    }

    public static enum HandMode {
        MainHand,
        OffHand,
        Both;

    }
}

