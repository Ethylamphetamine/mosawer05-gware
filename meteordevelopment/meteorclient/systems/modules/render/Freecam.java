/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.Perspective
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.events.entity.DamageEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.ChunkOcclusionEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Freecam
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> speed;
    private final Setting<Double> speedScrollSensitivity;
    private final Setting<Boolean> toggleOnDamage;
    private final Setting<Boolean> toggleOnDeath;
    private final Setting<Boolean> toggleOnLog;
    private final Setting<Boolean> reloadChunks;
    private final Setting<Boolean> renderHands;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> staticView;
    public final Vector3d pos;
    public final Vector3d prevPos;
    private Perspective perspective;
    private double speedValue;
    public float yaw;
    public float pitch;
    public float prevYaw;
    public float prevPitch;
    private double fovScale;
    private boolean bobView;
    private boolean forward;
    private boolean backward;
    private boolean right;
    private boolean left;
    private boolean up;
    private boolean down;

    public Freecam() {
        super(Categories.Render, "freecam", "Allows the camera to move away from the player.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed")).description("Your speed while in freecam.")).onChanged(aDouble -> {
            this.speedValue = aDouble;
        })).defaultValue(1.0).min(0.0).build());
        this.speedScrollSensitivity = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("speed-scroll-sensitivity")).description("Allows you to change speed value using scroll wheel. 0 to disable.")).defaultValue(0.0).min(0.0).sliderMax(2.0).build());
        this.toggleOnDamage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-damage")).description("Disables freecam when you take damage.")).defaultValue(false)).build());
        this.toggleOnDeath = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-death")).description("Disables freecam when you die.")).defaultValue(false)).build());
        this.toggleOnLog = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("toggle-on-log")).description("Disables freecam when you disconnect from a server.")).defaultValue(true)).build());
        this.reloadChunks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("reload-chunks")).description("Disables cave culling.")).defaultValue(true)).build());
        this.renderHands = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-hands")).description("Whether or not to render your hands in freecam.")).defaultValue(true)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates to the block or entity you are looking at.")).defaultValue(false)).build());
        this.staticView = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("static")).description("Disables settings that move the view.")).defaultValue(true)).build());
        this.pos = new Vector3d();
        this.prevPos = new Vector3d();
    }

    @Override
    public void onActivate() {
        this.fovScale = (Double)this.mc.options.getFovEffectScale().getValue();
        this.bobView = (Boolean)this.mc.options.getBobView().getValue();
        if (this.staticView.get().booleanValue()) {
            this.mc.options.getFovEffectScale().setValue((Object)0.0);
            this.mc.options.getBobView().setValue((Object)false);
        }
        this.yaw = this.mc.player.getYaw();
        this.pitch = this.mc.player.getPitch();
        this.perspective = this.mc.options.getPerspective();
        this.speedValue = this.speed.get();
        Utils.set(this.pos, this.mc.gameRenderer.getCamera().getPos());
        Utils.set(this.prevPos, this.mc.gameRenderer.getCamera().getPos());
        if (this.mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            this.yaw += 180.0f;
            this.pitch *= -1.0f;
        }
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.forward = this.mc.options.forwardKey.isPressed();
        this.backward = this.mc.options.backKey.isPressed();
        this.right = this.mc.options.rightKey.isPressed();
        this.left = this.mc.options.leftKey.isPressed();
        this.up = this.mc.options.jumpKey.isPressed();
        this.down = this.mc.options.sneakKey.isPressed();
        this.unpress();
        if (this.reloadChunks.get().booleanValue()) {
            this.mc.worldRenderer.reload();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.reloadChunks.get().booleanValue()) {
            this.mc.worldRenderer.reload();
        }
        this.mc.options.setPerspective(this.perspective);
        if (this.staticView.get().booleanValue()) {
            this.mc.options.getFovEffectScale().setValue((Object)this.fovScale);
            this.mc.options.getBobView().setValue((Object)this.bobView);
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        this.unpress();
        this.prevPos.set((Vector3dc)this.pos);
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
    }

    private void unpress() {
        this.mc.options.forwardKey.setPressed(false);
        this.mc.options.backKey.setPressed(false);
        this.mc.options.rightKey.setPressed(false);
        this.mc.options.leftKey.setPressed(false);
        this.mc.options.jumpKey.setPressed(false);
        this.mc.options.sneakKey.setPressed(false);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.cameraEntity.isInsideWall()) {
            this.mc.getCameraEntity().noClip = true;
        }
        if (!this.perspective.isFirstPerson()) {
            this.mc.options.setPerspective(Perspective.FIRST_PERSON);
        }
        Vec3d forward = Vec3d.fromPolar((float)0.0f, (float)this.yaw);
        Vec3d right = Vec3d.fromPolar((float)0.0f, (float)(this.yaw + 90.0f));
        double velX = 0.0;
        double velY = 0.0;
        double velZ = 0.0;
        if (this.rotate.get().booleanValue()) {
            if (this.mc.crosshairTarget instanceof EntityHitResult) {
                crossHairPos = ((EntityHitResult)this.mc.crosshairTarget).getEntity().getBlockPos();
                Rotations.rotate(Rotations.getYaw(crossHairPos), Rotations.getPitch(crossHairPos), 0, null);
            } else {
                Vec3d crossHairPosition = this.mc.crosshairTarget.getPos();
                crossHairPos = ((BlockHitResult)this.mc.crosshairTarget).getBlockPos();
                if (!this.mc.world.getBlockState(crossHairPos).isAir()) {
                    Rotations.rotate(Rotations.getYaw(crossHairPosition), Rotations.getPitch(crossHairPosition), 0, null);
                }
            }
        }
        double s = 0.5;
        if (this.mc.options.sprintKey.isPressed()) {
            s = 1.0;
        }
        boolean a = false;
        if (this.forward) {
            velX += forward.x * s * this.speedValue;
            velZ += forward.z * s * this.speedValue;
            a = true;
        }
        if (this.backward) {
            velX -= forward.x * s * this.speedValue;
            velZ -= forward.z * s * this.speedValue;
            a = true;
        }
        boolean b = false;
        if (this.right) {
            velX += right.x * s * this.speedValue;
            velZ += right.z * s * this.speedValue;
            b = true;
        }
        if (this.left) {
            velX -= right.x * s * this.speedValue;
            velZ -= right.z * s * this.speedValue;
            b = true;
        }
        if (a && b) {
            double diagonal = 1.0 / Math.sqrt(2.0);
            velX *= diagonal;
            velZ *= diagonal;
        }
        if (this.up) {
            velY += s * this.speedValue;
        }
        if (this.down) {
            velY -= s * this.speedValue;
        }
        this.prevPos.set((Vector3dc)this.pos);
        this.pos.set(this.pos.x + velX, this.pos.y + velY, this.pos.z + velZ);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        if (Input.isKeyPressed(292)) {
            return;
        }
        if (this.checkGuiMove()) {
            return;
        }
        boolean cancel = true;
        if (this.mc.options.forwardKey.matchesKey(event.key, 0)) {
            this.forward = event.action != KeyAction.Release;
            this.mc.options.forwardKey.setPressed(false);
        } else if (this.mc.options.backKey.matchesKey(event.key, 0)) {
            this.backward = event.action != KeyAction.Release;
            this.mc.options.backKey.setPressed(false);
        } else if (this.mc.options.rightKey.matchesKey(event.key, 0)) {
            this.right = event.action != KeyAction.Release;
            this.mc.options.rightKey.setPressed(false);
        } else if (this.mc.options.leftKey.matchesKey(event.key, 0)) {
            this.left = event.action != KeyAction.Release;
            this.mc.options.leftKey.setPressed(false);
        } else if (this.mc.options.jumpKey.matchesKey(event.key, 0)) {
            this.up = event.action != KeyAction.Release;
            this.mc.options.jumpKey.setPressed(false);
        } else if (this.mc.options.sneakKey.matchesKey(event.key, 0)) {
            this.down = event.action != KeyAction.Release;
            this.mc.options.sneakKey.setPressed(false);
        } else {
            cancel = false;
        }
        if (cancel) {
            event.cancel();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (this.checkGuiMove()) {
            return;
        }
        boolean cancel = true;
        if (this.mc.options.forwardKey.matchesMouse(event.button)) {
            this.forward = event.action != KeyAction.Release;
            this.mc.options.forwardKey.setPressed(false);
        } else if (this.mc.options.backKey.matchesMouse(event.button)) {
            this.backward = event.action != KeyAction.Release;
            this.mc.options.backKey.setPressed(false);
        } else if (this.mc.options.rightKey.matchesMouse(event.button)) {
            this.right = event.action != KeyAction.Release;
            this.mc.options.rightKey.setPressed(false);
        } else if (this.mc.options.leftKey.matchesMouse(event.button)) {
            this.left = event.action != KeyAction.Release;
            this.mc.options.leftKey.setPressed(false);
        } else if (this.mc.options.jumpKey.matchesMouse(event.button)) {
            this.up = event.action != KeyAction.Release;
            this.mc.options.jumpKey.setPressed(false);
        } else if (this.mc.options.sneakKey.matchesMouse(event.button)) {
            this.down = event.action != KeyAction.Release;
            this.mc.options.sneakKey.setPressed(false);
        } else {
            cancel = false;
        }
        if (cancel) {
            event.cancel();
        }
    }

    @EventHandler(priority=-100)
    private void onMouseScroll(MouseScrollEvent event) {
        if (this.speedScrollSensitivity.get() > 0.0 && this.mc.currentScreen == null) {
            this.speedValue += event.value * 0.25 * (this.speedScrollSensitivity.get() * this.speedValue);
            if (this.speedValue < 0.1) {
                this.speedValue = 0.1;
            }
            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onDamage(DamageEvent event) {
        if (event.entity.getUuid() == null) {
            return;
        }
        if (!event.entity.getUuid().equals(this.mc.player.getUuid())) {
            return;
        }
        if (this.toggleOnDamage.get().booleanValue()) {
            this.toggle();
            this.info("Toggled off because you took damage.", new Object[0]);
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!this.toggleOnLog.get().booleanValue()) {
            return;
        }
        this.toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        DeathMessageS2CPacket packet;
        Entity entity;
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof DeathMessageS2CPacket && (entity = this.mc.world.getEntityById((packet = (DeathMessageS2CPacket)packet2).comp_2275())) == this.mc.player && this.toggleOnDeath.get().booleanValue()) {
            this.toggle();
            this.info("Toggled off because you died.", new Object[0]);
        }
    }

    private boolean checkGuiMove() {
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        if (this.mc.currentScreen != null && !guiMove.isActive()) {
            return true;
        }
        return this.mc.currentScreen != null && guiMove.isActive() && guiMove.skip();
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.yaw = (float)((double)this.yaw + deltaX);
        this.pitch = (float)((double)this.pitch + deltaY);
        this.pitch = MathHelper.clamp((float)this.pitch, (float)-90.0f, (float)90.0f);
    }

    public boolean renderHands() {
        return !this.isActive() || this.renderHands.get() != false;
    }

    public double getX(float tickDelta) {
        return MathHelper.lerp((double)tickDelta, (double)this.prevPos.x, (double)this.pos.x);
    }

    public double getY(float tickDelta) {
        return MathHelper.lerp((double)tickDelta, (double)this.prevPos.y, (double)this.pos.y);
    }

    public double getZ(float tickDelta) {
        return MathHelper.lerp((double)tickDelta, (double)this.prevPos.z, (double)this.pos.z);
    }

    public double getYaw(float tickDelta) {
        return MathHelper.lerp((float)tickDelta, (float)this.prevYaw, (float)this.yaw);
    }

    public double getPitch(float tickDelta) {
        return MathHelper.lerp((float)tickDelta, (float)this.prevPitch, (float)this.pitch);
    }
}

