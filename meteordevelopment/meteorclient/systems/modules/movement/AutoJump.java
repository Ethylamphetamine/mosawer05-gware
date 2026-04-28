/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class AutoJump
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<JumpWhen> jumpIf;
    private final Setting<Double> velocityHeight;

    public AutoJump() {
        super(Categories.Movement, "auto-jump", "Automatically jumps.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The method of jumping.")).defaultValue(Mode.Jump)).build());
        this.jumpIf = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("jump-if")).description("Jump if.")).defaultValue(JumpWhen.Always)).build());
        this.velocityHeight = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("velocity-height")).description("The distance that velocity mode moves you.")).defaultValue(0.25).min(0.0).sliderMax(2.0).build());
    }

    private boolean jump() {
        return switch (this.jumpIf.get().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (this.mc.player.isSprinting() && (this.mc.player.forwardSpeed != 0.0f || this.mc.player.sidewaysSpeed != 0.0f)) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (this.mc.player.forwardSpeed != 0.0f || this.mc.player.sidewaysSpeed != 0.0f) {
                    yield true;
                }
                yield false;
            }
            case 2 -> true;
        };
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.mc.player.isOnGround() || this.mc.player.isSneaking() || !this.jump()) {
            return;
        }
        if (this.mode.get() == Mode.Jump) {
            this.mc.player.jump();
        } else {
            ((IVec3d)this.mc.player.getVelocity()).setY(this.velocityHeight.get());
        }
    }

    public static enum Mode {
        Jump,
        LowHop;

    }

    public static enum JumpWhen {
        Sprinting,
        Walking,
        Always;

    }
}

