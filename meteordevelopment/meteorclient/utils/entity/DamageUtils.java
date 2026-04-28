/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.AttributeModifiersComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.DamageUtil
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.attribute.EntityAttributeInstance
 *  net.minecraft.entity.attribute.EntityAttributeModifier
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.damage.DamageSource
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.MaceItem
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.registry.tag.DamageTypeTags
 *  net.minecraft.registry.tag.EntityTypeTags
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.util.shape.VoxelShapes
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.Difficulty
 *  net.minecraft.world.GameMode
 *  net.minecraft.world.Heightmap$Type
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  net.minecraft.world.World
 *  net.minecraft.world.explosion.Explosion
 *  net.minecraft.world.explosion.Explosion$DestructionType
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.utils.entity;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.mixininterface.IExplosion;
import meteordevelopment.meteorclient.mixininterface.IRaycastContext;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class DamageUtils {
    private static final Vec3d vec3d = new Vec3d(0.0, 0.0, 0.0);
    private static Explosion explosion;
    public static RaycastContext raycastContext;
    public static RaycastContext bedRaycast;
    public static final RaycastFactory HIT_FACTORY;

    private DamageUtils() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(DamageUtils.class);
    }

    @EventHandler
    private static void onGameJoined(GameJoinedEvent event) {
        explosion = new Explosion((World)MeteorClient.mc.world, null, 0.0, 0.0, 0.0, 6.0f, false, Explosion.DestructionType.DESTROY);
        raycastContext = new RaycastContext(null, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity)MeteorClient.mc.player);
        bedRaycast = new RaycastContext(null, null, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity)MeteorClient.mc.player);
    }

    public static float crystalDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, RaycastFactory raycastFactory) {
        return DamageUtils.explosionDamage(target, targetPos, targetBox, explosionPos, 12.0f, raycastFactory);
    }

    public static float bedDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, RaycastFactory raycastFactory) {
        return DamageUtils.explosionDamage(target, targetPos, targetBox, explosionPos, 10.0f, raycastFactory);
    }

    public static float anchorDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, RaycastFactory raycastFactory) {
        return DamageUtils.explosionDamage(target, targetPos, targetBox, explosionPos, 10.0f, raycastFactory);
    }

    public static float explosionDamage(LivingEntity target, Vec3d targetPos, Box targetBox, Vec3d explosionPos, float power, RaycastFactory raycastFactory) {
        double modDistance = PlayerUtils.distance(targetPos.x, targetPos.y, targetPos.z, explosionPos.x, explosionPos.y, explosionPos.z);
        if (modDistance > (double)power) {
            return 0.0f;
        }
        double exposure = DamageUtils.getExposure(explosionPos, targetBox, raycastFactory);
        double impact = (1.0 - modDistance / (double)power) * exposure;
        float damage = (int)((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);
        return DamageUtils.calculateReductions(damage, target, MeteorClient.mc.world.getDamageSources().explosion(null));
    }

    public static float crystalDamage(LivingEntity target, Vec3d crystal, boolean predictMovement, BlockPos obsidianPos) {
        return DamageUtils.overridingExplosionDamage(target, crystal, 12.0f, predictMovement, obsidianPos, Blocks.OBSIDIAN.getDefaultState());
    }

    public static float crystalDamage(LivingEntity target, Vec3d crystal) {
        return DamageUtils.explosionDamage(target, crystal, 12.0f, false);
    }

    public static float bedDamage(LivingEntity target, Vec3d bed) {
        return DamageUtils.explosionDamage(target, bed, 10.0f, false);
    }

    public static float anchorDamage(LivingEntity target, Vec3d anchor) {
        return DamageUtils.overridingExplosionDamage(target, anchor, 10.0f, false, BlockPos.ofFloored((Position)anchor), Blocks.AIR.getDefaultState());
    }

    private static float overridingExplosionDamage(LivingEntity target, Vec3d explosionPos, float power, boolean predictMovement, BlockPos overridePos, BlockState overrideState) {
        return DamageUtils.explosionDamage(target, explosionPos, power, predictMovement, DamageUtils.getOverridingHitFactory(overridePos, overrideState));
    }

    private static float explosionDamage(LivingEntity target, Vec3d explosionPos, float power, boolean predictMovement) {
        return DamageUtils.explosionDamage(target, explosionPos, power, predictMovement, HIT_FACTORY);
    }

    private static float explosionDamage(LivingEntity target, Vec3d explosionPos, float power, boolean predictMovement, RaycastFactory raycastFactory) {
        PlayerEntity player;
        if (target == null) {
            return 0.0f;
        }
        if (target instanceof PlayerEntity && EntityUtils.getGameMode(player = (PlayerEntity)target) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) {
            return 0.0f;
        }
        Vec3d position = predictMovement ? target.getPos().add(target.getVelocity()) : target.getPos();
        Box box = target.getBoundingBox();
        if (predictMovement) {
            box = box.offset(target.getVelocity());
        }
        return DamageUtils.explosionDamage(target, position, box, explosionPos, power, raycastFactory);
    }

    public static RaycastFactory getOverridingHitFactory(BlockPos overridePos, BlockState overrideState) {
        return (context, blockPos) -> {
            BlockState blockState;
            if (blockPos.equals((Object)overridePos)) {
                blockState = overrideState;
            } else {
                blockState = MeteorClient.mc.world.getBlockState(blockPos);
                if (blockState.getBlock().getBlastResistance() < 600.0f) {
                    return null;
                }
            }
            return blockState.getCollisionShape((BlockView)MeteorClient.mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
        };
    }

    public static double newCrystalDamage(PlayerEntity player, Box boundingBox, Vec3d crystal, Set<BlockPos> ignorePos) {
        if (player == null) {
            return 0.0;
        }
        if (EntityUtils.getGameMode(player) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) {
            return 0.0;
        }
        if (ignorePos != null && ignorePos.isEmpty()) {
            ignorePos = null;
        }
        ((IVec3d)vec3d).set((boundingBox.minX + boundingBox.maxX) / 2.0, boundingBox.minY, (boundingBox.minZ + boundingBox.maxZ) / 2.0);
        double modDistance = Math.sqrt(vec3d.squaredDistanceTo(crystal));
        if (modDistance > 12.0) {
            return 0.0;
        }
        double exposure = DamageUtils.getExposure(crystal, (Entity)player, boundingBox, raycastContext, ignorePos);
        double impact = (1.0 - modDistance / 12.0) * exposure;
        double damage = (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;
        damage = DamageUtils.getDamageForDifficulty(damage);
        damage = DamageUtil.getDamageLeft((LivingEntity)player, (float)((float)damage), (DamageSource)MeteorClient.mc.world.getDamageSources().explosion(null), (float)player.getArmor(), (float)((float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()));
        damage = DamageUtils.resistanceReduction((LivingEntity)player, damage);
        ((IExplosion)explosion).set(crystal, 6.0f, false);
        damage = DamageUtils.blastProtReduction((LivingEntity)player, damage, explosion);
        return damage < 0.0 ? 0.0 : damage;
    }

    public static double newCrystalDamage(PlayerEntity player, Box boundingBox, Vec3d crystal, Set<BlockPos> ignorePos, Map<BlockPos, BlockState> overridePos) {
        if (player == null) {
            return 0.0;
        }
        if (EntityUtils.getGameMode(player) == GameMode.CREATIVE && !(player instanceof FakePlayerEntity)) {
            return 0.0;
        }
        if (ignorePos != null && ignorePos.isEmpty()) {
            ignorePos = null;
        }
        if (overridePos != null && overridePos.isEmpty()) {
            overridePos = null;
        }
        ((IVec3d)vec3d).set((boundingBox.minX + boundingBox.maxX) / 2.0, boundingBox.minY, (boundingBox.minZ + boundingBox.maxZ) / 2.0);
        double modDistance = Math.sqrt(vec3d.squaredDistanceTo(crystal));
        if (modDistance > 12.0) {
            return 0.0;
        }
        double exposure = DamageUtils.getExposure(crystal, (Entity)player, boundingBox, raycastContext, ignorePos, overridePos);
        double impact = (1.0 - modDistance / 12.0) * exposure;
        double damage = (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;
        damage = DamageUtils.getDamageForDifficulty(damage);
        damage = DamageUtil.getDamageLeft((LivingEntity)player, (float)((float)damage), (DamageSource)MeteorClient.mc.world.getDamageSources().explosion(null), (float)player.getArmor(), (float)((float)player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue()));
        damage = DamageUtils.resistanceReduction((LivingEntity)player, damage);
        ((IExplosion)explosion).set(crystal, 6.0f, false);
        damage = DamageUtils.blastProtReduction((LivingEntity)player, damage, explosion);
        return damage < 0.0 ? 0.0 : damage;
    }

    public static double getExposure(Vec3d source, Entity entity, Box box, RaycastContext raycastContext, Set<BlockPos> ignore) {
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d < 0.0 || e < 0.0 || f < 0.0)) {
            int i = 0;
            int j = 0;
            Vec3d vec3d = new Vec3d(0.0, 0.0, 0.0);
            for (double k = 0.0; k <= 1.0; k += d) {
                for (double l = 0.0; l <= 1.0; l += e) {
                    for (double m = 0.0; m <= 1.0; m += f) {
                        double n = MathHelper.lerp((double)k, (double)box.minX, (double)box.maxX);
                        double o = MathHelper.lerp((double)l, (double)box.minY, (double)box.maxY);
                        double p = MathHelper.lerp((double)m, (double)box.minZ, (double)box.maxZ);
                        ((IVec3d)vec3d).set(n + g, o, p + h);
                        ((IRaycastContext)raycastContext).set(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
                        if (DamageUtils.raycast(raycastContext, ignore).getType() == HitResult.Type.MISS) {
                            ++i;
                        }
                        ++j;
                    }
                }
            }
            return (double)i / (double)j;
        }
        return 0.0;
    }

    public static double getExposure(Vec3d source, Entity entity, Box box, RaycastContext raycastContext, Set<BlockPos> ignore, Map<BlockPos, BlockState> override) {
        if (override == null) {
            return DamageUtils.getExposure(source, entity, box, raycastContext, ignore);
        }
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
        if (!(d < 0.0 || e < 0.0 || f < 0.0)) {
            int i = 0;
            int j = 0;
            Vec3d vec3d = new Vec3d(0.0, 0.0, 0.0);
            for (double k = 0.0; k <= 1.0; k += d) {
                for (double l = 0.0; l <= 1.0; l += e) {
                    for (double m = 0.0; m <= 1.0; m += f) {
                        double n = MathHelper.lerp((double)k, (double)box.minX, (double)box.maxX);
                        double o = MathHelper.lerp((double)l, (double)box.minY, (double)box.maxY);
                        double p = MathHelper.lerp((double)m, (double)box.minZ, (double)box.maxZ);
                        ((IVec3d)vec3d).set(n + g, o, p + h);
                        ((IRaycastContext)raycastContext).set(vec3d, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
                        if (DamageUtils.raycast(raycastContext, ignore, override).getType() == HitResult.Type.MISS) {
                            ++i;
                        }
                        ++j;
                    }
                }
            }
            return (double)i / (double)j;
        }
        return 0.0;
    }

    public static BlockHitResult raycast(RaycastContext context) {
        return (BlockHitResult)BlockView.raycast((Vec3d)context.getStart(), (Vec3d)context.getEnd(), (Object)context, (raycastContext, blockPos) -> {
            BlockState blockState = MeteorClient.mc.world.getBlockState(blockPos);
            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, (BlockView)MeteorClient.mc.world, blockPos);
            BlockHitResult blockHitResult = MeteorClient.mc.world.raycastBlock(raycastContext.getStart(), raycastContext.getEnd(), blockPos, voxelShape, blockState);
            if (blockHitResult != null) {
                return blockHitResult;
            }
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(raycastContext.getStart(), raycastContext.getEnd(), blockPos);
            return blockHitResult2 != null ? blockHitResult2 : BlockHitResult.createMissed((Vec3d)raycastContext.getEnd(), (Direction)Direction.getFacing((Vec3d)raycastContext.getStart().subtract(raycastContext.getEnd())), (BlockPos)BlockPos.ofFloored((Position)raycastContext.getEnd()));
        }, raycastContext -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed((Vec3d)raycastContext.getEnd(), (Direction)Direction.getFacing((double)vec3d.x, (double)vec3d.y, (double)vec3d.z), (BlockPos)BlockPos.ofFloored((Position)raycastContext.getEnd()));
        });
    }

    public static BlockHitResult raycast(RaycastContext context, Set<BlockPos> ignore) {
        return (BlockHitResult)BlockView.raycast((Vec3d)context.getStart(), (Vec3d)context.getEnd(), (Object)context, (raycastContext, blockPos) -> {
            BlockState blockState = ignore != null && ignore.contains(blockPos) ? Blocks.AIR.getDefaultState() : MeteorClient.mc.world.getBlockState(blockPos);
            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();
            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, (BlockView)MeteorClient.mc.world, blockPos);
            BlockHitResult blockHitResult = MeteorClient.mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, raycastContext -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed((Vec3d)raycastContext.getEnd(), (Direction)Direction.getFacing((double)vec3d.x, (double)vec3d.y, (double)vec3d.z), (BlockPos)BlockPos.ofFloored((Position)raycastContext.getEnd()));
        });
    }

    public static BlockHitResult raycast(RaycastContext context, Set<BlockPos> ignore, Map<BlockPos, BlockState> override) {
        return (BlockHitResult)BlockView.raycast((Vec3d)context.getStart(), (Vec3d)context.getEnd(), (Object)context, (raycastContext, blockPos) -> {
            BlockState blockState = ignore != null && ignore.contains(blockPos) ? Blocks.AIR.getDefaultState() : (override != null && override.containsKey(blockPos) ? (BlockState)override.get(blockPos) : MeteorClient.mc.world.getBlockState(blockPos));
            Vec3d vec3d = raycastContext.getStart();
            Vec3d vec3d2 = raycastContext.getEnd();
            VoxelShape voxelShape = raycastContext.getBlockShape(blockState, (BlockView)MeteorClient.mc.world, blockPos);
            BlockHitResult blockHitResult = MeteorClient.mc.world.raycastBlock(vec3d, vec3d2, blockPos, voxelShape, blockState);
            VoxelShape voxelShape2 = VoxelShapes.empty();
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, blockPos);
            double d = blockHitResult == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : raycastContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, raycastContext -> {
            Vec3d vec3d = raycastContext.getStart().subtract(raycastContext.getEnd());
            return BlockHitResult.createMissed((Vec3d)raycastContext.getEnd(), (Direction)Direction.getFacing((double)vec3d.x, (double)vec3d.y, (double)vec3d.z), (BlockPos)BlockPos.ofFloored((Position)raycastContext.getEnd()));
        });
    }

    public static double getDamageForDifficulty(double damage) {
        return switch (MeteorClient.mc.world.getDifficulty()) {
            case Difficulty.EASY -> Math.min(damage / 2.0 + 1.0, damage);
            case Difficulty.HARD, Difficulty.PEACEFUL -> damage * 3.0 / 2.0;
            default -> damage;
        };
    }

    private static double normalProtReduction(LivingEntity player, double damage) {
        int protLevel = 10;
        if (protLevel > 20) {
            protLevel = 20;
        }
        return (damage *= 1.0 - (double)protLevel / 25.0) < 0.0 ? 0.0 : damage;
    }

    private static double blastProtReduction(LivingEntity player, double damage, Explosion explosion) {
        int protLevel = 10;
        if (protLevel > 20) {
            protLevel = 20;
        }
        return (damage *= 1.0 - (double)protLevel / 25.0) < 0.0 ? 0.0 : damage;
    }

    public static double resistanceReduction(LivingEntity player, double damage) {
        if (player.hasStatusEffect(StatusEffects.RESISTANCE)) {
            int lvl = player.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1;
            damage *= 1.0 - (double)lvl * 0.2;
        }
        return damage < 0.0 ? 0.0 : damage;
    }

    public static float getAttackDamage(LivingEntity attacker, LivingEntity target) {
        DamageSource damageSource;
        float itemDamage = (float)attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)attacker;
            damageSource = MeteorClient.mc.world.getDamageSources().playerAttack(player);
        } else {
            damageSource = MeteorClient.mc.world.getDamageSources().mobAttack(attacker);
        }
        DamageSource damageSource2 = damageSource;
        float damage = DamageUtils.modifyAttackDamage(attacker, target, attacker.getWeaponStack(), damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    public static float getAttackDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon) {
        DamageSource damageSource;
        EntityAttributeInstance original = attacker.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        EntityAttributeInstance copy = new EntityAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE, o -> {});
        copy.setBaseValue(original.getBaseValue());
        for (EntityAttributeModifier modifier2 : original.getModifiers()) {
            copy.addTemporaryModifier(modifier2);
        }
        copy.removeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID);
        AttributeModifiersComponent attributeModifiers = (AttributeModifiersComponent)weapon.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry == EntityAttributes.GENERIC_ATTACK_DAMAGE) {
                    copy.updateModifier(modifier);
                }
            });
        }
        float itemDamage = (float)copy.getValue();
        if (attacker instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)attacker;
            damageSource = MeteorClient.mc.world.getDamageSources().playerAttack(player);
        } else {
            damageSource = MeteorClient.mc.world.getDamageSources().mobAttack(attacker);
        }
        DamageSource damageSource2 = damageSource;
        float damage = DamageUtils.modifyAttackDamage(attacker, target, weapon, damageSource2, itemDamage);
        return DamageUtils.calculateReductions(damage, target, damageSource2);
    }

    private static float modifyAttackDamage(LivingEntity attacker, LivingEntity target, ItemStack weapon, DamageSource damageSource, float damage) {
        int smite;
        int impaling;
        int baneOfArthropods;
        Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
        Utils.getEnchantments(weapon, (Object2IntMap<RegistryEntry<Enchantment>>)enchantments);
        float enchantDamage = 0.0f;
        int sharpness = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.SHARPNESS);
        if (sharpness > 0) {
            enchantDamage += 1.0f + 0.5f * (float)(sharpness - 1);
        }
        if ((baneOfArthropods = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.BANE_OF_ARTHROPODS)) > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
            enchantDamage += 2.5f * (float)baneOfArthropods;
        }
        if ((impaling = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.IMPALING)) > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
            enchantDamage += 2.5f * (float)impaling;
        }
        if ((smite = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.SMITE)) > 0 && target.getType().isIn(EntityTypeTags.SENSITIVE_TO_SMITE)) {
            enchantDamage += 2.5f * (float)smite;
        }
        if (attacker instanceof PlayerEntity) {
            MaceItem item;
            float bonusDamage;
            PlayerEntity playerEntity = (PlayerEntity)attacker;
            float charge = playerEntity.getAttackCooldownProgress(0.5f);
            damage *= 0.2f + charge * charge * 0.8f;
            enchantDamage *= charge;
            Item item2 = weapon.getItem();
            if (item2 instanceof MaceItem && (bonusDamage = (item = (MaceItem)item2).getBonusAttackDamage((Entity)target, damage, damageSource)) > 0.0f) {
                int density = Utils.getEnchantmentLevel(weapon, (RegistryKey<Enchantment>)Enchantments.DENSITY);
                if (density > 0) {
                    bonusDamage += 0.5f * attacker.fallDistance;
                }
                damage += bonusDamage;
            }
            if (!(!(charge > 0.9f) || !(attacker.fallDistance > 0.0f) || attacker.isOnGround() || attacker.isClimbing() || attacker.isTouchingWater() || attacker.hasStatusEffect(StatusEffects.BLINDNESS) || attacker.hasVehicle())) {
                damage *= 1.5f;
            }
        }
        return damage + enchantDamage;
    }

    public static float fallDamage(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (player.getAbilities().flying) {
                return 0.0f;
            }
        }
        if (entity.hasStatusEffect(StatusEffects.SLOW_FALLING) || entity.hasStatusEffect(StatusEffects.LEVITATION)) {
            return 0.0f;
        }
        int surface = MeteorClient.mc.world.getWorldChunk(entity.getBlockPos()).getHeightmap(Heightmap.Type.MOTION_BLOCKING).get(entity.getBlockX() & 0xF, entity.getBlockZ() & 0xF);
        if (entity.getBlockY() >= surface) {
            return DamageUtils.fallDamageReductions(entity, surface);
        }
        BlockHitResult raycastResult = MeteorClient.mc.world.raycast(new RaycastContext(entity.getPos(), new Vec3d(entity.getX(), (double)MeteorClient.mc.world.getBottomY(), entity.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.WATER, (Entity)entity));
        if (raycastResult.getType() == HitResult.Type.MISS) {
            return 0.0f;
        }
        return DamageUtils.fallDamageReductions(entity, raycastResult.getBlockPos().getY());
    }

    private static float fallDamageReductions(LivingEntity entity, int surface) {
        int fallHeight = (int)(entity.getY() - (double)surface + (double)entity.fallDistance - 3.0);
        @Nullable StatusEffectInstance jumpBoostInstance = entity.getStatusEffect(StatusEffects.JUMP_BOOST);
        if (jumpBoostInstance != null) {
            fallHeight -= jumpBoostInstance.getAmplifier() + 1;
        }
        return DamageUtils.calculateReductions(fallHeight, entity, MeteorClient.mc.world.getDamageSources().fall());
    }

    public static float calculateReductions(float damage, LivingEntity entity, DamageSource damageSource) {
        if (damageSource.isScaledWithDifficulty()) {
            switch (MeteorClient.mc.world.getDifficulty()) {
                case EASY: {
                    damage = Math.min(damage / 2.0f + 1.0f, damage);
                    break;
                }
                case HARD: {
                    damage *= 1.5f;
                }
            }
        }
        damage = DamageUtil.getDamageLeft((LivingEntity)entity, (float)damage, (DamageSource)damageSource, (float)DamageUtils.getArmor(entity), (float)((float)entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)));
        damage = DamageUtils.resistanceReduction(entity, damage);
        damage = DamageUtils.protectionReduction(entity, damage, damageSource);
        return Math.max(damage, 0.0f);
    }

    private static float getArmor(LivingEntity entity) {
        return (float)Math.floor(entity.getAttributeValue(EntityAttributes.GENERIC_ARMOR));
    }

    private static float protectionReduction(LivingEntity player, float damage, DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        int damageProtection = 0;
        for (ItemStack stack : player.getAllArmorItems()) {
            int featherFalling;
            int projectileProtection;
            int blastProtection;
            int fireProtection;
            Object2IntOpenHashMap enchantments = new Object2IntOpenHashMap();
            Utils.getEnchantments(stack, (Object2IntMap<RegistryEntry<Enchantment>>)enchantments);
            int protection = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.PROTECTION);
            if (protection > 0) {
                damageProtection += protection;
            }
            if ((fireProtection = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.FIRE_PROTECTION)) > 0 && source.isIn(DamageTypeTags.IS_FIRE)) {
                damageProtection += 2 * fireProtection;
            }
            if ((blastProtection = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.BLAST_PROTECTION)) > 0 && source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                damageProtection += 2 * blastProtection;
            }
            if ((projectileProtection = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.PROJECTILE_PROTECTION)) > 0 && source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                damageProtection += 2 * projectileProtection;
            }
            if ((featherFalling = Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)enchantments, (RegistryKey<Enchantment>)Enchantments.FEATHER_FALLING)) <= 0 || !source.isIn(DamageTypeTags.IS_FALL)) continue;
            damageProtection += 3 * featherFalling;
        }
        return DamageUtil.getInflictedDamage((float)damage, (float)damageProtection);
    }

    private static float resistanceReduction(LivingEntity player, float damage) {
        StatusEffectInstance resistance = player.getStatusEffect(StatusEffects.RESISTANCE);
        if (resistance != null) {
            int lvl = resistance.getAmplifier() + 1;
            damage *= 1.0f - (float)lvl * 0.2f;
        }
        return Math.max(damage, 0.0f);
    }

    private static float getExposure(Vec3d source, Box box, RaycastFactory raycastFactory) {
        double xDiff = box.maxX - box.minX;
        double yDiff = box.maxY - box.minY;
        double zDiff = box.maxZ - box.minZ;
        double xStep = 1.0 / (xDiff * 2.0 + 1.0);
        double yStep = 1.0 / (yDiff * 2.0 + 1.0);
        double zStep = 1.0 / (zDiff * 2.0 + 1.0);
        if (xStep > 0.0 && yStep > 0.0 && zStep > 0.0) {
            int misses = 0;
            int hits = 0;
            double xOffset = (1.0 - Math.floor(1.0 / xStep) * xStep) * 0.5;
            double zOffset = (1.0 - Math.floor(1.0 / zStep) * zStep) * 0.5;
            xStep *= xDiff;
            yStep *= yDiff;
            zStep *= zDiff;
            double startX = box.minX + xOffset;
            double startY = box.minY;
            double startZ = box.minZ + zOffset;
            double endX = box.maxX + xOffset;
            double endY = box.maxY;
            double endZ = box.maxZ + zOffset;
            for (double x = startX; x <= endX; x += xStep) {
                for (double y = startY; y <= endY; y += yStep) {
                    for (double z = startZ; z <= endZ; z += zStep) {
                        Vec3d position = new Vec3d(x, y, z);
                        if (DamageUtils.raycast(new ExposureRaycastContext(position, source), raycastFactory) == null) {
                            ++misses;
                        }
                        ++hits;
                    }
                }
            }
            return (float)misses / (float)hits;
        }
        return 0.0f;
    }

    private static BlockHitResult raycast(ExposureRaycastContext context, RaycastFactory raycastFactory) {
        return (BlockHitResult)BlockView.raycast((Vec3d)context.start, (Vec3d)context.end, (Object)context, (BiFunction)raycastFactory, ctx -> null);
    }

    static {
        HIT_FACTORY = (context, blockPos) -> {
            BlockState blockState = MeteorClient.mc.world.getBlockState(blockPos);
            if (blockState.getBlock().getBlastResistance() < 600.0f) {
                return null;
            }
            return blockState.getCollisionShape((BlockView)MeteorClient.mc.world, blockPos).raycast(context.start(), context.end(), blockPos);
        };
    }

    @FunctionalInterface
    public static interface RaycastFactory
    extends BiFunction<ExposureRaycastContext, BlockPos, BlockHitResult> {
    }

    public record ExposureRaycastContext(Vec3d start, Vec3d end) {
    }
}

