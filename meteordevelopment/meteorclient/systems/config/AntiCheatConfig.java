/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 */
package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class AntiCheatConfig
extends System<AntiCheatConfig> {
    public final Settings settings = new Settings();
    private final SettingGroup sgRotations = this.settings.createGroup("Rotations");
    private final SettingGroup sgBlockPlacement = this.settings.createGroup("Block Placement");
    private final SettingGroup sgSwap = this.settings.createGroup("Swap");
    private final SettingGroup sgPacketLimiter = this.settings.createGroup("Packet Limiter");
    private final SettingGroup sgMining = this.settings.createGroup("Mining");
    public final Setting<Boolean> tickSync = this.sgRotations.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tick-sync")).description("Rotate every tick.")).defaultValue(true)).build());
    public final Setting<Boolean> grimSync = this.sgRotations.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-sync")).description("Send full movement packet every tick.")).defaultValue(false)).visible(this.tickSync::get)).build());
    public final Setting<Boolean> grimRotation = this.sgRotations.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-rotation")).description("Send full movement packet when look changes.")).defaultValue(true)).visible(this.tickSync::get)).build());
    public final Setting<Boolean> grimSnapRotation = this.sgRotations.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-snap-rotation")).description("Send full movement packet when snapping rotation.")).defaultValue(true)).build());
    public final Setting<Boolean> blockRotatePlace = this.sgBlockPlacement.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("block-rotate-place")).description("Rotate to place blocks.")).defaultValue(false)).build());
    public final Setting<Boolean> blockPlaceAirPlace = this.sgBlockPlacement.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-air-place")).description("Allow air placing blocks.")).defaultValue(true)).build());
    public final Setting<Boolean> forceAirPlace = this.sgBlockPlacement.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("force-air-place")).description("Only air place blocks.")).defaultValue(true)).visible(this.blockPlaceAirPlace::get)).build());
    public final Setting<Double> blockPlacePerBlockCooldown = this.sgBlockPlacement.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("block-place-cooldown")).description("Cooldown before retrying same position.")).defaultValue(0.05).min(0.0).sliderMax(0.3).build());
    public final Setting<Integer> blocksPerPacketLimit = this.sgBlockPlacement.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("blocks-per-packet-limit")).description("Max blocks per blockpacketlimit timer.")).defaultValue(9)).min(0).sliderMax(9).build());
    public final Setting<Integer> blockPacketLimit = this.sgBlockPlacement.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("block-packet-limit")).description("Milliseconds to wait after rate limit.")).defaultValue(300)).min(300).sliderMax(350).build());
    public final Setting<Boolean> swapAntiScreenClose = this.sgSwap.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-screen-close")).description("Pause swapping when screens are open.")).defaultValue(true)).build());
    public final Setting<SwapManager.SwapMode> swapMode = this.sgSwap.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("item-swap-mode")).description("How to swap items.")).defaultValue(SwapManager.SwapMode.Auto)).build());
    public final Setting<Boolean> packetLimiter = this.sgPacketLimiter.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("packet-limiter")).description("Enables the packet limiter.")).defaultValue(true)).build());
    public final Setting<Integer> clickLimiter = this.sgPacketLimiter.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("click-limit")).description("Limit for clicking in the inventory.")).defaultValue(79)).min(0).sliderMax(150).visible(this.packetLimiter::get)).build());
    public final Setting<Integer> interactLimiter = this.sgPacketLimiter.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("interact-limit")).description("Limit for general interactions.")).defaultValue(180)).min(0).sliderMax(300).visible(this.packetLimiter::get)).build());
    public final Setting<Integer> globalLimiter = this.sgPacketLimiter.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("global-limit")).description("Global safety limit for ALL packets.")).defaultValue(1249)).min(0).sliderMax(2000).visible(this.packetLimiter::get)).build());
    public final Setting<Boolean> instantMineBypass = this.sgMining.add(((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("2b2t")).defaultValue(false)).build());

    public AntiCheatConfig() {
        super("anti-cheat-config");
    }

    public static AntiCheatConfig get() {
        return Systems.get(AntiCheatConfig.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", (NbtElement)this.settings.toTag());
        return tag;
    }

    @Override
    public AntiCheatConfig fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            this.settings.fromTag(tag.getCompound("settings"));
        }
        return this;
    }
}

