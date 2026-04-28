/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerInteractionManager
 *  net.minecraft.util.math.BlockPos
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientPlayerInteractionManager.class})
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor(value="currentBreakingProgress")
    public float getBreakingProgress();

    @Accessor(value="currentBreakingProgress")
    public void setCurrentBreakingProgress(float var1);

    @Accessor(value="currentBreakingPos")
    public BlockPos getCurrentBreakingBlockPos();
}

