/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.pathing.goals.Goal
 *  baritone.api.pathing.goals.GoalBlock
 *  baritone.api.pathing.goals.GoalGetToBlock
 *  baritone.api.pathing.goals.GoalXZ
 *  baritone.api.process.IBaritoneProcess
 *  baritone.api.process.PathingCommand
 *  baritone.api.process.PathingCommandType
 *  baritone.api.utils.Rotation
 *  baritone.api.utils.SettingsUtil
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.pathing;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.Rotation;
import baritone.api.utils.SettingsUtil;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.BaritoneSettings;
import meteordevelopment.meteorclient.pathing.IPathManager;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BaritonePathManager
implements IPathManager {
    private final VarHandle rotationField;
    private final BaritoneSettings settings;
    private GoalDirection directionGoal;
    private boolean pathingPaused;

    public BaritonePathManager() {
        MeteorClient.EVENT_BUS.subscribe(this);
        Class klass = BaritoneAPI.getProvider().getPrimaryBaritone().getLookBehavior().getClass();
        VarHandle rotationField = null;
        for (Field field : klass.getDeclaredFields()) {
            if (field.getType() != Rotation.class) continue;
            try {
                rotationField = MethodHandles.lookup().unreflectVarHandle(field);
                break;
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        this.rotationField = rotationField;
        this.settings = new BaritoneSettings();
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().registerProcess((IBaritoneProcess)new BaritoneProcess());
    }

    @Override
    public String getName() {
        return "Baritone";
    }

    @Override
    public boolean isPathing() {
        return BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing();
    }

    @Override
    public void pause() {
        this.pathingPaused = true;
    }

    @Override
    public void resume() {
        this.pathingPaused = false;
    }

    @Override
    public void stop() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
    }

    @Override
    public void moveTo(BlockPos pos, boolean ignoreY) {
        if (ignoreY) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalXZ(pos.getX(), pos.getZ()));
            return;
        }
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)new GoalGetToBlock(pos));
    }

    @Override
    public void moveToBlockPos(BlockPos pos) {
        BaritoneAPI.getProvider().getBaritoneForPlayer(MeteorClient.mc.player).getCustomGoalProcess().setGoalAndPath((Goal)new GoalBlock(pos));
    }

    @Override
    public void moveInDirection(float yaw) {
        this.directionGoal = new GoalDirection(yaw);
        BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath((Goal)this.directionGoal);
    }

    @Override
    public void mine(Block ... blocks) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getMineProcess().mine(blocks);
    }

    @Override
    public void follow(Predicate<Entity> entity) {
        BaritoneAPI.getProvider().getPrimaryBaritone().getFollowProcess().follow(entity);
    }

    @Override
    public float getTargetYaw() {
        Rotation rotation = this.rotationField.get(BaritoneAPI.getProvider().getPrimaryBaritone().getLookBehavior());
        return rotation == null ? 0.0f : rotation.getYaw();
    }

    @Override
    public float getTargetPitch() {
        Rotation rotation = this.rotationField.get(BaritoneAPI.getProvider().getPrimaryBaritone().getLookBehavior());
        return rotation == null ? 0.0f : rotation.getPitch();
    }

    @Override
    public IPathManager.ISettings getSettings() {
        return this.settings;
    }

    @EventHandler(priority=200)
    private void onTick(TickEvent.Pre event) {
        if (this.directionGoal == null) {
            return;
        }
        if (this.directionGoal != BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().getGoal()) {
            this.directionGoal = null;
            return;
        }
        this.directionGoal.tick();
    }

    private class BaritoneProcess
    implements IBaritoneProcess {
        private BaritoneProcess() {
        }

        public boolean isActive() {
            return BaritonePathManager.this.pathingPaused;
        }

        public PathingCommand onTick(boolean b, boolean b1) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().clearAllKeys();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }

        public boolean isTemporary() {
            return true;
        }

        public void onLostControl() {
        }

        public double priority() {
            return 0.0;
        }

        public String displayName0() {
            return "Meteor Client";
        }
    }

    private static class GoalDirection
    implements Goal {
        private static final double SQRT_2 = Math.sqrt(2.0);
        private final float yaw;
        private int x;
        private int z;
        private int timer;

        public GoalDirection(float yaw) {
            this.yaw = yaw;
            this.tick();
        }

        public static double calculate(double xDiff, double zDiff) {
            double straight;
            double z;
            double x = Math.abs(xDiff);
            if (x < (z = Math.abs(zDiff))) {
                straight = z - x;
                diagonal = x;
            } else {
                straight = x - z;
                diagonal = z;
            }
            return ((diagonal *= SQRT_2) + straight) * (Double)BaritoneAPI.getSettings().costHeuristic.value;
        }

        public void tick() {
            if (this.timer <= 0) {
                this.timer = 20;
                Vec3d pos = MeteorClient.mc.player.getPos();
                float theta = (float)Math.toRadians(this.yaw);
                this.x = (int)Math.floor(pos.x - (double)MathHelper.sin((float)theta) * 100.0);
                this.z = (int)Math.floor(pos.z + (double)MathHelper.cos((float)theta) * 100.0);
            }
            --this.timer;
        }

        public boolean isInGoal(int x, int y, int z) {
            return x == this.x && z == this.z;
        }

        public double heuristic(int x, int y, int z) {
            int xDiff = x - this.x;
            int zDiff = z - this.z;
            return GoalDirection.calculate(xDiff, zDiff);
        }

        public String toString() {
            return String.format("GoalXZ{x=%s,z=%s}", SettingsUtil.maybeCensor((int)this.x), SettingsUtil.maybeCensor((int)this.z));
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }
    }
}

