/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 */
package meteordevelopment.meteorclient.utils.entity;

import java.util.Comparator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public enum SortPriority implements Comparator<Entity>
{
    LowestDistance(Comparator.comparingDouble(PlayerUtils::squaredDistanceTo)),
    HighestDistance((e1, e2) -> Double.compare(PlayerUtils.squaredDistanceTo(e2), PlayerUtils.squaredDistanceTo(e1))),
    LowestHealth(SortPriority::sortHealth),
    HighestHealth((e1, e2) -> SortPriority.sortHealth(e2, e1)),
    ClosestAngle(SortPriority::sortAngle);

    private final Comparator<Entity> comparator;

    private SortPriority(Comparator<Entity> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int compare(Entity o1, Entity o2) {
        return this.comparator.compare(o1, o2);
    }

    private static int sortHealth(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;
        if (!e1l && !e2l) {
            return 0;
        }
        if (e1l && !e2l) {
            return 1;
        }
        if (!e1l) {
            return -1;
        }
        return Float.compare(((LivingEntity)e1).getHealth(), ((LivingEntity)e2).getHealth());
    }

    private static int sortAngle(Entity e1, Entity e2) {
        boolean e1l = e1 instanceof LivingEntity;
        boolean e2l = e2 instanceof LivingEntity;
        if (!e1l && !e2l) {
            return 0;
        }
        if (e1l && !e2l) {
            return 1;
        }
        if (!e1l) {
            return -1;
        }
        double e1yaw = Math.abs(Rotations.getYaw(e1) - (double)MeteorClient.mc.player.getYaw());
        double e2yaw = Math.abs(Rotations.getYaw(e2) - (double)MeteorClient.mc.player.getYaw());
        double e1pitch = Math.abs(Rotations.getPitch(e1) - (double)MeteorClient.mc.player.getPitch());
        double e2pitch = Math.abs(Rotations.getPitch(e2) - (double)MeteorClient.mc.player.getPitch());
        return Double.compare(e1yaw * e1yaw + e1pitch * e1pitch, e2yaw * e2yaw + e2pitch * e2pitch);
    }
}

