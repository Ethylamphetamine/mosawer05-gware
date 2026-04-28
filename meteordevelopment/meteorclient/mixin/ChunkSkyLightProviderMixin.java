/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.chunk.light.ChunkSkyLightProvider
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ChunkSkyLightProvider.class})
public abstract class ChunkSkyLightProviderMixin {
    @Inject(at={@At(value="HEAD")}, method={"method_51531"}, cancellable=true)
    private void recalculateLevel(long blockPos, long l, int lightLevel, CallbackInfo ci) {
        if (Modules.get().get(NoRender.class).noSkylightUpdates()) {
            ci.cancel();
        }
    }
}

