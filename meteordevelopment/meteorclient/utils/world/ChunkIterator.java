/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.chunk.Chunk
 */
package meteordevelopment.meteorclient.utils.world;

import java.util.Iterator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.ClientChunkManagerAccessor;
import meteordevelopment.meteorclient.mixin.ClientChunkMapAccessor;
import net.minecraft.world.chunk.Chunk;

public class ChunkIterator
implements Iterator<Chunk> {
    private final ClientChunkMapAccessor map;
    private final boolean onlyWithLoadedNeighbours;
    private int i;
    private Chunk chunk;

    public ChunkIterator(boolean onlyWithLoadedNeighbours) {
        this.map = (ClientChunkMapAccessor)((ClientChunkManagerAccessor)MeteorClient.mc.world.getChunkManager()).getChunks();
        this.i = 0;
        this.onlyWithLoadedNeighbours = onlyWithLoadedNeighbours;
        this.getNext();
    }

    private Chunk getNext() {
        Chunk prev = this.chunk;
        this.chunk = null;
        while (this.i < this.map.getChunks().length()) {
            this.chunk = (Chunk)this.map.getChunks().get(this.i++);
            if (this.chunk == null || this.onlyWithLoadedNeighbours && !this.isInRadius(this.chunk)) continue;
            break;
        }
        return prev;
    }

    private boolean isInRadius(Chunk chunk) {
        int x = chunk.getPos().x;
        int z = chunk.getPos().z;
        return MeteorClient.mc.world.getChunkManager().isChunkLoaded(x + 1, z) && MeteorClient.mc.world.getChunkManager().isChunkLoaded(x - 1, z) && MeteorClient.mc.world.getChunkManager().isChunkLoaded(x, z + 1) && MeteorClient.mc.world.getChunkManager().isChunkLoaded(x, z - 1);
    }

    @Override
    public boolean hasNext() {
        return this.chunk != null;
    }

    @Override
    public Chunk next() {
        return this.getNext();
    }
}

