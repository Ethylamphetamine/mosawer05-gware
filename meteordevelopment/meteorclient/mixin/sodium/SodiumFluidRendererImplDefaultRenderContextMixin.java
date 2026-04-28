/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.caffeinemc.mods.sodium.api.util.ColorABGR
 *  net.caffeinemc.mods.sodium.client.model.color.ColorProvider
 *  net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView
 *  net.caffeinemc.mods.sodium.client.world.LevelSlice
 *  net.minecraft.fluid.Fluid
 *  net.minecraft.fluid.FluidState
 *  net.minecraft.registry.tag.FluidTags
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin.sodium;

import java.util.Arrays;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets={"net.caffeinemc.mods.sodium.fabric.render.FluidRendererImpl$DefaultRenderContext"}, remap=false)
public abstract class SodiumFluidRendererImplDefaultRenderContextMixin {
    @Unique
    private Ambience ambience;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        this.ambience = Modules.get().get(Ambience.class);
    }

    @Inject(method={"getColorProvider"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetColorProvider(Fluid fluid, CallbackInfoReturnable<ColorProvider<FluidState>> info) {
        if (this.ambience.isActive() && this.ambience.customLavaColor.get().booleanValue() && fluid.getDefaultState().isIn(FluidTags.LAVA)) {
            info.setReturnValue(this::lavaColorProvider);
        }
    }

    @Unique
    private void lavaColorProvider(LevelSlice level, BlockPos pos, BlockPos.Mutable posMutable, FluidState state, ModelQuadView quads, int[] colors) {
        Color c = this.ambience.lavaColor.get();
        Arrays.fill(colors, ColorABGR.pack((int)c.r, (int)c.g, (int)c.b, (int)c.a));
    }
}

