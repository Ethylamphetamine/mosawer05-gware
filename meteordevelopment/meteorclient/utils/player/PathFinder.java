/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.command.argument.EntityAnchorArgumentType$EntityAnchor
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class PathFinder {
    private static final int PATH_AHEAD = 3;
    private static final int QUAD_1 = 1;
    private static final int QUAD_2 = 2;
    private static final int SOUTH = 0;
    private static final int NORTH = 180;
    private final ArrayList<PathBlock> path = new ArrayList(3);
    private Entity target;
    private PathBlock currentPathBlock;

    public PathBlock getNextPathBlock() {
        PathBlock nextBlock = new PathBlock(this, BlockPos.ofFloored((Position)this.getNextStraightPos()));
        if (this.isSolidFloor(nextBlock.blockPos) && this.isAirAbove(nextBlock.blockPos)) {
            return nextBlock;
        }
        if (!this.isSolidFloor(nextBlock.blockPos) && this.isAirAbove(nextBlock.blockPos)) {
            int drop = this.getDrop(nextBlock.blockPos);
            if (this.getDrop(nextBlock.blockPos) < 3) {
                nextBlock = new PathBlock(this, new BlockPos(nextBlock.blockPos.getX(), nextBlock.blockPos.getY() - drop, nextBlock.blockPos.getZ()));
            }
        }
        return nextBlock;
    }

    public int getDrop(BlockPos pos) {
        int drop;
        for (drop = 0; !this.isSolidFloor(pos) && drop < 3; ++drop) {
            pos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        }
        return drop;
    }

    public boolean isAirAbove(BlockPos blockPos) {
        if (!this.getBlockStateAtPos(blockPos.getX(), blockPos.getY(), blockPos.getZ()).isAir()) {
            return false;
        }
        return this.getBlockStateAtPos(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ()).isAir();
    }

    public Vec3d getNextStraightPos() {
        Vec3d nextPos = new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ());
        double multiplier = 1.0;
        while (nextPos == MeteorClient.mc.player.getPos()) {
            nextPos = new Vec3d((double)((int)(MeteorClient.mc.player.getX() + multiplier * Math.cos(Math.toRadians(MeteorClient.mc.player.getYaw())))), (double)((int)MeteorClient.mc.player.getY()), (double)((int)(MeteorClient.mc.player.getZ() + multiplier * Math.sin(Math.toRadians(MeteorClient.mc.player.getYaw())))));
            multiplier += 0.1;
        }
        return nextPos;
    }

    public int getYawToTarget() {
        if (this.target == null || MeteorClient.mc.player == null) {
            return Integer.MAX_VALUE;
        }
        Vec3d tPos = this.target.getPos();
        Vec3d pPos = MeteorClient.mc.player.getPos();
        int yaw = 0;
        int direction = this.getDirection();
        double tan = (tPos.z - pPos.z) / (tPos.x - pPos.x);
        if (direction == 1) {
            yaw = (int)(1.5707963267948966 - Math.atan(tan));
        } else if (direction == 2) {
            yaw = (int)(-1.5707963267948966 - Math.atan(tan));
        } else {
            return direction;
        }
        return yaw;
    }

    public int getDirection() {
        if (this.target == null || MeteorClient.mc.player == null) {
            return 0;
        }
        Vec3d targetPos = this.target.getPos();
        Vec3d playerPos = MeteorClient.mc.player.getPos();
        if (targetPos.x == playerPos.x && targetPos.z > playerPos.z) {
            return 0;
        }
        if (targetPos.x == playerPos.x && targetPos.z < playerPos.z) {
            return 180;
        }
        if (targetPos.x < playerPos.x) {
            return 1;
        }
        if (targetPos.x > playerPos.x) {
            return 2;
        }
        return 0;
    }

    public BlockState getBlockStateAtPos(BlockPos pos) {
        if (MeteorClient.mc.world != null) {
            return MeteorClient.mc.world.getBlockState(pos);
        }
        return null;
    }

    public BlockState getBlockStateAtPos(int x, int y, int z) {
        if (MeteorClient.mc.world != null) {
            return MeteorClient.mc.world.getBlockState(new BlockPos(x, y, z));
        }
        return null;
    }

    public Block getBlockAtPos(BlockPos pos) {
        if (MeteorClient.mc.world != null) {
            return MeteorClient.mc.world.getBlockState(pos).getBlock();
        }
        return null;
    }

    public boolean isSolidFloor(BlockPos blockPos) {
        return this.isAir(this.getBlockAtPos(blockPos));
    }

    public boolean isAir(Block block) {
        return block == Blocks.AIR;
    }

    public boolean isWater(Block block) {
        return block == Blocks.WATER;
    }

    public void lookAtDestination(PathBlock pathBlock) {
        if (MeteorClient.mc.player != null) {
            MeteorClient.mc.player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, new Vec3d((double)pathBlock.blockPos.getX(), (double)((float)pathBlock.blockPos.getY() + MeteorClient.mc.player.getStandingEyeHeight()), (double)pathBlock.blockPos.getZ()));
        }
    }

    @EventHandler
    private void moveEventListener(PlayerMoveEvent event) {
        if (this.target != null && MeteorClient.mc.player != null) {
            if (!PlayerUtils.isWithin(this.target, 3.0)) {
                if (this.currentPathBlock == null) {
                    this.currentPathBlock = this.getNextPathBlock();
                }
                Vec3d vec3d = new Vec3d((double)this.currentPathBlock.blockPos.getX(), (double)this.currentPathBlock.blockPos.getY(), (double)this.currentPathBlock.blockPos.getZ());
                if (MeteorClient.mc.player.getPos().squaredDistanceTo(vec3d) < 0.01) {
                    this.currentPathBlock = this.getNextPathBlock();
                }
                this.lookAtDestination(this.currentPathBlock);
                if (!MeteorClient.mc.options.forwardKey.isPressed()) {
                    MeteorClient.mc.options.forwardKey.setPressed(true);
                }
            } else {
                if (MeteorClient.mc.options.forwardKey.isPressed()) {
                    MeteorClient.mc.options.forwardKey.setPressed(false);
                }
                this.path.clear();
                this.currentPathBlock = null;
            }
        }
    }

    public void initiate(Entity entity) {
        this.target = entity;
        if (this.target != null) {
            this.currentPathBlock = this.getNextPathBlock();
        }
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void disable() {
        this.target = null;
        this.path.clear();
        if (MeteorClient.mc.options.forwardKey.isPressed()) {
            MeteorClient.mc.options.forwardKey.setPressed(false);
        }
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    public class PathBlock {
        public final Block block;
        public final BlockPos blockPos;
        public final BlockState blockState;
        public double yaw;

        public PathBlock(PathFinder this$0, Block b, BlockPos pos, BlockState state) {
            this.block = b;
            this.blockPos = pos;
            this.blockState = state;
        }

        public PathBlock(PathFinder this$0, Block b, BlockPos pos) {
            this.block = b;
            this.blockPos = pos;
            this.blockState = this$0.getBlockStateAtPos(this.blockPos);
        }

        public PathBlock(PathFinder this$0, BlockPos pos) {
            this.blockPos = pos;
            this.block = this$0.getBlockAtPos(pos);
            this.blockState = this$0.getBlockStateAtPos(this.blockPos);
        }
    }
}

