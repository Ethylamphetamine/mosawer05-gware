/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.sound.SoundSystem
 *  net.minecraft.client.sound.TickableSoundInstance
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.SoundBlocker;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SoundSystem.class})
public abstract class SoundSystemMixin {
    @Shadow
    public abstract void stop(SoundInstance var1);

    @Inject(method={"play(Lnet/minecraft/client/sound/SoundInstance;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onPlay(SoundInstance soundInstance, CallbackInfo info) {
        PlaySoundEvent event = MeteorClient.EVENT_BUS.post(PlaySoundEvent.get(soundInstance));
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"tick()V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/sound/TickableSoundInstance;tick()V", ordinal=0)})
    private void onTick(CallbackInfo ci, @Local TickableSoundInstance tickableSoundInstance) {
        if (Modules.get().get(SoundBlocker.class).shouldBlock((SoundInstance)tickableSoundInstance)) {
            this.stop((SoundInstance)tickableSoundInstance);
        }
    }
}

