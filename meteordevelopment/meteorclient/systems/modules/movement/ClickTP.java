/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Direction$Axis
 *  net.minecraft.util.shape.VoxelShape
 *  net.minecraft.world.BlockView
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ClickTP
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> maxDistance;

    public ClickTP() {
        super(Categories.Movement, "click-tp", "Teleports you to the block you click on.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.maxDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-distance")).description("The maximum distance you can teleport.")).defaultValue(5.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.mc.player.isUsingItem()) {
            return;
        }
        if (this.mc.options.useKey.isPressed()) {
            HitResult hitResult = this.mc.player.raycast(this.maxDistance.get().doubleValue(), 0.05f, false);
            if (hitResult.getType() == HitResult.Type.ENTITY && this.mc.player.interact(((EntityHitResult)hitResult).getEntity(), Hand.MAIN_HAND) != ActionResult.PASS) {
                return;
            }
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
                Direction side = ((BlockHitResult)hitResult).getSide();
                if (this.mc.world.getBlockState(pos).onUse((World)this.mc.world, (PlayerEntity)this.mc.player, (BlockHitResult)hitResult) != ActionResult.PASS) {
                    return;
                }
                BlockState state = this.mc.world.getBlockState(pos);
                VoxelShape shape = state.getCollisionShape((BlockView)this.mc.world, pos);
                if (shape.isEmpty()) {
                    shape = state.getOutlineShape((BlockView)this.mc.world, pos);
                }
                double height = shape.isEmpty() ? 1.0 : shape.getMax(Direction.Axis.Y);
                this.mc.player.setPosition((double)pos.getX() + 0.5 + (double)side.getOffsetX(), (double)pos.getY() + height, (double)pos.getZ() + 0.5 + (double)side.getOffsetZ());
            }
        }
    }
}

