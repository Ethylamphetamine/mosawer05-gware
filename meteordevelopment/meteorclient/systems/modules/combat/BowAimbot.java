/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.passive.AnimalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ArrowItem
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class BowAimbot
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> range;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<SortPriority> priority;
    private final Setting<Boolean> babies;
    private final Setting<Boolean> nametagged;
    private final Setting<Boolean> pauseOnCombat;
    private boolean wasPathing;
    private Entity target;

    public BowAimbot() {
        super(Categories.Combat, "bow-aimbot", "Automatically aims your bow for you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The maximum range the entity can be to aim at it.")).defaultValue(20.0).range(0.0, 100.0).sliderMax(100.0).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to attack.")).onlyAttackable().build());
        this.priority = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("priority")).description("What type of entities to target.")).defaultValue(SortPriority.LowestHealth)).build());
        this.babies = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("babies")).description("Whether or not to attack baby variants of the entity.")).defaultValue(true)).build());
        this.nametagged = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("nametagged")).description("Whether or not to attack mobs with a name tag.")).defaultValue(false)).build());
        this.pauseOnCombat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-combat")).description("Freezes Baritone temporarily until you released the bow.")).defaultValue(false)).build());
    }

    @Override
    public void onDeactivate() {
        this.target = null;
        this.wasPathing = false;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!PlayerUtils.isAlive() || !this.itemInHand()) {
            return;
        }
        if (!this.mc.player.getAbilities().creativeMode && !InvUtils.find(itemStack -> itemStack.getItem() instanceof ArrowItem).found()) {
            return;
        }
        this.target = TargetUtils.get(entity -> {
            if (entity == this.mc.player || entity == this.mc.cameraEntity) {
                return false;
            }
            if (entity instanceof LivingEntity && ((LivingEntity)entity).isDead() || !entity.isAlive()) {
                return false;
            }
            if (!PlayerUtils.isWithin(entity, (double)this.range.get())) {
                return false;
            }
            if (!this.entities.get().contains(entity.getType())) {
                return false;
            }
            if (!this.nametagged.get().booleanValue() && entity.hasCustomName()) {
                return false;
            }
            if (!PlayerUtils.canSeeEntity(entity)) {
                return false;
            }
            if (entity instanceof PlayerEntity) {
                if (((PlayerEntity)entity).isCreative()) {
                    return false;
                }
                if (!Friends.get().shouldAttack((PlayerEntity)entity)) {
                    return false;
                }
            }
            return !(entity instanceof AnimalEntity) || this.babies.get() != false || !((AnimalEntity)entity).isBaby();
        }, this.priority.get());
        if (this.target == null) {
            if (this.wasPathing) {
                PathManagers.get().resume();
                this.wasPathing = false;
            }
            return;
        }
        if (this.mc.options.useKey.isPressed() && this.itemInHand()) {
            if (this.pauseOnCombat.get().booleanValue() && PathManagers.get().isPathing() && !this.wasPathing) {
                PathManagers.get().pause();
                this.wasPathing = true;
            }
            this.aim(event.tickDelta);
        }
    }

    private boolean itemInHand() {
        return InvUtils.testInMainHand(Items.BOW, Items.CROSSBOW);
    }

    private void aim(float tickDelta) {
        float velocity = BowItem.getPullProgress((int)this.mc.player.getItemUseTime());
        Vec3d pos = this.target.getLerpedPos(tickDelta);
        double relativeX = pos.x - this.mc.player.getX();
        double relativeY = pos.y + (double)(this.target.getHeight() / 2.0f) - this.mc.player.getEyeY();
        float velocitySq = velocity * velocity;
        float g = 0.006f;
        double relativeZ = pos.z - this.mc.player.getZ();
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float pitch = (float)(-Math.toDegrees(Math.atan(((double)velocitySq - Math.sqrt((double)(velocitySq * velocitySq) - (double)g * ((double)g * hDistanceSq + 2.0 * relativeY * (double)velocitySq))) / ((double)g * hDistance))));
        if (Float.isNaN(pitch)) {
            Rotations.rotate(Rotations.getYaw(this.target), Rotations.getPitch(this.target));
        } else {
            Rotations.rotate(Rotations.getYaw(new Vec3d(pos.x, pos.y, pos.z)), pitch);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(this.target);
    }
}

