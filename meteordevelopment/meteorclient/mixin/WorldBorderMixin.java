/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.border.WorldBorder
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Collisions;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={WorldBorder.class})
public abstract class WorldBorderMixin {
    @Inject(method={"canCollide"}, at={@At(value="HEAD")}, cancellable=true)
    private void canCollide(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(Collisions.class).ignoreBorder()) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"contains(Lnet/minecraft/util/math/BlockPos;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void contains(CallbackInfoReturnable<Boolean> info) {
        if (Modules.get().get(Collisions.class).ignoreBorder()) {
            info.setReturnValue((Object)true);
        }
    }
}

