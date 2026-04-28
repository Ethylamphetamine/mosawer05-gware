/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.entity.BedBlockEntity
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.item.PotionItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.GameMode
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 *  org.joml.Math
 */
package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.text.TextUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.Dimension;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.RaycastContext;
import org.joml.Math;

public class PlayerUtils {
    private static final double diagonal = 1.0 / java.lang.Math.sqrt(2.0);
    private static final Vec3d horizontalVelocity = new Vec3d(0.0, 0.0, 0.0);
    private static final Color color = new Color();

    private PlayerUtils() {
    }

    public static Color getPlayerColor(PlayerEntity entity, Color defaultColor) {
        if (Friends.get().isFriend(entity)) {
            return color.set(Config.get().friendColor.get()).a(defaultColor.a);
        }
        if (Friends.get().isEnemy(entity)) {
            return color.set(Config.get().enemyColor.get()).a(defaultColor.a);
        }
        if (Config.get().useTeamColor.get().booleanValue() && !color.set(TextUtils.getMostPopularColor(entity.getDisplayName())).equals(Utils.WHITE)) {
            return color.a(defaultColor.a);
        }
        return defaultColor;
    }

    public static Vec3d getHorizontalVelocity(double bps) {
        float yaw = MeteorClient.mc.player.getYaw();
        if (PathManagers.get().isPathing()) {
            yaw = PathManagers.get().getTargetYaw();
        }
        Vec3d forward = Vec3d.fromPolar((float)0.0f, (float)yaw);
        Vec3d right = Vec3d.fromPolar((float)0.0f, (float)(yaw + 90.0f));
        double velX = 0.0;
        double velZ = 0.0;
        boolean a = false;
        if (MeteorClient.mc.player.input.pressingForward) {
            velX += forward.x / 20.0 * bps;
            velZ += forward.z / 20.0 * bps;
            a = true;
        }
        if (MeteorClient.mc.player.input.pressingBack) {
            velX -= forward.x / 20.0 * bps;
            velZ -= forward.z / 20.0 * bps;
            a = true;
        }
        boolean b = false;
        if (MeteorClient.mc.player.input.pressingRight) {
            velX += right.x / 20.0 * bps;
            velZ += right.z / 20.0 * bps;
            b = true;
        }
        if (MeteorClient.mc.player.input.pressingLeft) {
            velX -= right.x / 20.0 * bps;
            velZ -= right.z / 20.0 * bps;
            b = true;
        }
        if (a && b) {
            velX *= diagonal;
            velZ *= diagonal;
        }
        ((IVec3d)horizontalVelocity).setXZ(velX, velZ);
        return horizontalVelocity;
    }

