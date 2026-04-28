/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.List;
import java.util.Random;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;

public class AntiAFK
extends Module {
    private final SettingGroup sgActions;
    private final SettingGroup sgMessages;
    private final Setting<Boolean> jump;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> sneak;
    private final Setting<Integer> sneakTime;
    private final Setting<Boolean> strafe;
    private final Setting<Boolean> spin;
    private final Setting<SpinMode> spinMode;
    private final Setting<Integer> spinSpeed;
    private final Setting<Integer> pitch;
    private final Setting<Boolean> sendMessages;
    private final Setting<Boolean> randomMessage;
    private final Setting<Integer> delay;
    private final Setting<List<String>> messages;
    private final Random random;
    private int messageTimer;
    private int messageI;
    private int sneakTimer;
    private int strafeTimer;
    private boolean direction;
    private float prevYaw;

    public AntiAFK() {
        super(Categories.Player, "anti-afk", "Performs different actions to prevent getting kicked while AFK.");
        this.sgActions = this.settings.createGroup("Actions");
        this.sgMessages = this.settings.createGroup("Messages");
        this.jump = this.sgActions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("jump")).description("Jump randomly.")).defaultValue(true)).build());
        this.swing = this.sgActions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing")).description("Swings your hand.")).defaultValue(false)).build());
        this.sneak = this.sgActions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sneak")).description("Sneaks and unsneaks quickly.")).defaultValue(false)).build());
        this.sneakTime = this.sgActions.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("sneak-time")).description("How many ticks to stay sneaked.")).defaultValue(5)).min(1).sliderMin(1).visible(this.sneak::get)).build());
        this.strafe = this.sgActions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("strafe")).description("Strafe right and left.")).defaultValue(false)).onChanged(aBoolean -> {
            this.strafeTimer = 0;
            this.direction = false;
            if (this.isActive()) {
                this.mc.options.leftKey.setPressed(false);
                this.mc.options.rightKey.setPressed(false);
            }
        })).build());
        this.spin = this.sgActions.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("spin")).description("Spins the player in place.")).defaultValue(true)).build());
        this.spinMode = this.sgActions.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("spin-mode")).description("The method of rotating.")).defaultValue(SpinMode.Server)).visible(this.spin::get)).build());
        this.spinSpeed = this.sgActions.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("speed")).description("The speed to spin you.")).defaultValue(7)).visible(this.spin::get)).build());
        this.pitch = this.sgActions.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pitch")).description("The pitch to send to the server.")).defaultValue(0)).range(-90, 90).sliderRange(-90, 90).visible(() -> this.spin.get() != false && this.spinMode.get() == SpinMode.Server)).build());
        this.sendMessages = this.sgMessages.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("send-messages")).description("Sends messages to prevent getting kicked for AFK.")).defaultValue(false)).build());
        this.randomMessage = this.sgMessages.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("random")).description("Selects a random message from your message list.")).defaultValue(false)).visible(this.sendMessages::get)).build());
        this.delay = this.sgMessages.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The delay between specified messages in seconds.")).defaultValue(15)).min(0).sliderMax(30).visible(this.sendMessages::get)).build());
        this.messages = this.sgMessages.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("messages")).description("The messages to choose from.")).defaultValue("Meteor on top!", "Meteor on crack!").visible(this.sendMessages::get)).build());
        this.random = new Random();
        this.messageTimer = 0;
        this.messageI = 0;
        this.sneakTimer = 0;
        this.strafeTimer = 0;
        this.direction = false;
    }

    @Override
    public void onActivate() {
        if (this.sendMessages.get().booleanValue() && this.messages.get().isEmpty()) {
            this.warning("Message list is empty, disabling messages...", new Object[0]);
            this.sendMessages.set(false);
        }
        this.prevYaw = this.mc.player.getYaw();
        this.messageTimer = this.delay.get() * 20;
    }

    @Override
    public void onDeactivate() {
        if (this.strafe.get().booleanValue()) {
            this.mc.options.leftKey.setPressed(false);
            this.mc.options.rightKey.setPressed(false);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) {
            return;
        }
        if (this.jump.get().booleanValue()) {
            if (this.mc.options.jumpKey.isPressed()) {
                this.mc.options.jumpKey.setPressed(false);
            } else if (this.random.nextInt(99) == 0) {
                this.mc.options.jumpKey.setPressed(true);
            }
        }
        if (this.swing.get().booleanValue() && this.random.nextInt(99) == 0) {
            this.mc.player.swingHand(this.mc.player.getActiveHand());
        }
        if (this.sneak.get().booleanValue()) {
            if (this.sneakTimer++ >= this.sneakTime.get()) {
                this.mc.options.sneakKey.setPressed(false);
                if (this.random.nextInt(99) == 0) {
                    this.sneakTimer = 0;
                }
            } else {
                this.mc.options.sneakKey.setPressed(true);
            }
        }
        if (this.strafe.get().booleanValue() && this.strafeTimer-- <= 0) {
            this.mc.options.leftKey.setPressed(!this.direction);
            this.mc.options.rightKey.setPressed(this.direction);
            this.direction = !this.direction;
            this.strafeTimer = 20;
        }
        if (this.spin.get().booleanValue()) {
            this.prevYaw += (float)this.spinSpeed.get().intValue();
            switch (this.spinMode.get().ordinal()) {
                case 1: {
                    this.mc.player.setYaw(this.prevYaw);
                    break;
                }
                case 0: {
                    Rotations.rotate((double)this.prevYaw, (double)this.pitch.get().intValue(), -15);
                }
            }
        }
        if (this.sendMessages.get().booleanValue() && !this.messages.get().isEmpty() && this.messageTimer-- <= 0) {
            if (this.randomMessage.get().booleanValue()) {
                this.messageI = this.random.nextInt(this.messages.get().size());
            } else if (++this.messageI >= this.messages.get().size()) {
                this.messageI = 0;
            }
            ChatUtils.sendPlayerMsg(this.messages.get().get(this.messageI));
            this.messageTimer = this.delay.get() * 20;
        }
    }

    public static enum SpinMode {
        Server,
        Client;

    }
}

