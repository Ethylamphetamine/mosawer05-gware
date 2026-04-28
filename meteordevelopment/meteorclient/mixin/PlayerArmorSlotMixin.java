/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.inventory.Inventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.screen.slot.Slot
 *  org.spongepowered.asm.mixin.Mixin
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets={"net/minecraft/screen/PlayerScreenHandler$1"})
public abstract class PlayerArmorSlotMixin
extends Slot {
    public PlayerArmorSlotMixin(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public int getMaxItemCount() {
        if (Modules.get().get(InventoryTweaks.class).armorStorage()) {
            return 64;
        }
        return super.getMaxItemCount();
    }

    public boolean canInsert(ItemStack stack) {
        if (Modules.get().get(InventoryTweaks.class).armorStorage()) {
            return true;
        }
        return super.canInsert(stack);
    }

    public boolean canTakeItems(PlayerEntity playerEntity) {
        if (Modules.get().get(InventoryTweaks.class).armorStorage()) {
            return true;
        }
        return super.canTakeItems(playerEntity);
    }
}

