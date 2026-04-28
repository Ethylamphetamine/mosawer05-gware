/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen
 *  net.minecraft.client.gui.screen.ingame.AnvilScreen
 *  net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen
 *  net.minecraft.client.gui.screen.ingame.SignEditScreen
 *  net.minecraft.client.gui.screen.ingame.StructureBlockScreen
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Key
 *  net.minecraft.client.util.InputUtil$Type
 *  net.minecraft.item.ItemGroups
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerTickMovementEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.mixin.CreativeInventoryScreenAccessor;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.math.MathHelper;

public class GUIMove
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Screens> screens;
    private final Setting<Boolean> jump;
    private final Setting<Boolean> sneak;
    public final Setting<Boolean> sprint;
    private final Setting<Boolean> arrowsRotate;
    private final Setting<Double> rotateSpeed;

    public GUIMove() {
        super(Categories.Movement, "gui-move", "Allows you to perform various actions while in GUIs.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.screens = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("guis")).description("Which GUIs to move in.")).defaultValue(Screens.Inventory)).build());
        this.jump = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("jump")).description("Allows you to jump while in GUIs.")).defaultValue(true)).onChanged(aBoolean -> {
            if (this.isActive() && !aBoolean.booleanValue()) {
                this.set(this.mc.options.jumpKey, false);
            }
        })).build());
        this.sneak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sneak")).description("Allows you to sneak while in GUIs.")).defaultValue(true)).onChanged(aBoolean -> {
            if (this.isActive() && !aBoolean.booleanValue()) {
                this.set(this.mc.options.sneakKey, false);
            }
        })).build());
        this.sprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sprint")).description("Allows you to sprint while in GUIs.")).defaultValue(true)).onChanged(aBoolean -> {
            if (this.isActive() && !aBoolean.booleanValue()) {
                this.set(this.mc.options.sprintKey, false);
            }
        })).build());
        this.arrowsRotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("arrows-rotate")).description("Allows you to use your arrow keys to rotate while in GUIs.")).defaultValue(true)).build());
        this.rotateSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rotate-speed")).description("Rotation speed while in GUIs.")).defaultValue(4.0).min(0.0).build());
    }

    @Override
    public void onDeactivate() {
        this.set(this.mc.options.forwardKey, false);
        this.set(this.mc.options.backKey, false);
        this.set(this.mc.options.leftKey, false);
        this.set(this.mc.options.rightKey, false);
        if (this.jump.get().booleanValue()) {
            this.set(this.mc.options.jumpKey, false);
        }
        if (this.sneak.get().booleanValue()) {
            this.set(this.mc.options.sneakKey, false);
        }
        if (this.sprint.get().booleanValue()) {
            this.set(this.mc.options.sprintKey, false);
        }
    }

    public boolean disableSpace() {
        return this.isActive() && this.jump.get() != false && this.mc.options.jumpKey.isDefault();
    }

    public boolean disableArrows() {
        return this.isActive() && this.arrowsRotate.get() != false;
    }

    @EventHandler
    private void onPlayerMoveEvent(PlayerTickMovementEvent event) {
        if (this.skip()) {
            return;
        }
        if (this.screens.get() == Screens.GUI && !(this.mc.currentScreen instanceof WidgetScreen)) {
            return;
        }
        if (this.screens.get() == Screens.Inventory && this.mc.currentScreen instanceof WidgetScreen) {
            return;
        }
        this.set(this.mc.options.forwardKey, Input.isPressed(this.mc.options.forwardKey));
        this.set(this.mc.options.backKey, Input.isPressed(this.mc.options.backKey));
        this.set(this.mc.options.leftKey, Input.isPressed(this.mc.options.leftKey));
        this.set(this.mc.options.rightKey, Input.isPressed(this.mc.options.rightKey));
        if (this.jump.get().booleanValue()) {
            this.set(this.mc.options.jumpKey, Input.isPressed(this.mc.options.jumpKey));
        }
        if (this.sneak.get().booleanValue()) {
            this.set(this.mc.options.sneakKey, Input.isPressed(this.mc.options.sneakKey));
        }
        if (this.sprint.get().booleanValue()) {
            this.set(this.mc.options.sprintKey, Input.isPressed(this.mc.options.sprintKey));
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.skip()) {
            return;
        }
        if (this.screens.get() == Screens.GUI && !(this.mc.currentScreen instanceof WidgetScreen)) {
            return;
        }
        if (this.screens.get() == Screens.Inventory && this.mc.currentScreen instanceof WidgetScreen) {
            return;
        }
        float rotationDelta = Math.min((float)(this.rotateSpeed.get() * event.frameTime * 20.0), 100.0f);
        if (this.arrowsRotate.get().booleanValue()) {
            float yaw = this.mc.player.getYaw();
            float pitch = this.mc.player.getPitch();
            if (Input.isKeyPressed(263)) {
                yaw -= rotationDelta;
            }
            if (Input.isKeyPressed(262)) {
                yaw += rotationDelta;
            }
            if (Input.isKeyPressed(265)) {
                pitch -= rotationDelta;
            }
            if (Input.isKeyPressed(264)) {
                pitch += rotationDelta;
            }
            pitch = MathHelper.clamp((float)pitch, (float)-90.0f, (float)90.0f);
            this.mc.player.setYaw(yaw);
            this.mc.player.setPitch(pitch);
        }
    }

    private void set(KeyBinding bind, boolean pressed) {
        boolean wasPressed = bind.isPressed();
        bind.setPressed(pressed);
        InputUtil.Key key = ((KeyBindingAccessor)bind).getKey();
        if (wasPressed != pressed && key.getCategory() == InputUtil.Type.KEYSYM) {
            MeteorClient.EVENT_BUS.post(KeyEvent.get(key.getCode(), 0, pressed ? KeyAction.Press : KeyAction.Release));
        }
    }

    public boolean skip() {
        return this.mc.currentScreen == null || this.mc.currentScreen instanceof CreativeInventoryScreen && CreativeInventoryScreenAccessor.getSelectedTab() == ItemGroups.getSearchGroup() || this.mc.currentScreen instanceof ChatScreen || this.mc.currentScreen instanceof SignEditScreen || this.mc.currentScreen instanceof AnvilScreen || this.mc.currentScreen instanceof AbstractCommandBlockScreen || this.mc.currentScreen instanceof StructureBlockScreen;
    }

    public static enum Screens {
        GUI,
        Inventory,
        Both;

    }
}

