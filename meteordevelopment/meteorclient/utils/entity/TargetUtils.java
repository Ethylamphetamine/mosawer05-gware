/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.world.GameMode
 */
package meteordevelopment.meteorclient.utils.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

public class TargetUtils {
    private static final List<Entity> ENTITIES = new ArrayList<Entity>();

    private TargetUtils() {
    }

    @Nullable
    public static Entity get(Predicate<Entity> isGood, SortPriority sortPriority) {
        ENTITIES.clear();
        TargetUtils.getList(ENTITIES, isGood, sortPriority, 1);
        if (!ENTITIES.isEmpty()) {
            return ENTITIES.getFirst();
        }
        return null;
    }

    public static void getList(List<Entity> targetList, Predicate<Entity> isGood, SortPriority sortPriority, int maxCount) {
        targetList.clear();
        for (Entity entity : MeteorClient.mc.world.getEntities()) {
            if (entity == null || !isGood.test(entity)) continue;
            targetList.add(entity);
        }
        FakePlayerManager.forEach(fp -> {
            if (fp != null && isGood.test((Entity)fp)) {
                targetList.add((Entity)fp);
            }
        });
        targetList.sort(sortPriority);
        for (int i = targetList.size() - 1; i >= maxCount; --i) {
            targetList.remove(i);
        }
    }

    @Nullable
    public static PlayerEntity getPlayerTarget(double range, SortPriority priority) {
        if (!Utils.canUpdate()) {
            return null;
        }
        return (PlayerEntity)TargetUtils.get(entity -> {
            if (!(entity instanceof PlayerEntity) || entity == MeteorClient.mc.player) {
                return false;
            }
            if (((PlayerEntity)entity).isDead() || ((PlayerEntity)entity).getHealth() <= 0.0f) {
                return false;
            }
            if (!PlayerUtils.isWithin(entity, range)) {
                return false;
            }
            if (!Friends.get().shouldAttack((PlayerEntity)entity)) {
                return false;
            }
            return EntityUtils.getGameMode((PlayerEntity)entity) == GameMode.SURVIVAL || entity instanceof FakePlayerEntity;
        }, priority);
    }

    public static boolean isBadTarget(PlayerEntity target, double range) {
        if (target == null) {
            return true;
        }
        return !PlayerUtils.isWithin((Entity)target, range) || !target.isAlive() || target.isDead() || target.getHealth() <= 0.0f;
    }
}

