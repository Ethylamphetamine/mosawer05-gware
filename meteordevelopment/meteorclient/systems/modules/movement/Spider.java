/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Spider
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> speed;

    public Spider() {
        super(Categories.Movement, "spider", "Allows you to climb walls like a spider.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("climb-speed")).description("The speed you go up blocks.")).defaultValue(0.2).min(0.0).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.mc.player.horizontalCollision) {
            return;
        }
        Vec3d velocity = this.mc.player.getVelocity();
        if (velocity.y >= 0.2) {
            return;
        }
        this.mc.player.setVelocity(velocity.x, this.speed.get().doubleValue(), velocity.z);
    }
}

