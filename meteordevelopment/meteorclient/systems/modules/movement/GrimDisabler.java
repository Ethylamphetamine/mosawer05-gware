/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.screen.slot.SlotActionType
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class GrimDisabler
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<HorizontalDisablerMode> horizontalDisblerMode;
    private boolean fallFlyingBoostState;

    public GrimDisabler() {
        super(Categories.Movement, "grim-disabler", "Disables the Grim anti-cheat. Allows use of modules such as Speed and ClickTp");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.horizontalDisblerMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("horizontal-disabler-mode")).description("Determines mode of disabler for horizontal movement")).defaultValue(HorizontalDisablerMode.YawOverflow)).build());
        this.fallFlyingBoostState = false;
    }

    @EventHandler
    public void onPreMove(SendMovementPacketsEvent.Pre event) {
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
    }

    public boolean isInElytraFlyState() {
        return this.isActive() && this.fallFlyingBoostState;
    }

    public boolean shouldSetYawOverflowRotation() {
        return this.isActive() && this.horizontalDisblerMode.get() == HorizontalDisablerMode.YawOverflow;
    }

    private void stopFallFlying() {
        if (!this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return;
        }
        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
        this.mc.interactionManager.clickSlot(this.mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, (PlayerEntity)this.mc.player);
    }

    private void startFallFlying() {
        if (!this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return;
        }
        this.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
    }

    @Override
    public String getInfoString() {
        if (this.horizontalDisblerMode.get() == HorizontalDisablerMode.None) {
            return "";
        }
        return String.format("%s", this.horizontalDisblerMode.get().toString());
    }

    public static enum HorizontalDisablerMode {
        None,
        YawOverflow;

    }
}