    public static void centerPlayer() {
        double x = (double)MathHelper.floor((double)MeteorClient.mc.player.getX()) + 0.5;
        double z = (double)MathHelper.floor((double)MeteorClient.mc.player.getZ()) + 0.5;
        MeteorClient.mc.player.setPosition(x, MeteorClient.mc.player.getY(), z);
        MeteorClient.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ(), MeteorClient.mc.player.isOnGround()));
    }

    public static boolean canSeeEntity(Entity entity) {
        Vec3d vec1 = new Vec3d(0.0, 0.0, 0.0);
        Vec3d vec2 = new Vec3d(0.0, 0.0, 0.0);
        ((IVec3d)vec1).set(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getStandingEyeHeight(), MeteorClient.mc.player.getZ());
        ((IVec3d)vec2).set(entity.getX(), entity.getY(), entity.getZ());
        boolean canSeeFeet = MeteorClient.mc.world.raycast(new RaycastContext(vec1, vec2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)MeteorClient.mc.player)).getType() == HitResult.Type.MISS;
        ((IVec3d)vec2).set(entity.getX(), entity.getY() + (double)entity.getStandingEyeHeight(), entity.getZ());
        boolean canSeeEyes = MeteorClient.mc.world.raycast(new RaycastContext(vec1, vec2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)MeteorClient.mc.player)).getType() == HitResult.Type.MISS;
        return canSeeFeet || canSeeEyes;
    }

    public static float[] calculateAngle(Vec3d target) {
        Vec3d eyesPos = new Vec3d(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY() + (double)MeteorClient.mc.player.getEyeHeight(MeteorClient.mc.player.getPose()), MeteorClient.mc.player.getZ());
        double dX = target.x - eyesPos.x;
        double dY = (target.y - eyesPos.y) * -1.0;
        double dZ = target.z - eyesPos.z;
        double dist = java.lang.Math.sqrt(dX * dX + dZ * dZ);
        return new float[]{(float)MathHelper.wrapDegrees((double)(java.lang.Math.toDegrees(java.lang.Math.atan2(dZ, dX)) - 90.0)), (float)MathHelper.wrapDegrees((double)java.lang.Math.toDegrees(java.lang.Math.atan2(dY, dist)))};
    }

    public static boolean shouldPause(boolean ifBreaking, boolean ifEating, boolean ifDrinking) {
        if (ifBreaking && MeteorClient.mc.interactionManager.isBreakingBlock()) {
            return true;
        }
        if (ifEating && MeteorClient.mc.player.isUsingItem() && (MeteorClient.mc.player.getMainHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD) || MeteorClient.mc.player.getOffHandStack().getItem().getComponents().contains(DataComponentTypes.FOOD))) {
            return true;
        }
        return ifDrinking && MeteorClient.mc.player.isUsingItem() && (MeteorClient.mc.player.getMainHandStack().getItem() instanceof PotionItem || MeteorClient.mc.player.getOffHandStack().getItem() instanceof PotionItem);
    }

    public static boolean isMoving() {
        return MeteorClient.mc.player.forwardSpeed != 0.0f || MeteorClient.mc.player.sidewaysSpeed != 0.0f;
    }

    public static boolean isSprinting() {
        return MeteorClient.mc.player.isSprinting() && (MeteorClient.mc.player.forwardSpeed != 0.0f || MeteorClient.mc.player.sidewaysSpeed != 0.0f);
    }

    public static boolean isInHole(boolean doubles) {
        if (!Utils.canUpdate()) {
            return false;
        }
        BlockPos blockPos = MeteorClient.mc.player.getBlockPos();
        int air = 0;
        for (Direction direction : Direction.values()) {
            BlockState state;
            if (direction == Direction.UP || !((state = MeteorClient.mc.world.getBlockState(blockPos.offset(direction))).getBlock().getBlastResistance() < 600.0f)) continue;
            if (!doubles || direction == Direction.DOWN) {
                return false;
            }
            ++air;
            for (Direction dir : Direction.values()) {
                BlockState blockState1;
                if (dir == direction.getOpposite() || dir == Direction.UP || !((blockState1 = MeteorClient.mc.world.getBlockState(blockPos.offset(direction).offset(dir))).getBlock().getBlastResistance() < 600.0f)) continue;
                return false;
            }
        }
        return air < 2;
    }

    public static float possibleHealthReductions() {
        return PlayerUtils.possibleHealthReductions(true, true);
    }

    public static float possibleHealthReductions(boolean entities, boolean fall) {
        float damage;
        float damageTaken = 0.0f;
        if (entities) {
            for (Entity entity : MeteorClient.mc.world.getEntities()) {
                float attackDamage;
                if (entity instanceof EndCrystalEntity) {
                    float crystalDamage = DamageUtils.crystalDamage((LivingEntity)MeteorClient.mc.player, entity.getPos());
                    if (!(crystalDamage > damageTaken)) continue;
                    damageTaken = crystalDamage;
                    continue;
                }
                if (!(entity instanceof PlayerEntity)) continue;
                PlayerEntity player = (PlayerEntity)entity;
                if (Friends.get().isFriend(player) || !PlayerUtils.isWithin(entity, 5.0) || !((attackDamage = DamageUtils.getAttackDamage((LivingEntity)player, (LivingEntity)MeteorClient.mc.player)) > damageTaken)) continue;
                damageTaken = attackDamage;
            }
            if (PlayerUtils.getDimension() != Dimension.Overworld) {
                for (BlockEntity blockEntity : Utils.blockEntities()) {
                    float explosionDamage;
                    BlockPos bp = blockEntity.getPos();
                    Vec3d pos = new Vec3d((double)bp.getX(), (double)bp.getY(), (double)bp.getZ());
                    if (!(blockEntity instanceof BedBlockEntity) || !((explosionDamage = DamageUtils.bedDamage((LivingEntity)MeteorClient.mc.player, pos)) > damageTaken)) continue;
                    damageTaken = explosionDamage;
                }
            }
        }
        if (fall && !Modules.get().isActive(NoFall.class) && MeteorClient.mc.player.fallDistance > 3.0f && (damage = DamageUtils.fallDamage((LivingEntity)MeteorClient.mc.player)) > damageTaken && !EntityUtils.isAboveWater((Entity)MeteorClient.mc.player)) {
            damageTaken = damage;
        }
        return damageTaken;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return java.lang.Math.sqrt(PlayerUtils.squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    public static double distanceTo(Entity entity) {
        return PlayerUtils.distanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double distanceTo(BlockPos blockPos) {
        return PlayerUtils.distanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double distanceTo(Vec3d vec3d) {
        return PlayerUtils.distanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static double distanceTo(double x, double y, double z) {
        return java.lang.Math.sqrt(PlayerUtils.squaredDistanceTo(x, y, z));
    }

    public static double squaredDistanceTo(Entity entity) {
        return PlayerUtils.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double squaredDistanceTo(BlockPos blockPos) {
        return PlayerUtils.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static double squaredDistanceTo(double x, double y, double z) {
        return PlayerUtils.squaredDistance(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ(), x, y, z);
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double f = x1 - x2;
        double g = y1 - y2;
        double h = z1 - z2;
        return Math.fma((double)f, (double)f, (double)Math.fma((double)g, (double)g, (double)(h * h)));
    }

    public static boolean isWithin(Entity entity, double r) {
        return PlayerUtils.squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithin(Vec3d vec3d, double r) {
        return PlayerUtils.squaredDistanceTo(vec3d.getX(), vec3d.getY(), vec3d.getZ()) <= r * r;
    }

    public static boolean isWithin(BlockPos blockPos, double r) {
        return PlayerUtils.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static boolean isWithin(double x, double y, double z, double r) {
        return PlayerUtils.squaredDistanceTo(x, y, z) <= r * r;
    }

    public static double distanceToCamera(double x, double y, double z) {
        return java.lang.Math.sqrt(PlayerUtils.squaredDistanceToCamera(x, y, z));
    }

    public static double distanceToCamera(Entity entity) {
        return PlayerUtils.distanceToCamera(entity.getX(), entity.getY() + (double)entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static double squaredDistanceToCamera(double x, double y, double z) {
        Vec3d cameraPos = MeteorClient.mc.gameRenderer.getCamera().getPos();
        return PlayerUtils.squaredDistance(cameraPos.x, cameraPos.y, cameraPos.z, x, y, z);
    }

    public static double squaredDistanceToCamera(Entity entity) {
        return PlayerUtils.squaredDistanceToCamera(entity.getX(), entity.getY() + (double)entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static boolean isWithinCamera(Entity entity, double r) {
        return PlayerUtils.squaredDistanceToCamera(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(Vec3d vec3d, double r) {
        return PlayerUtils.squaredDistanceToCamera(vec3d.getX(), vec3d.getY(), vec3d.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(BlockPos blockPos, double r) {
        return PlayerUtils.squaredDistanceToCamera(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static boolean isWithinCamera(double x, double y, double z, double r) {
        return PlayerUtils.squaredDistanceToCamera(x, y, z) <= r * r;
    }

    public static boolean isWithinReach(Entity entity) {
        return PlayerUtils.isWithinReach(entity.getX(), entity.getY(), entity.getZ());
    }

    public static boolean isWithinReach(Vec3d vec3d) {
        return PlayerUtils.isWithinReach(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    public static boolean isWithinReach(BlockPos blockPos) {
        return PlayerUtils.isWithinReach(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static boolean isWithinReach(double x, double y, double z) {
        return PlayerUtils.squaredDistance(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getEyeY(), MeteorClient.mc.player.getZ(), x, y, z) <= MeteorClient.mc.player.getBlockInteractionRange() * MeteorClient.mc.player.getBlockInteractionRange();
    }

    public static Dimension getDimension() {
        if (MeteorClient.mc.world == null) {
            return Dimension.Overworld;
        }
        return switch (MeteorClient.mc.world.getRegistryKey().getValue().getPath()) {
            case "the_nether" -> Dimension.Nether;
            case "the_end" -> Dimension.End;
            default -> Dimension.Overworld;
        };
    }

    public static GameMode getGameMode() {
        if (MeteorClient.mc.player == null) {
            return null;
        }
        PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(MeteorClient.mc.player.getUuid());
        if (playerListEntry == null) {
            return null;
        }
        return playerListEntry.getGameMode();
    }

    public static float getTotalHealth() {
        return MeteorClient.mc.player.getHealth() + MeteorClient.mc.player.getAbsorptionAmount();
    }

    public static boolean isAlive() {
        return MeteorClient.mc.player.isAlive() && !MeteorClient.mc.player.isDead();
    }

    public static int getPing() {
        if (MeteorClient.mc.getNetworkHandler() == null) {
            return 0;
        }
        PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(MeteorClient.mc.player.getUuid());
        if (playerListEntry == null) {
            return 0;
        }
        return playerListEntry.getLatency();
    }

    public static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        }
        Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double)speed);
        float f = MathHelper.sin((float)(yaw * ((float)java.lang.Math.PI / 180)));
        float g = MathHelper.cos((float)(yaw * ((float)java.lang.Math.PI / 180)));
        return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
    }

    public static boolean silentSwapEquipChestplate() {
        MeteorClient.LOG.info(String.valueOf("started"));
        if (MeteorClient.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.DIAMOND_CHESTPLATE) || MeteorClient.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.NETHERITE_CHESTPLATE)) {
            return false;
        }
        FindItemResult hotbarChestplateSlot = InvUtils.findInHotbar(Items.NETHERITE_CHESTPLATE);
        if (!hotbarChestplateSlot.found()) {
            hotbarChestplateSlot = InvUtils.findInHotbar(Items.DIAMOND_CHESTPLATE);
        }
        if (hotbarChestplateSlot.found()) {
            MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, 6, hotbarChestplateSlot.slot(), SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
            return true;
        }
        FindItemResult inventorySlot = InvUtils.find(Items.NETHERITE_CHESTPLATE);
        if (!inventorySlot.found()) {
            MeteorClient.LOG.info("ChestSwap: DIDNT Found chestplate in hotbar slot ");
            inventorySlot = InvUtils.find(Items.DIAMOND_CHESTPLATE);
        }
        if (!inventorySlot.found()) {
            return false;
        }
        FindItemResult hotbarSlot = InvUtils.findInHotbar(new Item[0]);
        MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, inventorySlot.slot(), hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
        MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, 6, hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
        MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, inventorySlot.slot(), hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
        return true;
    }

    public static boolean silentSwapEquipElytra() {
        if (MeteorClient.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA)) {
            return false;
        }
        FindItemResult inventorySlot = InvUtils.findInHotbar(Items.ELYTRA);
        if (inventorySlot.found()) {
            MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, 6, inventorySlot.slot(), SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
            return true;
        }
        inventorySlot = InvUtils.find(Items.ELYTRA);
        if (!inventorySlot.found()) {
            return false;
        }
        FindItemResult hotbarSlot = InvUtils.findInHotbar(new Item[0]);
        MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, inventorySlot.slot(), hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
        MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, 6, hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
        MeteorClient.mc.interactionManager.clickSlot(MeteorClient.mc.player.playerScreenHandler.syncId, inventorySlot.slot(), hotbarSlot.found() ? hotbarSlot.slot() : 0, SlotActionType.SWAP, (PlayerEntity)MeteorClient.mc.player);
        return true;
    }

    public static boolean isPlayerPhased() {
        return MeteorClient.mc.world.getBlockCollisions((Entity)MeteorClient.mc.player, MeteorClient.mc.player.getBoundingBox()).iterator().hasNext();
    }

    public static boolean isPlayerPhased(PlayerEntity player) {
        return MeteorClient.mc.world.getBlockCollisions((Entity)player, player.getBoundingBox()).iterator().hasNext();
    }
}

