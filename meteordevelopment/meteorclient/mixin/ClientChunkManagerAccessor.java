/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.world.ClientChunkManager
 *  net.minecraft.client.world.ClientChunkManager$ClientChunkMap
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientChunkManager.class})
public interface ClientChunkManagerAccessor {
    @Accessor(value="chunks")
    public ClientChunkManager.ClientChunkMap getChunks();
}

