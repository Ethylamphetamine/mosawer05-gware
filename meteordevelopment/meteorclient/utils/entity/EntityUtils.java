/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.LongBidirectionalIterator
 *  it.unimi.dsi.fastutil.longs.LongSortedSet
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.fluid.Fluid
 *  net.minecraft.fluid.Fluids
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.ChunkSectionPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.GameMode
 *  net.minecraft.world.entity.EntityLookup
 *  net.minecraft.world.entity.EntityTrackingSection
 *  net.minecraft.world.entity.SectionedEntityCache
 *  net.minecraft.world.entity.SimpleEntityLookup
 */
package meteordevelopment.meteorclient.utils.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.EntityTrackingSectionAccessor;
import meteordevelopment.meteorclient.mixin.SectionedEntityCacheAccessor;
import meteordevelopment.meteorclient.mixin.SimpleEntityLookupAccessor;
import meteordevelopment.meteorclient.mixin.WorldAccessor;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.entity.SimpleEntityLookup;

public class EntityUtils {
    private static BlockPos.Mutable testPos = new BlockPos.Mutable();

    private EntityUtils() {
    }

    public static boolean isAttackable(EntityType<?> type) {
        return type != EntityType.AREA_EFFECT_CLOUD && type != EntityType.ARROW && type != EntityType.FALLING_BLOCK && type != EntityType.FIREWORK_ROCKET && type != EntityType.ITEM && type != EntityType.LLAMA_SPIT && type != EntityType.SPECTRAL_ARROW && type != EntityType.ENDER_PEARL && type != EntityType.EXPERIENCE_BOTTLE && type != EntityType.POTION && type != EntityType.TRIDENT && type != EntityType.LIGHTNING_BOLT && type != EntityType.FISHING_BOBBER && type != EntityType.EXPERIENCE_ORB && type != EntityType.EGG;
    }

    public static boolean isRideable(EntityType<?> type) {
        return type == EntityType.MINECART || type == EntityType.BOAT || type == EntityType.CAMEL || type == EntityType.DONKEY || type == EntityType.HORSE || type == EntityType.LLAMA || type == EntityType.MULE || type == EntityType.PIG || type == EntityType.SKELETON_HORSE || type == EntityType.STRIDER || type == EntityType.ZOMBIE_HORSE;
    }

    public static float getTotalHealth(LivingEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    public static int getPing(PlayerEntity player) {
        if (MeteorClient.mc.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) {
            return 0;
        }
        return playerListEntry.getLatency();
    }

    public static GameMode getGameMode(PlayerEntity player) {
        if (player == null) {
            return null;
        }
        PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        if (playerListEntry == null) {
            return null;
        }
        return playerListEntry.getGameMode();
    }

    public static boolean isAboveWater(Entity entity) {
        BlockState state;
        BlockPos.Mutable blockPos = entity.getBlockPos().mutableCopy();
        for (int i = 0; i < 64 && !(state = MeteorClient.mc.world.getBlockState((BlockPos)blockPos)).blocksMovement(); ++i) {
            Fluid fluid = state.getFluidState().getFluid();
            if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
                return true;
            }
            blockPos.move(0, -1, 0);
        }
        return false;
    }

    public static boolean isInRenderDistance(Entity entity) {
        if (entity == null) {
            return false;
        }
        return EntityUtils.isInRenderDistance(entity.getX(), entity.getZ());
    }

    public static boolean isInRenderDistance(BlockEntity entity) {
        if (entity == null) {
            return false;
        }
        return EntityUtils.isInRenderDistance(entity.getPos().getX(), entity.getPos().getZ());
    }

    public static boolean isInRenderDistance(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        return EntityUtils.isInRenderDistance(pos.getX(), pos.getZ());
    }

    public static boolean isInRenderDistance(double posX, double posZ) {
        double x = Math.abs(MeteorClient.mc.gameRenderer.getCamera().getPos().x - posX);
        double z = Math.abs(MeteorClient.mc.gameRenderer.getCamera().getPos().z - posZ);
        double d = ((Integer)MeteorClient.mc.options.getViewDistance().getValue() + 1) * 16;
        return x < d && z < d;
    }

