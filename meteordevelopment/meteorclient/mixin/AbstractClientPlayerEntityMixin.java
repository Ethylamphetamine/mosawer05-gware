/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.network.PlayerListEntry
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.misc.FakeClientPlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractClientPlayerEntity.class})
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method={"getPlayerListEntry"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetPlayerListEntry(CallbackInfoReturnable<PlayerListEntry> info) {
        if (MeteorClient.mc.getNetworkHandler() == null) {
            info.setReturnValue((Object)FakeClientPlayer.getPlayerListEntry());
        }
    }

    @Inject(method={"isSpectator"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsSpectator(CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.mc.getNetworkHandler() == null) {
            info.setReturnValue((Object)false);
        }
    }

    @Inject(method={"isCreative"}, at={@At(value="HEAD")}, cancellable=true)
    private void onIsCreative(CallbackInfoReturnable<Boolean> info) {
        if (MeteorClient.mc.getNetworkHandler() == null) {
            info.setReturnValue((Object)false);
        }
    }
}

