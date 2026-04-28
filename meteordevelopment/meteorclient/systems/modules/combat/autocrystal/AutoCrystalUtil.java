/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.combat.autocrystal;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class AutoCrystalUtil {
    private AutoCrystalUtil() {
    }

    public static BlockHitResult getPlaceBlockHitResult(BlockPos blockPos) {
        Direction dir = AutoCrystalUtil.getPlaceOnDirection(blockPos);
        Vec3d pos = AutoCrystalUtil.getPosForDir(blockPos, dir);
        return new BlockHitResult(pos, dir, blockPos, true);
    }

    private static Direction getPlaceOnDirection(BlockPos blockPos) {
        if (blockPos != null && MeteorClient.mc.world != null && MeteorClient.mc.player != null) {
            Direction bestDir = null;
            double bestDist = -1.0;
            Vec3d eye = MeteorClient.mc.player.getEyePos();
            for (Direction dir : Direction.values()) {
                Vec3d pos = AutoCrystalUtil.getPosForDir(blockPos, dir);
                double dist = eye.distanceTo(pos);
                if (!(dist >= 0.0) || !(bestDist < 0.0) && !(dist < bestDist)) continue;
                bestDir = dir;
                bestDist = dist;
            }
            return bestDir;
        }
        return null;
    }

    private static Vec3d getPosForDir(BlockPos blockPos, Direction dir) {
        Vec3d offset = new Vec3d((double)dir.getOffsetX() / 2.0, (double)dir.getOffsetY() / 2.0, (double)dir.getOffsetZ() / 2.0);
        return blockPos.toCenterPos().add(offset);
    }
}

