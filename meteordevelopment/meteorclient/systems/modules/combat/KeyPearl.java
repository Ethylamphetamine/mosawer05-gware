/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class KeyPearl
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> keybind;
    private final Setting<Boolean> bypassSwap;
    private final Setting<Boolean> disableInAir;
    private final Setting<Boolean> smartThrow;
    private boolean wasPressed;

    public KeyPearl() {
        super(Categories.Combat, "key-pearl", "Throws an ender pearl with a keybind.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.keybind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("keybind")).description("The keybind to throw a pearl.")).defaultValue(Keybind.none())).build());
        this.bypassSwap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bypass-swap")).description("True = Offhand/Eating-Safe. False = Mainhand/Standard.")).defaultValue(true)).build());
        this.disableInAir = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-in-air")).description("Disable while elytra flying")).defaultValue(false)).build());
        this.smartThrow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart-throw")).description("Calculates the trajectory to land the pearl on the block you are looking at.")).defaultValue(false)).build());
        this.wasPressed = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player == null || this.mc.world == null || this.mc.currentScreen != null) {
            return;
        }
        if (!this.keybind.get().isPressed()) {
            this.wasPressed = false;
            return;
        }
        if (this.disableInAir.get().booleanValue() && this.mc.player.isFallFlying()) {
            return;
        }
        if (!this.wasPressed && !this.mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)) {
            if (this.smartThrow.get().booleanValue() && !this.mc.player.isFallFlying()) {
                this.handleSmartThrow();
            } else {
                this.throwPearl(this.mc.player.getYaw(), this.mc.player.getPitch());
            }
            this.wasPressed = true;
        }
    }

    private void handleSmartThrow() {
        BlockHitResult blockHitResult;
        float[] rotations;
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        HitResult result = this.mc.player.raycast(100.0, 0.0f, false);
        if (result.getType() == HitResult.Type.BLOCK && (rotations = this.calculateRotations((blockHitResult = (BlockHitResult)result).getPos(), blockHitResult.getBlockPos())) != null) {
            float yaw = rotations[0];
            float pitch = rotations[1];
            MovementFix.bypassRotationForThisTick = true;
            Rotations.rotate((double)yaw, (double)pitch, () -> this.throwPearl(yaw, pitch));
            return;
        }
        this.throwPearl(this.mc.player.getYaw(), this.mc.player.getPitch());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void throwPearl(float yaw, float pitch) {
        if (this.mc.player == null || this.mc.world == null || this.mc.interactionManager == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        boolean oldAllow = MovementFix.allowMovementFix;
        MovementFix.allowMovementFix = false;
        try {
            FindItemResult pearl = InvUtils.find(Items.ENDER_PEARL);
            if (pearl.found()) {
                if (MeteorClient.SWAP.beginSwap(pearl, true)) {
                    PacketManager.INSTANCE.incrementGlobal();
                    this.sendThrowPacket(Hand.MAIN_HAND, yaw, pitch);
                    MeteorClient.SWAP.endSwap(true);
                    PacketManager.INSTANCE.incrementGlobal();
                }
                this.mc.player.playerScreenHandler.syncState();
            }
        }
        finally {
            MovementFix.allowMovementFix = oldAllow;
        }
    }

    private void sendThrowPacket(Hand hand, float yaw, float pitch) {
        if (this.mc.world == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        int sequence = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch));
        PacketManager.INSTANCE.incrementInteract();
    }

    private float[] calculateRotations(Vec3d targetPos, BlockPos targetBlock) {
        if (this.mc.player == null) {
            return null;
        }
        Vec3d eyePos = this.mc.player.getEyePos();
        double dx = targetPos.x - eyePos.x;
        double dz = targetPos.z - eyePos.z;
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        Vec3d tc = new Vec3d((double)targetBlock.getX() + 0.5, (double)targetBlock.getY() + 0.5, (double)targetBlock.getZ() + 0.5);
        double horiz = Math.sqrt(dx * dx + dz * dz);
        boolean preferDown = horiz < 8.0;
        float bestPitch = Float.NaN;
        double bestScore = Double.POSITIVE_INFINITY;
        int coarseSteps = 360;
        for (int i = 0; i <= coarseSteps; ++i) {
            float p = (float)i * 0.25f;
            float[] candidates = preferDown ? new float[]{-p, p} : new float[]{p, -p};
            for (float pitch : candidates) {
                double dy;
                double d2;
                double score;
                Vec3d hit = this.simulateImpact(yaw, pitch);
                if (hit == null || !((score = (d2 = hit.squaredDistanceTo(tc)) + (dy = Math.abs(hit.y - tc.y)) * dy * 4.0) < bestScore) && (!(Math.abs(score - bestScore) < 1.0E-9) || !(Math.abs(pitch) < Math.abs(bestPitch)))) continue;
                bestScore = score;
                bestPitch = pitch;
            }
        }
        if (Float.isNaN(bestPitch)) {
            return null;
        }
        float range = 1.0f;
        for (int pass = 0; pass < 3; ++pass) {
            float step = pass == 0 ? 0.25f : (pass == 1 ? 0.05f : 0.01f);
            float start = bestPitch - range;
            float end = bestPitch + range;
            float newBest = bestPitch;
            double newBestScore = bestScore;
            for (float p = start; p <= end; p += step) {
                double dy;
                double d2;
                double score;
                Vec3d hit = this.simulateImpact(yaw, p);
                if (hit == null || !((score = (d2 = hit.squaredDistanceTo(tc)) + (dy = Math.abs(hit.y - tc.y)) * dy * 4.0) < newBestScore) && (!(Math.abs(score - newBestScore) < 1.0E-9) || !(Math.abs(p) < Math.abs(newBest)))) continue;
                newBestScore = score;
                newBest = p;
            }
            bestPitch = newBest;
            bestScore = newBestScore;
            range = Math.max(0.2f, range * 0.5f);
        }
        if (bestScore <= 0.9) {
            return new float[]{yaw, bestPitch};
        }
        return new float[]{yaw, bestPitch};
    }

    private Vec3d simulateImpact(float yaw, float pitch) {
        if (this.mc.world == null || this.mc.player == null) {
            return null;
        }
        Vec3d direction = new Vec3d(-Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)), -Math.sin(Math.toRadians(pitch)), Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))).normalize();
        Vec3d pos = this.mc.player.getEyePos().add(direction.multiply(0.1));
        Vec3d velocity = direction.multiply(1.5);
        double gravity = 0.03;
        double drag = 0.99;
        double step = 0.05;
        int maxSteps = 4000;
        for (int i = 0; i < 4000; ++i) {
            Vec3d nextPos = pos.add(velocity.multiply(0.05));
            BlockHitResult hit = this.mc.world.raycast(new RaycastContext(pos, nextPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)this.mc.player));
            if (hit.getType() != HitResult.Type.MISS) {
                if (hit instanceof BlockHitResult) {
                    BlockHitResult bhr = hit;
                    return bhr.getPos();
                }
                return null;
            }
            pos = nextPos;
            velocity = velocity.multiply(Math.pow(0.99, 0.05));
            velocity = velocity.add(0.0, -0.0015, 0.0);
            if (pos.y < (double)(this.mc.world.getBottomY() - 5)) break;
        }
        return null;
    }
}

