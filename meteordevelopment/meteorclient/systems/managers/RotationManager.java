/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
 *  net.minecraft.network.packet.s2c.play.PositionFlag
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.managers;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.LookAtEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerJumpEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTravelEvent;
import meteordevelopment.meteorclient.events.entity.player.RotateEvent;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.entity.player.UpdatePlayerVelocity;
import meteordevelopment.meteorclient.events.input.KeyboardInputEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.managers.PacketPriority;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    public float nextYaw;
    public float nextPitch;
    public float rotationYaw = 0.0f;
    public float rotationPitch = 0.0f;
    public float lastYaw = 0.0f;
    public float lastPitch = 0.0f;
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    public static boolean sendDisablerPacket;
    public static float lastActualYaw;
    private int ticksExisted;
    public static Vec3d targetVec;
    public static boolean lastGround;
    public double lastX = 0.0;
    public double lastY = 0.0;
    public double lastZ = 0.0;
    private boolean shouldFulfilRequest = false;
    private static RotationRequest request;
    private final AntiCheatConfig antiCheatConfig = AntiCheatConfig.get();
    private float[] silentRotation = null;
    private float[] persistentRotation = null;
    private long lastSilentRotateTime = 0L;
    private float preSilentYaw;
    private float preSilentPitch;
    private boolean silentRotationActive = false;

    public RotationManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    public void silentRotate(Vec3d target, Runnable action) {
        float[] angle = this.getRotation(target);
        this.silentRotate(angle[0], angle[1], action);
    }

    public void silentRotate(float yaw, float pitch, Runnable action) {
        if (MeteorClient.mc.player == null || MeteorClient.mc.getNetworkHandler() == null) {
            return;
        }
        this.silentRotation = new float[]{yaw, pitch};
        this.persistentRotation = new float[]{yaw, pitch};
        this.lastSilentRotateTime = System.currentTimeMillis();
        Rotations.serverYaw = yaw;
        Rotations.serverPitch = pitch;
        Rotations.rotationTimer = 0;
        this.setRenderRotation(yaw, pitch, true);
        this.preSilentYaw = MeteorClient.mc.player.getYaw();
        this.preSilentPitch = MeteorClient.mc.player.getPitch();
        MeteorClient.mc.player.setYaw(yaw);
        MeteorClient.mc.player.setPitch(pitch);
        this.silentRotationActive = true;
        if (action != null) {
            action.run();
        }
    }

    public static float calculateOptimalYaw(float targetYaw) {
        if (MeteorClient.mc.player.isFallFlying()) {
            return targetYaw;
        }
        float bestYaw = targetYaw;
        float smallestDelta = 360.0f;
        for (int i = 0; i < 360; i += 90) {
            float candidate = MathHelper.wrapDegrees((float)(targetYaw + (float)i));
            float delta = MathHelper.angleBetween((float)candidate, (float)MeteorClient.mc.player.getYaw());
            if (!(delta <= smallestDelta)) continue;
            bestYaw = candidate;
            smallestDelta = delta;
        }
        return bestYaw;
    }

    public void snapAt(Vec3d target) {
        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.MEDIUM)) {
            return;
        }
        float[] angle = this.getRotation(target);
        if (this.antiCheatConfig.grimSnapRotation.get().booleanValue()) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(this.lastX, this.lastY, this.lastZ, angle[0], angle[1], lastGround));
        } else {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(angle[0], angle[1], lastGround));
        }
        PacketManager.INSTANCE.incrementGlobal();
    }

    public void snapAt(float yaw, float pitch) {
        if (PacketManager.INSTANCE.shouldThrottle(PacketPriority.MEDIUM)) {
            return;
        }
        if (this.antiCheatConfig.grimSnapRotation.get().booleanValue()) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.Full(this.lastX, this.lastY, this.lastZ, yaw, pitch, lastGround));
        } else {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, lastGround));
        }
        PacketManager.INSTANCE.incrementGlobal();
    }

    public void requestRotation(Vec3d target, double priority) {
        float[] angle = this.getRotation(target);
        this.requestRotation(angle[0], angle[1], priority, null);
    }

    public void requestRotation(Vec3d target, double priority, Runnable callback) {
        float[] angle = this.getRotation(target);
        this.requestRotation(angle[0], angle[1], priority, callback);
    }

    public void requestRotation(float yaw, float pitch, double priority) {
        this.requestRotation(yaw, pitch, priority, null);
    }

    public void requestRotation(float yaw, float pitch, double priority, Runnable callback) {
        if (RotationManager.request.priority > priority && !RotationManager.request.fulfilled) {
            return;
        }
        RotationManager.request.fulfilled = false;
        RotationManager.request.yaw = yaw;
        RotationManager.request.pitch = pitch;
        RotationManager.request.priority = priority;
        RotationManager.request.callback = callback;
    }

    public float[] getRotation(Vec3d eyesPos, Vec3d vec) {
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{MathHelper.wrapDegrees((float)yaw), MathHelper.wrapDegrees((float)pitch)};
    }

    public float[] getRotation(Vec3d vec) {
        Vec3d eyesPos = MeteorClient.mc.player.getEyePos();
        return this.getRotation(eyesPos, vec);
    }

    public boolean lookingAt(Box box) {
        return this.lookingAt(this.lastYaw, this.lastPitch, box);
    }

    public boolean lookingAt(float yaw, float pitch, Box box) {
        return this.raytraceCheck(MeteorClient.mc.player.getEyePos(), yaw, pitch, box);
    }

    @EventHandler(priority=-200)
    public void onLastRotation(RotateEvent event) {
        LookAtEvent lookAtEvent = new LookAtEvent();
        MeteorClient.EVENT_BUS.post(lookAtEvent);
        this.shouldFulfilRequest = false;
        if (request != null && !RotationManager.request.fulfilled && RotationManager.request.priority > (double)lookAtEvent.priority) {
            event.setYaw(RotationManager.request.yaw);
            event.setPitch(RotationManager.request.pitch);
            this.shouldFulfilRequest = true;
            return;
        }
        if (lookAtEvent.getRotation()) {
            event.setYaw(lookAtEvent.getYaw());
            event.setPitch(lookAtEvent.getPitch());
        } else if (lookAtEvent.getTarget() != null) {
            float[] newAngle = this.getRotation(lookAtEvent.getTarget());
            event.setYaw(newAngle[0]);
            event.setPitch(newAngle[1]);
        }
    }

    @EventHandler(priority=-999)
    public void onPacketSend(PacketEvent.Send event) {
        if (MeteorClient.mc.player == null || event.isCancelled()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket packet2 = (PlayerMoveC2SPacket)packet;
            if (packet2.changesLook()) {
                this.lastYaw = packet2.getYaw(this.lastYaw);
                if (sendDisablerPacket) {
                    sendDisablerPacket = false;
                    this.lastYaw = lastActualYaw;
                }
                this.lastPitch = packet2.getPitch(this.lastPitch);
                this.setRenderRotation(this.lastYaw, this.lastPitch, false);
            }
            if (packet2.changesPosition()) {
                this.lastX = packet2.getX(this.lastX);
                this.lastY = packet2.getY(this.lastY);
                this.lastZ = packet2.getZ(this.lastZ);
            }
            lastGround = packet2.isOnGround();
        }
    }

    @EventHandler(priority=100)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (MeteorClient.mc.player == null) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket packet2 = (PlayerPositionLookS2CPacket)packet;
            this.lastYaw = packet2.getFlags().contains(PositionFlag.X_ROT) ? (this.lastYaw += packet2.getYaw()) : packet2.getYaw();
            this.lastPitch = packet2.getFlags().contains(PositionFlag.Y_ROT) ? (this.lastPitch += packet2.getPitch()) : packet2.getPitch();
            this.lastX = packet2.getFlags().contains(PositionFlag.X) ? (this.lastX += packet2.getX()) : packet2.getX();
            this.lastY = packet2.getFlags().contains(PositionFlag.Y) ? (this.lastY += packet2.getY()) : packet2.getY();
            this.lastZ = packet2.getFlags().contains(PositionFlag.Z) ? (this.lastZ += packet2.getZ()) : packet2.getZ();
            this.setRenderRotation(this.lastYaw, this.lastPitch, true);
        }
    }

    @EventHandler
    public void onUpdateWalkingPost(SendMovementPacketsEvent.Post event) {
        if (this.silentRotationActive) {
            MeteorClient.mc.player.setYaw(this.preSilentYaw);
            MeteorClient.mc.player.setPitch(this.preSilentPitch);
            this.silentRotationActive = false;
        }
        this.silentRotation = null;
        if (this.persistentRotation != null && System.currentTimeMillis() - this.lastSilentRotateTime >= 500L) {
            this.persistentRotation = null;
        }
        if (this.persistentRotation != null) {
            this.setRenderRotation(this.persistentRotation[0], this.persistentRotation[1], true);
        } else {
            this.setRenderRotation(this.lastYaw, this.lastPitch, true);
        }
    }

    @EventHandler
    public void onMovementPacket(SendMovementPacketsEvent.Rotation event) {
        if (!this.antiCheatConfig.tickSync.get().booleanValue()) {
            return;
        }
        if (this.shouldFulfilRequest && !RotationManager.request.fulfilled) {
            RotationManager.request.fulfilled = true;
            this.shouldFulfilRequest = false;
        }
        if (this.silentRotation != null) {
            event.yaw = this.silentRotation[0];
            event.pitch = this.silentRotation[1];
        } else if (this.persistentRotation != null) {
            event.yaw = this.persistentRotation[0];
            event.pitch = this.persistentRotation[1];
        } else if (MovementFix.MOVE_FIX != null && MovementFix.MOVE_FIX.isActive() && !MovementFix.bypassRotationForThisTick) {
            event.yaw = this.nextYaw;
            event.pitch = this.nextPitch;
        } else {
            RotateEvent rotateEvent = new RotateEvent(event.yaw, event.pitch);
            MeteorClient.EVENT_BUS.post(rotateEvent);
            event.yaw = rotateEvent.getYaw();
            event.pitch = rotateEvent.getPitch();
        }
        if (this.antiCheatConfig.grimSync.get().booleanValue()) {
            event.forceFull = true;
        }
        if (this.antiCheatConfig.grimRotation.get().booleanValue()) {
            event.forceFullOnRotate = true;
        }
        MovementFix.bypassRotationForThisTick = false;
    }

    @EventHandler(priority=-200)
    public void onUpdatePlayerVelocity(UpdatePlayerVelocity event) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            MeteorClient.ROTATION.moveFixRotation();
        }
    }

    @EventHandler(priority=-200)
    public void onPreJump(PlayerJumpEvent.Pre e) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            MeteorClient.ROTATION.moveFixRotation();
        }
    }

    @EventHandler(priority=-200)
    public void onTravel(PlayerTravelEvent.Pre e) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            MeteorClient.ROTATION.moveFixRotation();
        }
    }

    @EventHandler(priority=200)
    public void onKeyInput(KeyboardInputEvent e) {
        if (MovementFix.MOVE_FIX.isActive() && MovementFix.MOVE_FIX.updateMode.get() != MovementFix.UpdateMode.Mouse) {
            MeteorClient.ROTATION.moveFixRotation();
        }
    }

    public void moveFixRotation() {
        if (MeteorClient.mc.player == null) {
            return;
        }
        if (MovementFix.MOVE_FIX == null) {
            return;
        }
        if (MovementFix.setRot) {
            MeteorClient.mc.player.setYaw(MovementFix.prevYaw);
            MeteorClient.mc.player.setPitch(MovementFix.prevPitch);
        }
        RotateEvent rotateEvent = new RotateEvent(MeteorClient.mc.player.getYaw(), MeteorClient.mc.player.getPitch());
        MeteorClient.EVENT_BUS.post(rotateEvent);
        this.nextYaw = rotateEvent.getYaw();
        this.nextPitch = rotateEvent.getPitch();
        MovementFix.fixYaw = this.nextYaw;
        MovementFix.fixPitch = this.nextPitch;
        if (MovementFix.setRot) {
            MeteorClient.mc.player.setYaw(MovementFix.fixYaw);
            MeteorClient.mc.player.setPitch(MovementFix.fixPitch);
        }
    }

    public static boolean hasPersistentRotation() {
        return MeteorClient.ROTATION.persistentRotation != null;
    }

    public boolean raytraceCheck(Vec3d pos, double y, double p, Box box) {
        double tMaxZ;
        double tMinZ;
        double tMaxY;
        double tMinY;
        Vec3d vec = new Vec3d(Math.cos(Math.toRadians(y + 90.0)) * Math.abs(Math.cos(Math.toRadians(p))), -Math.sin(Math.toRadians(p)), Math.sin(Math.toRadians(y + 90.0)) * Math.abs(Math.cos(Math.toRadians(p))));
        double rayX = pos.x;
        double rayY = pos.y;
        double rayZ = pos.z;
        double dirX = vec.x;
        double dirY = vec.y;
        double dirZ = vec.z;
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;
        double invDirX = dirX != 0.0 ? 1.0 / dirX : 1.0E10;
        double invDirY = dirY != 0.0 ? 1.0 / dirY : 1.0E10;
        double invDirZ = dirZ != 0.0 ? 1.0 / dirZ : 1.0E10;
        double tMinX = (minX - rayX) * invDirX;
        double tMaxX = (maxX - rayX) * invDirX;
        if (tMinX > tMaxX) {
            double temp = tMinX;
            tMinX = tMaxX;
            tMaxX = temp;
        }
        if ((tMinY = (minY - rayY) * invDirY) > (tMaxY = (maxY - rayY) * invDirY)) {
            double temp = tMinY;
            tMinY = tMaxY;
            tMaxY = temp;
        }
        if ((tMinZ = (minZ - rayZ) * invDirZ) > (tMaxZ = (maxZ - rayZ) * invDirZ)) {
            double temp = tMinZ;
            tMinZ = tMaxZ;
            tMaxZ = temp;
        }
        double tMin = Math.max(Math.max(tMinX, tMinY), tMinZ);
        double tMax = Math.min(Math.min(tMaxX, tMaxY), tMaxZ);
        return tMax >= 0.0 && tMin <= tMax;
    }

    public void setRenderRotation(float yaw, float pitch, boolean force) {
        if (MeteorClient.mc.player == null) {
            return;
        }
        if (MeteorClient.mc.player.age == this.ticksExisted && !force) {
            return;
        }
        this.ticksExisted = MeteorClient.mc.player.age;
        prevPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;
        renderPitch = pitch;
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float offset;
        double zDif;
        float result = offsetIn;
        double xDif = MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX;
        if (xDif * xDif + (zDif = MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ) * zDif > 0.002500000176951289) {
            offset = (float)MathHelper.atan2((double)zDif, (double)xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs((float)(MathHelper.wrapDegrees((float)yaw) - offset));
            result = 95.0f < wrap && wrap < 265.0f ? offset - 180.0f : offset;
        }
        if (MeteorClient.mc.player.handSwingProgress > 0.0f) {
            result = yaw;
        }
        if ((offset = MathHelper.wrapDegrees((float)(yaw - (result = offsetIn + MathHelper.wrapDegrees((float)(result - offsetIn)) * 0.3f)))) < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }
        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }
        return result;
    }

    static {
        sendDisablerPacket = false;
        lastActualYaw = 0.0f;
        targetVec = null;
        request = new RotationRequest();
    }

    public static class RotationRequest {
        public double priority;
        public float yaw;
        public float pitch;
        public boolean fulfilled = false;
        public Runnable callback = null;
    }
}

