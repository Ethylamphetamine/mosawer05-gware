/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket$InteractTypeHandler
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={PlayerInteractEntityC2SPacket.class})
public interface PlayerInteractEntityC2SPacketInvoker {
    @Invoker(value="<init>")
    public static PlayerInteractEntityC2SPacket invokeInit(int entityId, boolean playerSneaking, PlayerInteractEntityC2SPacket.InteractTypeHandler type) {
        throw new AssertionError();
    }
}

