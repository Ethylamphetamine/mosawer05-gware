/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.FireworksComponent
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.FireworkRocketEntity
 *  net.minecraft.item.FireworkRocketItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.sound.SoundCategory
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.util.ActionResult
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.InteractItemEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class ElytraBoost
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> dontConsumeFirework;
    private final Setting<Integer> fireworkLevel;
    private final Setting<Boolean> playSound;
    private final Setting<Keybind> keybind;
    private final List<FireworkRocketEntity> fireworks;

    public ElytraBoost() {
        super(Categories.Movement, "elytra-boost", "Boosts your elytra as if you used a firework.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.dontConsumeFirework = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-consume")).description("Prevents fireworks from being consumed when using Elytra Boost.")).defaultValue(true)).build());
        this.fireworkLevel = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("firework-duration")).description("The duration of the firework.")).defaultValue(0)).range(0, 255).sliderMax(255).build());
        this.playSound = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("play-sound")).description("Plays the firework sound when a boost is triggered.")).defaultValue(true)).build());
        this.keybind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("keybind")).description("The keybind to boost.")).action(this::boost).build());
        this.fireworks = new ArrayList<FireworkRocketEntity>();
    }

    @Override
    public void onDeactivate() {
        this.fireworks.clear();
    }

    @EventHandler
    private void onInteractItem(InteractItemEvent event) {
        ItemStack itemStack = this.mc.player.getStackInHand(event.hand);
        if (itemStack.getItem() instanceof FireworkRocketItem && this.dontConsumeFirework.get().booleanValue()) {
            event.toReturn = ActionResult.PASS;
            this.boost();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.fireworks.removeIf(Entity::isRemoved);
    }

    private void boost() {
        if (!Utils.canUpdate()) {
            return;
        }
        if (this.mc.player.isFallFlying() && this.mc.currentScreen == null) {
            ItemStack itemStack = Items.FIREWORK_ROCKET.getDefaultStack();
            itemStack.set(DataComponentTypes.FIREWORKS, (Object)new FireworksComponent(this.fireworkLevel.get().intValue(), ((FireworksComponent)itemStack.get(DataComponentTypes.FIREWORKS)).comp_2392()));
            FireworkRocketEntity entity = new FireworkRocketEntity((World)this.mc.world, itemStack, (LivingEntity)this.mc.player);
            this.fireworks.add(entity);
            if (this.playSound.get().booleanValue()) {
                this.mc.world.playSoundFromEntity((PlayerEntity)this.mc.player, (Entity)entity, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0f, 1.0f);
            }
            this.mc.world.addEntity((Entity)entity);
        }
    }

    public boolean isFirework(FireworkRocketEntity firework) {
        return this.isActive() && this.fireworks.contains(firework);
    }
}

