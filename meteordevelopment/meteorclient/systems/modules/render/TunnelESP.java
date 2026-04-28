/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.ChunkPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.Heightmap$Type
 *  net.minecraft.world.World
 *  net.minecraft.world.chunk.Chunk
 *  net.minecraft.world.chunk.ChunkSection
 *  net.minecraft.world.chunk.ChunkStatus
 */
package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.Dir;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;

public class TunnelESP
extends Module {
    private static final BlockPos.Mutable BP = new BlockPos.Mutable();
    private static final Direction[] DIRECTIONS = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
    private final SettingGroup sgGeneral;
    private final Setting<Double> height;
    private final Setting<Boolean> connected;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Long2ObjectMap<TChunk> chunks;

    public TunnelESP() {
        super(Categories.Render, "tunnel-esp", "Highlights tunnels.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.height = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("height")).description("Height of the rendered box.")).defaultValue(0.1).sliderMax(2.0).build());
        this.connected = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("connected")).description("If neighbouring holes should be connected.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 175, 25, 50)).build());
        this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 175, 25, 255)).build());
        this.chunks = new Long2ObjectOpenHashMap();
    }

    @Override
    public void onDeactivate() {
        this.chunks.clear();
    }

    private static int pack(int x, int y, int z) {
        return (x & 0xFF) << 24 | (y & 0xFFFF) << 8 | z & 0xFF;
    }

    private static byte getPackedX(int p) {
        return (byte)(p >> 24 & 0xFF);
    }

    private static short getPackedY(int p) {
        return (short)(p >> 8 & 0xFFFF);
    }

    private static byte getPackedZ(int p) {
        return (byte)(p & 0xFF);
    }

    private void searchChunk(Chunk chunk, TChunk tChunk) {
        Context ctx = new Context();
        IntOpenHashSet set = new IntOpenHashSet();
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();
        int endX = chunk.getPos().getEndX();
        int endZ = chunk.getPos().getEndZ();
        for (int x = startX; x <= endX; ++x) {
            for (int z = startZ; z <= endZ; ++z) {
                int height = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(x - startX, z - startZ);
                for (short y = (short)this.mc.world.getBottomY(); y < height; y = (short)(y + 1)) {
                    if (!this.isTunnel(ctx, x, y, z)) continue;
                    set.add(TunnelESP.pack(x - startX, y, z - startZ));
                }
            }
        }
        IntOpenHashSet positions = new IntOpenHashSet();
        IntIterator it = set.iterator();
        while (it.hasNext()) {
            int packed = it.nextInt();
            byte x = TunnelESP.getPackedX(packed);
            short y = TunnelESP.getPackedY(packed);
            byte z = TunnelESP.getPackedZ(packed);
            if (x == 0 || x == 15 || z == 0 || z == 15) {
                positions.add(packed);
                continue;
            }
            boolean has = false;
            for (Direction dir : DIRECTIONS) {
                if (!set.contains(TunnelESP.pack(x + dir.getOffsetX(), y, z + dir.getOffsetZ()))) continue;
                has = true;
                break;
            }
            if (!has) continue;
            positions.add(packed);
        }
        tChunk.positions = positions;
    }

    private boolean isTunnel(Context ctx, int x, int y, int z) {
        if (!this.canWalkIn(ctx, x, y, z)) {
            return false;
        }
        TunnelSide s1 = this.getTunnelSide(ctx, x + 1, y, z);
        if (s1 == TunnelSide.PartiallyBlocked) {
            return false;
        }
        TunnelSide s2 = this.getTunnelSide(ctx, x - 1, y, z);
        if (s2 == TunnelSide.PartiallyBlocked) {
            return false;
        }
        TunnelSide s3 = this.getTunnelSide(ctx, x, y, z + 1);
        if (s3 == TunnelSide.PartiallyBlocked) {
            return false;
        }
        TunnelSide s4 = this.getTunnelSide(ctx, x, y, z - 1);
        if (s4 == TunnelSide.PartiallyBlocked) {
            return false;
        }
        return s1 == TunnelSide.Walkable && s2 == TunnelSide.Walkable && s3 == TunnelSide.FullyBlocked && s4 == TunnelSide.FullyBlocked || s1 == TunnelSide.FullyBlocked && s2 == TunnelSide.FullyBlocked && s3 == TunnelSide.Walkable && s4 == TunnelSide.Walkable;
    }

    private TunnelSide getTunnelSide(Context ctx, int x, int y, int z) {
        if (this.canWalkIn(ctx, x, y, z)) {
            return TunnelSide.Walkable;
        }
        if (!this.canWalkThrough(ctx, x, y, z) && !this.canWalkThrough(ctx, x, y + 1, z)) {
            return TunnelSide.FullyBlocked;
        }
        return TunnelSide.PartiallyBlocked;
    }

    private boolean canWalkOn(Context ctx, int x, int y, int z) {
        BlockState state = ctx.get(x, y, z);
        if (state.isAir()) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return !state.getCollisionShape((BlockView)this.mc.world, (BlockPos)BP.set(x, y, z)).isEmpty();
    }

    private boolean canWalkThrough(Context ctx, int x, int y, int z) {
        BlockState state = ctx.get(x, y, z);
        if (state.isAir()) {
            return true;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        return state.getCollisionShape((BlockView)this.mc.world, (BlockPos)BP.set(x, y, z)).isEmpty();
    }

    private boolean canWalkIn(Context ctx, int x, int y, int z) {
        if (!this.canWalkOn(ctx, x, y - 1, z)) {
            return false;
        }
        if (!this.canWalkThrough(ctx, x, y, z)) {
            return false;
        }
        if (this.canWalkThrough(ctx, x, y + 2, z)) {
            return false;
        }
        return this.canWalkThrough(ctx, x, y + 1, z);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onTick(TickEvent.Post event) {
        Long2ObjectMap<TChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            for (TChunk tChunk2 : this.chunks.values()) {
                tChunk2.marked = false;
            }
            int added = 0;
            for (Chunk chunk : Utils.chunks(true)) {
                long key = ChunkPos.toLong((int)chunk.getPos().x, (int)chunk.getPos().z);
                if (this.chunks.containsKey(key)) {
                    ((TChunk)this.chunks.get((long)key)).marked = true;
                    continue;
                }
                if (added >= 48) continue;
                TChunk tChunk3 = new TChunk(chunk.getPos().x, chunk.getPos().z);
                this.chunks.put(tChunk3.getKey(), (Object)tChunk3);
                MeteorExecutor.execute(() -> this.searchChunk(chunk, tChunk3));
                ++added;
            }
            this.chunks.values().removeIf(tChunk -> !tChunk.marked);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        Long2ObjectMap<TChunk> long2ObjectMap = this.chunks;
        synchronized (long2ObjectMap) {
            for (TChunk chunk : this.chunks.values()) {
                chunk.render(event.renderer);
            }
        }
    }

    private boolean chunkContains(TChunk chunk, int x, int y, int z) {
        int key;
        if (x == -1) {
            chunk = (TChunk)this.chunks.get(ChunkPos.toLong((int)(chunk.x - 1), (int)chunk.z));
            key = TunnelESP.pack(15, y, z);
        } else if (x == 16) {
            chunk = (TChunk)this.chunks.get(ChunkPos.toLong((int)(chunk.x + 1), (int)chunk.z));
            key = TunnelESP.pack(0, y, z);
        } else if (z == -1) {
            chunk = (TChunk)this.chunks.get(ChunkPos.toLong((int)chunk.x, (int)(chunk.z - 1)));
            key = TunnelESP.pack(x, y, 15);
        } else if (z == 16) {
            chunk = (TChunk)this.chunks.get(ChunkPos.toLong((int)chunk.x, (int)(chunk.z + 1)));
            key = TunnelESP.pack(x, y, 0);
        } else {
            key = TunnelESP.pack(x, y, z);
        }
        return chunk != null && chunk.positions != null && chunk.positions.contains(key);
    }

    private static class Context {
        private final World world;
        private Chunk lastChunk;

        public Context() {
            this.world = MeteorClient.mc.world;
        }

        public BlockState get(int x, int y, int z) {
            if (this.world.isOutOfHeightLimit(y)) {
                return Blocks.VOID_AIR.getDefaultState();
            }
            int cx = x >> 4;
            int cz = z >> 4;
            Chunk chunk = this.lastChunk != null && this.lastChunk.getPos().x == cx && this.lastChunk.getPos().z == cz ? this.lastChunk : this.world.getChunk(cx, cz, ChunkStatus.FULL, false);
            if (chunk == null) {
                return Blocks.VOID_AIR.getDefaultState();
            }
            ChunkSection section = chunk.getSectionArray()[chunk.getSectionIndex(y)];
            if (section == null) {
                return Blocks.VOID_AIR.getDefaultState();
            }
            this.lastChunk = chunk;
            return section.getBlockState(x & 0xF, y & 0xF, z & 0xF);
        }
    }

    private class TChunk {
        private final int x;
        private final int z;
        public IntSet positions;
        public boolean marked;

        public TChunk(int x, int z) {
            this.x = x;
            this.z = z;
            this.marked = true;
        }

        public void render(Renderer3D renderer) {
            if (this.positions == null) {
                return;
            }
            IntIterator it = this.positions.iterator();
            while (it.hasNext()) {
                int pos = it.nextInt();
                int x = TunnelESP.getPackedX(pos);
                short y = TunnelESP.getPackedY(pos);
                int z = TunnelESP.getPackedZ(pos);
                int excludeDir = 0;
                if (TunnelESP.this.connected.get().booleanValue()) {
                    for (Direction dir : DIRECTIONS) {
                        if (!TunnelESP.this.chunkContains(this, x + dir.getOffsetX(), y, z + dir.getOffsetZ())) continue;
                        excludeDir |= Dir.get(dir);
                    }
                }
                renderer.box(x += this.x * 16, y, z += this.z * 16, x + 1, (double)y + TunnelESP.this.height.get(), z + 1, TunnelESP.this.sideColor.get(), TunnelESP.this.lineColor.get(), TunnelESP.this.shapeMode.get(), excludeDir);
            }
        }

        public long getKey() {
            return ChunkPos.toLong((int)this.x, (int)this.z);
        }
    }

    private static enum TunnelSide {
        Walkable,
        PartiallyBlocked,
        FullyBlocked;

    }
}

