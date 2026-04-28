/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.projectile.ProjectileEntity
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.EggItem
 *  net.minecraft.item.EnderPearlItem
 *  net.minecraft.item.ExperienceBottleItem
 *  net.minecraft.item.FishingRodItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.RangedWeaponItem
 *  net.minecraft.item.SnowballItem
 *  net.minecraft.item.ThrowablePotionItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.item.WindChargeItem
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.WindChargeItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Trajectories
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgRender;
    private final Setting<List<Item>> items;
    private final Setting<Boolean> otherPlayers;
    private final Setting<Boolean> firedProjectiles;
    private final Setting<Boolean> accurate;
    public final Setting<Integer> simulationSteps;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> renderPositionBox;
    private final Setting<Double> positionBoxSize;
    private final Setting<SettingColor> positionSideColor;
    private final Setting<SettingColor> positionLineColor;
    private final ProjectileEntitySimulator simulator;
    private final Pool<Vector3d> vec3s;
    private final List<Path> paths;
    private static final double MULTISHOT_OFFSET = Math.toRadians(10.0);

    public Trajectories() {
        super(Categories.Render, "trajectories", "Predicts the trajectory of throwable items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgRender = this.settings.createGroup("Render");
        this.items = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("items")).description("Items to display trajectories for.")).defaultValue(this.getDefaultItems())).filter(this::itemFilter).build());
        this.otherPlayers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("other-players")).description("Calculates trajectories for other players.")).defaultValue(true)).build());
        this.firedProjectiles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fired-projectiles")).description("Calculates trajectories for already fired projectiles.")).defaultValue(false)).build());
        this.accurate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("accurate")).description("Whether or not to calculate more accurate.")).defaultValue(false)).build());
        this.simulationSteps = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("simulation-steps")).description("How many steps to simulate projectiles. Zero for no limit")).defaultValue(500)).sliderMax(5000).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color.")).defaultValue(new SettingColor(255, 150, 0, 35)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color.")).defaultValue(new SettingColor(255, 150, 0)).build());
        this.renderPositionBox = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-position-boxes")).description("Renders the actual position the projectile will be at each tick along it's trajectory.")).defaultValue(false)).build());
        this.positionBoxSize = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("position-box-size")).description("The size of the box drawn at the simulated positions.")).defaultValue(0.02).sliderRange(0.01, 0.1).visible(this.renderPositionBox::get)).build());
        this.positionSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("position-side-color")).description("The side color.")).defaultValue(new SettingColor(255, 150, 0, 35)).visible(this.renderPositionBox::get)).build());
        this.positionLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("position-line-color")).description("The line color.")).defaultValue(new SettingColor(255, 150, 0)).visible(this.renderPositionBox::get)).build());
        this.simulator = new ProjectileEntitySimulator();
        this.vec3s = new Pool<Vector3d>(Vector3d::new);
        this.paths = new ArrayList<Path>();
    }

    private boolean itemFilter(Item item) {
        return item instanceof RangedWeaponItem || item instanceof FishingRodItem || item instanceof TridentItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem || item instanceof ExperienceBottleItem || item instanceof ThrowablePotionItem || item instanceof WindChargeItem;
    }

    private List<Item> getDefaultItems() {
        ArrayList<Item> items = new ArrayList<Item>();
        for (Item item : Registries.ITEM) {
            if (!this.itemFilter(item)) continue;
            items.add(item);
        }
        return items;
    }

    private Path getEmptyPath() {
        for (Path path : this.paths) {
            if (!path.points.isEmpty()) continue;
            return path;
        }
        Path path = new Path();
        this.paths.add(path);
        return path;
    }

    private void calculatePath(PlayerEntity player, float tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }
        ItemStack itemStack = player.getMainHandStack();
        if (!this.items.get().contains(itemStack.getItem())) {
            itemStack = player.getOffHandStack();
            if (!this.items.get().contains(itemStack.getItem())) {
                return;
            }
        }
        if (!this.simulator.set((Entity)player, itemStack, 0.0, this.accurate.get(), tickDelta)) {
            return;
        }
        this.getEmptyPath().calculate();
        if (itemStack.getItem() instanceof CrossbowItem && Utils.hasEnchantment(itemStack, (RegistryKey<Enchantment>)Enchantments.MULTISHOT)) {
            if (!this.simulator.set((Entity)player, itemStack, MULTISHOT_OFFSET, this.accurate.get(), tickDelta)) {
                return;
            }
            this.getEmptyPath().calculate();
            if (!this.simulator.set((Entity)player, itemStack, -MULTISHOT_OFFSET, this.accurate.get(), tickDelta)) {
                return;
            }
            this.getEmptyPath().calculate();
        }
    }

    private void calculateFiredPath(Entity entity, double tickDelta) {
        for (Path path : this.paths) {
            path.clear();
        }
        if (!this.simulator.set(entity, this.accurate.get())) {
            return;
        }
        this.getEmptyPath().setStart(entity, tickDelta).calculate();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        float tickDelta = this.mc.world.getTickManager().isFrozen() ? 1.0f : event.tickDelta;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            if (!this.otherPlayers.get().booleanValue() && player != this.mc.player) continue;
            this.calculatePath(player, tickDelta);
            for (Path path : this.paths) {
                path.render(event);
            }
        }
        if (this.firedProjectiles.get().booleanValue()) {
            for (Entity entity : this.mc.world.getEntities()) {
                if (!(entity instanceof ProjectileEntity)) continue;
                this.calculateFiredPath(entity, tickDelta);
                for (Path path : this.paths) {
                    path.render(event);
                }
            }
        }
    }

    private class Path {
        private final List<Vector3d> points = new ArrayList<Vector3d>();
        private boolean hitQuad;
        private boolean hitQuadHorizontal;
        private double hitQuadX1;
        private double hitQuadY1;
        private double hitQuadZ1;
        private double hitQuadX2;
        private double hitQuadY2;
        private double hitQuadZ2;
        private Entity collidingEntity;
        public Vector3d lastPoint;

        private Path() {
        }

        public void clear() {
            for (Vector3d point : this.points) {
                Trajectories.this.vec3s.free(point);
            }
            this.points.clear();
            this.hitQuad = false;
            this.collidingEntity = null;
            this.lastPoint = null;
        }

        public void calculate() {
            this.addPoint();
            for (int i = 0; i < (Trajectories.this.simulationSteps.get() > 0 ? Trajectories.this.simulationSteps.get() : Integer.MAX_VALUE); ++i) {
                HitResult result = Trajectories.this.simulator.tick();
                if (result != null) {
                    this.processHitResult(result);
                    break;
                }
                this.addPoint();
            }
        }

        public Path setStart(Entity entity, double tickDelta) {
            this.lastPoint = new Vector3d(MathHelper.lerp((double)tickDelta, (double)entity.lastRenderX, (double)entity.getX()), MathHelper.lerp((double)tickDelta, (double)entity.lastRenderY, (double)entity.getY()), MathHelper.lerp((double)tickDelta, (double)entity.lastRenderZ, (double)entity.getZ()));
            return this;
        }

        private void addPoint() {
            this.points.add(Trajectories.this.vec3s.get().set((Vector3dc)Trajectories.this.simulator.pos));
        }

        private void processHitResult(HitResult result) {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult r = (BlockHitResult)result;
                this.hitQuad = true;
                this.hitQuadX1 = r.getPos().x;
                this.hitQuadY1 = r.getPos().y;
                this.hitQuadZ1 = r.getPos().z;
                this.hitQuadX2 = r.getPos().x;
                this.hitQuadY2 = r.getPos().y;
                this.hitQuadZ2 = r.getPos().z;
                if (r.getSide() == Direction.UP || r.getSide() == Direction.DOWN) {
                    this.hitQuadHorizontal = true;
                    this.hitQuadX1 -= 0.25;
                    this.hitQuadZ1 -= 0.25;
                    this.hitQuadX2 += 0.25;
                    this.hitQuadZ2 += 0.25;
                } else if (r.getSide() == Direction.NORTH || r.getSide() == Direction.SOUTH) {
                    this.hitQuadHorizontal = false;
                    this.hitQuadX1 -= 0.25;
                    this.hitQuadY1 -= 0.25;
                    this.hitQuadX2 += 0.25;
                    this.hitQuadY2 += 0.25;
                } else {
                    this.hitQuadHorizontal = false;
                    this.hitQuadZ1 -= 0.25;
                    this.hitQuadY1 -= 0.25;
                    this.hitQuadZ2 += 0.25;
                    this.hitQuadY2 += 0.25;
                }
                this.points.add(Utils.set(Trajectories.this.vec3s.get(), result.getPos()));
            } else if (result.getType() == HitResult.Type.ENTITY) {
                this.collidingEntity = ((EntityHitResult)result).getEntity();
                this.points.add(Utils.set(Trajectories.this.vec3s.get(), result.getPos()).add(0.0, (double)(this.collidingEntity.getHeight() / 2.0f), 0.0));
            }
        }

        public void render(Render3DEvent event) {
            for (Vector3d point : this.points) {
                if (this.lastPoint != null) {
                    event.renderer.line(this.lastPoint.x, this.lastPoint.y, this.lastPoint.z, point.x, point.y, point.z, Trajectories.this.lineColor.get());
                    if (Trajectories.this.renderPositionBox.get().booleanValue()) {
                        event.renderer.box(point.x - Trajectories.this.positionBoxSize.get(), point.y - Trajectories.this.positionBoxSize.get(), point.z - Trajectories.this.positionBoxSize.get(), point.x + Trajectories.this.positionBoxSize.get(), point.y + Trajectories.this.positionBoxSize.get(), point.z + Trajectories.this.positionBoxSize.get(), Trajectories.this.positionSideColor.get(), Trajectories.this.positionLineColor.get(), Trajectories.this.shapeMode.get(), 0);
                    }
                }
                this.lastPoint = point;
            }
            if (this.hitQuad) {
                if (this.hitQuadHorizontal) {
                    event.renderer.sideHorizontal(this.hitQuadX1, this.hitQuadY1, this.hitQuadZ1, this.hitQuadX1 + 0.5, this.hitQuadZ1 + 0.5, Trajectories.this.sideColor.get(), Trajectories.this.lineColor.get(), Trajectories.this.shapeMode.get());
                } else {
                    event.renderer.sideVertical(this.hitQuadX1, this.hitQuadY1, this.hitQuadZ1, this.hitQuadX2, this.hitQuadY2, this.hitQuadZ2, Trajectories.this.sideColor.get(), Trajectories.this.lineColor.get(), Trajectories.this.shapeMode.get());
                }
            }
            if (this.collidingEntity != null) {
                double x = (this.collidingEntity.getX() - this.collidingEntity.prevX) * (double)event.tickDelta;
                double y = (this.collidingEntity.getY() - this.collidingEntity.prevY) * (double)event.tickDelta;
                double z = (this.collidingEntity.getZ() - this.collidingEntity.prevZ) * (double)event.tickDelta;
                Box box = this.collidingEntity.getBoundingBox();
                event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, Trajectories.this.sideColor.get(), Trajectories.this.lineColor.get(), Trajectories.this.shapeMode.get(), 0);
            }
        }
    }
}

