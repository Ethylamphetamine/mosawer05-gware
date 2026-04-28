/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.Saddleable
 *  net.minecraft.entity.mob.SkeletonHorseEntity
 *  net.minecraft.entity.mob.ZombieHorseEntity
 *  net.minecraft.entity.passive.LlamaEntity
 *  net.minecraft.entity.passive.PigEntity
 *  net.minecraft.entity.passive.StriderEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.SpawnEggItem
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;

public class AutoMount
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> checkSaddle;
    private final Setting<Boolean> rotate;
    private final Setting<Set<EntityType<?>>> entities;

    public AutoMount() {
        super(Categories.World, "auto-mount", "Automatically mounts entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.checkSaddle = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("check-saddle")).description("Checks if the entity contains a saddle before mounting.")).defaultValue(false)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Faces the entity you mount.")).defaultValue(true)).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Rideable entities.")).filter(EntityUtils::isRideable).build());
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player.hasVehicle()) {
            return;
        }
        if (this.mc.player.isSneaking()) {
            return;
        }
        if (this.mc.player.getMainHandStack().getItem() instanceof SpawnEggItem) {
            return;
        }
        for (Entity entity : this.mc.world.getEntities()) {
            if (!this.entities.get().contains(entity.getType()) || !PlayerUtils.isWithin(entity, 4.0) || (entity instanceof PigEntity || entity instanceof SkeletonHorseEntity || entity instanceof StriderEntity || entity instanceof ZombieHorseEntity) && !((Saddleable)entity).isSaddled()) continue;
            if (!(entity instanceof LlamaEntity) && entity instanceof Saddleable) {
                Saddleable saddleable = (Saddleable)entity;
                if (this.checkSaddle.get().booleanValue() && !saddleable.isSaddled()) continue;
            }
            this.interact(entity);
            return;
        }
    }

    private void interact(Entity entity) {
        if (this.rotate.get().booleanValue()) {
            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> this.mc.interactionManager.interactEntity((PlayerEntity)this.mc.player, entity, Hand.MAIN_HAND));
        } else {
            this.mc.interactionManager.interactEntity((PlayerEntity)this.mc.player, entity, Hand.MAIN_HAND);
        }
    }
}

