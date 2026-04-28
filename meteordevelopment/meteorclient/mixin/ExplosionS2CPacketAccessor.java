/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.s2c.play.ExplosionS2CPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ExplosionS2CPacket.class})
public interface ExplosionS2CPacketAccessor {
    @Accessor(value="playerVelocityX")
    public float getPlayerVelocityX();

    @Accessor(value="playerVelocityX")
    public void setPlayerVelocityX(float var1);

    @Accessor(value="playerVelocityY")
    public float getPlayerVelocityY();

    @Accessor(value="playerVelocityY")
    public void setPlayerVelocityY(float var1);

    @Accessor(value="playerVelocityZ")
    public float getPlayerVelocityZ();

    @Accessor(value="playerVelocityZ")
    public void setPlayerVelocityZ(float var1);
}

