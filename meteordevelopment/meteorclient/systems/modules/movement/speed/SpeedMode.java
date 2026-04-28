/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 */
package meteordevelopment.meteorclient.systems.modules.movement.speed;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.speed.Speed;
import meteordevelopment.meteorclient.systems.modules.movement.speed.SpeedModes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SpeedMode {
    protected final MinecraftClient mc;
    protected final Speed settings = Modules.get().get(Speed.class);
    private final SpeedModes type;
    protected int stage;
    protected double distance;
    protected double speed;

    public SpeedMode(SpeedModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
        this.reset();
    }

    public void onTick() {
    }

    public void onMove(PlayerMoveEvent event) {
    }

    public void onRubberband() {
        this.reset();
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }

    protected double getDefaultSpeed() {
        int amplifier;
        double defaultSpeed = 0.2873;
        if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
            amplifier = this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier();
            defaultSpeed *= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
            amplifier = this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier();
            defaultSpeed /= 1.0 + 0.2 * (double)(amplifier + 1);
        }
        return defaultSpeed;
    }

    protected void reset() {
        this.stage = 0;
        this.distance = 0.0;
        this.speed = 0.2873;
    }

    protected double getHop(double height) {
        StatusEffectInstance jumpBoost;
        StatusEffectInstance statusEffectInstance = jumpBoost = this.mc.player.hasStatusEffect(StatusEffects.JUMP_BOOST) ? this.mc.player.getStatusEffect(StatusEffects.JUMP_BOOST) : null;
        if (jumpBoost != null) {
            height += (double)((float)(jumpBoost.getAmplifier() + 1) * 0.1f);
        }
        return height;
    }

    public String getHudString() {
        return this.type.name();
    }
}

