/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.ChunkPos
 *  net.minecraft.world.chunk.Chunk
 */
package meteordevelopment.meteorclient.systems.modules.render.blockesp;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.BlockUpdateEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.GenericSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPChunk;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.RainbowColors;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

public class BlockESP
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<Block>> blocks;
    private final Setting<ESPBlockData> defaultBlockConfig;
    private final Setting<Map<Block, ESPBlockData>> blockConfigs;
    private final Setting<Boolean> tracers;
    private final Setting<Boolean> activatedSpawners;
    private final BlockPos.Mutable blockPos;
    private final Long2ObjectMap<ESPChunk> chunks;
    private final Set<ESPGroup> groups;
    private final ExecutorService workerThread;
    private Dimension lastDimension;

    public BlockESP() {
        super(Categories.Render, "block-esp", "Renders specified blocks through walls.", "search");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Blocks to search for.")).onChanged(blocks1 -> {
            if (this.isActive() && Utils.canUpdate()) {
                this.onActivate();
            }
        })).build());
        this.defaultBlockConfig = this.sgGeneral.add(((GenericSetting.Builder)((GenericSetting.Builder)((GenericSetting.Builder)new GenericSetting.Builder().name("default-block-config")).description("Default block config.")).defaultValue(new ESPBlockData(ShapeMode.Lines, new SettingColor(0, 255, 200), new SettingColor(0, 255, 200, 25), true, new SettingColor(0, 255, 200, 125)))).build());
        this.blockConfigs = this.sgGeneral.add(((BlockDataSetting.Builder)((BlockDataSetting.Builder)new BlockDataSetting.Builder().name("block-configs")).description("Config for each block.")).defaultData(this.defaultBlockConfig).build());
        this.tracers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tracers")).description("Render tracer lines.")).defaultValue(false)).build());
        this.activatedSpawners = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("activated-spawners")).description("Only highlights activated spawners")).defaultValue(true)).visible(() -> this.blocks.get().contains(Blocks.SPAWNER))).build());
        this.blockPos = new BlockPos.Mutable();
        this.chunks = new Long2ObjectOpenHashMap();
        this.groups = new ReferenceOpenHashSet();
        this.workerThread = Executors.newSingleThreadExecutor();
        RainbowColors.register(this::onTickRainbow);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onActivate() {
        Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            this.chunks.clear();
            this.groups.clear();
        }
        for (Chunk chunk : Utils.chunks()) {
            this.searchChunk(chunk);
        }
        this.lastDimension = PlayerUtils.getDimension();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onDeactivate() {
        Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            this.chunks.clear();
            this.groups.clear();
        }
    }

    private void onTickRainbow() {
        if (!this.isActive()) {
            return;
        }
        this.defaultBlockConfig.get().tickRainbow();
        for (ESPBlockData blockData : this.blockConfigs.get().values()) {
            blockData.tickRainbow();
        }
    }

    ESPBlockData getBlockData(Block block) {
        ESPBlockData blockData = this.blockConfigs.get().get(block);
        return blockData == null ? this.defaultBlockConfig.get() : blockData;
    }

    private void updateChunk(int x, int z) {
        ESPChunk chunk = (ESPChunk)this.chunks.get(ChunkPos.toLong((int)x, (int)z));
        if (chunk != null) {
            chunk.update();
        }
    }

    private void updateBlock(int x, int y, int z) {
        ESPChunk chunk = (ESPChunk)this.chunks.get(ChunkPos.toLong((int)(x >> 4), (int)(z >> 4)));
        if (chunk != null) {
            chunk.update(x, y, z);
        }
    }

    public ESPBlock getBlock(int x, int y, int z) {
        ESPChunk chunk = (ESPChunk)this.chunks.get(ChunkPos.toLong((int)(x >> 4), (int)(z >> 4)));
        return chunk == null ? null : chunk.get(x, y, z);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ESPGroup newGroup(Block block) {
        Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            ESPGroup group = new ESPGroup(block);
            this.groups.add(group);
            return group;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeGroup(ESPGroup group) {
        Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            this.groups.remove(group);
        }
    }

    @EventHandler
    private void onChunkData(ChunkDataEvent event) {
        this.searchChunk((Chunk)event.chunk());
    }

    private void searchChunk(Chunk chunk) {
        this.workerThread.submit(() -> {
            if (!this.isActive()) {
                return;
            }
            ESPChunk schunk = ESPChunk.searchChunk(chunk, this.blocks.get(), this.activatedSpawners.get());
            if (schunk.size() > 0) {
                Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
                synchronized (long2ObjectMap) {
                    this.chunks.put(chunk.getPos().toLong(), (Object)schunk);
                    schunk.update();
                    this.updateChunk(chunk.getPos().x - 1, chunk.getPos().z);
                    this.updateChunk(chunk.getPos().x + 1, chunk.getPos().z);
                    this.updateChunk(chunk.getPos().x, chunk.getPos().z - 1);
                    this.updateChunk(chunk.getPos().x, chunk.getPos().z + 1);
                }
            }
        });
    }

    @EventHandler
    private void onBlockUpdate(BlockUpdateEvent event) {
        boolean removed;
        int bx = event.pos.getX();
        int by = event.pos.getY();
        int bz = event.pos.getZ();
        int chunkX = bx >> 4;
        int chunkZ = bz >> 4;
        long key = ChunkPos.toLong((int)chunkX, (int)chunkZ);
        boolean added = this.blocks.get().contains(event.newState.getBlock()) && !this.blocks.get().contains(event.oldState.getBlock());
        boolean bl = removed = !added && !this.blocks.get().contains(event.newState.getBlock()) && this.blocks.get().contains(event.oldState.getBlock());
        if (added || removed) {
            this.workerThread.submit(() -> {
                Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
                synchronized (long2ObjectMap) {
                    ESPChunk chunk = (ESPChunk)this.chunks.get(key);
                    if (chunk == null) {
                        chunk = new ESPChunk(chunkX, chunkZ);
                        if (chunk.shouldBeDeleted()) {
                            return;
                        }
                        this.chunks.put(key, (Object)chunk);
                    }
                    this.blockPos.set(bx, by, bz);
                    if (added) {
                        chunk.add((BlockPos)this.blockPos);
                    } else {
                        chunk.remove((BlockPos)this.blockPos);
                    }
                    for (int x = -1; x < 2; ++x) {
                        for (int z = -1; z < 2; ++z) {
                            for (int y = -1; y < 2; ++y) {
                                if (x == 0 && y == 0 && z == 0) continue;
                                this.updateBlock(bx + x, by + y, bz + z);
                            }
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        Dimension dimension = PlayerUtils.getDimension();
        if (this.lastDimension != dimension) {
            this.onActivate();
        }
        this.lastDimension = dimension;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onRender(Render3DEvent event) {
        Long2ObjectMap<ESPChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            ObjectIterator it = this.chunks.values().iterator();
            while (it.hasNext()) {
                ESPChunk chunk = (ESPChunk)it.next();
                if (chunk.shouldBeDeleted()) {
                    this.workerThread.submit(() -> {
                        for (ESPBlock block : chunk.blocks.values()) {
                            block.group.remove(block, false);
                            block.loaded = false;
                        }
                    });
                    it.remove();
                    continue;
                }
                chunk.render(event);
            }
            if (this.tracers.get().booleanValue()) {
                for (ESPGroup group : this.groups) {
                    group.render(event);
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        return "%s groups".formatted(this.groups.size());
    }
}

