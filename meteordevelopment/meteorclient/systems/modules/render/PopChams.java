/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PopChams
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> onlyOne;
    private final Setting<Double> renderTime;
    private final Setting<Double> yModifier;
    private final Setting<Double> scaleModifier;
    private final Setting<Boolean> fadeOut;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final List<GhostPlayer> ghosts;

    public PopChams() {
        super(Categories.Render, "pop-chams", "Renders a ghost where players pop totem.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.onlyOne = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-one")).description("Only allow one ghost per player.")).defaultValue(false)).build());
        this.renderTime = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("render-time")).description("How long the ghost is rendered in seconds.")).defaultValue(1.0).min(0.1).sliderMax(6.0).build());
        this.yModifier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("y-modifier")).description("How much should the Y position of the ghost change per second.")).defaultValue(0.75).sliderRange(-4.0, 4.0).build());
        this.scaleModifier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale-modifier")).description("How much should the scale of the ghost change per second.")).defaultValue(-0.25).sliderRange(-4.0, 4.0).build());
        this.fadeOut = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fade-out")).description("Fades out the color.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 255, 255, 25)).build());
        this.lineColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 255, 255, 60)).build());
        this.ghosts = new ArrayList<GhostPlayer>();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void onDeactivate() {
        List<GhostPlayer> list = this.ghosts;
        synchronized (list) {
            this.ghosts.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        PlayerEntity player;
        Entity entity;
        block10: {
            block9: {
                Packet<?> packet = event.packet;
                if (!(packet instanceof EntityStatusS2CPacket)) {
                    return;
                }
                EntityStatusS2CPacket p = (EntityStatusS2CPacket)packet;
                if (p.getStatus() != 35) {
                    return;
                }
                entity = p.getEntity((World)this.mc.world);
                if (!(entity instanceof PlayerEntity)) break block9;
                player = (PlayerEntity)entity;
                if (entity != this.mc.player) break block10;
            }
            return;
        }
        List<GhostPlayer> list = this.ghosts;
        synchronized (list) {
            if (this.onlyOne.get().booleanValue()) {
                this.ghosts.removeIf(ghostPlayer -> ghostPlayer.uuid.equals(entity.getUuid()));
            }
            this.ghosts.add(new GhostPlayer(player));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onRender3D(Render3DEvent event) {
        List<GhostPlayer> list = this.ghosts;
        synchronized (list) {
            this.ghosts.removeIf(ghostPlayer -> ghostPlayer.render(event));
        }
    }

    private class GhostPlayer {
        private final UUID uuid;
        private double timer;
        private double scale = 1.0;
        private PlayerEntity player;
        private List<WireframeEntityRenderer.RenderablePart> parts;
        private Vec3d pos;

        public GhostPlayer(PlayerEntity player) {
            this.uuid = player.getUuid();
            this.player = player;
            this.pos = new Vec3d(0.0, 0.0, 0.0);
        }

        public boolean render(Render3DEvent event) {
            if (this.parts == null) {
                this.parts = WireframeEntityRenderer.cloneEntityForRendering(event, (Entity)this.player, this.pos);
            }
            this.timer += event.frameTime;
            if (this.timer > PopChams.this.renderTime.get()) {
                return true;
            }
            ((IVec3d)this.pos).setY(this.pos.y + PopChams.this.yModifier.get() * event.frameTime);
            this.scale += PopChams.this.scaleModifier.get() * event.frameTime;
            int preSideA = PopChams.this.sideColor.get().a;
            int preLineA = PopChams.this.lineColor.get().a;
            if (PopChams.this.fadeOut.get().booleanValue()) {
                PopChams.this.sideColor.get().a = (int)((double)PopChams.this.sideColor.get().a * (1.0 - this.timer / PopChams.this.renderTime.get()));
                PopChams.this.lineColor.get().a = (int)((double)PopChams.this.lineColor.get().a * (1.0 - this.timer / PopChams.this.renderTime.get()));
            }
            WireframeEntityRenderer.render(event, this.pos, this.parts, this.scale, PopChams.this.sideColor.get(), PopChams.this.lineColor.get(), PopChams.this.shapeMode.get());
            PopChams.this.sideColor.get().a = preSideA;
            PopChams.this.lineColor.get().a = preLineA;
            return false;
        }
    }
}

