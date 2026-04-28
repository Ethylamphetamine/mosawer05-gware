/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.texture.PlayerSkinProvider
 *  net.minecraft.client.texture.PlayerSkinProvider$FileCache
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.texture.PlayerSkinProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={PlayerSkinProvider.class})
public interface PlayerSkinProviderAccessor {
    @Accessor(value="skinCache")
    public PlayerSkinProvider.FileCache getSkinCache();
}

