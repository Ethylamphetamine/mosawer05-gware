/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.ExperienceOrbEntity
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.SpawnGroup
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.math.Vec2f
 *  org.joml.Vector2f
 *  org.joml.Vector2fc
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3d;

public class Tracers
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgAppearance;
    private final SettingGroup sgColors;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<List<Item>> itemTargets;
    private final Setting<Integer> minExperienceOrbSize;
    private final Setting<Boolean> ignoreSelf;
    public final Setting<Boolean> ignoreFriends;
    public final Setting<Boolean> ignoreNakeds;
    public final Setting<Boolean> showInvis;
    private final Setting<TracerStyle> style;
    private final Setting<Target> target;
    private final Setting<Boolean> stem;
    private final Setting<Integer> maxDist;
    private final Setting<Integer> distanceOffscreen;
    private final Setting<Integer> sizeOffscreen;
    private final Setting<Boolean> blinkOffscreen;
    private final Setting<Double> blinkOffscreenSpeed;
    public final Setting<Boolean> distance;
    public final Setting<Boolean> friendOverride;
    private final Setting<SettingColor> playersColor;
    private final Setting<SettingColor> animalsColor;
    private final Setting<SettingColor> waterAnimalsColor;
    private final Setting<SettingColor> monstersColor;
    private final Setting<SettingColor> ambientColor;
    private final Setting<SettingColor> miscColor;
    private int count;
    private Instant initTimer;

    public Tracers() {
        super(Categories.Render, "tracers", "Displays tracer lines to specified entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgAppearance = this.settings.createGroup("Appearance");
        this.sgColors = this.settings.createGroup("Colors");
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select specific entities.")).defaultValue(EntityType.PLAYER).build());
        this.itemTargets = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("items")).description("Select specific items to target.")).visible(() -> this.entities.get().contains(EntityType.ITEM))).build());
        this.minExperienceOrbSize = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("minimum-experience-orb-size")).description("Only draws tracers to specific sizes of xp orbs.")).visible(() -> this.entities.get().contains(EntityType.EXPERIENCE_ORB))).defaultValue(0)).min(0).sliderMax(10).build());
        this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Doesn't draw tracers to yourself when in third person or freecam.")).defaultValue(false)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Doesn't draw tracers to friends.")).defaultValue(false)).build());
        this.ignoreNakeds = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Doesn't draw tracers to players with no armor.")).defaultValue(false)).build());
        this.showInvis = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-invisible")).description("Shows invisible entities.")).defaultValue(true)).build());
        this.style = this.sgAppearance.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("style")).description("What display mode should be used")).defaultValue(TracerStyle.Lines)).build());
        this.target = this.sgAppearance.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target")).description("What part of the entity to target.")).defaultValue(Target.Body)).visible(() -> this.style.get() == TracerStyle.Lines)).build());
        this.stem = this.sgAppearance.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stem")).description("Draw a line through the center of the tracer target.")).defaultValue(true)).visible(() -> this.style.get() == TracerStyle.Lines)).build());
        this.maxDist = this.sgAppearance.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-distance")).description("Maximum distance for tracers to show.")).defaultValue(256)).min(0).sliderMax(256).build());
        this.distanceOffscreen = this.sgAppearance.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("distance-offscreen")).description("Offscreen's distance from center.")).defaultValue(200)).min(0).sliderMax(500).visible(() -> this.style.get() == TracerStyle.Offscreen)).build());
        this.sizeOffscreen = this.sgAppearance.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("size-offscreen")).description("Offscreen's size.")).defaultValue(10)).min(2).sliderMax(50).visible(() -> this.style.get() == TracerStyle.Offscreen)).build());
        this.blinkOffscreen = this.sgAppearance.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blink-offscreen")).description("Make offscreen Blink.")).defaultValue(true)).visible(() -> this.style.get() == TracerStyle.Offscreen)).build());
        this.blinkOffscreenSpeed = this.sgAppearance.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("blink-offscreen-speed")).description("Offscreen's blink speed.")).defaultValue(4.0).min(1.0).sliderMax(15.0).visible(() -> this.style.get() == TracerStyle.Offscreen && this.blinkOffscreen.get() != false)).build());
        this.distance = this.sgColors.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance-colors")).description("Changes the color of tracers depending on distance.")).defaultValue(false)).build());
        this.friendOverride = this.sgColors.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-friend-colors")).description("Whether or not to override the distance color of friends with the friend color.")).defaultValue(true)).visible(() -> this.distance.get() != false && this.ignoreFriends.get() == false)).build());
        this.playersColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("players-colors")).description("The player's color.")).defaultValue(new SettingColor(205, 205, 205, 127)).visible(() -> this.distance.get() == false)).build());
        this.animalsColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("animals-color")).description("The animal's color.")).defaultValue(new SettingColor(145, 255, 145, 127)).visible(() -> this.distance.get() == false)).build());
        this.waterAnimalsColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("water-animals-color")).description("The water animal's color.")).defaultValue(new SettingColor(145, 145, 255, 127)).visible(() -> this.distance.get() == false)).build());
        this.monstersColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("monsters-color")).description("The monster's color.")).defaultValue(new SettingColor(255, 145, 145, 127)).visible(() -> this.distance.get() == false)).build());
        this.ambientColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ambient-color")).description("The ambient color.")).defaultValue(new SettingColor(75, 75, 75, 127)).visible(() -> this.distance.get() == false)).build());
        this.miscColor = this.sgColors.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("misc-color")).description("The misc color.")).defaultValue(new SettingColor(145, 145, 145, 127)).visible(() -> this.distance.get() == false)).build());
        this.initTimer = Instant.now();
    }

    private boolean shouldBeIgnored(Entity entity) {
        ExperienceOrbEntity exp;
        boolean normalIgnore;
        boolean bl = !PlayerUtils.isWithin(entity, (double)this.maxDist.get().intValue()) || !Modules.get().isActive(Freecam.class) && entity == this.mc.player || !this.entities.get().contains(entity.getType()) || this.ignoreSelf.get() != false && entity == this.mc.player || this.ignoreFriends.get() != false && entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity)entity) || (this.showInvis.get() == false && entity.isInvisible()) | !EntityUtils.isInRenderDistance(entity) ? true : (normalIgnore = false);
        if (normalIgnore) {
            return true;
        }
        if (entity instanceof ItemEntity) {
            ItemEntity item = (ItemEntity)entity;
            if (!this.itemTargets.get().contains(item.getStack().getItem())) {
                return true;
            }
        }
        if (entity instanceof ExperienceOrbEntity && (exp = (ExperienceOrbEntity)entity).getOrbSize() < this.minExperienceOrbSize.get()) {
            return true;
        }
        if (this.ignoreNakeds.get().booleanValue() && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (((ItemStack)player.getInventory().armor.get(0)).isEmpty() && ((ItemStack)player.getInventory().armor.get(1)).isEmpty() && ((ItemStack)player.getInventory().armor.get(2)).isEmpty() && ((ItemStack)player.getInventory().armor.get(3)).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Color getEntityColor(Entity entity) {
        Color color;
        if (this.distance.get().booleanValue()) {
            color = this.friendOverride.get().booleanValue() && entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity)entity) ? (Color)Config.get().friendColor.get() : EntityUtils.getColorFromDistance(entity);
        } else if (entity instanceof PlayerEntity) {
            color = PlayerUtils.getPlayerColor((PlayerEntity)entity, this.playersColor.get());
        } else {
            color = switch (entity.getType().getSpawnGroup()) {
                case SpawnGroup.CREATURE -> this.animalsColor.get();
                case SpawnGroup.WATER_AMBIENT, SpawnGroup.WATER_CREATURE, SpawnGroup.UNDERGROUND_WATER_CREATURE, SpawnGroup.AXOLOTLS -> this.waterAnimalsColor.get();
                case SpawnGroup.MONSTER -> this.monstersColor.get();
                case SpawnGroup.AMBIENT -> this.ambientColor.get();
                default -> this.miscColor.get();
            };
        }
        return new Color(color);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.mc.options.hudHidden || this.style.get() == TracerStyle.Offscreen) {
            return;
        }
        this.count = 0;
        for (Entity entity : this.mc.world.getEntities()) {
            if (this.shouldBeIgnored(entity)) continue;
            Color color = this.getEntityColor(entity);
            double x = entity.prevX + (entity.getX() - entity.prevX) * (double)event.tickDelta;
            double y = entity.prevY + (entity.getY() - entity.prevY) * (double)event.tickDelta;
            double z = entity.prevZ + (entity.getZ() - entity.prevZ) * (double)event.tickDelta;
            double height = entity.getBoundingBox().maxY - entity.getBoundingBox().minY;
            if (this.target.get() == Target.Head) {
                y += height;
            } else if (this.target.get() == Target.Body) {
                y += height / 2.0;
            }
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, x, y, z, color);
            if (this.stem.get().booleanValue()) {
                event.renderer.line(x, entity.getY(), z, x, entity.getY() + height, z, color);
            }
            ++this.count;
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (this.mc.options.hudHidden || this.style.get() != TracerStyle.Offscreen) {
            return;
        }
        this.count = 0;
        Renderer2D.COLOR.begin();
        for (Entity entity : this.mc.world.getEntities()) {
            if (this.shouldBeIgnored(entity)) continue;
            Color color = this.getEntityColor(entity);
            if (this.blinkOffscreen.get().booleanValue()) {
                color.a = (int)((float)color.a * this.getAlpha());
            }
            Vec2f screenCenter = new Vec2f((float)this.mc.getWindow().getFramebufferWidth() / 2.0f, (float)this.mc.getWindow().getFramebufferHeight() / 2.0f);
            Vector3d projection = new Vector3d(entity.prevX, entity.prevY, entity.prevZ);
            boolean projSucceeded = NametagUtils.to2D(projection, 1.0, false, false);
            if (projSucceeded && projection.x > 0.0 && projection.x < (double)this.mc.getWindow().getFramebufferWidth() && projection.y > 0.0 && projection.y < (double)this.mc.getWindow().getFramebufferHeight()) continue;
            projection = new Vector3d(entity.prevX, entity.prevY, entity.prevZ);
            NametagUtils.to2D(projection, 1.0, false, true);
            Vector2f angle = this.vectorAngles(new Vector3d((double)screenCenter.x - projection.x, (double)screenCenter.y - projection.y, 0.0));
            angle.y += 180.0f;
            float angleYawRad = (float)Math.toRadians(angle.y);
            Vector2f newPoint = new Vector2f(screenCenter.x + (float)this.distanceOffscreen.get().intValue() * (float)Math.cos(angleYawRad), screenCenter.y + (float)this.distanceOffscreen.get().intValue() * (float)Math.sin(angleYawRad));
            Vector2f[] trianglePoints = new Vector2f[]{new Vector2f(newPoint.x - (float)this.sizeOffscreen.get().intValue(), newPoint.y - (float)this.sizeOffscreen.get().intValue()), new Vector2f(newPoint.x + (float)this.sizeOffscreen.get().intValue() * 0.73205f, newPoint.y), new Vector2f(newPoint.x - (float)this.sizeOffscreen.get().intValue(), newPoint.y + (float)this.sizeOffscreen.get().intValue())};
            this.rotateTriangle(trianglePoints, angle.y);
            Renderer2D.COLOR.triangle(trianglePoints[0].x, trianglePoints[0].y, trianglePoints[1].x, trianglePoints[1].y, trianglePoints[2].x, trianglePoints[2].y, color);
            ++this.count;
        }
        Renderer2D.COLOR.render(null);
    }

    private void rotateTriangle(Vector2f[] points, float ang) {
        Vector2f triangleCenter = new Vector2f(0.0f, 0.0f);
        triangleCenter.add((Vector2fc)points[0]).add((Vector2fc)points[1]).add((Vector2fc)points[2]).div(3.0f);
        float theta = (float)Math.toRadians(ang);
        float cos = (float)Math.cos(theta);
        float sin = (float)Math.sin(theta);
        for (int i = 0; i < 3; ++i) {
            Vector2f point = new Vector2f(points[i].x, points[i].y).sub((Vector2fc)triangleCenter);
            Vector2f newPoint = new Vector2f(point.x * cos - point.y * sin, point.x * sin + point.y * cos);
            newPoint.add((Vector2fc)triangleCenter);
            points[i] = newPoint;
        }
    }

    private Vector2f vectorAngles(Vector3d forward) {
        float pitch;
        float yaw;
        if (forward.x == 0.0 && forward.y == 0.0) {
            yaw = 0.0f;
            pitch = forward.z > 0.0 ? 270.0f : 90.0f;
        } else {
            float tmp;
            yaw = (float)(Math.atan2(forward.y, forward.x) * 180.0 / Math.PI);
            if (yaw < 0.0f) {
                yaw += 360.0f;
            }
            if ((pitch = (float)(Math.atan2(-forward.z, tmp = (float)Math.sqrt(forward.x * forward.x + forward.y * forward.y)) * 180.0 / Math.PI)) < 0.0f) {
                pitch += 360.0f;
            }
        }
        return new Vector2f(pitch, yaw);
    }

    private float getAlpha() {
        double speed = this.blinkOffscreenSpeed.get() / 4.0;
        double duration = (double)Math.abs(Duration.between(Instant.now(), this.initTimer).toMillis()) * speed;
        return (float)Math.abs(duration % 1000.0 - 500.0) / 500.0f;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.count);
    }

    public static enum TracerStyle {
        Lines,
        Offscreen;

    }
}

