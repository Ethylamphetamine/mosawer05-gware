/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class AutoGap
extends Module {
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, AnchorAura.class, BedAura.class};
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPotions;
    private final SettingGroup sgHealth;
    private final Setting<Boolean> allowEgap;
    private final Setting<Boolean> always;
    private final Setting<Boolean> pauseAuras;
    private final Setting<Boolean> pauseBaritone;
    private final Setting<Boolean> potionsRegeneration;
    private final Setting<Boolean> potionsFireResistance;
    private final Setting<Boolean> potionsResistance;
    private final Setting<Boolean> healthEnabled;
    private final Setting<Integer> healthThreshold;
    private boolean requiresEGap;
    private boolean eating;
    private int slot;
    private int prevSlot;
    private final List<Class<? extends Module>> wasAura;
    private boolean wasBaritone;

    public AutoGap() {
        super(Categories.Player, "auto-gap", "Automatically eats Gaps or E-Gaps.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPotions = this.settings.createGroup("Potions");
        this.sgHealth = this.settings.createGroup("Health");
        this.allowEgap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("allow-egap")).description("Allow eating E-Gaps over Gaps if found.")).defaultValue(true)).build());
        this.always = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("always")).description("If it should always eat.")).defaultValue(false)).build());
        this.pauseAuras = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-auras")).description("Pauses all auras when eating.")).defaultValue(true)).build());
        this.pauseBaritone = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-baritone")).description("Pause baritone when eating.")).defaultValue(true)).build());
        this.potionsRegeneration = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potions-regeneration")).description("If it should eat when Regeneration runs out.")).defaultValue(false)).build());
        this.potionsFireResistance = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potions-fire-resistance")).description("If it should eat when Fire Resistance runs out. Requires E-Gaps.")).defaultValue(true)).visible(this.allowEgap::get)).build());
        this.potionsResistance = this.sgPotions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("potions-absorption")).description("If it should eat when Resistance runs out. Requires E-Gaps.")).defaultValue(false)).visible(this.allowEgap::get)).build());
        this.healthEnabled = this.sgHealth.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("health-enabled")).description("If it should eat when health drops below threshold.")).defaultValue(true)).build());
        this.healthThreshold = this.sgHealth.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("health-threshold")).description("Health threshold to eat at. Includes absorption.")).defaultValue(20)).min(0).sliderMax(40).build());
        this.wasAura = new ArrayList<Class<? extends Module>>();
    }

    @Override
    public void onDeactivate() {
        if (this.eating) {
            this.stopEating();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.eating) {
            if (this.shouldEat()) {
                if (this.isNotGapOrEGap(this.mc.player.getInventory().getStack(this.slot))) {
                    int slot = this.findSlot();
                    if (slot == -1) {
                        this.stopEating();
                        return;
                    }
                    this.changeSlot(slot);
                }
                this.eat();
            } else {
                this.stopEating();
            }
        } else if (this.shouldEat()) {
            this.slot = this.findSlot();
            if (this.slot != -1) {
                this.startEating();
            }
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (this.eating) {
            event.target = null;
        }
    }

    private void startEating() {
        this.prevSlot = this.mc.player.getInventory().selectedSlot;
        this.eat();
        this.wasAura.clear();
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);
                if (!module.isActive()) continue;
                this.wasAura.add(klass);
                module.toggle();
            }
        }
        this.wasBaritone = false;
        if (this.pauseBaritone.get().booleanValue() && PathManagers.get().isPathing()) {
            this.wasBaritone = true;
            PathManagers.get().pause();
        }
    }

    private void eat() {
        this.changeSlot(this.slot);
        this.setPressed(true);
        if (!this.mc.player.isUsingItem()) {
            Utils.rightClick();
        }
        this.eating = true;
    }

    private void stopEating() {
        this.changeSlot(this.prevSlot);
        this.setPressed(false);
        this.eating = false;
        if (this.pauseAuras.get().booleanValue()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);
                if (!this.wasAura.contains(klass) || module.isActive()) continue;
                module.toggle();
            }
        }
        if (this.pauseBaritone.get().booleanValue() && this.wasBaritone) {
            PathManagers.get().resume();
        }
    }

    private void setPressed(boolean pressed) {
        this.mc.options.useKey.setPressed(pressed);
    }

    private void changeSlot(int slot) {
        InvUtils.swap(slot, false);
        this.slot = slot;
    }

    private boolean shouldEat() {
        this.requiresEGap = false;
        if (this.always.get().booleanValue()) {
            return true;
        }
        if (this.shouldEatPotions()) {
            return true;
        }
        return this.shouldEatHealth();
    }

    private boolean shouldEatPotions() {
        Map effects = this.mc.player.getActiveStatusEffects();
        if (this.potionsRegeneration.get().booleanValue() && !effects.containsKey(StatusEffects.REGENERATION)) {
            return true;
        }
        if (this.potionsFireResistance.get().booleanValue() && !effects.containsKey(StatusEffects.FIRE_RESISTANCE)) {
            this.requiresEGap = true;
            return true;
        }
        if (this.potionsResistance.get().booleanValue() && !effects.containsKey(StatusEffects.RESISTANCE)) {
            this.requiresEGap = true;
            return true;
        }
        return false;
    }

    private boolean shouldEatHealth() {
        if (!this.healthEnabled.get().booleanValue()) {
            return false;
        }
        int health = Math.round(this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount());
        return health < this.healthThreshold.get();
    }

    private int findSlot() {
        boolean preferEGap = this.allowEgap.get() != false || this.requiresEGap;
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = this.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || this.isNotGapOrEGap(stack)) continue;
            Item item = stack.getItem();
            if (item == Items.ENCHANTED_GOLDEN_APPLE && preferEGap) {
                slot = i;
                break;
            }
            if (item != Items.GOLDEN_APPLE || this.requiresEGap) continue;
            slot = i;
            if (!preferEGap) break;
        }
        return slot;
    }

    private boolean isNotGapOrEGap(ItemStack stack) {
        Item item = stack.getItem();
        return item != Items.GOLDEN_APPLE && item != Items.ENCHANTED_GOLDEN_APPLE;
    }

    public boolean isEating() {
        return this.isActive() && this.eating;
    }
}

