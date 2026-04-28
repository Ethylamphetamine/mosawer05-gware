/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.registry.Registries
 *  net.minecraft.sound.SoundEvent
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.List;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.SoundEventListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

public class SoundBlocker
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<SoundEvent>> sounds;
    private final Setting<Double> crystalHitVolume;
    private final Setting<Double> crystalVolume;

    public SoundBlocker() {
        super(Categories.Misc, "sound-blocker", "Cancels out selected sounds.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sounds = this.sgGeneral.add(((SoundEventListSetting.Builder)((SoundEventListSetting.Builder)new SoundEventListSetting.Builder().name("sounds")).description("Sounds to block.")).build());
        this.crystalHitVolume = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("crystal-hit-volume")).description("Sets the volume of hitting the crystals")).min(0.0).defaultValue(0.2).sliderMax(1.0).build());
        this.crystalVolume = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("crystal-volume")).description("Sets the volume of the crystals")).min(0.0).defaultValue(0.2).sliderMax(1.0).build());
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        for (SoundEvent sound : this.sounds.get()) {
            if (!sound.getId().equals((Object)event.sound.getId())) continue;
            event.cancel();
            break;
        }
    }

    public boolean shouldBlock(SoundInstance soundInstance) {
        return this.isActive() && this.sounds.get().contains(Setting.parseId(Registries.SOUND_EVENT, soundInstance.getId().getPath()));
    }

    public double getCrystalVolume() {
        return this.crystalVolume.get();
    }

    public double getCrystalHitVolume() {
        return this.crystalHitVolume.get();
    }
}

