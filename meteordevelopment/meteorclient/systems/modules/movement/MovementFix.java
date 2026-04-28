/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.entity.player.PlayerJumpEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerTravelEvent;
import meteordevelopment.meteorclient.events.entity.player.UpdatePlayerVelocity;
import meteordevelopment.meteorclient.events.input.KeyboardInputEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GrimDisabler;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

public class MovementFix
extends Module {
    public static MovementFix MOVE_FIX;
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> grimStrict;
    private final Setting<Boolean> grimCobwebSprintJump;
    private final Setting<Boolean> travel;
    public final Setting<UpdateMode> updateMode;
    public static boolean inWebs;
    public static boolean realInWebs;
    public static float fixYaw;
    public static float fixPitch;
    public static float prevYaw;
    public static float prevPitch;
    public static boolean setRot;
    private boolean preJumpSprint;
    public static boolean allowMovementFix;
    public static boolean bypassRotationForThisTick;

    public MovementFix() {
        super(Categories.Movement, "movement-fix", "Fixes movement for rotations");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.grimStrict = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-strict")).description("Strict mode for Grim. Should be off for 2b2t.org and on for other Grim servers.")).defaultValue(false)).build());
        this.grimCobwebSprintJump = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-cobweb-sprint-jump-fix")).description("Fixes rubberbanding when sprint jumping in cobwebs with no slow.")).defaultValue(true)).build());
        this.travel = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fix-travel-rotation")).description("Fixes rotation for travel events.")).defaultValue(true)).build());
        this.updateMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("update-mode")).description("When to fix movement / sync rotations.")).defaultValue(UpdateMode.Packet)).build());
        this.preJumpSprint = false;
        MOVE_FIX = this;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        realInWebs = inWebs;
        inWebs = false;
    }

    @EventHandler
    public void onPreJump(PlayerJumpEvent.Pre e) {
        if (!allowMovementFix) {
            return;
        }
        if (this.mc.player.isRiding() || Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            return;
        }
        prevYaw = this.mc.player.getYaw();
        prevPitch = this.mc.player.getPitch();
        this.mc.player.setYaw(fixYaw);
        this.mc.player.setPitch(fixPitch);
        setRot = true;
        if (realInWebs && this.mc.player.isSprinting() && this.grimCobwebSprintJump.get().booleanValue()) {
            this.preJumpSprint = this.mc.player.isSprinting();
            this.mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onPostJump(PlayerJumpEvent.Post e) {
        if (!allowMovementFix) {
            return;
        }
        if (this.mc.player.isRiding() || Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            return;
        }
        this.mc.player.setYaw(prevYaw);
        this.mc.player.setPitch(prevPitch);
        setRot = false;
        if (realInWebs && this.grimCobwebSprintJump.get().booleanValue()) {
            this.mc.player.setSprinting(this.preJumpSprint);
        }
    }

    @EventHandler
    public void onPreTravel(PlayerTravelEvent.Pre e) {
        if (!allowMovementFix) {
            return;
        }
        if (!this.travel.get().booleanValue()) {
            return;
        }
        if (this.mc.player.isRiding() || Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            return;
        }
        prevYaw = this.mc.player.getYaw();
        prevPitch = this.mc.player.getPitch();
        this.mc.player.setYaw(fixYaw);
        this.mc.player.setPitch(fixPitch);
        setRot = true;
    }

    @EventHandler
    public void onPostTravel(PlayerTravelEvent.Post e) {
        if (!allowMovementFix) {
            return;
        }
        if (!this.travel.get().booleanValue()) {
            return;
        }
        if (this.mc.player.isRiding() || Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            return;
        }
        this.mc.player.setYaw(prevYaw);
        this.mc.player.setPitch(prevPitch);
        setRot = false;
    }

    @EventHandler
    public void onPlayerMove(UpdatePlayerVelocity event) {
        if (!allowMovementFix) {
            return;
        }
        if (this.mc.player.isRiding() || Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            return;
        }
        event.cancel();
        event.setVelocity(PlayerUtils.movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixYaw));
    }

    @EventHandler(priority=-100)
    public void onKeyInput(KeyboardInputEvent e) {
        if (!allowMovementFix) {
            return;
        }
        if (this.mc.player.isRiding() || Modules.get().get(Freecam.class).isActive() || this.mc.player.isFallFlying() || Modules.get().get(GrimDisabler.class).shouldSetYawOverflowRotation()) {
            return;
        }
        float mF = this.mc.player.input.movementForward;
        float mS = this.mc.player.input.movementSideways;
        float delta = (this.mc.player.getYaw() - fixYaw) * ((float)Math.PI / 180);
        float cos = MathHelper.cos((float)delta);
        float sin = MathHelper.sin((float)delta);
        if (this.grimStrict.get().booleanValue()) {
            this.mc.player.input.movementSideways = Math.round(mS * cos - mF * sin);
            this.mc.player.input.movementForward = Math.round(mF * cos + mS * sin);
        } else {
            this.mc.player.input.movementSideways = mS * cos - mF * sin;
            this.mc.player.input.movementForward = mF * cos + mS * sin;
        }
    }

    static {
        inWebs = false;
        realInWebs = false;
        setRot = false;
        allowMovementFix = true;
        bypassRotationForThisTick = false;
    }

    public static enum UpdateMode {
        Packet,
        Mouse,
        Both;

    }
}

