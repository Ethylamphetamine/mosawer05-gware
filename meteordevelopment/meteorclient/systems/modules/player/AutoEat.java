/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.FoodComponent
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AutoEat
extends Module {
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, AnchorAura.class, BedAura.class};
    private final SettingGroup sgGeneral;
    private final SettingGroup sgThreshold;
    private final Setting<List<Item>> blacklist;
    private final Setting<Boolean> pauseAuras;
    private final Setting<Boolean> pauseBaritone;
    private final Setting<ThresholdMode> thresholdMode;
    private final Setting<Double> healthThreshold;
    private final Setting<Integer> hungerThreshold;
    public boolean eating;
    private int slot;
    private int prevSlot;
    private final List<Class<? extends Module>> wasAura;
    private boolean wasBaritone;

    public AutoEat() {
        super(Categories.Player, "auto-eat", "Automatically eats food.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgThreshold = this.settings.createGroup("Threshold");
        this.blacklist = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("blacklist")).description("Which items to not eat.")).defaultValue(Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.POISONOUS_POTATO, Items.PUFFERFISH, Items.CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.SUSPICIOUS_STEW).filter(item -> item.getComponents().get(DataComponentTypes.FOOD) != null).build());
        this.pauseAuras = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-auras")).description("Pauses all auras when eating.")).defaultValue(true)).build());
        this.pauseBaritone = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-baritone")).description("Pause baritone when eating.")).defaultValue(true)).build());
        this.thresholdMode = this.sgThreshold.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("threshold-mode")).description("The threshold mode to trigger auto eat.")).defaultValue(ThresholdMode.Any)).build());
        this.healthThreshold = this.sgThreshold.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("health-threshold")).description("The level of health you eat at.")).defaultValue(10.0).range(1.0, 19.0).sliderRange(1.0, 19.0).visible(() -> this.thresholdMode.get() != ThresholdMode.Hunger)).build());
        this.hungerThreshold = this.sgThreshold.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hunger-threshold")).description("The level of hunger you eat at.")).defaultValue(16)).range(1, 19).sliderRange(1, 19).visible(() -> this.thresholdMode.get() != ThresholdMode.Health)).build());
        this.wasAura = new ArrayList<Class<? extends Module>>();
        this.wasBaritone = false;
    }

    @Override
    public void onDeactivate() {
        if (this.eating) {
            this.stopEating();
        }
    }

    @EventHandler(priority=-100)
    private void onTick(TickEvent.Pre event) {
        if (Modules.get().get(AutoGap.class).isEating()) {
            return;
        }
        if (this.eating) {
            if (this.shouldEat()) {
                if (this.mc.player.getInventory().getStack(this.slot).get(DataComponentTypes.FOOD) != null) {
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
        if (this.pauseBaritone.get().booleanValue() && PathManagers.get().isPathing() && !this.wasBaritone) {
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
            this.wasBaritone = false;
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

    public boolean shouldEat() {
        boolean health = (double)this.mc.player.getHealth() <= this.healthThreshold.get();
        boolean hunger = this.mc.player.getHungerManager().getFoodLevel() <= this.hungerThreshold.get();
        return this.thresholdMode.get().test(health, hunger);
    }

    private int findSlot() {
        int slot = -1;
        int bestHunger = -1;
        for (int i = 0; i < 9; ++i) {
            int hunger;
            Item item = this.mc.player.getInventory().getStack(i).getItem();
            FoodComponent foodComponent = (FoodComponent)item.getComponents().get(DataComponentTypes.FOOD);
            if (foodComponent == null || (hunger = foodComponent.comp_2491()) <= bestHunger || this.blacklist.get().contains(item)) continue;
            slot = i;
            bestHunger = hunger;
        }
        Item offHandItem = this.mc.player.getOffHandStack().getItem();
        if (offHandItem.getComponents().get(DataComponentTypes.FOOD) != null && !this.blacklist.get().contains(offHandItem) && ((FoodComponent)offHandItem.getComponents().get(DataComponentTypes.FOOD)).comp_2491() > bestHunger) {
            slot = 45;
        }
        return slot;
    }

    public static enum ThresholdMode {
        Health((health, hunger) -> health),
        Hunger((health, hunger) -> hunger),
        Any((health, hunger) -> health != false || hunger != false),
        Both((health, hunger) -> health != false && hunger != false);

        private final BiPredicate<Boolean, Boolean> predicate;

        private ThresholdMode(BiPredicate<Boolean, Boolean> predicate) {
            this.predicate = predicate;
        }

        public boolean test(boolean health, boolean hunger) {
            return this.predicate.test(health, hunger);
        }
    }
}

