/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.block.entity.LootableContainerBlockEntity
 *  net.minecraft.block.entity.MobSpawnerBlockEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.ChunkSectionPos
 *  net.minecraft.world.Heightmap$Type
 *  net.minecraft.world.chunk.Chunk
 */
package meteordevelopment.meteorclient.systems.modules.render.blockesp;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;

public class ESPChunk {
    private final int x;
    private final int z;
    public Long2ObjectMap<ESPBlock> blocks;

    public ESPChunk(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public ESPBlock get(int x, int y, int z) {
        return this.blocks == null ? null : (ESPBlock)this.blocks.get(ESPBlock.getKey(x, y, z));
    }

    public void add(BlockPos blockPos, boolean update) {
        ESPBlock block = new ESPBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (this.blocks == null) {
            this.blocks = new Long2ObjectOpenHashMap(64);
        }
        this.blocks.put(ESPBlock.getKey(blockPos), (Object)block);
        if (update) {
            block.update();
        }
    }

    public void add(BlockPos blockPos) {
        this.add(blockPos, true);
    }

    public void remove(BlockPos blockPos) {
        ESPBlock block;
        if (this.blocks != null && (block = (ESPBlock)this.blocks.remove(ESPBlock.getKey(blockPos))) != null) {
            block.group.remove(block);
        }
    }

    public void update() {
        if (this.blocks != null) {
            for (ESPBlock block : this.blocks.values()) {
                block.update();
            }
        }
    }

    public void update(int x, int y, int z) {
        ESPBlock block;
        if (this.blocks != null && (block = (ESPBlock)this.blocks.get(ESPBlock.getKey(x, y, z))) != null) {
            block.update();
        }
    }

    public int size() {
        return this.blocks == null ? 0 : this.blocks.size();
    }

    public boolean shouldBeDeleted() {
        int viewDist = Utils.getRenderDistance() + 1;
        int chunkX = ChunkSectionPos.getSectionCoord((int)MeteorClient.mc.player.getBlockPos().getX());
        int chunkZ = ChunkSectionPos.getSectionCoord((int)MeteorClient.mc.player.getBlockPos().getZ());
        return this.x > chunkX + viewDist || this.x < chunkX - viewDist || this.z > chunkZ + viewDist || this.z < chunkZ - viewDist;
    }

    public void render(Render3DEvent event) {
        if (this.blocks != null) {
            for (ESPBlock block : this.blocks.values()) {
                block.render(event);
            }
        }
    }

    public static ESPChunk searchChunk(Chunk chunk, List<Block> blocks, boolean activatedSpawners) {
        ESPChunk schunk = new ESPChunk(chunk.getPos().x, chunk.getPos().z);
        if (schunk.shouldBeDeleted()) {
            return schunk;
        }
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        for (int x = chunk.getPos().getStartX(); x <= chunk.getPos().getEndX(); ++x) {
            for (int z = chunk.getPos().getStartZ(); z <= chunk.getPos().getEndZ(); ++z) {
                int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x - chunk.getPos().getStartX(), z - chunk.getPos().getStartZ());
                for (int y = MeteorClient.mc.world.getBottomY(); y < height; ++y) {
                    BlockEntity blockEntity;
                    blockPos.set(x, y, z);
                    BlockState bs = chunk.getBlockState((BlockPos)blockPos);
                    if (!blocks.contains(bs.getBlock())) continue;
                    if (activatedSpawners && bs.isOf(Blocks.SPAWNER) && (blockEntity = chunk.getBlockEntity((BlockPos)blockPos)) instanceof MobSpawnerBlockEntity) {
                        MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity)blockEntity;
                        if (spawner.getLogic().spawnDelay == 20 || !ESPChunk.isChestNearSpawner((BlockPos)blockPos)) continue;
                        schunk.add((BlockPos)blockPos, false);
                        continue;
                    }
                    schunk.add((BlockPos)blockPos, false);
                }
            }
        }
        return schunk;
    }

    private static boolean isChestNearSpawner(BlockPos spawnerPos) {
        for (int dx = -3; dx <= 3; ++dx) {
            for (int dy = -3; dy <= 3; ++dy) {
                for (int dz = -3; dz <= 3; ++dz) {
                    BlockEntity blockEntity;
                    BlockPos checkPos = spawnerPos.add(dx, dy, dz);
                    if (!MeteorClient.mc.world.isChunkLoaded(checkPos) || (blockEntity = MeteorClient.mc.world.getBlockEntity(checkPos)) == null || !(blockEntity instanceof LootableContainerBlockEntity)) continue;
                    return true;
                }
            }
        }
        return false;
    }
}

