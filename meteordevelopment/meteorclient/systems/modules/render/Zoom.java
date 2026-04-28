/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.modules.render;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.MathHelper;

public class Zoom
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> zoom;
    private final Setting<Double> scrollSensitivity;
    private final Setting<Boolean> smooth;
    private final Setting<Boolean> cinematic;
    private final Setting<Boolean> renderHands;
    private boolean enabled;
    private boolean preCinematic;
    private double preMouseSensitivity;
    private double value;
    private double lastFov;
    private double time;

    public Zoom() {
        super(Categories.Render, "zoom", "Zooms your view.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.zoom = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("zoom")).description("How much to zoom.")).defaultValue(6.0).min(1.0).build());
        this.scrollSensitivity = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scroll-sensitivity")).description("Allows you to change zoom value using scroll wheel. 0 to disable.")).defaultValue(1.0).min(0.0).build());
        this.smooth = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smooth")).description("Smooth transition.")).defaultValue(true)).build());
        this.cinematic = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("cinematic")).description("Enables cinematic camera.")).defaultValue(false)).build());
        this.renderHands = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-hands")).description("Whether or not to render your hands.")).defaultValue(false)).build());
        this.autoSubscribe = false;
    }

    @Override
    public void onActivate() {
        if (!this.enabled) {
            this.preCinematic = this.mc.options.smoothCameraEnabled;
            this.preMouseSensitivity = (Double)this.mc.options.getMouseSensitivity().getValue();
            this.value = this.zoom.get();
            this.lastFov = ((Integer)this.mc.options.getFov().getValue()).intValue();
            this.time = 0.001;
            MeteorClient.EVENT_BUS.subscribe(this);
            this.enabled = true;
        }
    }

    public void onStop() {
        this.mc.options.smoothCameraEnabled = this.preCinematic;
        this.mc.options.getMouseSensitivity().setValue((Object)this.preMouseSensitivity);
        this.mc.worldRenderer.scheduleTerrainUpdate();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.mc.options.smoothCameraEnabled = this.cinematic.get();
        if (!this.cinematic.get().booleanValue()) {
            this.mc.options.getMouseSensitivity().setValue((Object)(this.preMouseSensitivity / Math.max(this.getScaling() * 0.5, 1.0)));
        }
        if (this.time == 0.0) {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            this.enabled = false;
            this.onStop();
        }
    }

    @EventHandler
    private void onMouseScroll(MouseScrollEvent event) {
        if (this.scrollSensitivity.get() > 0.0 && this.isActive()) {
            this.value += event.value * 0.25 * (this.scrollSensitivity.get() * this.value);
            if (this.value < 1.0) {
                this.value = 1.0;
            }
            event.cancel();
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!this.smooth.get().booleanValue()) {
            this.time = this.isActive() ? 1.0 : 0.0;
            return;
        }
        this.time = this.isActive() ? (this.time += event.frameTime * 5.0) : (this.time -= event.frameTime * 5.0);
        this.time = MathHelper.clamp((double)this.time, (double)0.0, (double)1.0);
    }

    @EventHandler
    private void onGetFov(GetFovEvent event) {
        event.fov /= this.getScaling();
        if (this.lastFov != event.fov) {
            this.mc.worldRenderer.scheduleTerrainUpdate();
        }
        this.lastFov = event.fov;
    }

    public double getScaling() {
        double delta = this.time < 0.5 ? 4.0 * this.time * this.time * this.time : 1.0 - Math.pow(-2.0 * this.time + 2.0, 3.0) / 2.0;
        return MathHelper.lerp((double)delta, (double)1.0, (double)this.value);
    }

    public boolean renderHands() {
        return !this.isActive() || this.renderHands.get() != false;
    }
}

