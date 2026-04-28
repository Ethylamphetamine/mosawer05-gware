/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.events.entity.player;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.util.math.BlockPos;

public class BreakBlockEvent
extends Cancellable {
    private static final BreakBlockEvent INSTANCE = new BreakBlockEvent();
    public BlockPos blockPos;

    public static BreakBlockEvent get(BlockPos blockPos) {
        INSTANCE.setCancelled(false);
        BreakBlockEvent.INSTANCE.blockPos = blockPos;
        return INSTANCE;
    }
}

