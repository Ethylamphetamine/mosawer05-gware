/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.math.Box
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import com.google.common.collect.Streams;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

public class Parkour
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> edgeDistance;

    public Parkour() {
        super(Categories.Movement, "parkour", "Automatically jumps at the edges of blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.edgeDistance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("edge-distance")).description("How far from the edge should you jump.")).range(0.001, 0.1).defaultValue(0.001).build());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!this.mc.player.isOnGround() || this.mc.options.jumpKey.isPressed()) {
            return;
        }
        if (this.mc.player.isSneaking() || this.mc.options.sneakKey.isPressed()) {
            return;
        }
        Box box = this.mc.player.getBoundingBox();
        Box adjustedBox = box.offset(0.0, -0.5, 0.0).expand(-this.edgeDistance.get().doubleValue(), 0.0, -this.edgeDistance.get().doubleValue());
        Stream blockCollisions = Streams.stream((Iterable)this.mc.world.getBlockCollisions((Entity)this.mc.player, adjustedBox));
        if (blockCollisions.findAny().isPresent()) {
            return;
        }
        this.mc.player.jump();
    }
}

