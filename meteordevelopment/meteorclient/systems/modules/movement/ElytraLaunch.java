/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

public class ElytraLaunch
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> activationMode;
    private final Setting<Keybind> launchBind;
    private final Setting<Boolean> fireFirstRocket;
    private final Setting<Boolean> autoFireRockets;
    private final Setting<Integer> rocketDelay;
    private final Setting<Boolean> bounce;
    private final Setting<Boolean> keepElytraOn;
    private int spacePressTimer;
    private int globalRocketTimer;
    private boolean wasSpacePressed;
    private boolean wasElytraEquipped;
    private int equipDelay;
    private boolean isLaunching;
    private boolean hasFiredFirstRocket;
    private boolean waitingForConfirmation;
    private int expectedRocketCount;
    private int confirmationTimeout;

    public ElytraLaunch() {
        super(Categories.Movement, "elytra-launch", "Quickly equips an elytra and fires a rocket.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.activationMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("activation-mode")).description("How to activate the launch.")).defaultValue(Mode.DoubleTap)).build());
        this.launchBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("launch-bind")).description("Key to hold for launching.")).defaultValue(Keybind.none())).visible(() -> this.activationMode.get() != Mode.DoubleTap)).build());
        this.fireFirstRocket = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fire-first-rocket")).description("Fires a rocket immediately when the launch starts.")).defaultValue(true)).build());
        this.autoFireRockets = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-fire-rockets")).description("Automatically continues firing rockets while flying.")).defaultValue(true)).build());
        this.rocketDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rocket-delay")).description("Global delay (in ticks) between firing rockets.")).defaultValue(40)).min(10).sliderMax(100).visible(this.autoFireRockets::get)).build());
        this.bounce = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bounce")).description("Bounce when hitting the ground.")).defaultValue(false)).build());
        this.keepElytraOn = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("keep-elytra-on")).description("If you start the launch with an elytra already equipped, it will not swap back to a chestplate.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.globalRocketTimer = 0;
        this.resetState();
    }

    @Override
    public void onDeactivate() {
        if (this.isLaunching) {
            this.stopLaunch();
        }
        this.resetState();
    }

    private void resetState() {
        this.spacePressTimer = 0;
        this.wasSpacePressed = false;
        this.wasElytraEquipped = false;
        this.equipDelay = 0;
        this.isLaunching = false;
        this.waitingForConfirmation = false;
        this.hasFiredFirstRocket = false;
    }

    /*
     * Enabled aggressive block sorting
     */
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        Mode mode;
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (this.mc.currentScreen != null) {
            if (this.isLaunching) {
                this.stopLaunch();
            }
            return;
        }
        if (this.globalRocketTimer > 0) {
            --this.globalRocketTimer;
        }
        if (this.waitingForConfirmation) {
            int currentCount = this.getRocketCount();
            if (currentCount < this.expectedRocketCount || this.mc.player.getAbilities().creativeMode) {
                this.globalRocketTimer = this.rocketDelay.get();
                this.waitingForConfirmation = false;
                this.hasFiredFirstRocket = true;
            } else {
                --this.confirmationTimeout;
                if (this.confirmationTimeout <= 0) {
                    this.waitingForConfirmation = false;
                }
            }
        }
        boolean pressingBind = (mode = this.activationMode.get()) != Mode.DoubleTap && this.launchBind.get().isPressed();
        boolean pressingSpace = this.mc.options.jumpKey.isPressed();
        if (!this.isLaunching) {
            boolean shouldLaunch = false;
            if (pressingBind) {
                shouldLaunch = true;
            } else if (mode != Mode.Keybind) {
                if (this.spacePressTimer > 0) {
                    --this.spacePressTimer;
                }
                if (pressingSpace && !this.wasSpacePressed) {
                    if (this.spacePressTimer > 0) {
                        shouldLaunch = true;
                    } else {
                        this.spacePressTimer = 7;
                    }
                }
            }
            if (shouldLaunch && !this.mc.player.isSubmergedInWater() && !this.mc.player.isTouchingWater() && !this.mc.player.isSwimming()) {
                this.startLaunchSequence();
            }
        } else {
            boolean keepGoing;
            if (mode == Mode.Keybind) {
                keepGoing = pressingBind;
            } else if (mode == Mode.DoubleTap) {
                keepGoing = pressingSpace;
            } else {
                boolean bl = keepGoing = pressingBind || pressingSpace;
            }
            if (!keepGoing) {
                this.stopLaunch();
                this.wasSpacePressed = pressingSpace;
                return;
            }
            if (this.mc.player.isOnGround()) {
                if (!pressingBind && !this.bounce.get().booleanValue()) {
                    this.stopLaunch();
                    this.wasSpacePressed = pressingSpace;
                    return;
                }
                this.mc.player.jump();
                this.equipDelay = 0;
            } else if (this.equipDelay > 0) {
                --this.equipDelay;
            } else {
                if (!this.mc.player.isFallFlying()) {
                    this.mc.player.startFallFlying();
                    this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
                if (this.mc.player.isFallFlying()) {
                    boolean readyToFire = false;
                    if (!this.hasFiredFirstRocket) {
                        if (this.fireFirstRocket.get().booleanValue()) {
                            readyToFire = true;
                        } else {
                            this.hasFiredFirstRocket = true;
                        }
                    } else if (this.autoFireRockets.get().booleanValue()) {
                        readyToFire = true;
                    }
                    if (readyToFire && this.globalRocketTimer <= 0 && !this.waitingForConfirmation) {
                        this.attemptRocketFireWithConfirmation();
                    }
                }
            }
        }
        this.wasSpacePressed = pressingSpace;
    }

    private void attemptRocketFireWithConfirmation() {
        int startCount = this.getRocketCount();
        if (this.fireRocket()) {
            this.globalRocketTimer = 4;
            this.waitingForConfirmation = true;
            this.expectedRocketCount = startCount;
            this.confirmationTimeout = 10;
        }
    }

    private int getRocketCount() {
        return InvUtils.find(itemStack -> itemStack.getItem() == Items.FIREWORK_ROCKET && itemStack.get(DataComponentTypes.FIREWORKS) != null).count();
    }

    private void startLaunchSequence() {
        boolean bl = this.wasElytraEquipped = this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        if (!this.wasElytraEquipped) {
            if (!PlayerUtils.silentSwapEquipElytra()) {
                this.warning("No elytra found in inventory.", new Object[0]);
                this.resetState();
                return;
            }
            this.equipDelay = 1;
        } else {
            this.equipDelay = 0;
        }
        this.isLaunching = true;
        this.hasFiredFirstRocket = false;
    }

    private void stopLaunch() {
        if (this.mc.player != null) {
            this.mc.player.stopFallFlying();
            this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
            this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        if (!(this.keepElytraOn.get().booleanValue() && this.wasElytraEquipped || this.mc.player.isSubmergedInWater())) {
            PlayerUtils.silentSwapEquipChestplate();
        }
        this.resetState();
    }

    private boolean fireRocket() {
        Predicate<ItemStack> rocketPredicate = itemStack -> itemStack.getItem() == Items.FIREWORK_ROCKET && itemStack.get(DataComponentTypes.FIREWORKS) != null;
        FindItemResult result = InvUtils.find(rocketPredicate);
        if (!result.found()) {
            return false;
        }
        if (MeteorClient.SWAP.beginSwap(result, true)) {
            PacketManager.INSTANCE.incrementGlobal();
            this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
            MeteorClient.SWAP.endSwap(true);
            PacketManager.INSTANCE.incrementGlobal();
            this.mc.player.playerScreenHandler.syncState();
            return true;
        }
        return false;
    }

    public static enum Mode {
        DoubleTap,
        Keybind,
        Both;

    }
}

