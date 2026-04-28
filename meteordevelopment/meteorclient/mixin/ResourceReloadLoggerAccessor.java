/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resource.ResourceReloadLogger
 *  net.minecraft.client.resource.ResourceReloadLogger$ReloadState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.resource.ResourceReloadLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ResourceReloadLogger.class})
public interface ResourceReloadLoggerAccessor {
    @Accessor(value="reloadState")
    public ResourceReloadLogger.ReloadState getReloadState();
}

