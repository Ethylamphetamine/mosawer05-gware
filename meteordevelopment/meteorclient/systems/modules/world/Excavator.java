/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.IBaritone
 *  baritone.api.utils.BetterBlockPos
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.systems.modules.world;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.utils.BetterBlockPos;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class Excavator
extends Module {
    private final IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRendering = this.settings.createGroup("Rendering");
    private final Setting<Keybind> selectionBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("selection-bind")).description("Bind to draw selection.")).defaultValue(Keybind.fromButton(1))).build());
    private final Setting<Boolean> logSelection = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("log-selection")).description("Logs the selection coordinates to the chat.")).defaultValue(true)).build());
    private final Setting<Boolean> keepActive = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("keep-active")).description("Keep the module active after finishing the excavation.")).defaultValue(false)).build());
    private final Setting<ShapeMode> shapeMode = this.sgRendering.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
    private final Setting<SettingColor> sideColor = this.sgRendering.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 255, 255, 50)).build());
    private final Setting<SettingColor> lineColor = this.sgRendering.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 255)).build());
    private Status status = Status.SEL_START;
    private BetterBlockPos start;
    private BetterBlockPos end;

    public Excavator() {
        super(Categories.World, "excavator", "Excavate a selection area.");
    }

    @Override
    public void onDeactivate() {
        this.baritone.getSelectionManager().removeSelection(this.baritone.getSelectionManager().getLastSelection());
        if (this.baritone.getBuilderProcess().isActive()) {
            this.baritone.getCommandManager().execute("stop");
        }
        this.status = Status.SEL_START;
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press || !this.selectionBind.get().isPressed() || this.mc.currentScreen != null) {
            return;
        }
        this.selectCorners();
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action != KeyAction.Press || !this.selectionBind.get().isPressed() || this.mc.currentScreen != null) {
            return;
        }
        this.selectCorners();
    }

    private void selectCorners() {
        HitResult hitResult = this.mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult)) {
            return;
        }
        BlockHitResult result = (BlockHitResult)hitResult;
        if (this.status == Status.SEL_START) {
            this.start = BetterBlockPos.from((BlockPos)result.getBlockPos());
            this.status = Status.SEL_END;
            if (this.logSelection.get().booleanValue()) {
                this.info("Start corner set: (%d, %d, %d)".formatted(this.start.getX(), this.start.getY(), this.start.getZ()), new Object[0]);
            }
        } else if (this.status == Status.SEL_END) {
            this.end = BetterBlockPos.from((BlockPos)result.getBlockPos());
            this.status = Status.WORKING;
            if (this.logSelection.get().booleanValue()) {
                this.info("End corner set: (%d, %d, %d)".formatted(this.end.getX(), this.end.getY(), this.end.getZ()), new Object[0]);
            }
            this.baritone.getSelectionManager().addSelection(this.start, this.end);
            this.baritone.getBuilderProcess().clearArea((BlockPos)this.start, (BlockPos)this.end);
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.status == Status.SEL_START || this.status == Status.SEL_END) {
            HitResult hitResult = this.mc.crosshairTarget;
            if (!(hitResult instanceof BlockHitResult)) {
                return;
            }
            BlockHitResult result = (BlockHitResult)hitResult;
            event.renderer.box(result.getBlockPos(), (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
        } else if (this.status == Status.WORKING && !this.baritone.getBuilderProcess().isActive()) {
            if (this.keepActive.get().booleanValue()) {
                this.baritone.getSelectionManager().removeSelection(this.baritone.getSelectionManager().getLastSelection());
                this.status = Status.SEL_START;
            } else {
                this.toggle();
            }
        }
    }

    private static enum Status {
        SEL_START,
        SEL_END,
        WORKING;

    }
}

