/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.util.math.BlockPos;

public interface IBox {
    public void expand(double var1);

    public void set(double var1, double var3, double var5, double var7, double var9, double var11);

    default public void set(BlockPos pos) {
        this.set(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}

