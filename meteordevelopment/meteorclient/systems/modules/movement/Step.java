/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.decoration.EndCrystalEntity
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import com.google.common.collect.Streams;
import java.util.OptionalDouble;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;

public class Step
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Double> height;
    private final Setting<ActiveWhen> activeWhen;
    private final Setting<Boolean> safeStep;
    private final Setting<Integer> stepHealth;
    private float prevStepHeight;
    private boolean prevPathManagerStep;

    public Step() {
        super(Categories.Movement, "step", "Allows you to walk up full blocks instantly.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.height = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("height")).description("Step height.")).defaultValue(1.0).min(0.0).build());
        this.activeWhen = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("active-when")).description("Step is active when you meet these requirements.")).defaultValue(ActiveWhen.Always)).build());
        this.safeStep = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("safe-step")).description("Doesn't let you step out of a hole if you are low on health or there is a crystal nearby.")).defaultValue(false)).build());
        this.stepHealth = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("step-health")).description("The health you stop being able to step at.")).defaultValue(5)).range(1, 36).sliderRange(1, 36).visible(this.safeStep::get)).build());
    }

    @Override
    public void onActivate() {
        this.prevStepHeight = this.mc.player.getStepHeight();
        this.prevPathManagerStep = PathManagers.get().getSettings().getStep().get();
        PathManagers.get().getSettings().getStep().set(true);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean work = this.activeWhen.get() == ActiveWhen.Always || this.activeWhen.get() == ActiveWhen.Sneaking && this.mc.player.isSneaking() || this.activeWhen.get() == ActiveWhen.NotSneaking && !this.mc.player.isSneaking();
        this.mc.player.setBoundingBox(this.mc.player.getBoundingBox().offset(0.0, 1.0, 0.0));
        if (work && (!this.safeStep.get().booleanValue() || this.getHealth() > (float)this.stepHealth.get().intValue() && (double)this.getHealth() - this.getExplosionDamage() > (double)this.stepHealth.get().intValue())) {
            this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue(this.height.get().doubleValue());
        } else {
            this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue((double)this.prevStepHeight);
        }
        this.mc.player.setBoundingBox(this.mc.player.getBoundingBox().offset(0.0, -1.0, 0.0));
    }

    @Override
    public void onDeactivate() {
        this.mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT).setBaseValue((double)this.prevStepHeight);
        PathManagers.get().getSettings().getStep().set(this.prevPathManagerStep);
    }

    private float getHealth() {
        return this.mc.player.getHealth() + this.mc.player.getAbsorptionAmount();
    }

    private double getExplosionDamage() {
        OptionalDouble crystalDamage = Streams.stream((Iterable)this.mc.world.getEntities()).filter(entity -> entity instanceof EndCrystalEntity).filter(Entity::isAlive).mapToDouble(entity -> DamageUtils.crystalDamage((LivingEntity)this.mc.player, entity.getPos())).max();
        return crystalDamage.orElse(0.0);
    }

    public static enum ActiveWhen {
        Always,
        Sneaking,
        NotSneaking;

    }
}

