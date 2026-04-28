/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class Chams
extends Module {
    private final SettingGroup sgThroughWalls;
    private final SettingGroup sgPlayers;
    private final SettingGroup sgCrystals;
    private final SettingGroup sgHand;
    public final Setting<Set<EntityType<?>>> entities;
    public final Setting<Shader> shader;
    public final Setting<SettingColor> shaderColor;
    public final Setting<Double> alpha;
    public final Setting<Boolean> ignoreSelfDepth;
    public final Setting<Boolean> players;
    public final Setting<Boolean> ignoreSelf;
    public final Setting<Boolean> playersTexture;
    public final Setting<SettingColor> playersColor;
    public final Setting<Double> playersScale;
    public final Setting<Boolean> crystals;
    public final Setting<Double> crystalsScale;
    public final Setting<Double> crystalsBounce;
    public final Setting<Double> crystalsRotationSpeed;
    public final Setting<Boolean> crystalsTexture;
    public final Setting<Boolean> renderCore;
    public final Setting<SettingColor> crystalsCoreColor;
    public final Setting<Boolean> renderFrame1;
    public final Setting<SettingColor> crystalsFrame1Color;
    public final Setting<Boolean> renderFrame2;
    public final Setting<SettingColor> crystalsFrame2Color;
    public final Setting<Boolean> hand;
    public final Setting<Boolean> handTexture;
    public final Setting<SettingColor> handColor;
    public static final Identifier BLANK = MeteorClient.identifier("textures/blank.png");

    public Chams() {
        super(Categories.Render, "chams", "Tweaks rendering of entities.");
        this.sgThroughWalls = this.settings.createGroup("Through Walls");
        this.sgPlayers = this.settings.createGroup("Players");
        this.sgCrystals = this.settings.createGroup("Crystals");
        this.sgHand = this.settings.createGroup("Hand");
        this.entities = this.sgThroughWalls.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select entities to show through walls.")).build());
        this.shader = this.sgThroughWalls.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shader")).description("Renders a shader over of the entities.")).defaultValue(Shader.Normal)).onModuleActivated(setting -> this.updateShader((Shader)((Object)((Object)setting.get()))))).onChanged(this::updateShader)).build());
        this.shaderColor = this.sgThroughWalls.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The color that the shader is drawn with.")).defaultValue(new SettingColor(255, 255, 255, 150)).visible(() -> this.shader.get() == Shader.Image)).build());
        this.alpha = this.sgThroughWalls.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("alpha")).description("The transparency of the entities.")).defaultValue(1.0).min(0.0).max(1.0).onChanged(this::updateAlpha)).build());
        this.ignoreSelfDepth = this.sgThroughWalls.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Ignores yourself drawing the player.")).defaultValue(true)).build());
        this.players = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("players")).description("Enables model tweaks for players.")).defaultValue(true)).build());
        this.ignoreSelf = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Ignores yourself when tweaking player models.")).defaultValue(false)).visible(this.players::get)).build());
        this.playersTexture = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("texture")).description("Enables player model textures.")).defaultValue(true)).visible(this.players::get)).build());
        this.playersColor = this.sgPlayers.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("color")).description("The color of player models.")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(this.players::get)).build());
        this.playersScale = this.sgPlayers.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Players scale.")).defaultValue(1.0).min(0.0).visible(this.players::get)).build());
        this.crystals = this.sgCrystals.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("crystals")).description("Enables model tweaks for end crystals.")).defaultValue(true)).build());
        this.crystalsScale = this.sgCrystals.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Crystal scale.")).defaultValue(0.6).min(0.0).visible(this.crystals::get)).build());
        this.crystalsBounce = this.sgCrystals.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("bounce")).description("How high crystals bounce.")).defaultValue(0.6).min(0.0).visible(this.crystals::get)).build());
        this.crystalsRotationSpeed = this.sgCrystals.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rotation-speed")).description("Multiplies the rotation speed of the crystal.")).defaultValue(0.3).min(0.0).visible(this.crystals::get)).build());
        this.crystalsTexture = this.sgCrystals.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("texture")).description("Whether to render crystal model textures.")).defaultValue(true)).visible(this.crystals::get)).build());
        this.renderCore = this.sgCrystals.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-core")).description("Enables rendering of the core of the crystal.")).defaultValue(false)).visible(this.crystals::get)).build());
        this.crystalsCoreColor = this.sgCrystals.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("core-color")).description("The color of the core of the crystal.")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> this.crystals.get() != false && this.renderCore.get() != false)).build());
        this.renderFrame1 = this.sgCrystals.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-inner-frame")).description("Enables rendering of the inner frame of the crystal.")).defaultValue(true)).visible(this.crystals::get)).build());
        this.crystalsFrame1Color = this.sgCrystals.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("inner-frame-color")).description("The color of the inner frame of the crystal.")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> this.crystals.get() != false && this.renderFrame1.get() != false)).build());
        this.renderFrame2 = this.sgCrystals.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-outer-frame")).description("Enables rendering of the outer frame of the crystal.")).defaultValue(true)).visible(this.crystals::get)).build());
        this.crystalsFrame2Color = this.sgCrystals.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("outer-frame-color")).description("The color of the outer frame of the crystal.")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(() -> this.crystals.get() != false && this.renderFrame2.get() != false)).build());
        this.hand = this.sgHand.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enabled")).description("Enables tweaks of hand rendering.")).defaultValue(true)).build());
        this.handTexture = this.sgHand.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("texture")).description("Whether to render hand textures.")).defaultValue(true)).visible(this.hand::get)).build());
        this.handColor = this.sgHand.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("hand-color")).description("The color of your hand.")).defaultValue(new SettingColor(255, 255, 255, 255)).visible(this.hand::get)).build());
    }

    public boolean shouldRender(Entity entity) {
        if (!this.isActive()) {
            return false;
        }
        if (this.isShader()) {
            return false;
        }
        boolean shouldRender = this.entities.get().contains(entity.getType());
        if (this.players.get().booleanValue() && entity.getType() == EntityType.PLAYER) {
            shouldRender = true;
        }
        if (this.crystals.get().booleanValue() && entity.getType() == EntityType.END_CRYSTAL) {
            shouldRender = true;
        }
        return shouldRender && (entity != this.mc.player || this.ignoreSelfDepth.get() != false);
    }

    public boolean isShader() {
        return this.isActive() && this.shader.get() == Shader.Image;
    }

    public void updateShader(Shader value) {
        if (value == Shader.Normal) {
            return;
        }
        PostProcessShaders.CHAMS.init(Utils.titleToName(value.name()));
    }

    private void updateAlpha(double value) {
        int newAlpha;
        this.shaderColor.get().a = newAlpha = (int)(value * 255.0);
        this.playersColor.get().a = newAlpha;
        this.handColor.get().a = newAlpha;
        this.crystalsCoreColor.get().a = newAlpha;
        this.crystalsFrame1Color.get().a = newAlpha;
        this.crystalsFrame2Color.get().a = newAlpha;
    }

    public static enum Shader {
        Image,
        Normal;

    }
}

