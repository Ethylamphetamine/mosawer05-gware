/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.block.AbstractBlock
 *  net.minecraft.block.AbstractBlock$Settings
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.world.BlockView
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Slippy;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Block.class})
public abstract class BlockMixin
extends AbstractBlock
implements ItemConvertible {
    public BlockMixin(AbstractBlock.Settings settings) {
        super(settings);
    }

    @ModifyReturnValue(method={"shouldDrawSide"}, at={@At(value="RETURN")})
    private static boolean onShouldDrawSide(boolean original, BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos) {
        Xray xray = Modules.get().get(Xray.class);
        if (xray.isActive()) {
            return xray.modifyDrawSide(state, world, pos, side, original);
        }
        return original;
    }

    @ModifyReturnValue(method={"getSlipperiness"}, at={@At(value="RETURN")})
    public float getSlipperiness(float original) {
        if (Modules.get() == null) {
            return original;
        }
        Slippy slippy = Modules.get().get(Slippy.class);
        Block block = (Block)this;
        if (slippy.isActive() && (slippy.listMode.get() == Slippy.ListMode.Whitelist ? slippy.allowedBlocks.get().contains(block) : !slippy.ignoredBlocks.get().contains(block))) {
            return slippy.friction.get().floatValue();
        }
        if (block == Blocks.SLIME_BLOCK && Modules.get().get(NoSlow.class).slimeBlock()) {
            return 0.6f;
        }
        return original;
    }
}

