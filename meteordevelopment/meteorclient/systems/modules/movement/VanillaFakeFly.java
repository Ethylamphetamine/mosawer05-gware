/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class VanillaFakeFly
extends Module {
    private final SettingGroup sgGeneral;

    public VanillaFakeFly() {
        super(Categories.Movement, "vanilla-elytra-fakefly", "Fakes your fly.");
        this.sgGeneral = this.settings.getDefaultGroup();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        PlayerUtils.silentSwapEquipElytra();
        this.mc.player.networkHandler.sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        PlayerUtils.silentSwapEquipChestplate();
    }

    public boolean isFlying() {
        return this.isActive();
    }
}

