/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 */
package meteordevelopment.meteorclient.events.world;

import net.minecraft.block.BlockState;

public class BlockActivateEvent {
    private static final BlockActivateEvent INSTANCE = new BlockActivateEvent();
    public BlockState blockState;

    public static BlockActivateEvent get(BlockState blockState) {
        BlockActivateEvent.INSTANCE.blockState = blockState;
        return INSTANCE;
    }
}

