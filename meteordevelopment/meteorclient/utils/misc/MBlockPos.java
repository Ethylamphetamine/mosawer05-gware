/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 */
package meteordevelopment.meteorclient.utils.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class MBlockPos {
    private static final BlockPos.Mutable POS = new BlockPos.Mutable();
    public int x;
    public int y;
    public int z;

    public MBlockPos() {
    }

    public MBlockPos(Entity entity) {
        this.set(entity);
    }

    public MBlockPos set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public MBlockPos set(MBlockPos pos) {
        return this.set(pos.x, pos.y, pos.z);
    }

    public MBlockPos set(Entity entity) {
        return this.set(entity.getBlockX(), entity.getBlockY(), entity.getBlockZ());
    }

    public MBlockPos offset(HorizontalDirection dir, int amount) {
        this.x += dir.offsetX * amount;
        this.z += dir.offsetZ * amount;
        return this;
    }

    public MBlockPos offset(HorizontalDirection dir) {
        return this.offset(dir, 1);
    }

    public MBlockPos add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public BlockPos getBlockPos() {
        return POS.set(this.x, this.y, this.z);
    }

    public BlockState getState() {
        return MeteorClient.mc.world.getBlockState(this.getBlockPos());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MBlockPos mBlockPos = (MBlockPos)o;
        if (this.x != mBlockPos.x) {
            return false;
        }
        if (this.y != mBlockPos.y) {
            return false;
        }
        return this.z == mBlockPos.z;
    }

    public int hashCode() {
        int result = this.x;
        result = 31 * result + this.y;
        result = 31 * result + this.z;
        return result;
    }

    public String toString() {
        return this.x + ", " + this.y + ", " + this.z;
    }
}

