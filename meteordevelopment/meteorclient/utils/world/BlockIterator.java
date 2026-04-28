/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 */
package meteordevelopment.meteorclient.utils.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockIterator {
    private static final Pool<Callback> callbackPool = new Pool<Callback>(Callback::new);
    private static final List<Callback> callbacks = new ArrayList<Callback>();
    private static final List<Runnable> afterCallbacks = new ArrayList<Runnable>();
    private static final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private static int hRadius;
    private static int vRadius;
    private static boolean disableCurrent;

    private BlockIterator() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(BlockIterator.class);
    }

    @EventHandler(priority=-201)
    private static void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) {
            return;
        }
        int px = MeteorClient.mc.player.getBlockX();
        int py = MeteorClient.mc.player.getBlockY();
        int pz = MeteorClient.mc.player.getBlockZ();
        for (int x = px - hRadius; x <= px + hRadius; ++x) {
            for (int z = pz - hRadius; z <= pz + hRadius; ++z) {
                for (int y = Math.max(MeteorClient.mc.world.getBottomY(), py - vRadius); y <= py + vRadius && y <= MeteorClient.mc.world.getTopY(); ++y) {
                    blockPos.set(x, y, z);
                    BlockState blockState = MeteorClient.mc.world.getBlockState((BlockPos)blockPos);
                    int dx = Math.abs(x - px);
                    int dy = Math.abs(y - py);
                    int dz = Math.abs(z - pz);
                    Iterator<Callback> it = callbacks.iterator();
                    while (it.hasNext()) {
                        Callback callback = it.next();
                        if (dx > callback.hRadius || dy > callback.vRadius || dz > callback.hRadius) continue;
                        disableCurrent = false;
                        callback.function.accept((BlockPos)blockPos, blockState);
                        if (!disableCurrent) continue;
                        it.remove();
                    }
                }
            }
        }
        hRadius = 0;
        vRadius = 0;
        for (Callback callback : callbacks) {
            callbackPool.free(callback);
        }
        callbacks.clear();
        for (Runnable callback : afterCallbacks) {
            callback.run();
        }
        afterCallbacks.clear();
    }

    public static void register(int horizontalRadius, int verticalRadius, BiConsumer<BlockPos, BlockState> function) {
        hRadius = Math.max(hRadius, horizontalRadius);
        vRadius = Math.max(vRadius, verticalRadius);
        Callback callback = callbackPool.get();
        callback.function = function;
        callback.hRadius = horizontalRadius;
        callback.vRadius = verticalRadius;
        callbacks.add(callback);
    }

    public static void disableCurrent() {
        disableCurrent = true;
    }

    public static void after(Runnable callback) {
        afterCallbacks.add(callback);
    }

    private static class Callback {
        public BiConsumer<BlockPos, BlockState> function;
        public int hRadius;
        public int vRadius;

        private Callback() {
        }
    }
}

