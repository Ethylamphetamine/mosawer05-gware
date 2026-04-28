/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.particle.ParticleEffect
 *  net.minecraft.particle.ParticleType
 *  net.minecraft.particle.ParticleTypes
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ParticleTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

public class Trail
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<ParticleType<?>>> particles;
    private final Setting<Boolean> pause;

    public Trail() {
        super(Categories.Render, "trail", "Renders a customizable trail behind your player.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.particles = this.sgGeneral.add(((ParticleTypeListSetting.Builder)((ParticleTypeListSetting.Builder)new ParticleTypeListSetting.Builder().name("particles")).description("Particles to draw.")).defaultValue(new ParticleType[]{ParticleTypes.DRIPPING_OBSIDIAN_TEAR, ParticleTypes.CAMPFIRE_COSY_SMOKE}).build());
        this.pause = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-when-stationary")).description("Whether or not to add particles when you are not moving.")).defaultValue(true)).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.pause.get().booleanValue() && this.mc.player.getX() == this.mc.player.prevX && this.mc.player.getY() == this.mc.player.prevY && this.mc.player.getZ() == this.mc.player.prevZ) {
            return;
        }
        for (ParticleType<?> particleType : this.particles.get()) {
            this.mc.world.addParticle((ParticleEffect)particleType, this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ(), 0.0, 0.0, 0.0);
        }
    }
}