    public static BlockPos getCityBlock(PlayerEntity self, PlayerEntity player, BlockPos excludeBlockPos) {
        if (player == null) {
            return null;
        }
        double bestDistanceSquared = 36.0;
        Direction bestDirection = null;
        for (Direction direction : Direction.HORIZONTAL) {
            testPos.set((Vec3i)player.getBlockPos().offset(direction));
            if (excludeBlockPos != null && testPos.equals((Object)excludeBlockPos)) continue;
            Block block = MeteorClient.mc.world.getBlockState((BlockPos)testPos).getBlock();
            if (block != Blocks.OBSIDIAN && block != Blocks.NETHERITE_BLOCK && block != Blocks.CRYING_OBSIDIAN && block != Blocks.RESPAWN_ANCHOR && block != Blocks.ANCIENT_DEBRIS) {
                if (block != Blocks.AIR || MeteorClient.mc.world.getBlockState(player.getBlockPos()).getBlock() != Blocks.OBSIDIAN) continue;
                return player.getBlockPos();
            }
            double testDistanceSquared = PlayerUtils.squaredDistanceTo((BlockPos)testPos);
            for (Direction direction2 : Direction.HORIZONTAL) {
                BlockPos selfBlockPos = self.getBlockPos().offset(direction2);
                if (!selfBlockPos.equals((Object)testPos)) continue;
                testDistanceSquared += 2.0;
            }
            if (!(testDistanceSquared < bestDistanceSquared)) continue;
            bestDistanceSquared = testDistanceSquared;
            bestDirection = direction;
        }
        if (bestDirection == null) {
            return null;
        }
        return player.getBlockPos().offset(bestDirection);
    }

    public static String getName(Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof PlayerEntity) {
            return entity.getName().getString();
        }
        return entity.getType().getName().getString();
    }

    public static Color getColorFromDistance(Entity entity) {
        int g;
        int r;
        Color distanceColor = new Color(255, 255, 255);
        double distance = PlayerUtils.distanceToCamera(entity);
        double percent = distance / 60.0;
        if (percent < 0.0 || percent > 1.0) {
            distanceColor.set(0, 255, 0, 255);
            return distanceColor;
        }
        if (percent < 0.5) {
            r = 255;
            g = (int)(255.0 * percent / 0.5);
        } else {
            g = 255;
            r = 255 - (int)(255.0 * (percent - 0.5) / 0.5);
        }
        distanceColor.set(r, g, 0, 255);
        return distanceColor;
    }

    public static boolean intersectsWithEntity(Box box, Predicate<Entity> predicate) {
        EntityLookup<Entity> entityLookup = ((WorldAccessor)MeteorClient.mc.world).getEntityLookup();
        if (entityLookup instanceof SimpleEntityLookup) {
            SimpleEntityLookup simpleEntityLookup = (SimpleEntityLookup)entityLookup;
            SectionedEntityCache cache = ((SimpleEntityLookupAccessor)simpleEntityLookup).getCache();
            LongSortedSet trackedPositions = ((SectionedEntityCacheAccessor)cache).getTrackedPositions();
            Long2ObjectMap trackingSections = ((SectionedEntityCacheAccessor)cache).getTrackingSections();
            int i = ChunkSectionPos.getSectionCoord((double)(box.minX - 2.0));
            int j = ChunkSectionPos.getSectionCoord((double)(box.minY - 2.0));
            int k = ChunkSectionPos.getSectionCoord((double)(box.minZ - 2.0));
            int l = ChunkSectionPos.getSectionCoord((double)(box.maxX + 2.0));
            int m = ChunkSectionPos.getSectionCoord((double)(box.maxY + 2.0));
            int n = ChunkSectionPos.getSectionCoord((double)(box.maxZ + 2.0));
            for (int o = i; o <= l; ++o) {
                long p = ChunkSectionPos.asLong((int)o, (int)0, (int)0);
                long q = ChunkSectionPos.asLong((int)o, (int)-1, (int)-1);
                LongBidirectionalIterator longIterator = trackedPositions.subSet(p, q + 1L).iterator();
                while (longIterator.hasNext()) {
                    EntityTrackingSection entityTrackingSection;
                    long r = longIterator.nextLong();
                    int s = ChunkSectionPos.unpackY((long)r);
                    int t = ChunkSectionPos.unpackZ((long)r);
                    if (s < j || s > m || t < k || t > n || (entityTrackingSection = (EntityTrackingSection)trackingSections.get(r)) == null || !entityTrackingSection.getStatus().shouldTrack()) continue;
                    for (Entity entity2 : ((EntityTrackingSectionAccessor)entityTrackingSection).getCollection()) {
                        if (!entity2.getBoundingBox().intersects(box) || !predicate.test(entity2)) continue;
                        return true;
                    }
                }
            }
            return false;
        }
        AtomicBoolean found = new AtomicBoolean(false);
        entityLookup.forEachIntersects(box, entity -> {
            if (!found.get() && predicate.test((Entity)entity)) {
                found.set(true);
            }
        });
        return found.get();
    }

    public static EntityType<?> getGroup(Entity entity) {
        return entity.getType();
    }
}

