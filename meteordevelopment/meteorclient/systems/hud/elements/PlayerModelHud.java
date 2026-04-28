/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.util.math.MathHelper
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PlayerModelHud
extends HudElement {
    public static final HudElementInfo<PlayerModelHud> INFO = new HudElementInfo<PlayerModelHud>(Hud.GROUP, "player-model", "Displays a model of your player.", PlayerModelHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBackground;
    private final Setting<Double> scale;
    private final Setting<Boolean> copyYaw;
    private final Setting<Integer> customYaw;
    private final Setting<Boolean> copyPitch;
    private final Setting<Integer> customPitch;
    private final Setting<CenterOrientation> centerOrientation;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public PlayerModelHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBackground = this.settings.createGroup("Background");
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).onChanged(aDouble -> this.calculateSize())).build());
        this.copyYaw = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("copy-yaw")).description("Makes the player model's yaw equal to yours.")).defaultValue(true)).build());
        this.customYaw = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("custom-yaw")).description("Custom yaw for when copy yaw is off.")).defaultValue(0)).range(-180, 180).sliderRange(-180, 180).visible(() -> this.copyYaw.get() == false)).build());
        this.copyPitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("copy-pitch")).description("Makes the player model's pitch equal to yours.")).defaultValue(true)).build());
        this.customPitch = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("custom-pitch")).description("Custom pitch for when copy pitch is off.")).defaultValue(0)).range(-90, 90).sliderRange(-90, 90).visible(() -> this.copyPitch.get() == false)).build());
        this.centerOrientation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("center-orientation")).description("Which direction the player faces when the HUD model faces directly forward.")).defaultValue(CenterOrientation.South)).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            ClientPlayerEntity player = MeteorClient.mc.player;
            if (player == null) {
                return;
            }
            float offset = this.centerOrientation.get() == CenterOrientation.North ? 180.0f : 0.0f;
            float yaw = this.copyYaw.get() != false ? MathHelper.wrapDegrees((float)(player.prevYaw + (player.getYaw() - player.prevYaw) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true) + offset)) : (float)this.customYaw.get().intValue();
            float pitch = this.copyPitch.get() != false ? player.getPitch() : (float)this.customPitch.get().intValue();
            this.drawEntity(renderer.drawContext, this.x, this.y, (int)(30.0 * this.scale.get()), -yaw, -pitch, (LivingEntity)player);
        });
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        } else if (MeteorClient.mc.player == null) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
            renderer.line(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.GRAY);
            renderer.line(this.x + this.getWidth(), this.y, this.x, this.y + this.getHeight(), Color.GRAY);
        }
    }

    private void calculateSize() {
        this.setSize(50.0 * this.scale.get(), 75.0 * this.scale.get());
    }

    private void drawEntity(DrawContext context, int x, int y, int size, float yaw, float pitch, LivingEntity entity) {
        float tanYaw = (float)Math.atan(yaw / 40.0f);
        float tanPitch = (float)Math.atan(pitch / 40.0f);
        Quaternionf quaternion = new Quaternionf().rotateZ((float)Math.PI);
        float previousBodyYaw = entity.bodyYaw;
        float previousYaw = entity.getYaw();
        float previousPitch = entity.getPitch();
        float previousPrevHeadYaw = entity.prevHeadYaw;
        float prevHeadYaw = entity.headYaw;
        entity.bodyYaw = 180.0f + tanYaw * 20.0f;
        entity.setYaw(180.0f + tanYaw * 40.0f);
        entity.setPitch(-tanPitch * 20.0f);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();
        InventoryScreen.drawEntity((DrawContext)context, (float)(x + this.getWidth() / 2), (float)((float)y + (float)this.getHeight() * 0.9f), (float)size, (Vector3f)new Vector3f(), (Quaternionf)quaternion, null, (LivingEntity)entity);
        entity.bodyYaw = previousBodyYaw;
        entity.setYaw(previousYaw);
        entity.setPitch(previousPitch);
        entity.prevHeadYaw = previousPrevHeadYaw;
        entity.headYaw = prevHeadYaw;
    }

    private static enum CenterOrientation {
        North,
        South;

    }
}

