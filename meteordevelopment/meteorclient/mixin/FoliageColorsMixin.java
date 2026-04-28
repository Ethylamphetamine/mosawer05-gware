/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.biome.FoliageColors
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import net.minecraft.world.biome.FoliageColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={FoliageColors.class})
public abstract class FoliageColorsMixin {
    @ModifyReturnValue(method={"getBirchColor"}, at={@At(value="RETURN")})
    private static int onGetBirchColor(int original) {
        return FoliageColorsMixin.getModifiedColor(original);
    }

    @ModifyReturnValue(method={"getSpruceColor"}, at={@At(value="RETURN")})
    private static int onGetSpruceColor(int original) {
        return FoliageColorsMixin.getModifiedColor(original);
    }

    @ModifyReturnValue(method={"getMangroveColor"}, at={@At(value="RETURN")})
    private static int onGetMangroveColor(int original) {
        return FoliageColorsMixin.getModifiedColor(original);
    }

    @Unique
    private static int getModifiedColor(int original) {
        if (Modules.get() == null) {
            return original;
        }
        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFoliageColor.get().booleanValue()) {
            return ambience.foliageColor.get().getPacked();
        }
        return original;
    }
}

