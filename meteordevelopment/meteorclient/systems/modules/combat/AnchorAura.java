/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  org.jetbrains.annotations.Nullable
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
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.player.Safety;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.Nullable;

public class AnchorAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgPause;
    private final SettingGroup sgRender;
    private final Setting<Double> targetRange;
    private final Setting<SortPriority> targetPriority;
    private final Setting<RotationMode> rotationMode;
    private final Setting<Double> maxDamage;
    private final Setting<Double> minHealth;
    private final Setting<Boolean> place;
    private final Setting<Integer> placeDelay;
    private final Setting<Safety> placeMode;
    private final Setting<Double> placeRange;
    private final Setting<PlaceMode> placePositions;
    private final Setting<Integer> breakDelay;
    private final Setting<Safety> breakMode;
    private final Setting<Double> breakRange;
    private final Setting<Boolean> pauseOnEat;
    private final Setting<Boolean> pauseOnDrink;
    private final Setting<Boolean> pauseOnMine;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<Boolean> renderPlace;
    private final Setting<SettingColor> placeSideColor;
    private final Setting<SettingColor> placeLineColor;
    private final Setting<Boolean> renderBreak;
    private final Setting<SettingColor> breakSideColor;
    private final Setting<SettingColor> breakLineColor;
    private int placeDelayLeft;
    private int breakDelayLeft;
    private PlayerEntity target;
    private final BlockPos.Mutable mutable;

    public AnchorAura() {
        super(Categories.Combat, "anchor-aura", "Automatically places and breaks Respawn Anchors to harm entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlace = this.settings.createGroup("Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgPause = this.settings.createGroup("Pause");
        this.sgRender = this.settings.createGroup("Render");
        this.targetRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("The radius in which players get targeted.")).defaultValue(4.0).min(0.0).sliderMax(5.0).build());
        this.targetPriority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("target-priority")).description("How to select the player to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.rotationMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("rotation-mode")).description("The mode to rotate you server-side.")).defaultValue(RotationMode.Both)).build());
        this.maxDamage = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-self-damage")).description("The maximum self-damage allowed.")).defaultValue(8.0).build());
        this.minHealth = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-health")).description("The minimum health you have to be for Anchor Aura to work.")).defaultValue(15.0).build());
        this.place = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place")).description("Allows Anchor Aura to place anchors.")).defaultValue(true)).build());
        this.placeDelay = this.sgPlace.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The tick delay between placing anchors.")).defaultValue(2)).range(0, 10).visible(this.place::get)).build());
        this.placeMode = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("place-mode")).description("The way anchors are allowed to be placed near you.")).defaultValue(Safety.Safe)).visible(this.place::get)).build());
        this.placeRange = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("The radius in which anchors are placed in.")).defaultValue(5.0).min(0.0).sliderMax(5.0).visible(this.place::get)).build());
        this.placePositions = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("placement-positions")).description("Where the Anchors will be placed on the entity.")).defaultValue(PlaceMode.AboveAndBelow)).visible(this.place::get)).build());
        this.breakDelay = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-delay")).description("The tick delay between breaking anchors.")).defaultValue(10)).range(0, 10).build());
        this.breakMode = this.sgBreak.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("break-mode")).description("The way anchors are allowed to be broken near you.")).defaultValue(Safety.Safe)).build());
        this.breakRange = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-range")).description("The radius in which anchors are broken in.")).defaultValue(5.0).min(0.0).sliderMax(5.0).build());
        this.pauseOnEat = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-eat")).description("Pauses while eating.")).defaultValue(false)).build());
        this.pauseOnDrink = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-drink")).description("Pauses while drinking potions.")).defaultValue(false)).build());
        this.pauseOnMine = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-mine")).description("Pauses while mining blocks.")).defaultValue(false)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.renderPlace = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-place")).description("Renders the block where it is placing an anchor.")).defaultValue(true)).build());
        this.placeSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("place-side-color")).description("The side color for positions to be placed.")).defaultValue(new SettingColor(255, 0, 0, 75)).visible(this.renderPlace::get)).build());
        this.placeLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("place-line-color")).description("The line color for positions to be placed.")).defaultValue(new SettingColor(255, 0, 0, 255)).visible(this.renderPlace::get)).build());
        this.renderBreak = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-break")).description("Renders the block where it is breaking an anchor.")).defaultValue(true)).build());
        this.breakSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("break-side-color")).description("The side color for anchors to be broken.")).defaultValue(new SettingColor(255, 0, 0, 75)).visible(this.renderBreak::get)).build());
        this.breakLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("break-line-color")).description("The line color for anchors to be broken.")).defaultValue(new SettingColor(255, 0, 0, 255)).visible(this.renderBreak::get)).build());
        this.mutable = new BlockPos.Mutable();
    }

    @Override
    public void onActivate() {
        this.placeDelayLeft = 0;
        this.breakDelayLeft = 0;
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        BlockPos placePos;
        BlockPos breakPos;
        if (this.mc.world.getDimension().comp_649()) {
            this.error("You are in the Nether... disabling.", new Object[0]);
            this.toggle();
            return;
        }
        if (PlayerUtils.shouldPause(this.pauseOnMine.get(), this.pauseOnEat.get(), this.pauseOnDrink.get())) {
            return;
        }
        if ((double)EntityUtils.getTotalHealth((LivingEntity)this.mc.player) <= this.minHealth.get()) {
            return;
        }
        if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
            this.target = TargetUtils.getPlayerTarget(this.targetRange.get(), this.targetPriority.get());
            if (TargetUtils.isBadTarget(this.target, this.targetRange.get())) {
                return;
            }
        }
        FindItemResult anchor = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
        FindItemResult glowStone = InvUtils.findInHotbar(Items.GLOWSTONE);
        if (!anchor.found() || !glowStone.found()) {
            return;
        }
        if (this.breakDelayLeft >= this.breakDelay.get() && (breakPos = this.findBreakPos(this.target.getBlockPos())) != null) {
            this.breakDelayLeft = 0;
            if (this.rotationMode.get() == RotationMode.Both || this.rotationMode.get() == RotationMode.Break) {
                BlockPos immutableBreakPos = breakPos.toImmutable();
                Rotations.rotate(Rotations.getYaw(breakPos), Rotations.getPitch(breakPos), 50, () -> this.breakAnchor(immutableBreakPos, anchor, glowStone));
            } else {
                this.breakAnchor(breakPos, anchor, glowStone);
            }
        }
        if (this.placeDelayLeft >= this.placeDelay.get() && this.place.get().booleanValue() && (placePos = this.findPlacePos(this.target.getBlockPos())) != null) {
            this.placeDelayLeft = 0;
            BlockUtils.place(placePos.toImmutable(), anchor, this.rotationMode.get() == RotationMode.Place || this.rotationMode.get() == RotationMode.Both, 50);
        }
        ++this.placeDelayLeft;
        ++this.breakDelayLeft;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.target == null) {
            return;
        }
        if (this.renderPlace.get().booleanValue()) {
            BlockPos placePos = this.findPlacePos(this.target.getBlockPos());
            if (placePos == null) {
                return;
            }
            event.renderer.box(placePos, (Color)this.placeSideColor.get(), (Color)this.placeLineColor.get(), this.shapeMode.get(), 0);
        }
        if (this.renderBreak.get().booleanValue()) {
            BlockPos breakPos = this.findBreakPos(this.target.getBlockPos());
            if (breakPos == null) {
                return;
            }
            event.renderer.box(breakPos, (Color)this.breakSideColor.get(), (Color)this.breakLineColor.get(), this.shapeMode.get(), 0);
        }
    }

    @Nullable
    private BlockPos findPlacePos(BlockPos targetPlacePos) {
        switch (this.placePositions.get().ordinal()) {
            case 3: {
                if (this.isValidPlace(targetPlacePos, 0, -1, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 0, 2, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 1, 0, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, -1, 0, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 0, 0, 1)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 0, 0, -1)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 1, 1, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, -1, -1, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 0, 1, 1)) {
                    return this.mutable;
                }
                if (!this.isValidPlace(targetPlacePos, 0, 0, -1)) break;
                return this.mutable;
            }
            case 0: {
                if (!this.isValidPlace(targetPlacePos, 0, 2, 0)) break;
                return this.mutable;
            }
            case 2: {
                if (this.isValidPlace(targetPlacePos, 0, -1, 0)) {
                    return this.mutable;
                }
                if (!this.isValidPlace(targetPlacePos, 0, 2, 0)) break;
                return this.mutable;
            }
            case 1: {
                if (this.isValidPlace(targetPlacePos, 0, 0, -1)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, 1, 0, 0)) {
                    return this.mutable;
                }
                if (this.isValidPlace(targetPlacePos, -1, 0, 0)) {
                    return this.mutable;
                }
                if (!this.isValidPlace(targetPlacePos, 0, 0, 1)) break;
                return this.mutable;
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findBreakPos(BlockPos targetPos) {
        if (this.isValidBreak(targetPos, 0, -1, 0)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 0, 2, 0)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 1, 0, 0)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, -1, 0, 0)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 0, 0, 1)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 0, 0, -1)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 1, 1, 0)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, -1, -1, 0)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 0, 1, 1)) {
            return this.mutable;
        }
        if (this.isValidBreak(targetPos, 0, 0, -1)) {
            return this.mutable;
        }
        return null;
    }

    private boolean getDamagePlace(BlockPos pos) {
        return this.placeMode.get() == Safety.Suicide || (double)DamageUtils.bedDamage((LivingEntity)this.mc.player, pos.toCenterPos()) <= this.maxDamage.get();
    }

    private boolean getDamageBreak(BlockPos pos) {
        return this.breakMode.get() == Safety.Suicide || (double)DamageUtils.anchorDamage((LivingEntity)this.mc.player, pos.toCenterPos()) <= this.maxDamage.get();
    }

    private boolean isValidPlace(BlockPos origin, int xOffset, int yOffset, int zOffset) {
        BlockUtils.mutateAround(this.mutable, origin, xOffset, yOffset, zOffset);
        return Math.sqrt(this.mc.player.getBlockPos().getSquaredDistance((Vec3i)this.mutable)) <= this.placeRange.get() && this.getDamagePlace((BlockPos)this.mutable) && BlockUtils.canPlace((BlockPos)this.mutable);
    }

    private boolean isValidBreak(BlockPos origin, int xOffset, int yOffset, int zOffset) {
        BlockUtils.mutateAround(this.mutable, origin, xOffset, yOffset, zOffset);
        return this.mc.world.getBlockState((BlockPos)this.mutable).getBlock() == Blocks.RESPAWN_ANCHOR && Math.sqrt(this.mc.player.getBlockPos().getSquaredDistance((Vec3i)this.mutable)) <= this.breakRange.get() && this.getDamageBreak((BlockPos)this.mutable);
    }

    private void breakAnchor(BlockPos pos, FindItemResult anchor, FindItemResult glowStone) {
        if (pos == null || this.mc.world.getBlockState(pos).getBlock() != Blocks.RESPAWN_ANCHOR) {
            return;
        }
        this.mc.player.setSneaking(false);
        if (glowStone.isOffhand()) {
            this.mc.interactionManager.interactBlock(this.mc.player, Hand.OFF_HAND, new BlockHitResult(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), Direction.UP, pos, true));
        } else {
            InvUtils.swap(glowStone.slot(), true);
            this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), Direction.UP, pos, true));
        }
        if (anchor.isOffhand()) {
            this.mc.interactionManager.interactBlock(this.mc.player, Hand.OFF_HAND, new BlockHitResult(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), Direction.UP, pos, true));
        } else {
            InvUtils.swap(anchor.slot(), true);
            this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), Direction.UP, pos, true));
        }
        InvUtils.swapBack();
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName((Entity)this.target);
    }

    public static enum RotationMode {
        Place,
        Break,
        Both,
        None;

    }

    public static enum PlaceMode {
        Above,
        Around,
        AboveAndBelow,
        All;

    }
}

