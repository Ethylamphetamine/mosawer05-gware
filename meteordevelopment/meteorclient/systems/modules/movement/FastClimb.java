/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.PowderSnowBlock
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.LivingEntityAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class FastClimb
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> timerMode;
    private final Setting<Double> speed;
    private final Setting<Double> timer;
    private boolean resetTimer;

    public FastClimb() {
        super(Categories.Movement, "fast-climb", "Allows you to climb faster.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.timerMode = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("timer-mode")).description("Use timer.")).defaultValue(false)).build());
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("climb-speed")).description("Your climb speed.")).defaultValue(0.2872).min(0.0).visible(() -> this.timerMode.get() == false)).build());
        this.timer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("timer")).description("The timer value for Timer.")).defaultValue(1.436).min(1.0).sliderMin(1.0).visible(this.timerMode::get)).build());
    }

    @Override
    public void onActivate() {
        this.resetTimer = false;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (this.timerMode.get().booleanValue()) {
            if (this.climbing()) {
                this.resetTimer = false;
                Modules.get().get(Timer.class).setOverride(this.timer.get());
            } else if (!this.resetTimer) {
                Modules.get().get(Timer.class).setOverride(1.0);
                this.resetTimer = true;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.timerMode.get().booleanValue() && this.climbing()) {
            Vec3d velocity = this.mc.player.getVelocity();
            this.mc.player.setVelocity(velocity.x, this.speed.get().doubleValue(), velocity.z);
        }
    }

    private boolean climbing() {
        return (this.mc.player.horizontalCollision || ((LivingEntityAccessor)this.mc.player).isJumping()) && (this.mc.player.isClimbing() || this.mc.player.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow((Entity)this.mc.player));
    }
}

