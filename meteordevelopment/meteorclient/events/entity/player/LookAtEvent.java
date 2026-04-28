/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.events.entity.player;

import net.minecraft.util.math.Vec3d;

public class LookAtEvent {
    private Vec3d target;
    private float yaw;
    private float pitch;
    private boolean rotation;
    public float priority = 0.0f;

    public Vec3d getTarget() {
        return this.target;
    }

    public boolean getRotation() {
        return this.rotation;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setTarget(Vec3d target, float priority) {
        if (priority >= this.priority) {
            this.rotation = false;
            this.priority = priority;
            this.target = target;
        }
    }

    public void setRotation(float yaw, float pitch, float priority) {
        if (priority >= this.priority) {
            this.rotation = true;
            this.priority = priority;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}

