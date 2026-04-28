/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.particle.ParticleEffect
 */
package meteordevelopment.meteorclient.events.world;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent
extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();
    public ParticleEffect particle;

    public static ParticleEvent get(ParticleEffect particle) {
        INSTANCE.setCancelled(false);
        ParticleEvent.INSTANCE.particle = particle;
        return INSTANCE;
    }
}

