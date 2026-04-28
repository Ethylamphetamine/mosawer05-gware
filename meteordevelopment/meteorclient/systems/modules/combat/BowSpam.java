/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArrowItem
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.Items;

public class BowSpam
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Integer> charge;
    private final Setting<Boolean> onlyWhenHoldingRightClick;
    private boolean wasBow;
    private boolean wasHoldingRightClick;

    public BowSpam() {
        super(Categories.Combat, "bow-spam", "Spams arrows.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.charge = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("charge")).description("How long to charge the bow before releasing in ticks.")).defaultValue(5)).range(5, 20).sliderRange(5, 20).build());
        this.onlyWhenHoldingRightClick = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("when-holding-right-click")).description("Works only when holding right click.")).defaultValue(false)).build());
        this.wasBow = false;
        this.wasHoldingRightClick = false;
    }

    @Override
    public void onActivate() {
        this.wasBow = false;
        this.wasHoldingRightClick = false;
    }

    @Override
    public void onDeactivate() {
        this.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.mc.player.getAbilities().creativeMode && !InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found()) {
            return;
        }
        if (!this.onlyWhenHoldingRightClick.get().booleanValue() || this.mc.options.useKey.isPressed()) {
            boolean isBow;
            boolean bl = isBow = this.mc.player.getMainHandStack().getItem() == Items.BOW;
            if (!isBow && this.wasBow) {
                this.setPressed(false);
            }
            this.wasBow = isBow;
            if (!isBow) {
                return;
            }
            if (this.mc.player.getItemUseTime() >= this.charge.get()) {
                this.mc.interactionManager.stopUsingItem((PlayerEntity)this.mc.player);
            } else {
                this.setPressed(true);
            }
            this.wasHoldingRightClick = this.mc.options.useKey.isPressed();
        } else if (this.wasHoldingRightClick) {
            this.setPressed(false);
            this.wasHoldingRightClick = false;
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.options.useKey.setPressed(pressed);
    }
}

