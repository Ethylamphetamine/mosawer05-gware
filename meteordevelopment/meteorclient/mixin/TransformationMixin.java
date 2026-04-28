/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.model.json.Transformation
 *  net.minecraft.client.util.math.MatrixStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.ApplyTransformationEvent;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Transformation.class})
public abstract class TransformationMixin {
    @Inject(method={"apply"}, at={@At(value="HEAD")}, cancellable=true)
    private void onApply(boolean leftHanded, MatrixStack matrices, CallbackInfo info) {
        ApplyTransformationEvent event = MeteorClient.EVENT_BUS.post(ApplyTransformationEvent.get((Transformation)this, leftHanded, matrices));
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}

