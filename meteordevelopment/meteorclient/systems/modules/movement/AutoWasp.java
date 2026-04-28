/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.s2c.common.DisconnectS2CPacket
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Vector3dSetting;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3d;

public class AutoWasp
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> horizontalSpeed;
    private final Setting<Double> verticalSpeed;
    private final Setting<Boolean> avoidLanding;
    private final Setting<Boolean> predictMovement;
    private final Setting<Boolean> onlyFriends;
    private final Setting<Action> action;
    private final Setting<Vector3d> offset;
    public PlayerEntity target;
    private int jumpTimer;
    private boolean incrementJumpTimer;

    public AutoWasp() {
        super(Categories.Movement, "auto-wasp", "Wasps for you. Unable to traverse around blocks, assumes a clear straight line to the target.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.horizontalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("horizontal-speed")).description("Horizontal elytra speed.")).defaultValue(2.0).build());
        this.verticalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vertical-speed")).description("Vertical elytra speed.")).defaultValue(3.0).build());
        this.avoidLanding = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("avoid-landing")).description("Will try to avoid landing if your target is on the ground.")).defaultValue(true)).build());
        this.predictMovement = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict-movement")).description("Tries to predict the targets position according to their movement.")).defaultValue(true)).build());
        this.onlyFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-friends")).description("Will only follow friends.")).defaultValue(false)).build());
        this.action = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("action-on-target-loss")).description("What to do if you lose the target.")).defaultValue(Action.TOGGLE)).build());
        this.offset = this.sgGeneral.add(((Vector3dSetting.Builder)((Vector3dSetting.Builder)new Vector3dSetting.Builder().name("offset")).description("How many blocks offset to wasp at from the target.")).defaultValue(0.0, 0.0, 0.0).build());
        this.jumpTimer = 0;
        this.incrementJumpTimer = false;
    }

    @Override
    public void onActivate() {
        if (this.target == null || this.target.isRemoved()) {
            this.target = (PlayerEntity)TargetUtils.get(entity -> {
                if (!(entity instanceof PlayerEntity) || entity == this.mc.player) {
                    return false;
                }
                if (((PlayerEntity)entity).isDead() || ((PlayerEntity)entity).getHealth() <= 0.0f) {
                    return false;
                }
                return this.onlyFriends.get() == false || Friends.get().get((PlayerEntity)entity) != null;
            }, SortPriority.LowestDistance);
            if (this.target == null) {
                this.error("No valid targets.", new Object[0]);
                this.toggle();
                return;
            }
            this.info(this.target.getName().getString() + " set as target.", new Object[0]);
        }
        this.jumpTimer = 0;
        this.incrementJumpTimer = false;
    }

    @Override
    public void onDeactivate() {
        this.target = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.target.isRemoved()) {
            this.warning("Lost target!", new Object[0]);
            switch (this.action.get().ordinal()) {
                case 1: {
                    this.onActivate();
                    break;
                }
                case 0: {
                    this.toggle();
                    break;
                }
                case 2: {
                    this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket((Text)Text.literal((String)"%s[%sAuto Wasp%s] Lost target.".formatted(Formatting.GRAY, Formatting.BLUE, Formatting.GRAY))));
                }
            }
            if (!this.isActive()) {
                return;
            }
        }
        if (!(this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem)) {
            return;
        }
        if (this.incrementJumpTimer) {
            ++this.jumpTimer;
        }
        if (!this.mc.player.isFallFlying()) {
            if (!this.incrementJumpTimer) {
                this.incrementJumpTimer = true;
            }
            if (this.mc.player.isOnGround() && this.incrementJumpTimer) {
                this.mc.player.jump();
                return;
            }
            if (this.jumpTimer >= 4) {
                this.jumpTimer = 0;
                this.mc.player.setJumping(false);
                this.mc.player.setSprinting(true);
                this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        } else {
            this.incrementJumpTimer = false;
            this.jumpTimer = 0;
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        double yDist;
        if (!(this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem)) {
            return;
        }
        if (!this.mc.player.isFallFlying()) {
            return;
        }
        double xVel = 0.0;
        double yVel = 0.0;
        double zVel = 0.0;
        Vec3d targetPos = this.target.getPos().add(this.offset.get().x, this.offset.get().y, this.offset.get().z);
        if (this.predictMovement.get().booleanValue()) {
            targetPos.add(PlayerEntity.adjustMovementForCollisions((Entity)this.target, (Vec3d)this.target.getVelocity(), (Box)this.target.getBoundingBox(), (World)this.mc.world, (List)this.mc.world.getEntityCollisions((Entity)this.target, this.target.getBoundingBox().stretch(this.target.getVelocity()))));
        }
        if (this.avoidLanding.get().booleanValue()) {
            double d = this.target.getBoundingBox().getLengthX() / 2.0;
            for (Direction dir : Direction.HORIZONTAL) {
                BlockPos pos = BlockPos.ofFloored((Position)targetPos.offset(dir, d).offset(dir.rotateYClockwise(), d)).down();
                if (!this.mc.world.getBlockState((BlockPos)pos).getBlock().collidable || !(Math.abs(targetPos.getY() - (double)(pos.getY() + 1)) <= 0.25)) continue;
                targetPos = new Vec3d(targetPos.x, (double)pos.getY() + 1.25, targetPos.z);
                break;
            }
        }
        double xDist = targetPos.getX() - this.mc.player.getX();
        double zDist = targetPos.getZ() - this.mc.player.getZ();
        double absX = Math.abs(xDist);
        double absZ = Math.abs(zDist);
        double diag = 0.0;
        if (absX > (double)1.0E-5f && absZ > (double)1.0E-5f) {
            diag = 1.0 / Math.sqrt(absX * absX + absZ * absZ);
        }
        if (absX > (double)1.0E-5f) {
            xVel = absX < this.horizontalSpeed.get() ? xDist : this.horizontalSpeed.get() * Math.signum(xDist);
            if (diag != 0.0) {
                xVel *= absX * diag;
            }
        }
        if (absZ > (double)1.0E-5f) {
            zVel = absZ < this.horizontalSpeed.get() ? zDist : this.horizontalSpeed.get() * Math.signum(zDist);
            if (diag != 0.0) {
                zVel *= absZ * diag;
            }
        }
        if (Math.abs(yDist = targetPos.getY() - this.mc.player.getY()) > (double)1.0E-5f) {
            yVel = Math.abs(yDist) < this.verticalSpeed.get() ? yDist : this.verticalSpeed.get() * Math.signum(yDist);
        }
        ((IVec3d)event.movement).set(xVel, yVel, zVel);
    }

    public static enum Action {
        TOGGLE,
        CHOOSE_NEW_TARGET,
        DISCONNECT;


        public String toString() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Toggle module";
                case 1 -> "Choose new target";
                case 2 -> "Disconnect";
            };
        }
    }
}

