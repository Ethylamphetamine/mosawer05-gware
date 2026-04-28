/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BedBlock
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.util.math.BlockPos;

public class ReverseStep
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> fallSpeed;
    private final Setting<Double> fallDistance;

    public ReverseStep() {
        super(Categories.Movement, "reverse-step", "Allows you to fall down blocks at a greater speed.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.fallSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-speed")).description("How fast to fall in blocks per second.")).defaultValue(3.0).min(0.0).build());
        this.fallDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-distance")).description("The maximum fall distance this setting will activate at.")).defaultValue(3.0).min(0.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.mc.player.isOnGround() || this.mc.player.isHoldingOntoLadder() || this.mc.player.isSubmergedInWater() || this.mc.player.isInLava() || this.mc.options.jumpKey.isPressed() || this.mc.player.noClip || this.mc.player.forwardSpeed == 0.0f && this.mc.player.sidewaysSpeed == 0.0f) {
            return;
        }
        if (!this.isOnBed() && !this.mc.world.isSpaceEmpty(this.mc.player.getBoundingBox().offset(0.0, (double)((float)(-(this.fallDistance.get() + 0.01))), 0.0))) {
            ((IVec3d)this.mc.player.getVelocity()).setY(-this.fallSpeed.get().doubleValue());
        }
    }

    private boolean isOnBed() {
        BlockPos.Mutable blockPos = this.mc.player.getBlockPos().mutableCopy();
        if (this.check(blockPos, 0, 0)) {
            return true;
        }
        double xa = this.mc.player.getX() - (double)blockPos.getX();
        double za = this.mc.player.getZ() - (double)blockPos.getZ();
        if (xa >= 0.0 && xa <= 0.3 && this.check(blockPos, -1, 0)) {
            return true;
        }
        if (xa >= 0.7 && this.check(blockPos, 1, 0)) {
            return true;
        }
        if (za >= 0.0 && za <= 0.3 && this.check(blockPos, 0, -1)) {
            return true;
        }
        if (za >= 0.7 && this.check(blockPos, 0, 1)) {
            return true;
        }
        if (xa >= 0.0 && xa <= 0.3 && za >= 0.0 && za <= 0.3 && this.check(blockPos, -1, -1)) {
            return true;
        }
        if (xa >= 0.0 && xa <= 0.3 && za >= 0.7 && this.check(blockPos, -1, 1)) {
            return true;
        }
        if (xa >= 0.7 && za >= 0.0 && za <= 0.3 && this.check(blockPos, 1, -1)) {
            return true;
        }
        return xa >= 0.7 && za >= 0.7 && this.check(blockPos, 1, 1);
    }

    private boolean check(BlockPos.Mutable blockPos, int x, int z) {
        blockPos.move(x, 0, z);
        boolean is = this.mc.world.getBlockState((BlockPos)blockPos).getBlock() instanceof BedBlock;
        blockPos.move(-x, 0, -z);
        return is;
    }
}

