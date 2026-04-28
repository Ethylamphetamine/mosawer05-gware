/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class KeyPot
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> keybind;
    private final Setting<Boolean> bypassSwap;
    private final Setting<Boolean> lookDown;
    private boolean wasPressed;

    public KeyPot() {
        super(Categories.Combat, "key-pot", "Throws a splash potion with a keybind.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.keybind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("keybind")).description("The keybind to throw a potion.")).defaultValue(Keybind.none())).build());
        this.bypassSwap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bypass-swap")).description("True = Offhand/Eating-Safe. False = Mainhand/Standard.")).defaultValue(true)).build());
        this.lookDown = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("look-down")).description("Looks down when throwing the potion.")).defaultValue(true)).build());
        this.wasPressed = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.mc.player == null || this.mc.world == null || this.mc.currentScreen != null) {
            return;
        }
        if (!this.keybind.get().isPressed()) {
            this.wasPressed = false;
            return;
        }
        MovementFix.bypassRotationForThisTick = true;
        if (!this.wasPressed && !this.mc.player.getItemCooldownManager().isCoolingDown(Items.SPLASH_POTION)) {
            if (this.lookDown.get().booleanValue()) {
                Rotations.rotate((double)this.mc.player.getYaw(), 90.0, () -> this.throwPot(this.mc.player.getYaw(), 90.0f));
            } else {
                this.throwPot(this.mc.player.getYaw(), this.mc.player.getPitch());
            }
            this.wasPressed = true;
        }
    }

    private void throwPot(float yaw, float pitch) {
        if (this.mc.player == null || this.mc.world == null || this.mc.interactionManager == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        FindItemResult pot = InvUtils.find(Items.SPLASH_POTION);
        if (pot.found()) {
            if (MeteorClient.SWAP.beginSwap(pot, true)) {
                PacketManager.INSTANCE.incrementGlobal();
                this.sendThrowPacket(Hand.MAIN_HAND, yaw, pitch);
                MeteorClient.SWAP.endSwap(true);
                PacketManager.INSTANCE.incrementGlobal();
            }
            this.mc.player.playerScreenHandler.syncState();
        }
    }

    private void sendThrowPacket(Hand hand, float yaw, float pitch) {
        if (this.mc.world == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        int sequence = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch));
    }
}

