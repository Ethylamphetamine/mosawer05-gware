/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.mob.EndermanEntity
 *  net.minecraft.entity.mob.ZombifiedPiglinEntity
 *  net.minecraft.entity.passive.WolfEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class TargetManager {
    private final Settings settings = new Settings();
    private final SettingGroup sgTargets = this.settings.createGroup("Targets");
    private final Setting<Double> range = this.sgTargets.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Max range to target.")).defaultValue(6.5).min(0.0).sliderMax(7.0).build());
    private final Setting<TargetMode> targetMode = this.sgTargets.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-mode")).description("How many targets to choose.")).defaultValue(TargetMode.Single)).build());
    private final Setting<TargetSortMode> targetSortMode = this.sgTargets.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-sort-mode")).description("How to sort the targets.")).defaultValue(TargetSortMode.ClosestAngle)).build());
    private final Setting<Integer> numTargets = this.sgTargets.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("num-targets")).description("Max range to target.")).defaultValue(2)).min(1).sliderMax(5).visible(() -> this.targetMode.get() == TargetMode.Multi)).build());
    private final Setting<Boolean> ignoreNakeds = this.sgTargets.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Ignore players with no items.")).defaultValue(true)).build());
    private final Setting<Boolean> ignorePassive = this.sgTargets.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-passive")).description("Does not attack passive mobs.")).defaultValue(false)).build());
    private Setting<Set<EntityType<?>>> validEntities = null;

    public TargetManager(Module module, boolean entityListFilter) {
        module.settings.groups.addAll(this.settings.groups);
        this.validEntities = this.sgTargets.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to target.")).onlyAttackable().defaultValue(EntityType.PLAYER).build());
    }

    public List<PlayerEntity> getPlayerTargets() {
        return this.getPlayerTargets(entity -> true);
    }

    public List<PlayerEntity> getPlayerTargets(Predicate<PlayerEntity> isGood) {
        List<PlayerEntity> entities = new ArrayList<PlayerEntity>();
        Vec3d pos = MeteorClient.mc.player.getPos();
        Box box = new Box(pos.x - this.range.get(), pos.y - this.range.get(), pos.z - this.range.get(), pos.x + this.range.get(), pos.y + this.range.get(), pos.z + this.range.get());
        double rangeSqr = this.range.get() * this.range.get();
        for (PlayerEntity entity : MeteorClient.mc.world.getEntitiesByClass(PlayerEntity.class, box, e -> !e.isRemoved())) {
            if (entity == null || !(entity.getBoundingBox().squaredMagnitude(pos) < rangeSqr) || !isGood.test(entity) || this.ignoreNakeds.get().booleanValue() && ((ItemStack)entity.getInventory().armor.get(0)).isEmpty() && ((ItemStack)entity.getInventory().armor.get(1)).isEmpty() && ((ItemStack)entity.getInventory().armor.get(2)).isEmpty() && ((ItemStack)entity.getInventory().armor.get(3)).isEmpty() || entity.isCreative() || !Friends.get().shouldAttack(entity) || entity.equals((Object)MeteorClient.mc.player) || entity.equals((Object)MeteorClient.mc.cameraEntity) || entity.isDead()) continue;
            entities.add(entity);
        }
        entities.sort(this.targetSortMode.get());
        switch (this.targetMode.get().ordinal()) {
            case 0: {
                if (entities.size() < 1) break;
                entities = List.of((PlayerEntity)entities.get(0));
                break;
            }
            case 1: {
                if (entities.size() <= this.numTargets.get()) break;
                entities.subList(this.numTargets.get(), entities.size()).clear();
                break;
            }
        }
        return entities;
    }

    public List<Entity> getEntityTargets() {
        return this.getEntityTargets(entity -> true);
    }

    public List<Entity> getEntityTargets(Predicate<Entity> isGood) {
        List<Entity> entities = new ArrayList<Entity>();
        Vec3d pos = MeteorClient.mc.player.getPos();
        Box box = new Box(pos.x - this.range.get(), pos.y - this.range.get(), pos.z - this.range.get(), pos.x + this.range.get(), pos.y + this.range.get(), pos.z + this.range.get());
        double rangeSqr = this.range.get() * this.range.get();
        for (Entity entity : MeteorClient.mc.world.getEntitiesByClass(Entity.class, box, e -> !e.isRemoved())) {
            PlayerEntity player;
            WolfEntity wolf;
            ZombifiedPiglinEntity piglin;
            EndermanEntity enderman;
            LivingEntity livingEntity;
            if (entity == null || !(entity.getBoundingBox().squaredMagnitude(pos) < rangeSqr) || !isGood.test(entity) || entity.equals((Object)MeteorClient.mc.player) || entity.equals((Object)MeteorClient.mc.cameraEntity) || entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isDead() || !entity.isAlive() || this.validEntities != null && !this.validEntities.get().contains(entity.getType()) || this.ignorePassive.get().booleanValue() && (entity instanceof EndermanEntity && !(enderman = (EndermanEntity)entity).isAttacking() || entity instanceof ZombifiedPiglinEntity && !(piglin = (ZombifiedPiglinEntity)entity).isAttacking() || entity instanceof WolfEntity && !(wolf = (WolfEntity)entity).isAttacking()) || entity instanceof PlayerEntity && ((player = (PlayerEntity)entity).isCreative() || !Friends.get().shouldAttack(player))) continue;
            entities.add(entity);
        }
        entities.sort(this.targetSortMode.get());
        switch (this.targetMode.get().ordinal()) {
            case 0: {
                if (entities.size() < 1) break;
                entities = List.of((Entity)entities.get(0));
                break;
            }
            case 1: {
                if (entities.size() <= this.numTargets.get()) break;
                entities.subList(this.numTargets.get(), entities.size()).clear();
                break;
            }
        }
        return entities;
    }

    public static enum TargetMode {
        Single,
        Multi,
        All;

    }

    public static enum TargetSortMode implements Comparator<Entity>
    {
        LowestDistance(Comparator.comparingDouble(entity -> entity.getEyePos().squaredDistanceTo(MeteorClient.mc.player.getEyePos()))),
        HighestDistance((e1, e2) -> Double.compare(e2.getEyePos().squaredDistanceTo(MeteorClient.mc.player.getEyePos()), e1.getEyePos().squaredDistanceTo(MeteorClient.mc.player.getEyePos()))),
        ClosestAngle(TargetSortMode::sortAngle);

        private final Comparator<Entity> comparator;

        private TargetSortMode(Comparator<Entity> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Entity o1, Entity o2) {
            return this.comparator.compare(o1, o2);
        }

        private static int sortAngle(Entity e1, Entity e2) {
            float[] angle1 = MeteorClient.ROTATION.getRotation(e1.getEyePos());
            float[] angle2 = MeteorClient.ROTATION.getRotation(e1.getEyePos());
            double e1yaw = Math.abs(angle1[0] - MeteorClient.mc.player.getYaw());
            double e2yaw = Math.abs(angle2[0] - MeteorClient.mc.player.getYaw());
            return Double.compare(e1yaw * e1yaw, e2yaw * e2yaw);
        }
    }
}

