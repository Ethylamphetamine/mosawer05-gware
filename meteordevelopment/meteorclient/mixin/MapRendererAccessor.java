/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.MapRenderer
 *  net.minecraft.client.render.MapRenderer$MapTexture
 *  net.minecraft.component.type.MapIdComponent
 *  net.minecraft.item.map.MapState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.render.MapRenderer;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={MapRenderer.class})
public interface MapRendererAccessor {
    @Invoker(value="getMapTexture")
    public MapRenderer.MapTexture invokeGetMapTexture(MapIdComponent var1, MapState var2);
}

