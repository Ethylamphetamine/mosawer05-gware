/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.render.VertexConsumer
 *  net.minecraft.client.render.block.FluidRenderer
 *  net.minecraft.fluid.FluidState
 *  net.minecraft.registry.tag.FluidTags
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.BlockRenderView
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FluidRenderer.class})
public abstract class FluidRendererMixin {
    @Unique
    private final ThreadLocal<Integer> alphas = new ThreadLocal();

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRender(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo info) {
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customLavaColor.get().booleanValue() && fluidState.isIn(FluidTags.LAVA)) {
            this.alphas.set(-2);
        } else {
            int alpha = Xray.getAlpha(fluidState.getBlockState(), pos);
            if (alpha == 0) {
                info.cancel();
            } else {
                this.alphas.set(alpha);
            }
        }
    }

    @Inject(method={"vertex"}, at={@At(value="HEAD")}, cancellable=true)
    private void onVertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, CallbackInfo info) {
        int alpha = this.alphas.get();
        if (alpha == -2) {
            Color color = Modules.get().get(Ambience.class).lavaColor.get();
            this.vertex(vertexConsumer, x, y, z, color.r, color.g, color.b, color.a, u, v, light);
            info.cancel();
        } else if (alpha != -1) {
            this.vertex(vertexConsumer, x, y, z, (int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f), alpha, u, v, light);
            info.cancel();
        }
    }

    @Unique
    private void vertex(VertexConsumer vertexConsumer, float x, float y, float z, int red, int green, int blue, int alpha, float u, float v, int light) {
        vertexConsumer.vertex(x, y, z).color(red, green, blue, alpha).texture(u, v).light(light).normal(0.0f, 1.0f, 0.0f);
    }
}

