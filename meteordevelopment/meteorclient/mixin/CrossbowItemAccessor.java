/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.type.ChargedProjectilesComponent
 *  net.minecraft.item.CrossbowItem
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={CrossbowItem.class})
public interface CrossbowItemAccessor {
    @Invoker(value="getSpeed")
    public static float getSpeed(ChargedProjectilesComponent itemStack) {
        return 0.0f;
    }
}

