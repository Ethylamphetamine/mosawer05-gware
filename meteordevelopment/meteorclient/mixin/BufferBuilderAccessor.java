/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.BufferBuilder
 *  net.minecraft.client.render.VertexFormat
 *  net.minecraft.client.util.BufferAllocator
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BufferBuilder.class})
public interface BufferBuilderAccessor {
    @Accessor(value="allocator")
    public BufferAllocator meteor$getAllocator();

    @Accessor(value="format")
    public VertexFormat getVertexFormat();
}

