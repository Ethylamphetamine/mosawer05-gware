/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.player;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;

public class GhostHand
extends Module {
    private final Set<BlockPos> posList = new ObjectOpenHashSet();

    public GhostHand() {
        super(Categories.Player, "ghost-hand", "Opens containers through walls.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.mc.options.useKey.isPressed() || this.mc.player.isSneaking()) {
            return;
        }
        if (this.mc.world.getBlockState(BlockPos.ofFloored((Position)this.mc.player.raycast(this.mc.player.getBlockInteractionRange(), this.mc.getRenderTickCounter().getTickDelta(true), false).getPos())).hasBlockEntity()) {
            return;
        }
        Vec3d direction = new Vec3d(0.0, 0.0, 0.1).rotateX(-((float)Math.toRadians(this.mc.player.getPitch()))).rotateY(-((float)Math.toRadians(this.mc.player.getYaw())));
        this.posList.clear();
        int i = 1;
        while ((double)i < this.mc.player.getBlockInteractionRange() * 10.0) {
            BlockPos pos = BlockPos.ofFloored((Position)this.mc.player.getCameraPosVec(this.mc.getRenderTickCounter().getTickDelta(true)).add(direction.multiply((double)i)));
            if (!this.posList.contains(pos)) {
                this.posList.add(pos);
                if (this.mc.world.getBlockState(pos).hasBlockEntity()) {
                    this.mc.interactionManager.interactBlock(this.mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), Direction.UP, pos, true));
                    return;
                }
            }
            ++i;
        }
    }
}

