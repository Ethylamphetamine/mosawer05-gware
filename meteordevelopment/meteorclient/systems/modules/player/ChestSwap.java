/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.item.ArmorItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class ChestSwap
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> swapBind;
    private boolean keyUnpressed;

    public ChestSwap() {
        super(Categories.Player, "chest-swap", "Automatically swaps between a chestplate and an elytra.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.swapBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("swap-bind")).description("Swaps on this key press.")).build());
        this.keyUnpressed = false;
    }

    @EventHandler(priority=200)
    private void onRender(Render3DEvent event) {
        this.update();
    }

    private void update() {
        if (!this.swapBind.get().isPressed()) {
            this.keyUnpressed = true;
        }
        if (this.swapBind.get().isPressed() && this.keyUnpressed && !(this.mc.currentScreen instanceof ChatScreen)) {
            this.swap();
            this.keyUnpressed = false;
        }
    }

    public void swap() {
        Item currentItem = this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();
        if (currentItem == Items.ELYTRA) {
            PlayerUtils.silentSwapEquipChestplate();
        } else if (currentItem instanceof ArmorItem && ((ArmorItem)currentItem).getSlotType() == EquipmentSlot.CHEST) {
            PlayerUtils.silentSwapEquipElytra();
        } else if (!PlayerUtils.silentSwapEquipChestplate()) {
            PlayerUtils.silentSwapEquipElytra();
        }
    }
}

