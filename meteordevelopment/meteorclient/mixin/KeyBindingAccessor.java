/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Key
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import java.util.Map;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={KeyBinding.class})
public interface KeyBindingAccessor {
    @Accessor(value="CATEGORY_ORDER_MAP")
    public static Map<String, Integer> getCategoryOrderMap() {
        return null;
    }

    @Accessor(value="boundKey")
    public InputUtil.Key getKey();

    @Accessor(value="timesPressed")
    public int meteor$getTimesPressed();

    @Accessor(value="timesPressed")
    public void meteor$setTimesPressed(int var1);
}

