/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class AutoBreed
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> range;
    private final Setting<Hand> hand;
    private final Setting<EntityAge> mobAgeFilter;
    private final List<Entity> animalsFed;

    public AutoBreed() {
        super(Categories.World, "auto-breed", "Automatically breeds specified animals.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to breed.")).defaultValue(EntityType.HORSE, EntityType.DONKEY, EntityType.COW, EntityType.MOOSHROOM, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF, EntityType.CAT, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.TURTLE, EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.STRIDER, EntityType.HOGLIN).onlyAttackable().build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("How far away the animals can be to be bred.")).min(0.0).defaultValue(4.5).build());
        this.hand = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("hand-for-breeding")).description("The hand to use for breeding.")).defaultValue(Hand.MAIN_HAND)).build());
        this.mobAgeFilter = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mob-age-filter")).description("Determines the age of the mobs to target (baby, adult, or both).")).defaultValue(EntityAge.Adult)).build());
        this.animalsFed = new ArrayList<Entity>();
    }

    @Override
    public void onActivate() {
        this.animalsFed.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        block5: for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof AnimalEntity)) continue;
            AnimalEntity animal = (AnimalEntity)entity;
            if (!this.entities.get().contains(animal.getType())) continue;
            switch (this.mobAgeFilter.get().ordinal()) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    if (!animal.isBaby()) continue block5;
                    break;
                }
                case 1: {
                    if (animal.isBaby()) continue block5;
                }
                case 2: 
            }
            if (this.animalsFed.contains(animal) || !PlayerUtils.isWithin((Entity)animal, (double)this.range.get()) || !animal.isBreedingItem(this.hand.get() == Hand.MAIN_HAND ? this.mc.player.getMainHandStack() : this.mc.player.getOffHandStack())) continue;
            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> {
                this.mc.interactionManager.interactEntity((PlayerEntity)this.mc.player, (Entity)animal, this.hand.get());
                this.mc.player.swingHand(this.hand.get());
                this.animalsFed.add((Entity)animal);
            });
            return;
        }
    }

    public static enum EntityAge {
        Baby,
        Adult,
        Both;

    }
}

