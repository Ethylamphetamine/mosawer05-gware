/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.client.item.CompassAnglePredicateProvider
 *  net.minecraft.client.render.Camera
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={CompassAnglePredicateProvider.class})
public abstract class CompassAnglePredicateProviderMixin {
    @ModifyExpressionValue(method={"getBodyYaw"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;getBodyYaw()F")})
    private float callLivingEntityGetYaw(float original) {
        if (Modules.get().isActive(Freecam.class)) {
            return MeteorClient.mc.gameRenderer.getCamera().getYaw();
        }
        return original;
    }

    @ModifyReturnValue(method={"getAngleTo(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;)D"}, at={@At(value="RETURN")})
    private double modifyGetAngleTo(double original, Entity entity, BlockPos pos) {
        if (Modules.get().isActive(Freecam.class)) {
            Vec3d vec3d = Vec3d.ofCenter((Vec3i)pos);
            Camera camera = MeteorClient.mc.gameRenderer.getCamera();
            return Math.atan2(vec3d.getZ() - camera.getPos().z, vec3d.getX() - camera.getPos().x) / 6.2831854820251465;
        }
        return original;
    }
}

