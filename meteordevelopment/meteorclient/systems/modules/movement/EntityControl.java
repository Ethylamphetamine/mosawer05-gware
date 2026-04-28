/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.passive.AbstractHorseEntity
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerEntityAccessor;
import meteordevelopment.meteorclient.mixininterface.IHorseBaseEntity;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;

public class EntityControl
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> maxJump;

    public EntityControl() {
        super(Categories.Movement, "entity-control", "Lets you control rideable entities without a saddle.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.maxJump = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("max-jump")).description("Sets jump power to maximum.")).defaultValue(true)).build());
    }

    @Override
    public void onDeactivate() {
        if (!Utils.canUpdate() || this.mc.world.getEntities() == null) {
            return;
        }
        for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof AbstractHorseEntity)) continue;
            ((IHorseBaseEntity)entity).setSaddled(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (Entity entity : this.mc.world.getEntities()) {
            if (!(entity instanceof AbstractHorseEntity)) continue;
            ((IHorseBaseEntity)entity).setSaddled(true);
        }
        if (this.maxJump.get().booleanValue()) {
            ((ClientPlayerEntityAccessor)this.mc.player).setMountJumpStrength(1.0f);
        }
    }
}

