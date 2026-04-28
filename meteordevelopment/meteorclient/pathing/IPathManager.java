/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.pathing;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface IPathManager {
    public String getName();

    public boolean isPathing();

    public void pause();

    public void resume();

    public void stop();

    default public void moveTo(BlockPos pos) {
        this.moveTo(pos, false);
    }

    public void moveTo(BlockPos var1, boolean var2);

    public void moveInDirection(float var1);

    public void moveToBlockPos(BlockPos var1);

    public void mine(Block ... var1);

    public void follow(Predicate<Entity> var1);

    public float getTargetYaw();

    public float getTargetPitch();

    public ISettings getSettings();

    public static interface ISettings {
        public Settings get();

        public Setting<Boolean> getWalkOnWater();

        public Setting<Boolean> getWalkOnLava();

        public Setting<Boolean> getStep();

        public Setting<Boolean> getNoFall();

        public void save();
    }
}

