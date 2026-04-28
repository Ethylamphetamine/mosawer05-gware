/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.KeyBinding
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.NopPathManager;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;

public class AutoWalk
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Direction> direction;

    public AutoWalk() {
        super(Categories.Movement, "auto-walk", "Automatically walks forward.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Walking mode.")).defaultValue(Mode.Smart)).onChanged(mode1 -> {
            if (this.isActive()) {
                if (mode1 == Mode.Simple) {
                    PathManagers.get().stop();
                } else {
                    this.createGoal();
                }
                this.unpress();
            }
        })).build());
        this.direction = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("simple-direction")).description("The direction to walk in simple mode.")).defaultValue(Direction.Forwards)).onChanged(direction1 -> {
            if (this.isActive()) {
                this.unpress();
            }
        })).visible(() -> this.mode.get() == Mode.Simple)).build());
    }

    @Override
    public void onActivate() {
        if (this.mode.get() == Mode.Smart) {
            this.createGoal();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.mode.get() == Mode.Simple) {
            this.unpress();
        } else {
            PathManagers.get().stop();
        }
    }

    @EventHandler(priority=100)
    private void onTick(TickEvent.Pre event) {
        if (this.mode.get() == Mode.Simple) {
            switch (this.direction.get().ordinal()) {
                case 0: {
                    this.setPressed(this.mc.options.forwardKey, true);
                    break;
                }
                case 1: {
                    this.setPressed(this.mc.options.backKey, true);
                    break;
                }
                case 2: {
                    this.setPressed(this.mc.options.leftKey, true);
                    break;
                }
                case 3: {
                    this.setPressed(this.mc.options.rightKey, true);
                }
            }
        } else if (PathManagers.get() instanceof NopPathManager) {
            this.info("Smart mode requires Baritone", new Object[0]);
            this.toggle();
        }
    }

    private void unpress() {
        this.setPressed(this.mc.options.forwardKey, false);
        this.setPressed(this.mc.options.backKey, false);
        this.setPressed(this.mc.options.leftKey, false);
        this.setPressed(this.mc.options.rightKey, false);
    }

    private void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
        Input.setKeyState(key, pressed);
    }

    private void createGoal() {
        PathManagers.get().moveInDirection(this.mc.player.getYaw());
    }

    public static enum Mode {
        Simple,
        Smart;

    }

    public static enum Direction {
        Forwards,
        Backwards,
        Left,
        Right;

    }
}

