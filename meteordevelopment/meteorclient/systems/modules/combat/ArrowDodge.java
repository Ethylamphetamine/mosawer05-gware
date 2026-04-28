/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.projectile.ArrowEntity
 *  net.minecraft.entity.projectile.ProjectileEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.BlockView
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ProjectileEntityAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.ProjectileEntitySimulator;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ArrowDodge
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgMovement;
    private final Setting<MoveType> moveType;
    private final Setting<Double> moveSpeed;
    private final Setting<Double> distanceCheck;
    private final Setting<Boolean> accurate;
    private final Setting<Boolean> groundCheck;
    private final Setting<Boolean> allProjectiles;
    private final Setting<Boolean> ignoreOwn;
    public final Setting<Integer> simulationSteps;
    private final List<Vec3d> possibleMoveDirections;
    private final ProjectileEntitySimulator simulator;
    private final Pool<Vector3d> vec3s;
    private final List<Vector3d> points;

    public ArrowDodge() {
        super(Categories.Combat, "arrow-dodge", "Tries to dodge arrows coming at you.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgMovement = this.settings.createGroup("Movement");
        this.moveType = this.sgMovement.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("move-type")).description("The way you are moved by this module.")).defaultValue(MoveType.Velocity)).build());
        this.moveSpeed = this.sgMovement.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("move-speed")).description("How fast should you be when dodging arrow.")).defaultValue(1.0).min(0.01).sliderRange(0.01, 5.0).build());
        this.distanceCheck = this.sgMovement.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("distance-check")).description("How far should an arrow be from the player to be considered not hitting.")).defaultValue(1.0).min(0.01).sliderRange(0.01, 5.0).build());
        this.accurate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("accurate")).description("Whether or not to calculate more accurate.")).defaultValue(false)).build());
        this.groundCheck = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ground-check")).description("Tries to prevent you from falling to your death.")).defaultValue(true)).build());
        this.allProjectiles = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("all-projectiles")).description("Dodge all projectiles, not only arrows.")).defaultValue(false)).build());
        this.ignoreOwn = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-own")).description("Ignore your own projectiles.")).defaultValue(false)).build());
        this.simulationSteps = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("simulation-steps")).description("How many steps to simulate projectiles. Zero for no limit.")).defaultValue(500)).sliderMax(5000).build());
        this.possibleMoveDirections = Arrays.asList(new Vec3d(1.0, 0.0, 1.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(-1.0, 0.0, 1.0), new Vec3d(1.0, 0.0, 0.0), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, -1.0), new Vec3d(0.0, 0.0, -1.0), new Vec3d(-1.0, 0.0, -1.0));
        this.simulator = new ProjectileEntitySimulator();
        this.vec3s = new Pool<Vector3d>(Vector3d::new);
        this.points = new ArrayList<Vector3d>();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Vector3d point : this.points) {
            this.vec3s.free(point);
        }
        this.points.clear();
        block1: for (Entity e : this.mc.world.getEntities()) {
            UUID owner;
            if (!(e instanceof ProjectileEntity) || !this.allProjectiles.get().booleanValue() && !(e instanceof ArrowEntity) || this.ignoreOwn.get().booleanValue() && (owner = ((ProjectileEntityAccessor)e).getOwnerUuid()) != null && owner.equals(this.mc.player.getUuid()) || !this.simulator.set(e, this.accurate.get())) continue;
            for (int i = 0; i < (this.simulationSteps.get() > 0 ? this.simulationSteps.get() : Integer.MAX_VALUE); ++i) {
                this.points.add(this.vec3s.get().set((Vector3dc)this.simulator.pos));
                if (this.simulator.tick() != null) continue block1;
            }
        }
        if (this.isValid(Vec3d.ZERO, false)) {
            return;
        }
        double speed = this.moveSpeed.get();
        for (int i = 0; i < 500; ++i) {
            boolean didMove = false;
            Collections.shuffle(this.possibleMoveDirections);
            for (Vec3d direction : this.possibleMoveDirections) {
                Vec3d velocity = direction.multiply(speed);
                if (!this.isValid(velocity, true)) continue;
                this.move(velocity);
                didMove = true;
                break;
            }
            if (didMove) break;
            speed += this.moveSpeed.get().doubleValue();
        }
    }

    private void move(Vec3d vel) {
        this.move(vel.x, vel.y, vel.z);
    }

    private void move(double velX, double velY, double velZ) {
        switch (this.moveType.get().ordinal()) {
            case 0: {
                this.mc.player.setVelocity(velX, velY, velZ);
                break;
            }
            case 1: {
                Vec3d newPos = this.mc.player.getPos().add(velX, velY, velZ);
                this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, false));
                this.mc.player.networkHandler.sendPacket((Packet)new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y - 0.01, newPos.z, true));
            }
        }
    }

    private boolean isValid(Vec3d velocity, boolean checkGround) {
        Vec3d playerPos = this.mc.player.getPos().add(velocity);
        Vec3d headPos = playerPos.add(0.0, 1.0, 0.0);
        for (Vector3d pos : this.points) {
            Vec3d projectilePos = new Vec3d(pos.x, pos.y, pos.z);
            if (projectilePos.isInRange((Position)playerPos, this.distanceCheck.get().doubleValue())) {
                return false;
            }
            if (!projectilePos.isInRange((Position)headPos, this.distanceCheck.get().doubleValue())) continue;
            return false;
        }
        if (checkGround) {
            BlockPos blockPos = this.mc.player.getBlockPos().add((Vec3i)BlockPos.ofFloored((double)velocity.x, (double)velocity.y, (double)velocity.z));
            if (!this.mc.world.getBlockState(blockPos).getCollisionShape((BlockView)this.mc.world, blockPos).isEmpty()) {
                return false;
            }
            if (!this.mc.world.getBlockState(blockPos.up()).getCollisionShape((BlockView)this.mc.world, blockPos.up()).isEmpty()) {
                return false;
            }
            if (this.groundCheck.get().booleanValue()) {
                return !this.mc.world.getBlockState(blockPos.down()).getCollisionShape((BlockView)this.mc.world, blockPos.down()).isEmpty();
            }
        }
        return true;
    }

    public static enum MoveType {
        Velocity,
        Packet;

    }
}

