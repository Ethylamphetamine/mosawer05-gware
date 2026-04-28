/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec3i
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {
    public void set(double var1, double var3, double var5);

    default public void set(Vec3i vec) {
        this.set(vec.getX(), vec.getY(), vec.getZ());
    }

    default public void set(Vector3d vec) {
        this.set(vec.x, vec.y, vec.z);
    }

    public void setXZ(double var1, double var3);

    public void setY(double var1);
}

