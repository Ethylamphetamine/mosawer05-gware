/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.SpawnGroup
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.WireframeEntityRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;

public class ESP
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgColors;
    public final Setting<Mode> mode;
    public final Setting<Integer> outlineWidth;
    public final Setting<Double> glowMultiplier;
    public final Setting<Boolean> ignoreSelf;
    public final Setting<ShapeMode> shapeMode;
    public final Setting<Double> fillOpacity;
    private final Setting<Double> fadeDistance;
    private final Setting<Double> endCrystalFadeDistance;
    private final Setting<Double> maxRange;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<SettingColor> playersLineColor;
    private final Setting<SettingColor> playersSideColor;
    private final Setting<SettingColor> friendPlayersLineColor;
    private final Setting<SettingColor> friendPlayersSideColor;
    private final Setting<SettingColor> enemyPlayersLineColor;
    private final Setting<SettingColor> enemyPlayersSideColor;
    private final Setting<SettingColor> animalsLineColor;
    private final Setting<SettingColor> animalsSideColor;
    private final Setting<SettingColor> waterAnimalsLineColor;
    private final Setting<SettingColor> waterAnimalsSideColor;
    private final Setting<SettingColor> monstersLineColor;
    private final Setting<SettingColor> monstersSideColor;
    private final Setting<SettingColor> ambientLineColor;
    private final Setting<SettingColor> ambientSideColor;
    private final Setting<SettingColor> miscLineColor;
    private final Setting<SettingColor> miscSideColor;
    private final Color lineColor;
    private final Color sideColor;
    private final Color baseSideColor;
    private final Color baseLineColor;
    private final Vector3d pos1;
    private final Vector3d pos2;
    private final Vector3d pos;
    private int count;

    public ESP() {
        super(Categories.Render, "esp", "Renders entities through walls.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgColors = this.settings.createGroup("Colors");
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Rendering mode.")).defaultValue(Mode.Shader)).build());
        this.outlineWidth = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("outline-width")).description("The width of the shader outline.")).visible(() -> this.mode.get() == Mode.Shader)).defaultValue(2)).range(1, 10).sliderRange(1, 5).build());
        this.glowMultiplier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("glow-multiplier")).description("Multiplier for glow effect")).visible(() -> this.mode.get() == Mode.Shader)).decimalPlaces(3).defaultValue(3.5).min(0.0).sliderMax(10.0).build());
        this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Ignores yourself drawing the shader.")).defaultValue(true)).build());
        this.shapeMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).visible(() -> this.mode.get() != Mode.Glow)).defaultValue(ShapeMode.Both)).build());
        this.fillOpacity = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fill-opacity")).description("The opacity of the shape fill.")).visible(() -> this.shapeMode.get() != ShapeMode.Lines && this.mode.get() != Mode.Glow)).defaultValue(0.3).range(0.0, 1.0).sliderMax(1.0).build());
        this.fadeDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fade-distance")).description("The distance from an entity where the color begins to fade.")).defaultValue(3.0).min(0.0).sliderMax(12.0).build());
        this.endCrystalFadeDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("end-crystal-fade-distance")).description("The distance from an end crystal where the color begins to fade.")).defaultValue(3.0).min(0.0).sliderMax(12.0).build());
        this.maxRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-range")).description("Maximum distance to render ESP. Set to 0 for unlimited.")).defaultValue(0.0).min(0.0).sliderMax(256.0).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select specific entities.")).defaultValue(EntityType.PLAYER).build());
        this.playersLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("players-line-color")).description("The line color for players.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.playersSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("players-side-color")).description("The side color for players.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.friendPlayersLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("friend-players-line-color")).description("The line color for players you have added.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.friendPlayersSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("friend-players-side-color")).description("The side color for playersyou have added.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.enemyPlayersLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("enemy-players-line-color")).description("The line color for players you have enemied.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.enemyPlayersSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("enemy-players-side-color")).description("The side color for players you have enemied.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.animalsLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("animals-line-color")).description("The line color for animals.")).defaultValue(new SettingColor(25, 255, 25, 255)).build());
        this.animalsSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("animals-side-color")).description("The side color for animals.")).defaultValue(new SettingColor(25, 255, 25, 255)).build());
        this.waterAnimalsLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("water-animals-line-color")).description("The line color for water animals.")).defaultValue(new SettingColor(25, 25, 255, 255)).build());
        this.waterAnimalsSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("water-animals-side-color")).description("The side color for water animals.")).defaultValue(new SettingColor(25, 25, 255, 255)).build());
        this.monstersLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("monsters-line-color")).description("The line color for monsters.")).defaultValue(new SettingColor(255, 25, 25, 255)).build());
        this.monstersSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("monsters-side-color")).description("The side color for monsters.")).defaultValue(new SettingColor(255, 25, 25, 255)).build());
        this.ambientLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ambient-line-color")).description("The line color for ambient entities.")).defaultValue(new SettingColor(25, 25, 25, 255)).build());
        this.ambientSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ambient-side-color")).description("The side color for ambient entities.")).defaultValue(new SettingColor(25, 25, 25, 255)).build());
        this.miscLineColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("misc-line-color")).description("The line color for miscellaneous entities.")).defaultValue(new SettingColor(175, 175, 175, 255)).build());
        this.miscSideColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("misc-side-color")).description("The side color for miscellaneous entities.")).defaultValue(new SettingColor(175, 175, 175, 255)).build());
        this.lineColor = new Color();
        this.sideColor = new Color();
        this.baseSideColor = new Color();
        this.baseLineColor = new Color();
        this.pos1 = new Vector3d();
        this.pos2 = new Vector3d();
        this.pos = new Vector3d();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (this.mode.get() == Mode._2D) {
            return;
        }
        this.count = 0;
        for (Entity entity : this.mc.world.getEntities()) {
            if (this.shouldSkip(entity)) continue;
            if (this.mode.get() == Mode.Box || this.mode.get() == Mode.Wireframe) {
                this.drawBoundingBox(event, entity);
            }
            ++this.count;
        }
    }

    private void drawBoundingBox(Render3DEvent event, Entity entity) {
        Color entitySideColor = this.getSideColor(entity);
        Color entityLineColor = this.getLineColor(entity);
        if (entitySideColor != null && entityLineColor != null) {
            double alpha = 1.0;
            if (entity instanceof EndCrystalEntity) {
                double fadeDist = this.endCrystalFadeDistance.get() * this.endCrystalFadeDistance.get();
                double distance = PlayerUtils.squaredDistanceToCamera(entity);
                alpha = distance <= fadeDist / 2.0 ? 1.0 : (distance >= fadeDist * 2.0 ? 0.0 : 1.0 - (distance - fadeDist / 2.0) / (fadeDist * 1.5));
                alpha = alpha <= 0.075 ? 0.0 : (alpha += 0.1);
                if (alpha > 1.0) {
                    alpha = 1.0;
                }
            }
            this.sideColor.set(entitySideColor).a((int)((double)this.sideColor.a * this.fillOpacity.get() * alpha * alpha));
            this.lineColor.set(entityLineColor).a((int)((double)this.lineColor.a * alpha * alpha));
        }
        if (this.mode.get() == Mode.Box) {
            double x = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderX, (double)entity.getX()) - entity.getX();
            double y = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderY, (double)entity.getY()) - entity.getY();
            double z = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderZ, (double)entity.getZ()) - entity.getZ();
            Box box = entity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, this.sideColor, this.lineColor, this.shapeMode.get(), 0);
        } else {
            WireframeEntityRenderer.render(event, entity, 1.0, this.sideColor, this.lineColor, this.shapeMode.get());
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.mode.get() != Mode._2D) {
            return;
        }
        Renderer2D.COLOR.begin();
        this.count = 0;
        for (Entity entity : this.mc.world.getEntities()) {
            if (this.shouldSkip(entity)) continue;
            Box box = entity.getBoundingBox();
            double x = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderX, (double)entity.getX()) - entity.getX();
            double y = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderY, (double)entity.getY()) - entity.getY();
            double z = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderZ, (double)entity.getZ()) - entity.getZ();
            this.pos1.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            this.pos2.set(0.0, 0.0, 0.0);
            if (this.checkCorner(box.minX + x, box.minY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.minY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.minX + x, box.minY + y, box.maxZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.minY + y, box.maxZ + z, this.pos1, this.pos2) || this.checkCorner(box.minX + x, box.maxY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.maxY + y, box.minZ + z, this.pos1, this.pos2) || this.checkCorner(box.minX + x, box.maxY + y, box.maxZ + z, this.pos1, this.pos2) || this.checkCorner(box.maxX + x, box.maxY + y, box.maxZ + z, this.pos1, this.pos2)) continue;
            Color entitySideColor = this.getSideColor(entity);
            Color entityLineColor = this.getLineColor(entity);
            if (entitySideColor != null && entityLineColor != null) {
                this.sideColor.set(entitySideColor).a((int)((double)this.sideColor.a * this.fillOpacity.get()));
                this.lineColor.set(entityLineColor);
            }
            if (this.shapeMode.get() != ShapeMode.Lines && this.sideColor.a > 0) {
                Renderer2D.COLOR.quad(this.pos1.x, this.pos1.y, this.pos2.x - this.pos1.x, this.pos2.y - this.pos1.y, this.sideColor);
            }
            if (this.shapeMode.get() != ShapeMode.Sides) {
                Renderer2D.COLOR.line(this.pos1.x, this.pos1.y, this.pos1.x, this.pos2.y, this.lineColor);
                Renderer2D.COLOR.line(this.pos2.x, this.pos1.y, this.pos2.x, this.pos2.y, this.lineColor);
                Renderer2D.COLOR.line(this.pos1.x, this.pos1.y, this.pos2.x, this.pos1.y, this.lineColor);
                Renderer2D.COLOR.line(this.pos1.x, this.pos2.y, this.pos2.x, this.pos2.y, this.lineColor);
            }
            ++this.count;
        }
        Renderer2D.COLOR.render(null);
    }

    private boolean checkCorner(double x, double y, double z, Vector3d min, Vector3d max) {
        this.pos.set(x, y, z);
        if (!NametagUtils.to2D(this.pos, 1.0)) {
            return true;
        }
        if (this.pos.x < min.x) {
            min.x = this.pos.x;
        }
        if (this.pos.y < min.y) {
            min.y = this.pos.y;
        }
        if (this.pos.z < min.z) {
            min.z = this.pos.z;
        }
        if (this.pos.x > max.x) {
            max.x = this.pos.x;
        }
        if (this.pos.y > max.y) {
            max.y = this.pos.y;
        }
        if (this.pos.z > max.z) {
            max.z = this.pos.z;
        }
        return false;
    }

    public boolean shouldSkip(Entity entity) {
        if (!this.entities.get().contains(entity.getType())) {
            return true;
        }
        if (entity == this.mc.player && this.ignoreSelf.get().booleanValue()) {
            return true;
        }
        if (entity == this.mc.cameraEntity && this.mc.options.getPerspective().isFirstPerson()) {
            return true;
        }
        double range = this.maxRange.get();
        if (range > 0.0 && PlayerUtils.squaredDistanceToCamera(entity) > range * range) {
            return true;
        }
        return !EntityUtils.isInRenderDistance(entity);
    }

    public Color getLineColor(Entity entity) {
        if (!this.entities.get().contains(entity.getType())) {
            return null;
        }
        double alpha = this.getFadeAlpha(entity);
        if (alpha == 0.0) {
            return null;
        }
        Color color = this.getEntityTypeLineColor(entity);
        return this.baseLineColor.set(color.r, color.g, color.b, (int)((double)color.a * alpha));
    }

    public Color getSideColor(Entity entity) {
        if (!this.entities.get().contains(entity.getType())) {
            return null;
        }
        double alpha = this.getFadeAlpha(entity);
        if (alpha == 0.0) {
            return null;
        }
        Color color = this.getEntityTypeSideColor(entity);
        return this.baseSideColor.set(color.r, color.g, color.b, (int)((double)color.a * alpha));
    }

    public Color getEntityTypeLineColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (Friends.get().isFriend(player)) {
                return this.friendPlayersLineColor.get();
            }
            if (Friends.get().isEnemy(player)) {
                return this.enemyPlayersLineColor.get();
            }
            return this.playersLineColor.get();
        }
        return switch (entity.getType().getSpawnGroup()) {
            case SpawnGroup.CREATURE -> this.animalsLineColor.get();
            case SpawnGroup.WATER_AMBIENT, SpawnGroup.WATER_CREATURE, SpawnGroup.UNDERGROUND_WATER_CREATURE, SpawnGroup.AXOLOTLS -> this.waterAnimalsLineColor.get();
            case SpawnGroup.MONSTER -> this.monstersLineColor.get();
            case SpawnGroup.AMBIENT -> this.ambientLineColor.get();
            default -> this.miscLineColor.get();
        };
    }

    public Color getEntityTypeSideColor(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (Friends.get().isFriend(player)) {
                return this.friendPlayersSideColor.get();
            }
            if (Friends.get().isEnemy(player)) {
                return this.enemyPlayersSideColor.get();
            }
            return this.playersSideColor.get();
        }
        return switch (entity.getType().getSpawnGroup()) {
            case SpawnGroup.CREATURE -> this.animalsSideColor.get();
            case SpawnGroup.WATER_AMBIENT, SpawnGroup.WATER_CREATURE, SpawnGroup.UNDERGROUND_WATER_CREATURE, SpawnGroup.AXOLOTLS -> this.waterAnimalsSideColor.get();
            case SpawnGroup.MONSTER -> this.monstersSideColor.get();
            case SpawnGroup.AMBIENT -> this.ambientSideColor.get();
            default -> this.miscSideColor.get();
        };
    }

    private double getFadeAlpha(Entity entity) {
        double dist = PlayerUtils.squaredDistanceToCamera(entity.getX() + (double)(entity.getWidth() / 2.0f), entity.getY() + (double)entity.getEyeHeight(entity.getPose()), entity.getZ() + (double)(entity.getWidth() / 2.0f));
        double fadeDist = Math.pow(this.fadeDistance.get(), 2.0);
        double alpha = 1.0;
        if (dist <= fadeDist * fadeDist) {
            alpha = (float)(Math.sqrt(dist) / fadeDist);
        }
        if (alpha <= 0.075) {
            alpha = 0.0;
        }
        return alpha;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public boolean isShader() {
        return this.isActive() && this.mode.get() == Mode.Shader;
    }

    public boolean isGlow() {
        return this.isActive() && this.mode.get() == Mode.Glow;
    }

    public static enum Mode {
        Box,
        Wireframe,
        _2D,
        Shader,
        Glow;


        public String toString() {
            return this == _2D ? "2D" : super.toString();
        }
    }
}

