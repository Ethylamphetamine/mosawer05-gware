/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AutoMend
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<Item>> blacklist;
    private final Setting<Boolean> force;
    private final Setting<Boolean> autoDisable;
    private boolean didMove;

    public AutoMend() {
        super(Categories.Player, "auto-mend", "Automatically replaces items in your offhand with mending when fully repaired.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blacklist = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("blacklist")).description("Item blacklist.")).filter(item -> item.getComponents().get(DataComponentTypes.DAMAGE) != null).build());
        this.force = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("force")).description("Replaces item in offhand even if there is some other non-repairable item.")).defaultValue(false)).build());
        this.autoDisable = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-disable")).description("Automatically disables when there are no more items to repair.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.didMove = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.shouldWait()) {
            return;
        }
        int slot = this.getSlot();
        if (slot == -1) {
            if (this.autoDisable.get().booleanValue()) {
                this.info("Repaired all items, disabling", new Object[0]);
                if (this.didMove) {
                    int emptySlot = this.getEmptySlot();
                    InvUtils.move().fromOffhand().to(emptySlot);
                }
                this.toggle();
            }
        } else {
            InvUtils.move().from(slot).toOffhand();
            this.didMove = true;
        }
    }

    private boolean shouldWait() {
        ItemStack itemStack = this.mc.player.getOffHandStack();
        if (itemStack.isEmpty()) {
            return false;
        }
        if (Utils.hasEnchantments(itemStack, Enchantments.MENDING)) {
            return itemStack.getDamage() != 0;
        }
        return this.force.get() == false;
    }

    private int getSlot() {
        for (int i = 0; i < this.mc.player.getInventory().main.size(); ++i) {
            ItemStack itemStack = this.mc.player.getInventory().getStack(i);
            if (this.blacklist.get().contains(itemStack.getItem()) || !Utils.hasEnchantments(itemStack, Enchantments.MENDING) || itemStack.getDamage() <= 0) continue;
            return i;
        }
        return -1;
    }

    private int getEmptySlot() {
        for (int i = 0; i < this.mc.player.getInventory().main.size(); ++i) {
            if (!this.mc.player.getInventory().getStack(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }
}

