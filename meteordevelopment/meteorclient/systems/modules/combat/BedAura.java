/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BedBlock
 *  net.minecraft.block.entity.BedBlockEntity
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BedItem
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BedAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTargeting;
    private final SettingGroup sgAutoMove;
    private final SettingGroup sgPause;
    private final SettingGroup sgRender;
    private final Setting<Integer> delay;
    private final Setting<Boolean> strictDirection;
    private final Setting<Double> targetRange;
    private final Setting<SortPriority> priority;
    private final Setting<Double> minDamage;
    private final Setting<Double> maxSelfDamage;
    private final Setting<Boolean> antiSuicide;
    private final Setting<Boolean> autoMove;
    private final Setting<Integer> autoMoveSlot;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Boolean> pauseOnDrink;
    private final Setting<Boolean> pauseOnMine;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private CardinalDirection direction;
    private PlayerEntity target;
    private BlockPos placePos;
    private BlockPos breakPos;
    private int timer;

    public BedAura() {
        super(Categories.Combat, "bed-aura", "Automatically places and explodes beds in the Nether and End.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTargeting = this.settings.createGroup("Targeting");
        this.sgAutoMove = this.settings.createGroup("Inventory");
        this.sgPause = this.settings.createGroup("Pause");
        this.sgRender = this.settings.createGroup("Render");
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The delay between placing beds in ticks.")).defaultValue(9)).min(0).sliderMax(20).build());
        this.strictDirection = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("strict-direction")).description("Only places beds in the direction you are facing.")).defaultValue(false)).build());
        this.targetRange = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("The range at which players can be targeted.")).defaultValue(4.0).min(0.0).sliderMax(5.0).build());
        this.priority = this.sgTargeting.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to filter targets within range.")).defaultValue(SortPriority.LowestHealth)).build());
        this.minDamage = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-damage")).description("The minimum damage to inflict on your target.")).defaultValue(7.0).range(0.0, 36.0).sliderMax(36.0).build());
        this.maxSelfDamage = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-self-damage")).description("The maximum damage to inflict on yourself.")).defaultValue(7.0).range(0.0, 36.0).sliderMax(36.0).build());
        this.antiSuicide = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-suicide")).description("Will not place and break beds if they will kill you.")).defaultValue(true)).build());
        this.autoMove = this.sgAutoMove.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-move")).description("Moves beds into a selected hotbar slot.")).defaultValue(false)).build());
        this.autoMoveSlot = this.sgAutoMove.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("auto-move-slot")).description("The slot auto move moves beds to.")).defaultValue(9)).range(1, 9).sliderRange(1, 9).visible(this.autoMove::get)).build());
        this.autoSwitch = this.sgAutoMove.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-switch")).description("Switches to and from beds automatically.")).defaultValue(true)).build());
        this.pauseOnEat = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-eat")).description("Pauses while eating.")).defaultValue(true)).build());
        this.pauseOnDrink = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-drink")).description("Pauses while drinking.")).defaultValue(true)).build());
        this.pauseOnMine = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-mine")).description("Pauses while mining.")).defaultValue(true)).build());
        this.swing = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Whether to swing hand client-side.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Renders the block where it is placing a bed.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color for positions to be placed.")).defaultValue(new SettingColor(15, 255, 211, 75)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color for positions to be placed.")).defaultValue(new SettingColor(15, 255, 211)).build());
    }

    @Override
    public void onActivate() {
        this.timer = this.delay.get();
        this.direction = CardinalDirection.North;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        FindItemResult bed;
        if (this.mc.world.getDimension().comp_648()) {
            this.error("You can't blow up beds in this dimension, disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (PlayerUtils.shouldPause(this.pauseOnMine.get(), this.pauseOnEat.get(), this.pauseOnDrink.get())) {
            return;
        }
        this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), this.priority.get());
        if (this.target == null) {
            this.placePos = null;
            this.breakPos = null;
            return;
        }
        if (this.autoMove.get().booleanValue() && (bed = InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem)).found() && bed.slot() != this.autoMoveSlot.get() - 1) {
            InvUtils.move().from(bed.slot()).toHotbar(this.autoMoveSlot.get() - 1);
        }
        if (this.breakPos == null) {
            this.placePos = this.findPlace(this.target);
        }
        this.timer = this.timer <= 0 && this.placeBed(this.placePos) ? this.delay.get() : --this.timer;
        if (this.breakPos == null) {
            this.breakPos = this.findBreak();
        }
        this.breakBed(this.breakPos);
    }

    private BlockPos findPlace(PlayerEntity target) {
        if (!InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).found()) {
            return null;
        }
        for (int index = 0; index < 3; ++index) {
            int i = index == 0 ? 1 : (index == 1 ? 0 : 2);
            for (CardinalDirection dir : CardinalDirection.values()) {
                if (this.strictDirection.get().booleanValue() && dir.toDirection() != this.mc.player.getHorizontalFacing() && dir.toDirection().getOpposite() != this.mc.player.getHorizontalFacing()) continue;
                BlockPos centerPos = target.getBlockPos().up(i);
                float headSelfDamage = DamageUtils.bedDamage((LivingEntity)this.mc.player, Utils.vec3d(centerPos));
                float offsetSelfDamage = DamageUtils.bedDamage((LivingEntity)this.mc.player, Utils.vec3d(centerPos.offset(dir.toDirection())));
                if (!this.mc.world.getBlockState(centerPos).isReplaceable() || !BlockUtils.canPlace(centerPos.offset(dir.toDirection())) || !((double)DamageUtils.bedDamage((LivingEntity)target, Utils.vec3d(centerPos)) >= this.minDamage.get()) || !((double)offsetSelfDamage < this.maxSelfDamage.get()) || !((double)headSelfDamage < this.maxSelfDamage.get()) || this.antiSuicide.get().booleanValue() && !(PlayerUtils.getTotalHealth() - headSelfDamage > 0.0f) || this.antiSuicide.get().booleanValue() && !(PlayerUtils.getTotalHealth() - offsetSelfDamage > 0.0f)) continue;
                this.direction = dir;
                return centerPos.offset(this.direction.toDirection());
            }
        }
        return null;
    }

    private BlockPos findBreak() {
        for (BlockEntity blockEntity : Utils.blockEntities()) {
            BlockPos bedPos;
            Vec3d bedVec;
            if (!(blockEntity instanceof BedBlockEntity) || !PlayerUtils.isWithinReach(bedVec = Utils.vec3d(bedPos = blockEntity.getPos())) || !((double)DamageUtils.bedDamage((LivingEntity)this.target, bedVec) >= this.minDamage.get()) || !((double)DamageUtils.bedDamage((LivingEntity)this.mc.player, bedVec) < this.maxSelfDamage.get()) || this.antiSuicide.get().booleanValue() && !(PlayerUtils.getTotalHealth() - DamageUtils.bedDamage((LivingEntity)this.mc.player, bedVec) > 0.0f)) continue;
            return bedPos;
        }
        return null;
    }

    private boolean placeBed(BlockPos pos) {
        if (pos == null) {
            return false;
        }
        FindItemResult bed = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
        if (bed.getHand() == null && !this.autoSwitch.get().booleanValue()) {
            return false;
        }
        double yaw = switch (this.direction) {
            case CardinalDirection.East -> 90.0;
            case CardinalDirection.South -> 180.0;
            case CardinalDirection.West -> -90.0;
            default -> 0.0;
        };
        Rotations.rotate(yaw, Rotations.getPitch(pos), () -> {
            BlockUtils.place(pos, bed, false, 0, this.swing.get(), true);
            this.breakPos = pos;
        });
        return true;
    }

    private void breakBed(BlockPos pos) {
        if (pos == null) {
            return;
        }
        this.breakPos = null;
        if (!(this.mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) {
            return;
        }
        boolean wasSneaking = this.mc.player.isSneaking();
        if (wasSneaking) {
            this.mc.player.setSneaking(false);
        }
        this.mc.interactionManager.interactBlock(this.mc.player, Hand.OFF_HAND, new BlockHitResult(Vec3d.ofCenter((Vec3i)pos), Direction.UP, pos, false));
        this.mc.player.setSneaking(wasSneaking);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.render.get().booleanValue() && this.placePos != null && this.breakPos == null) {
            int x = this.placePos.getX();
            int y = this.placePos.getY();
            int z = this.placePos.getZ();
            switch (this.direction) {
                case North: {
                    event.renderer.box(x, y, z, x + 1, (double)y + 0.6, z + 2, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                }
                case South: {
                    event.renderer.box(x, y, z - 1, x + 1, (double)y + 0.6, z + 1, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                }
                case East: {
                    event.renderer.box(x - 1, y, z, x + 1, (double)y + 0.6, z + 1, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                    break;
                }
                case West: {
                    event.renderer.box(x, y, z, x + 2, (double)y + 0.6, z + 1, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0);
                }
            }
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }
}

