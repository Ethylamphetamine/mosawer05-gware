/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.block.PowderSnowBlock
 *  net.minecraft.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Jesus;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={PowderSnowBlock.class})
public abstract class PowderSnowBlockMixin {
    @ModifyReturnValue(method={"canWalkOnPowderSnow"}, at={@At(value="RETURN")})
    private static boolean onCanWalkOnPowderSnow(boolean original, Entity entity) {
        if (entity == MeteorClient.mc.player && Modules.get().get(Jesus.class).canWalkOnPowderSnow()) {
            return true;
        }
        return original;
    }
}

