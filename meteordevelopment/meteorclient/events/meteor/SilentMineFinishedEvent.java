/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.events.meteor;

import net.minecraft.util.math.BlockPos;

public class SilentMineFinishedEvent {

    public static class Post {
        private boolean isRebreak;
        private BlockPos blockPos;

        public Post(BlockPos blockPos, boolean isRebreak) {
            this.blockPos = blockPos;
            this.isRebreak = isRebreak;
        }

        public boolean getIsRebreak() {
            return this.isRebreak;
        }

        public BlockPos getBlockPos() {
            return this.blockPos;
        }
    }

    public static class Pre {
        private boolean isRebreak;
        private BlockPos blockPos;

        public Pre(BlockPos blockPos, boolean isRebreak) {
            this.blockPos = blockPos;
            this.isRebreak = isRebreak;
        }

        public boolean getIsRebreak() {
            return this.isRebreak;
        }

        public BlockPos getBlockPos() {
            return this.blockPos;
        }
    }
}

