/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.chunk.Chunk
 */
package meteordevelopment.meteorclient.utils.world;

import java.util.Iterator;
import java.util.Map;
import meteordevelopment.meteorclient.mixin.ChunkAccessor;
import meteordevelopment.meteorclient.utils.world.ChunkIterator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class BlockEntityIterator
implements Iterator<BlockEntity> {
    private final Iterator<Chunk> chunks = new ChunkIterator(false);
    private Iterator<BlockEntity> blockEntities;

    public BlockEntityIterator() {
        this.nextChunk();
    }

    private void nextChunk() {
        while (this.chunks.hasNext()) {
            Map<BlockPos, BlockEntity> blockEntityMap = ((ChunkAccessor)this.chunks.next()).getBlockEntities();
            if (blockEntityMap.isEmpty()) continue;
            this.blockEntities = blockEntityMap.values().iterator();
            break;
        }
    }

    @Override
    public boolean hasNext() {
        if (this.blockEntities == null) {
            return false;
        }
        if (this.blockEntities.hasNext()) {
            return true;
        }
        this.nextChunk();
        return this.blockEntities.hasNext();
    }

    @Override
    public BlockEntity next() {
        return this.blockEntities.next();
    }
}

