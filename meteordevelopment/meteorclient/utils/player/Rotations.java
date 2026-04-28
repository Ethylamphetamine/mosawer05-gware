/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.utils.player;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Rotations {
    private static final Pool<Rotation> rotationPool = new Pool<Rotation>(Rotation::new);
    private static final List<Rotation> rotations = new ArrayList<Rotation>();
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    private static float preYaw;
    private static float prePitch;
    private static int i;
    private static Rotation lastRotation;
    private static int lastRotationTimer;
    private static boolean sentLastRotation;
    public static boolean rotating;

    private Rotations() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Rotations.class);
    }

    public static void rotate(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
        int i;
        Rotation rotation = rotationPool.get();
        rotation.set(yaw, pitch, priority, clientSide, callback);
        for (i = 0; i < rotations.size() && priority <= Rotations.rotations.get((int)i).priority; ++i) {
        }
        rotations.add(i, rotation);
    }

    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        Rotations.rotate(yaw, pitch, priority, false, callback);
    }

    public static void rotate(double yaw, double pitch, Runnable callback) {
        Rotations.rotate(yaw, pitch, 0, callback);
    }

    public static void rotate(double yaw, double pitch, int priority) {
        Rotations.rotate(yaw, pitch, priority, null);
    }

    public static void rotate(double yaw, double pitch) {
        Rotations.rotate(yaw, pitch, 0, null);
    }

    private static void resetLastRotation() {
        if (lastRotation != null) {
            rotationPool.free(lastRotation);
            lastRotation = null;
            lastRotationTimer = 0;
        }
    }

    @EventHandler
    private static void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        if (MeteorClient.mc.cameraEntity != MeteorClient.mc.player) {
            return;
        }
        sentLastRotation = false;
        if (!rotations.isEmpty()) {
            rotating = true;
            Rotations.resetLastRotation();
            Rotation rotation = rotations.get(i);
            Rotations.setupMovementPacketRotation(rotation);
            if (rotations.size() > 1) {
                rotationPool.free(rotation);
            }
            ++i;
        } else if (lastRotation != null) {
            if (lastRotationTimer >= Config.get().rotationHoldTicks.get()) {
                Rotations.resetLastRotation();
                rotating = false;
            } else {
                Rotations.setupMovementPacketRotation(lastRotation);
                sentLastRotation = true;
                ++lastRotationTimer;
            }
        }
    }

    private static void setupMovementPacketRotation(Rotation rotation) {
        Rotations.setClientRotation(rotation);
        Rotations.setCamRotation(rotation.yaw, rotation.pitch);
    }

    private static void setClientRotation(Rotation rotation) {
        preYaw = MeteorClient.mc.player.getYaw();
        prePitch = MeteorClient.mc.player.getPitch();
        MeteorClient.mc.player.setYaw((float)rotation.yaw);
        MeteorClient.mc.player.setPitch((float)rotation.pitch);
    }

    @EventHandler
    private static void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        if (!rotations.isEmpty()) {
            if (MeteorClient.mc.cameraEntity == MeteorClient.mc.player) {
                rotations.get(i - 1).runCallback();
                if (rotations.size() == 1) {
                    lastRotation = rotations.get(i - 1);
                }
                Rotations.resetPreRotation();
            }
            while (i < rotations.size()) {
                Rotation rotation = rotations.get(i);
                Rotations.setCamRotation(rotation.yaw, rotation.pitch);
                if (rotation.clientSide) {
                    Rotations.setClientRotation(rotation);
                }
                rotation.sendPacket();
                if (rotation.clientSide) {
                    Rotations.resetPreRotation();
                }
                if (i == rotations.size() - 1) {
                    lastRotation = rotation;
                } else {
                    rotationPool.free(rotation);
                }
                ++i;
            }
            rotations.clear();
            i = 0;
        } else if (sentLastRotation) {
            Rotations.resetPreRotation();
        }
    }

    private static void resetPreRotation() {
        MeteorClient.mc.player.setYaw(preYaw);
        MeteorClient.mc.player.setPitch(prePitch);
    }

    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        ++rotationTimer;
    }

    public static double getYaw(Entity entity) {
        return MeteorClient.mc.player.getYaw() + MathHelper.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(entity.getZ() - MeteorClient.mc.player.getZ(), entity.getX() - MeteorClient.mc.player.getX())) - 90.0f - MeteorClient.mc.player.getYaw()));
    }

    public static double getYaw(Vec3d pos) {
        return MeteorClient.mc.player.getYaw() + MathHelper.wrapDegrees((float)((float)Math.toDegrees(Math.atan2(pos.getZ() - MeteorClient.mc.player.getZ(), pos.getX() - MeteorClient.mc.player.getX())) - 90.0f - MeteorClient.mc.player.getYaw()));
    }

    public static double getPitch(Vec3d pos) {
        double diffX = pos.getX() - MeteorClient.mc.player.getX();
        double diffY = pos.getY() - (MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
        double diffZ = pos.getZ() - MeteorClient.mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        return MeteorClient.mc.player.getPitch() + MathHelper.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getPitch()));
    }

    public static double getPitch(Entity entity, Target target) {
        double y = target == Target.Head ? entity.getEyeY() : (target == Target.Body ? entity.getY() + (double)(entity.getHeight() / 2.0f) : entity.getY());
        double diffX = entity.getX() - MeteorClient.mc.player.getX();
        double diffY = y - (MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
        double diffZ = entity.getZ() - MeteorClient.mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        return MeteorClient.mc.player.getPitch() + MathHelper.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getPitch()));
    }

    public static double getPitch(Entity entity) {
        return Rotations.getPitch(entity, Target.Body);
    }

    public static double getYaw(BlockPos pos) {
        return MeteorClient.mc.player.getYaw() + MathHelper.wrapDegrees((float)((float)Math.toDegrees(Math.atan2((double)pos.getZ() + 0.5 - MeteorClient.mc.player.getZ(), (double)pos.getX() + 0.5 - MeteorClient.mc.player.getX())) - 90.0f - MeteorClient.mc.player.getYaw()));
    }

    public static double getPitch(BlockPos pos) {
        double diffX = (double)pos.getX() + 0.5 - MeteorClient.mc.player.getX();
        double diffY = (double)pos.getY() + 0.5 - (MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()));
        double diffZ = (double)pos.getZ() + 0.5 - MeteorClient.mc.player.getZ();
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        return MeteorClient.mc.player.getPitch() + MathHelper.wrapDegrees((float)((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getPitch()));
    }

    public static void setCamRotation(double yaw, double pitch) {
        serverYaw = (float)yaw;
        serverPitch = (float)pitch;
        rotationTimer = 0;
    }

    public static double yawAngle(double current, double target) {
        double t;
        double c = MathHelper.wrapDegrees((double)current) + 180.0;
        if (c > (t = MathHelper.wrapDegrees((double)target) + 180.0)) {
            return t + 360.0 - c < Math.abs(c - t) ? 360.0 - c + t : t - c;
        }
        return 360.0 - t + c < Math.abs(c - t) ? -(360.0 - t + c) : t - c;
    }

    public static Vec3d getDirection(Vec3d from, Vec3d to) {
        Vec3d direction = to.subtract(from);
        return direction.normalize();
    }

    static {
        i = 0;
        rotating = false;
    }

    private static class Rotation {
        public double yaw;
        public double pitch;
        public int priority;
        public boolean clientSide;
        public Runnable callback;

        private Rotation() {
        }

        public void set(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.priority = priority;
            this.clientSide = clientSide;
            this.callback = callback;
        }

        public void sendPacket() {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround((float)this.yaw, (float)this.pitch, MeteorClient.mc.player.isOnGround()));
            this.runCallback();
        }

        public void runCallback() {
            if (this.callback != null) {
                this.callback.run();
            }
        }
    }
}

