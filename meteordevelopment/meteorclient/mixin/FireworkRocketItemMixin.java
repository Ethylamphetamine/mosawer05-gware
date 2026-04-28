/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.item.FireworkRocketItem
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraFakeFly;
import net.minecraft.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FireworkRocketItem.class})
public class FireworkRocketItemMixin {
    @ModifyExpressionValue(method={"use"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/player/PlayerEntity;isFallFlying()Z")})
    private boolean overrideIsFallFlying(boolean original) {
        ElytraFakeFly fakeFly = Modules.get().get(ElytraFakeFly.class);
        if (fakeFly.isFlying()) {
            return false;
        }
        return original;
    }
}

