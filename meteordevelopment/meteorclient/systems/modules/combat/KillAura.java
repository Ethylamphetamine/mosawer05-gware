/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.Tameable
 *  net.minecraft.entity.mob.EndermanEntity
 *  net.minecraft.entity.mob.ZombifiedPiglinEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.passive.WolfEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.MaceItem
 *  net.minecraft.item.SwordItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.GameMode
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
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
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;

public class KillAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTargeting;
    private final SettingGroup sgTiming;
    private final Setting<Weapon> weapon;
    private final Setting<RotationMode> rotation;
    private final Setting<Boolean> autoSwitch;
    private final Setting<Boolean> onlyOnClick;
    private final Setting<Boolean> onlyOnLook;
    private final Setting<Boolean> pauseOnCombat;
    private final Setting<ShieldMode> shieldMode;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<SortPriority> priority;
    private final Setting<Integer> maxTargets;
    private final Setting<Double> range;
    private final Setting<Double> wallsRange;
    private final Setting<EntityAge> mobAgeFilter;
    private final Setting<Boolean> ignoreNamed;
    private final Setting<Boolean> ignorePassive;
    private final Setting<Boolean> ignoreTamed;
    private final Setting<Boolean> pauseOnLag;
    private final Setting<Boolean> pauseOnUse;
    private final Setting<Boolean> tpsSync;
    private final Setting<Boolean> customDelay;
    private final Setting<Integer> hitDelay;
    private final Setting<Integer> switchDelay;
    private final List<Entity> targets;
    private int switchTimer;
    private int hitTimer;
    private boolean wasPathing;
    public boolean attacking;

    public KillAura() {
        super(Categories.Combat, "kill-aura", "Attacks specified entities around you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTargeting = this.settings.createGroup("Targeting");
        this.sgTiming = this.settings.createGroup("Timing");
        this.weapon = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("weapon")).description("Only attacks an entity when a specified weapon is in your hand.")).defaultValue(Weapon.All)).build());
        this.rotation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("rotate")).description("Determines when you should rotate towards the target.")).defaultValue(RotationMode.Always)).build());
        this.autoSwitch = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-switch")).description("Switches to your selected weapon when attacking the target.")).defaultValue(false)).build());
        this.onlyOnClick = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-click")).description("Only attacks when holding left click.")).defaultValue(false)).build());
        this.onlyOnLook = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-look")).description("Only attacks when looking at an entity.")).defaultValue(false)).build());
        this.pauseOnCombat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-baritone")).description("Freezes Baritone temporarily until you are finished attacking the entity.")).defaultValue(true)).build());
        this.shieldMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shield-mode")).description("Will try and use an axe to break target shields.")).defaultValue(ShieldMode.Break)).visible(() -> this.autoSwitch.get() != false && this.weapon.get() != Weapon.Axe)).build());
        this.entities = this.sgTargeting.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to attack.")).onlyAttackable().defaultValue(EntityType.PLAYER).build());
        this.priority = this.sgTargeting.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("priority")).description("How to filter targets within range.")).defaultValue(SortPriority.ClosestAngle)).build());
        this.maxTargets = this.sgTargeting.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("max-targets")).description("How many entities to target at once.")).defaultValue(1)).min(1).sliderRange(1, 5).visible(() -> this.onlyOnLook.get() == false)).build());
        this.range = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The maximum range the entity can be to attack it.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.wallsRange = this.sgTargeting.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("The maximum range the entity can be attacked through walls.")).defaultValue(3.5).min(0.0).sliderMax(6.0).build());
        this.mobAgeFilter = this.sgTargeting.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mob-age-filter")).description("Determines the age of the mobs to target (baby, adult, or both).")).defaultValue(EntityAge.Adult)).build());
        this.ignoreNamed = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-named")).description("Whether or not to attack mobs with a name.")).defaultValue(false)).build());
        this.ignorePassive = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-passive")).description("Will only attack sometimes passive mobs if they are targeting you.")).defaultValue(true)).build());
        this.ignoreTamed = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-tamed")).description("Will avoid attacking mobs you tamed.")).defaultValue(false)).build());
        this.pauseOnLag = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-lag")).description("Pauses if the server is lagging.")).defaultValue(true)).build());
        this.pauseOnUse = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-use")).description("Does not attack while using an item.")).defaultValue(false)).build());
        this.tpsSync = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("TPS-sync")).description("Tries to sync attack delay with the server's TPS.")).defaultValue(true)).build());
        this.customDelay = this.sgTiming.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-delay")).description("Use a custom delay instead of the vanilla cooldown.")).defaultValue(false)).build());
        this.hitDelay = this.sgTiming.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hit-delay")).description("How fast you hit the entity in ticks.")).defaultValue(11)).min(0).sliderMax(60).visible(this.customDelay::get)).build());
        this.switchDelay = this.sgTiming.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("switch-delay")).description("How many ticks to wait before hitting an entity after switching hotbar slots.")).defaultValue(0)).min(0).sliderMax(10).build());
        this.targets = new ArrayList<Entity>();
        this.wasPathing = false;
    }

    @Override
    public void onDeactivate() {
        this.targets.clear();
        this.attacking = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (this.pauseOnUse.get().booleanValue() && (this.mc.interactionManager.isBreakingBlock() || this.mc.player.isUsingItem())) {
            return;
        }
        if (this.onlyOnClick.get().booleanValue() && !this.mc.options.attackKey.isPressed()) {
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1.0f && this.pauseOnLag.get().booleanValue()) {
            return;
        }
        if (this.onlyOnLook.get().booleanValue()) {
            Entity targeted = this.mc.targetedEntity;
            if (targeted == null) {
                return;
            }
            if (!this.entityCheck(targeted)) {
                return;
            }
            this.targets.clear();
            this.targets.add(this.mc.targetedEntity);
        } else {
            this.targets.clear();
            TargetUtils.getList(this.targets, this::entityCheck, this.priority.get(), this.maxTargets.get());
        }
        if (this.targets.isEmpty()) {
            this.attacking = false;
            if (this.wasPathing) {
                PathManagers.get().resume();
                this.wasPathing = false;
            }
            return;
        }
        Entity primary = this.targets.getFirst();
        if (this.autoSwitch.get().booleanValue()) {
            FindItemResult axeResult;
            Predicate<ItemStack> predicate = switch (this.weapon.get().ordinal()) {
                case 1 -> stack -> stack.getItem() instanceof AxeItem;
                case 0 -> stack -> stack.getItem() instanceof SwordItem;
                case 2 -> stack -> stack.getItem() instanceof MaceItem;
                case 3 -> stack -> stack.getItem() instanceof TridentItem;
                case 4 -> stack -> stack.getItem() instanceof AxeItem || stack.getItem() instanceof SwordItem || stack.getItem() instanceof MaceItem || stack.getItem() instanceof TridentItem;
                default -> o -> true;
            };
            FindItemResult weaponResult = InvUtils.findInHotbar(predicate);
            if (this.shouldShieldBreak() && (axeResult = InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof AxeItem)).found()) {
                weaponResult = axeResult;
            }
            InvUtils.swap(weaponResult.slot(), false);
        }
        if (!this.itemInHand()) {
            return;
        }
        this.attacking = true;
        if (this.rotation.get() == RotationMode.Always) {
            Rotations.rotate(Rotations.getYaw(primary), Rotations.getPitch(primary, Target.Body));
        }
        if (this.pauseOnCombat.get().booleanValue() && PathManagers.get().isPathing() && !this.wasPathing) {
            PathManagers.get().pause();
            this.wasPathing = true;
        }
        if (this.delayCheck()) {
            this.targets.forEach(this::attack);
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            this.switchTimer = this.switchDelay.get();
        }
    }

    private boolean shouldShieldBreak() {
        for (Entity target : this.targets) {
            PlayerEntity player;
            if (!(target instanceof PlayerEntity) || !(player = (PlayerEntity)target).blockedByShield(this.mc.world.getDamageSources().playerAttack((PlayerEntity)this.mc.player)) || this.shieldMode.get() != ShieldMode.Break) continue;
            return true;
        }
        return false;
    }

    private boolean entityCheck(Entity entity) {
        Tameable tameable;
        LivingEntity livingEntity;
        if (entity.equals((Object)this.mc.player) || entity.equals((Object)this.mc.cameraEntity)) {
            return false;
        }
        if (entity instanceof LivingEntity && (livingEntity = (LivingEntity)entity).isDead() || !entity.isAlive()) {
            return false;
        }
        Box hitbox = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(MathHelper.clamp((double)this.mc.player.getX(), (double)hitbox.minX, (double)hitbox.maxX), MathHelper.clamp((double)this.mc.player.getY(), (double)hitbox.minY, (double)hitbox.maxY), MathHelper.clamp((double)this.mc.player.getZ(), (double)hitbox.minZ, (double)hitbox.maxZ), this.range.get())) {
            return false;
        }
        if (!this.entities.get().contains(entity.getType())) {
            return false;
        }
        if (this.ignoreNamed.get().booleanValue() && entity.hasCustomName()) {
            return false;
        }
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, (double)this.wallsRange.get())) {
            return false;
        }
        if (this.ignoreTamed.get().booleanValue() && entity instanceof Tameable && (tameable = (Tameable)entity).getOwnerUuid() != null && tameable.getOwnerUuid().equals(this.mc.player.getUuid())) {
            return false;
        }
        if (this.ignorePassive.get().booleanValue()) {
            WolfEntity wolf;
            ZombifiedPiglinEntity piglin;
            EndermanEntity enderman;
            if (entity instanceof EndermanEntity && !(enderman = (EndermanEntity)entity).isAngry()) {
                return false;
            }
            if (entity instanceof ZombifiedPiglinEntity && !(piglin = (ZombifiedPiglinEntity)entity).isAttacking()) {
                return false;
            }
            if (entity instanceof WolfEntity && !(wolf = (WolfEntity)entity).isAttacking()) {
                return false;
            }
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (player.isCreative()) {
                return false;
            }
            if (!Friends.get().shouldAttack(player)) {
                return false;
            }
            if (this.shieldMode.get() == ShieldMode.Ignore && player.blockedByShield(this.mc.world.getDamageSources().playerAttack((PlayerEntity)this.mc.player))) {
                return false;
            }
        }
        if (entity instanceof AnimalEntity) {
            AnimalEntity animal = (AnimalEntity)entity;
            return switch (this.mobAgeFilter.get().ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> animal.isBaby();
                case 1 -> {
                    if (!animal.isBaby()) {
                        yield true;
                    }
                    yield false;
                }
                case 2 -> true;
            };
        }
        return true;
    }

    private boolean delayCheck() {
        float delay;
        if (this.switchTimer > 0) {
            --this.switchTimer;
            return false;
        }
        float f = delay = this.customDelay.get() != false ? (float)this.hitDelay.get().intValue() : 0.5f;
        if (this.tpsSync.get().booleanValue()) {
            delay /= TickRate.INSTANCE.getTickRate() / 20.0f;
        }
        if (this.customDelay.get().booleanValue()) {
            if ((float)this.hitTimer < delay) {
                ++this.hitTimer;
                return false;
            }
            return true;
        }
        return this.mc.player.getAttackCooldownProgress(delay) >= 1.0f;
    }

    private void attack(Entity target) {
        if (this.rotation.get() == RotationMode.OnHit) {
            Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, Target.Body));
        }
        this.mc.interactionManager.attackEntity((PlayerEntity)this.mc.player, target);
        this.mc.player.swingHand(Hand.MAIN_HAND);
        this.hitTimer = 0;
    }

    private boolean itemInHand() {
        if (this.shouldShieldBreak()) {
            return this.mc.player.getMainHandStack().getItem() instanceof AxeItem;
        }
        return switch (this.weapon.get().ordinal()) {
            case 1 -> this.mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case 0 -> this.mc.player.getMainHandStack().getItem() instanceof SwordItem;
            case 2 -> this.mc.player.getMainHandStack().getItem() instanceof MaceItem;
            case 3 -> this.mc.player.getMainHandStack().getItem() instanceof TridentItem;
            case 4 -> {
                if (this.mc.player.getMainHandStack().getItem() instanceof AxeItem || this.mc.player.getMainHandStack().getItem() instanceof SwordItem || this.mc.player.getMainHandStack().getItem() instanceof MaceItem || this.mc.player.getMainHandStack().getItem() instanceof TridentItem) {
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    public Entity getTarget() {
        if (!this.targets.isEmpty()) {
            return this.targets.getFirst();
        }
        return null;
    }

    @Override
    public String getInfoString() {
        if (!this.targets.isEmpty()) {
            return EntityUtils.getName(this.getTarget());
        }
        return null;
    }

    public static enum Weapon {
        Sword,
        Axe,
        Mace,
        Trident,
        All,
        Any;

    }

    public static enum RotationMode {
        Always,
        OnHit,
        None;

    }

    public static enum ShieldMode {
        Ignore,
        Break,
        None;

    }

    public static enum EntityAge {
        Baby,
        Adult,
        Both;

    }
}

