/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.AttributeModifiersComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.util.Hand
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Vec3d
 *  org.apache.commons.lang3.mutable.MutableDouble
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;

public class MaceAura
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> snapRotation;
    private final Setting<Boolean> silentSwapOverrideDelay;
    private final Setting<Boolean> chestSwapOnApproach;
    private final Setting<Double> swapRange;
    private final Setting<Boolean> breachSwap;
    private final Setting<Boolean> flipFlop;
    private static final RegistryKey<Enchantment> BREACH_KEY = RegistryKey.of((RegistryKey)RegistryKeys.ENCHANTMENT, (Identifier)Identifier.of((String)"minecraft", (String)"breach"));
    private long lastSwordAttackTime;
    private int flipFlopCount;
    private boolean useDensityNext;
    private SwapState swapState;
    private final TargetManager targetManager;
    private final SettingGroup sgGrim;
    private final Setting<Boolean> grimSafe;
    private final Setting<Boolean> enablePrediction;
    private final Setting<Boolean> adjustForFlying;
    private final Setting<Double> maxRotStep;
    private final Setting<Double> predictionMs;
    private final Setting<Double> elytraPredictionScale;
    private final Setting<Double> flyingRangeBonus;
    private final Setting<Double> aimYOffset;

    public MaceAura() {
        super(Categories.Combat, "mace-aura", "Automatically attacks targets with a mace using vanilla delays.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("Attack range.")).defaultValue(3.0).min(1.0).sliderMax(6.0).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotate to face the target before attacking.")).defaultValue(true)).build());
        this.snapRotation = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("snap-rotate")).description("Instantly rotate to target when in range.")).defaultValue(true)).visible(this.rotate::get)).build());
        this.silentSwapOverrideDelay = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silent-swap-override-delay")).description("Use held-item delay when silent swapping to mace.")).defaultValue(true)).build());
        this.chestSwapOnApproach = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chest-swap-on-approach")).description("If wearing elytra, swap to chestplate when a target is within range (not hit range).")).defaultValue(true)).build());
        this.swapRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("swap-range")).description("Range to trigger chestplate swap when wearing elytra.")).defaultValue(6.0).min(1.0).sliderMax(12.0).visible(this.chestSwapOnApproach::get)).build());
        this.breachSwap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("breach-swap")).description("Hit with a breach mace, swap to sword and hit, then swap back and hit again.")).defaultValue(false)).build());
        this.flipFlop = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("flip-flop")).description("Alternate between breach and density maces after each successful hit.")).defaultValue(false)).build());
        this.lastSwordAttackTime = 0L;
        this.flipFlopCount = 0;
        this.useDensityNext = false;
        this.swapState = SwapState.NONE;
        this.targetManager = new TargetManager(this, true);
        this.sgGrim = this.settings.createGroup("2b2t / Grim");
        this.grimSafe = this.sgGrim.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-safe")).description("Clamp rotation and avoid sketchy behavior for Grim v3.")).defaultValue(true)).build());
        this.enablePrediction = this.sgGrim.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enable-prediction")).description("Predict target movement when aiming.")).defaultValue(true)).build());
        this.adjustForFlying = this.sgGrim.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("adjust-for-flying")).description("Adjust range and prediction for flying targets.")).defaultValue(true)).build());
        this.maxRotStep = this.sgGrim.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-rot-step")).description("Max degrees per tick to rotate when grim-safe.")).defaultValue(35.0).min(5.0).sliderMax(90.0).visible(this.grimSafe::get)).build());
        this.predictionMs = this.sgGrim.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("prediction-ms")).description("Lead prediction in milliseconds.")).defaultValue(120.0).min(0.0).sliderMax(300.0).visible(this.enablePrediction::get)).build());
        this.elytraPredictionScale = this.sgGrim.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("elytra-predict-scale")).description("Extra prediction scale when target is flying.")).defaultValue(1.4).min(1.0).sliderMax(2.5).visible(() -> this.enablePrediction.get() != false && this.adjustForFlying.get() != false)).build());
        this.flyingRangeBonus = this.sgGrim.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("flying-range-bonus")).description("Extra acquisition range when target is flying.")).defaultValue(0.3).min(0.0).sliderMax(1.0).visible(this.adjustForFlying::get)).build());
        this.aimYOffset = this.sgGrim.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("aim-y-offset")).description("Vertical aim offset for flying targets.")).defaultValue(-0.2).min(-1.0).sliderMax(1.0).visible(() -> this.enablePrediction.get() != false && this.adjustForFlying.get() != false)).build());
    }

    @Override
    public String getInfoString() {
        if (this.flipFlop.get().booleanValue() && this.flipFlopCount > 0) {
            return "FF:" + this.flipFlopCount;
        }
        return null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ChestSwap chestSwap;
        double swapDist;
        if (this.mc.player == null || this.mc.player.isDead() || this.mc.player.isSpectator()) {
            return;
        }
        FindItemResult weapon = this.findWeapon();
        if (!weapon.found()) {
            return;
        }
        List<Entity> targets = this.targetManager.getEntityTargets();
        if (targets.isEmpty()) {
            return;
        }
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        Vec3d eyes = this.mc.player.getEyePos();
        for (Entity e : targets) {
            double d;
            double acqRange = this.range.get();
            if (this.adjustForFlying.get().booleanValue() && this.isEntityFlying(e)) {
                acqRange += this.flyingRangeBonus.get().doubleValue();
            }
            if (!((d = MaceAura.closestPointOnBox(e.getBoundingBox(), eyes).distanceTo(eyes)) <= acqRange) || !(d < bestDist)) continue;
            best = e;
            bestDist = d;
        }
        if (best == null) {
            return;
        }
        if (this.chestSwapOnApproach.get().booleanValue() && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA) && (swapDist = MaceAura.closestPointOnBox(best.getBoundingBox(), eyes).distanceTo(eyes)) <= this.swapRange.get() && (chestSwap = Modules.get().get(ChestSwap.class)) != null) {
            PlayerUtils.silentSwapEquipChestplate();
        }
        if (this.rotate.get().booleanValue()) {
            Vec3d point;
            Vec3d vec3d = point = this.enablePrediction.get() != false ? this.predictedAimPoint(best, eyes) : MaceAura.closestPointOnBox(best.getBoundingBox(), eyes);
            if (this.grimSafe.get().booleanValue()) {
                float[] tgt = MeteorClient.ROTATION.getRotation(point);
                float curYaw = this.mc.player.getYaw();
                float curPitch = this.mc.player.getPitch();
                float nextYaw = this.clampAngle(curYaw, tgt[0], this.maxRotStep.get().floatValue());
                float nextPitch = this.clampAngle(curPitch, tgt[1], this.maxRotStep.get().floatValue());
                MeteorClient.ROTATION.requestRotation(nextYaw, nextPitch, 9.0);
            } else {
                if (this.snapRotation.get().booleanValue()) {
                    MeteorClient.ROTATION.snapAt(point);
                }
                MeteorClient.ROTATION.requestRotation(point, 9.0);
            }
            if (!MeteorClient.ROTATION.lookingAt(best.getBoundingBox())) {
                return;
            }
        }
        if (this.breachSwap.get().booleanValue()) {
            this.handleBreachSwap(best);
            return;
        }
        if (this.flipFlop.get().booleanValue()) {
            FindItemResult chosen;
            FindItemResult densityMace = this.findWeaponWithEnchant((RegistryKey<Enchantment>)Enchantments.DENSITY);
            FindItemResult breachMace = this.findWeaponWithEnchant(BREACH_KEY);
            FindItemResult findItemResult = chosen = this.useDensityNext ? densityMace : breachMace;
            if (!chosen.found()) {
                FindItemResult findItemResult2 = chosen = this.useDensityNext ? breachMace : densityMace;
            }
            if (!chosen.found()) {
                chosen = weapon;
            }
            int delayCheckSlot = chosen.slot();
            if (this.silentSwapOverrideDelay.get().booleanValue()) {
                delayCheckSlot = this.mc.player.getInventory().selectedSlot;
            }
            if (!this.delayReady(delayCheckSlot)) {
                return;
            }
            boolean isHolding = chosen.isMainHand();
            if (MeteorClient.SWAP.beginSwap(chosen, true)) {
                this.attack(best, !isHolding);
                MeteorClient.SWAP.endSwap(true);
                this.useDensityNext = !this.useDensityNext;
                ++this.flipFlopCount;
            }
            return;
        }
        int delayCheckSlot = weapon.slot();
        if (this.silentSwapOverrideDelay.get().booleanValue()) {
            delayCheckSlot = this.mc.player.getInventory().selectedSlot;
        }
        if (!this.delayReady(delayCheckSlot)) {
            return;
        }
        boolean isHolding = weapon.isMainHand();
        if (MeteorClient.SWAP.beginSwap(weapon, true)) {
            this.attack(best, !isHolding);
            MeteorClient.SWAP.endSwap(true);
        }
    }

    private boolean delayReady(int slotForCooldown) {
        return this.mc.player.getAttackCooldownProgress(0.0f) >= 1.0f;
    }

    private void attack(Entity target, boolean didSwap) {
        this.mc.interactionManager.attackEntity((PlayerEntity)this.mc.player, target);
        this.mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void handleBreachSwap(Entity target) {
        if (this.swapState == SwapState.NONE) {
            FindItemResult breachMace = this.findWeaponWithEnchant(BREACH_KEY);
            if (!breachMace.found()) {
                return;
            }
            int delayCheckSlot = breachMace.slot();
            if (this.silentSwapOverrideDelay.get().booleanValue()) {
                delayCheckSlot = this.mc.player.getInventory().selectedSlot;
            }
            if (!this.delayReady(delayCheckSlot)) {
                return;
            }
            boolean isHolding = breachMace.isMainHand();
            if (MeteorClient.SWAP.beginSwap(breachMace, true)) {
                this.attack(target, !isHolding);
                MeteorClient.SWAP.endSwap(true);
                this.swapState = SwapState.SWORD_PENDING;
            }
            return;
        }
        if (this.swapState == SwapState.SWORD_PENDING) {
            FindItemResult sword = this.findSword();
            if (!sword.found()) {
                this.swapState = SwapState.NONE;
                return;
            }
            if (!this.swordDelayReady(sword.slot())) {
                return;
            }
            boolean isHolding = sword.isMainHand();
            if (MeteorClient.SWAP.beginSwap(sword, true)) {
                this.mc.interactionManager.attackEntity((PlayerEntity)this.mc.player, target);
                this.mc.player.swingHand(Hand.MAIN_HAND);
                this.lastSwordAttackTime = System.currentTimeMillis();
                MeteorClient.SWAP.endSwap(true);
                this.swapState = SwapState.MACE2_PENDING;
            }
            return;
        }
        if (this.swapState == SwapState.MACE2_PENDING) {
            FindItemResult breachMace = this.findWeaponWithEnchant(BREACH_KEY);
            if (!breachMace.found()) {
                this.swapState = SwapState.NONE;
                return;
            }
            int delayCheckSlot = breachMace.slot();
            if (this.silentSwapOverrideDelay.get().booleanValue()) {
                delayCheckSlot = this.mc.player.getInventory().selectedSlot;
            }
            if (!this.delayReady(delayCheckSlot)) {
                return;
            }
            boolean isHolding = breachMace.isMainHand();
            if (MeteorClient.SWAP.beginSwap(breachMace, true)) {
                this.attack(target, !isHolding);
                MeteorClient.SWAP.endSwap(true);
                this.swapState = SwapState.NONE;
            }
        }
    }

    private boolean swordDelayReady(int slot) {
        ItemStack itemStack = this.mc.player.getInventory().getStack(slot);
        MutableDouble attackSpeed = new MutableDouble(this.mc.player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
        AttributeModifiersComponent attributeModifiers = (AttributeModifiersComponent)itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry.matches(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                    attackSpeed.add(modifier.comp_2449());
                }
            });
        }
        double attackCooldownTicks = 1.0 / attackSpeed.getValue() * 20.0;
        long currentTime = System.currentTimeMillis();
        return (double)(currentTime - this.lastSwordAttackTime) / 50.0 > attackCooldownTicks;
    }

    private FindItemResult findWeaponWithEnchant(RegistryKey<Enchantment> enchantKey) {
        for (int slot = 0; slot < this.mc.player.getInventory().size(); ++slot) {
            ItemStack stack = this.mc.player.getInventory().getStack(slot);
            if (!stack.isOf(Items.MACE) && !stack.isOf(Items.NETHERITE_AXE) || Utils.getEnchantmentLevel(stack, enchantKey) <= 0) continue;
            return new FindItemResult(slot, stack.getCount());
        }
        ItemStack off = this.mc.player.getOffHandStack();
        if ((off.isOf(Items.MACE) || off.isOf(Items.NETHERITE_AXE)) && Utils.getEnchantmentLevel(off, enchantKey) > 0) {
            return new FindItemResult(45, off.getCount());
        }
        return new FindItemResult(-1, 0);
    }

    private FindItemResult findWeapon() {
        FindItemResult res = MeteorClient.SWAP.getSlot(Items.MACE);
        if (res.found()) {
            return res;
        }
        res = MeteorClient.SWAP.getSlot(Items.NETHERITE_AXE);
        return res;
    }

    private FindItemResult findSword() {
        FindItemResult res = MeteorClient.SWAP.getSlot(Items.NETHERITE_SWORD);
        if (!res.found()) {
            res = MeteorClient.SWAP.getSlot(Items.DIAMOND_SWORD);
        }
        return res;
    }

    private static Vec3d closestPointOnBox(Box box, Vec3d point) {
        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3d(x, y, z);
    }

    private boolean isEntityFlying(Entity e) {
        return e.getVelocity().y < -0.1 || e.getVelocity().y > 0.1 || (double)e.fallDistance > 1.5;
    }

    private Vec3d predictedAimPoint(Entity target, Vec3d eyes) {
        Vec3d base = MaceAura.closestPointOnBox(target.getBoundingBox(), eyes);
        double ms = this.predictionMs.get();
        Vec3d vel = target.getVelocity();
        if (this.adjustForFlying.get().booleanValue() && this.isEntityFlying(target)) {
            vel = vel.multiply(this.elytraPredictionScale.get().doubleValue());
        }
        Vec3d lead = vel.multiply(ms / 1000.0);
        return base.add(lead.x, lead.y + this.aimYOffset.get(), lead.z);
    }

    private float clampAngle(float cur, float target, float maxStep) {
        float diff = this.wrapDegrees(target - cur);
        if (Math.abs(diff) <= maxStep) {
            return target;
        }
        return cur + Math.copySign(maxStep, diff);
    }

    private float wrapDegrees(float f) {
        if ((f %= 360.0f) >= 180.0f) {
            f -= 360.0f;
        }
        if (f < -180.0f) {
            f += 360.0f;
        }
        return f;
    }

    private static enum SwapState {
        NONE,
        SWORD_PENDING,
        MACE2_PENDING;

    }
}

