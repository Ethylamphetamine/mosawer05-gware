/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.ClientPlayerEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ClientPlayerEntity.class})
public interface ClientPlayerEntityAccessor {
    @Accessor(value="mountJumpStrength")
    public void setMountJumpStrength(float var1);

    @Accessor(value="ticksSinceLastPositionPacketSent")
    public void setTicksSinceLastPositionPacketSent(int var1);

    @Invoker(value="canSprint")
    public boolean invokeCanSprint();
}

