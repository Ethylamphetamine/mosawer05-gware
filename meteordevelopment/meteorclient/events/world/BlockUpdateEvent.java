/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.events.world;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockUpdateEvent {
    private static final BlockUpdateEvent INSTANCE = new BlockUpdateEvent();
    public BlockPos pos;
    public BlockState oldState;
    public BlockState newState;

    public static BlockUpdateEvent get(BlockPos pos, BlockState oldState, BlockState newState) {
        BlockUpdateEvent.INSTANCE.pos = pos;
        BlockUpdateEvent.INSTANCE.oldState = oldState;
        BlockUpdateEvent.INSTANCE.newState = newState;
        return INSTANCE;
    }
}

