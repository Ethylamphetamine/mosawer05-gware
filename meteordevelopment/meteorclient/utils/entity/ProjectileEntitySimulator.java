/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.ChargedProjectilesComponent
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.DragonFireballEntity
 *  net.minecraft.entity.projectile.FireballEntity
 *  net.minecraft.entity.projectile.ProjectileUtil
 *  net.minecraft.entity.projectile.TridentEntity
 *  net.minecraft.entity.projectile.WindChargeEntity
 *  net.minecraft.entity.projectile.WitherSkullEntity
 *  net.minecraft.entity.projectile.thrown.EggEntity
 *  net.minecraft.entity.projectile.thrown.EnderPearlEntity
 *  net.minecraft.entity.projectile.thrown.ExperienceBottleEntity
 *  net.minecraft.entity.projectile.thrown.PotionEntity
 *  net.minecraft.entity.projectile.thrown.SnowballEntity
 *  net.minecraft.fluid.FluidState
 *  net.minecraft.fluid.Fluids
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.EggItem
 *  net.minecraft.item.EnderPearlItem
 *  net.minecraft.item.ExperienceBottleItem
 *  net.minecraft.item.FishingRodItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.SnowballItem
 *  net.minecraft.item.ThrowablePotionItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.item.WindChargeItem
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.ChunkSectionPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  net.minecraft.world.World
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package meteordevelopment.meteorclient.utils.entity;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.CrossbowItemAccessor;
import meteordevelopment.meteorclient.mixin.ProjectileInGroundAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MissHitResult;
import meteordevelopment.meteorclient.utils.player.Rotations;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.WindChargeItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ProjectileEntitySimulator {
    private static final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private static final Vec3d pos3d = new Vec3d(0.0, 0.0, 0.0);
    private static final Vec3d prevPos3d = new Vec3d(0.0, 0.0, 0.0);
    public final Vector3d pos = new Vector3d();
    private final Vector3d velocity = new Vector3d();
    private Entity simulatingEntity;
    private double gravity;
    private double airDrag;
    private double waterDrag;
    private float height;
    private float width;

    public boolean set(Entity user, ItemStack itemStack, double simulated, boolean accurate, float tickDelta) {
        Item item;
        Item item2 = item = itemStack.getItem();
        Objects.requireNonNull(item2);
        Item item3 = item2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{BowItem.class, CrossbowItem.class, WindChargeItem.class, FishingRodItem.class, TridentItem.class, SnowballItem.class, EggItem.class, EnderPearlItem.class, ExperienceBottleItem.class, ThrowablePotionItem.class}, (Object)item3, n)) {
            case 0: {
                BowItem ignored = (BowItem)item3;
                double charge = BowItem.getPullProgress((int)MeteorClient.mc.player.getItemUseTime());
                if (charge <= 0.1) {
                    return false;
                }
                this.set(user, 0.0, charge * 3.0, simulated, 0.05, 0.6, accurate, tickDelta, EntityType.ARROW);
                break;
            }
            case 1: {
                CrossbowItem ignored = (CrossbowItem)item3;
                ChargedProjectilesComponent projectilesComponent = (ChargedProjectilesComponent)itemStack.get(DataComponentTypes.CHARGED_PROJECTILES);
                if (projectilesComponent == null) {
                    return false;
                }
                if (projectilesComponent.contains(Items.FIREWORK_ROCKET)) {
                    this.set(user, 0.0, CrossbowItemAccessor.getSpeed(projectilesComponent), simulated, 0.0, 0.6, accurate, tickDelta, EntityType.FIREWORK_ROCKET);
                    break;
                }
                this.set(user, 0.0, CrossbowItemAccessor.getSpeed(projectilesComponent), simulated, 0.05, 0.6, accurate, tickDelta, EntityType.ARROW);
                break;
            }
            case 2: {
                WindChargeItem ignored = (WindChargeItem)item3;
                this.set(user, 0.0, 1.5, simulated, 0.0, 1.0, accurate, tickDelta, EntityType.WIND_CHARGE);
                this.airDrag = 1.0;
                break;
            }
            case 3: {
                FishingRodItem ignored = (FishingRodItem)item3;
                this.setFishingBobber(user, tickDelta);
                break;
            }
            case 4: {
                TridentItem ignored = (TridentItem)item3;
                this.set(user, 0.0, 2.5, simulated, 0.05, 0.99, accurate, tickDelta, EntityType.TRIDENT);
                break;
            }
            case 5: {
                SnowballItem ignored = (SnowballItem)item3;
                this.set(user, 0.0, 1.5, simulated, 0.03, 0.8, accurate, tickDelta, EntityType.SNOWBALL);
                break;
            }
            case 6: {
                EggItem ignored = (EggItem)item3;
                this.set(user, 0.0, 1.5, simulated, 0.03, 0.8, accurate, tickDelta, EntityType.EGG);
                break;
            }
            case 7: {
                EnderPearlItem ignored = (EnderPearlItem)item3;
                this.set(user, 0.0, 1.5, simulated, 0.03, 0.8, accurate, tickDelta, EntityType.ENDER_PEARL);
                break;
            }
            case 8: {
                ExperienceBottleItem ignored = (ExperienceBottleItem)item3;
                this.set(user, -20.0, 0.7, simulated, 0.07, 0.8, accurate, tickDelta, EntityType.EXPERIENCE_BOTTLE);
                break;
            }
            case 9: {
                ThrowablePotionItem ignored = (ThrowablePotionItem)item3;
                this.set(user, -20.0, 0.5, simulated, 0.05, 0.8, accurate, tickDelta, EntityType.POTION);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    public void set(Entity user, double roll, double speed, double simulated, double gravity, double waterDrag, boolean accurate, float tickDelta, EntityType<?> type) {
        double z;
        double y;
        double x;
        double pitch;
        double yaw;
        Utils.set(this.pos, user, tickDelta).add(0.0, (double)user.getEyeHeight(user.getPose()), 0.0);
        if (user == MeteorClient.mc.player && Rotations.rotating) {
            yaw = Rotations.serverYaw;
            pitch = Rotations.serverPitch;
        } else {
            yaw = user.getYaw(tickDelta);
            pitch = user.getPitch(tickDelta);
        }
        if (simulated == 0.0) {
            x = -Math.sin(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
            y = -Math.sin((pitch + roll) * 0.017453292);
            z = Math.cos(yaw * 0.017453292) * Math.cos(pitch * 0.017453292);
        } else {
            Vec3d vec3d = user.getOppositeRotationVector(1.0f);
            Quaterniond quaternion = new Quaterniond().setAngleAxis(simulated, vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = user.getRotationVec(1.0f);
            Vector3d vector3f = new Vector3d(vec3d2.x, vec3d2.y, vec3d2.z);
            vector3f.rotate((Quaterniondc)quaternion);
            x = vector3f.x;
            y = vector3f.y;
            z = vector3f.z;
        }
        this.velocity.set(x, y, z).normalize().mul(speed);
        if (accurate) {
            Vec3d vel = user.getVelocity();
            this.velocity.add(vel.x, user.isOnGround() ? 0.0 : vel.y, vel.z);
        }
        this.simulatingEntity = user;
        this.gravity = gravity;
        this.airDrag = 0.99;
        this.waterDrag = waterDrag;
        this.width = type.getWidth();
        this.height = type.getHeight();
    }

    public boolean set(Entity entity, boolean accurate) {
        ProjectileInGroundAccessor ppe;
        if (entity instanceof ProjectileInGroundAccessor && (ppe = (ProjectileInGroundAccessor)entity).getInGround()) {
            return false;
        }
        if (entity instanceof ArrowEntity) {
            this.set(entity, 0.05, 0.6, accurate);
        } else if (entity instanceof TridentEntity) {
            this.set(entity, 0.05, 0.99, accurate);
        } else if (entity instanceof EnderPearlEntity || entity instanceof SnowballEntity || entity instanceof EggEntity) {
            this.set(entity, 0.03, 0.8, accurate);
        } else if (entity instanceof ExperienceBottleEntity) {
            this.set(entity, 0.07, 0.8, accurate);
        } else if (entity instanceof PotionEntity) {
            this.set(entity, 0.05, 0.8, accurate);
        } else if (entity instanceof WitherSkullEntity || entity instanceof FireballEntity || entity instanceof DragonFireballEntity || entity instanceof WindChargeEntity) {
            this.set(entity, 0.0, 1.0, accurate);
            this.airDrag = 1.0;
        } else {
            return false;
        }
        if (entity.hasNoGravity()) {
            this.gravity = 0.0;
        }
        return true;
    }

    public void set(Entity entity, double gravity, double waterDrag, boolean accurate) {
        this.pos.set(entity.getX(), entity.getY(), entity.getZ());
        double speed = entity.getVelocity().length();
        this.velocity.set(entity.getVelocity().x, entity.getVelocity().y, entity.getVelocity().z).normalize().mul(speed);
        if (accurate) {
            Vec3d vel = entity.getVelocity();
            this.velocity.add(vel.x, entity.isOnGround() ? 0.0 : vel.y, vel.z);
        }
        this.simulatingEntity = entity;
        this.gravity = gravity;
        this.airDrag = 0.99;
        this.waterDrag = waterDrag;
        this.width = entity.getWidth();
        this.height = entity.getHeight();
    }

    public void setFishingBobber(Entity user, float tickDelta) {
        double pitch;
        double yaw;
        if (user == MeteorClient.mc.player && Rotations.rotating) {
            yaw = Rotations.serverYaw;
            pitch = Rotations.serverPitch;
        } else {
            yaw = user.getYaw(tickDelta);
            pitch = user.getPitch(tickDelta);
        }
        double h = Math.cos(-yaw * 0.01745329238474369 - 3.1415927410125732);
        double i = Math.sin(-yaw * 0.01745329238474369 - 3.1415927410125732);
        double j = -Math.cos(-pitch * 0.01745329238474369);
        double k = Math.sin(-pitch * 0.01745329238474369);
        Utils.set(this.pos, user, tickDelta).sub(i * 0.3, 0.0, h * 0.3).add(0.0, (double)user.getEyeHeight(user.getPose()), 0.0);
        this.velocity.set(-i, MathHelper.clamp((double)(-(k / j)), (double)-5.0, (double)5.0), -h);
        double l = this.velocity.length();
        this.velocity.mul(0.6 / l + 0.5, 0.6 / l + 0.5, 0.6 / l + 0.5);
        this.simulatingEntity = user;
        this.gravity = 0.03;
        this.airDrag = 0.92;
        this.waterDrag = 0.0;
        this.width = EntityType.FISHING_BOBBER.getWidth();
        this.height = EntityType.FISHING_BOBBER.getHeight();
    }

    public HitResult tick() {
        ((IVec3d)prevPos3d).set(this.pos);
        this.pos.add((Vector3dc)this.velocity);
        this.velocity.mul(this.isTouchingWater() ? this.waterDrag : this.airDrag);
        this.velocity.sub(0.0, this.gravity, 0.0);
        if (this.pos.y < (double)MeteorClient.mc.world.getBottomY()) {
            return MissHitResult.INSTANCE;
        }
        int chunkX = ChunkSectionPos.getSectionCoord((double)this.pos.x);
        int chunkZ = ChunkSectionPos.getSectionCoord((double)this.pos.z);
        if (!MeteorClient.mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
            return MissHitResult.INSTANCE;
        }
        ((IVec3d)pos3d).set(this.pos);
        if (pos3d.equals((Object)prevPos3d)) {
            return MissHitResult.INSTANCE;
        }
        HitResult hitResult = this.getCollision();
        return hitResult.getType() == HitResult.Type.MISS ? null : hitResult;
    }

    private boolean isTouchingWater() {
        blockPos.set(this.pos.x, this.pos.y, this.pos.z);
        FluidState fluidState = MeteorClient.mc.world.getFluidState((BlockPos)blockPos);
        if (fluidState.getFluid() != Fluids.WATER && fluidState.getFluid() != Fluids.FLOWING_WATER) {
            return false;
        }
        return this.pos.y - (double)((int)this.pos.y) <= (double)fluidState.getHeight();
    }

    private HitResult getCollision() {
        Box box;
        EntityHitResult hitResult2;
        BlockHitResult hitResult = MeteorClient.mc.world.raycast(new RaycastContext(prevPos3d, pos3d, RaycastContext.ShapeType.COLLIDER, this.waterDrag == 0.0 ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, this.simulatingEntity));
        if (hitResult.getType() != HitResult.Type.MISS) {
            ((IVec3d)pos3d).set(hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
        }
        if ((hitResult2 = ProjectileUtil.getEntityCollision((World)MeteorClient.mc.world, (Entity)(this.simulatingEntity == MeteorClient.mc.player ? null : this.simulatingEntity), (Vec3d)prevPos3d, (Vec3d)pos3d, (Box)(box = new Box(ProjectileEntitySimulator.prevPos3d.x - (double)(this.width / 2.0f), ProjectileEntitySimulator.prevPos3d.y, ProjectileEntitySimulator.prevPos3d.z - (double)(this.width / 2.0f), ProjectileEntitySimulator.prevPos3d.x + (double)(this.width / 2.0f), ProjectileEntitySimulator.prevPos3d.y + (double)this.height, ProjectileEntitySimulator.prevPos3d.z + (double)(this.width / 2.0f)).stretch(this.velocity.x, this.velocity.y, this.velocity.z).expand(1.0)), entity -> !entity.isSpectator() && entity.isAlive() && entity.canHit())) != null) {
            hitResult = hitResult2;
        }
        return hitResult;
    }
}

