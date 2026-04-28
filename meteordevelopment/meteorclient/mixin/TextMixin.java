/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={Text.class})
public interface TextMixin
extends IText {
    @Override
    default public void meteor$invalidateCache() {
    }
}

