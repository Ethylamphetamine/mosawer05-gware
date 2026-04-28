/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.enums.CameraSubmersionType
 *  net.minecraft.client.render.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.BlockView
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArgs
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 *  org.spongepowered.asm.mixin.injection.invoke.arg.Args
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ICamera;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.CameraTweaks;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value={Camera.class})
public abstract class CameraMixin
implements ICamera {
    @Shadow
    private boolean thirdPerson;
    @Shadow
    private float yaw;
    @Shadow
    private float pitch;
    @Unique
    private float tickDelta;

    @Shadow
    protected abstract void setRotation(float var1, float var2);

    @Inject(method={"getSubmersionType"}, at={@At(value="HEAD")}, cancellable=true)
    private void getSubmergedFluidState(CallbackInfoReturnable<CameraSubmersionType> ci) {
        if (Modules.get().get(NoRender.class).noLiquidOverlay()) {
            ci.setReturnValue((Object)CameraSubmersionType.NONE);
        }
    }

    @ModifyVariable(method={"clipToSpace"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private float modifyClipToSpace(float d) {
        return Modules.get().get(Freecam.class).isActive() ? 0.0f : (float)Modules.get().get(CameraTweaks.class).getDistance();
    }

    @Inject(method={"clipToSpace"}, at={@At(value="HEAD")}, cancellable=true)
    private void onClipToSpace(float desiredCameraDistance, CallbackInfoReturnable<Float> info) {
        if (Modules.get().get(CameraTweaks.class).clip()) {
            info.setReturnValue((Object)Float.valueOf(desiredCameraDistance));
        }
    }

    @Inject(method={"update"}, at={@At(value="HEAD")})
    private void onUpdateHead(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        this.tickDelta = tickDelta;
    }

    @Inject(method={"update"}, at={@At(value="TAIL")})
    private void onUpdateTail(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (Modules.get().isActive(Freecam.class)) {
            this.thirdPerson = true;
        }
    }

    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void onUpdateSetPosArgs(Args args) {
        Freecam freecam = Modules.get().get(Freecam.class);
        if (freecam.isActive()) {
            args.set(0, (Object)freecam.getX(this.tickDelta));
            args.set(1, (Object)freecam.getY(this.tickDelta));
            args.set(2, (Object)freecam.getZ(this.tickDelta));
        }
    }

    @ModifyArgs(method={"update"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void onUpdateSetRotationArgs(Args args) {
        Freecam freecam = Modules.get().get(Freecam.class);
        FreeLook freeLook = Modules.get().get(FreeLook.class);
        if (freecam.isActive()) {
            args.set(0, (Object)Float.valueOf((float)freecam.getYaw(this.tickDelta)));
            args.set(1, (Object)Float.valueOf((float)freecam.getPitch(this.tickDelta)));
        } else if (Modules.get().isActive(HighwayBuilder.class)) {
            args.set(0, (Object)Float.valueOf(this.yaw));
            args.set(1, (Object)Float.valueOf(this.pitch));
        } else if (freeLook.isActive()) {
            args.set(0, (Object)Float.valueOf(freeLook.cameraYaw));
            args.set(1, (Object)Float.valueOf(freeLook.cameraPitch));
        }
    }

    @Override
    public void setRot(double yaw, double pitch) {
        this.setRotation((float)yaw, (float)MathHelper.clamp((double)pitch, (double)-90.0, (double)90.0));
    }
}

