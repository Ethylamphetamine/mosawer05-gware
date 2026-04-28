/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.DisconnectedScreen
 *  org.apache.commons.lang3.RandomStringUtils
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.List;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import org.apache.commons.lang3.RandomStringUtils;

public class Spam
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<List<String>> messages;
    private final Setting<Integer> delay;
    private final Setting<Boolean> disableOnLeave;
    private final Setting<Boolean> disableOnDisconnect;
    private final Setting<Boolean> random;
    private final Setting<Boolean> autoSplitMessages;
    private final Setting<Integer> splitLength;
    private final Setting<Integer> autoSplitDelay;
    private final Setting<Boolean> bypass;
    private final Setting<Boolean> uppercase;
    private final Setting<Integer> length;
    private int messageI;
    private int timer;
    private int splitNum;
    private String text;

    public Spam() {
        super(Categories.Misc, "spam", "Spams specified messages in chat.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.messages = this.sgGeneral.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("messages")).description("Messages to use for spam.")).defaultValue(List.of("Meteor on Crack!"))).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The delay between specified messages in ticks.")).defaultValue(20)).min(0).sliderMax(200).build());
        this.disableOnLeave = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-on-leave")).description("Disables spam when you leave a server.")).defaultValue(true)).build());
        this.disableOnDisconnect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("disable-on-disconnect")).description("Disables spam when you are disconnected from a server.")).defaultValue(true)).build());
        this.random = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("randomise")).description("Selects a random message from your spam message list.")).defaultValue(false)).build());
        this.autoSplitMessages = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-split-messages")).description("Automatically split up large messages after a certain length")).defaultValue(false)).build());
        this.splitLength = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("split-length")).description("The length after which to split messages in chat")).visible(this.autoSplitMessages::get)).defaultValue(256)).min(1).sliderMax(256).build());
        this.autoSplitDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("split-delay")).description("The delay between split messages in ticks.")).visible(this.autoSplitMessages::get)).defaultValue(20)).min(0).sliderMax(200).build());
        this.bypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("bypass")).description("Add random text at the end of the message to try to bypass anti spams.")).defaultValue(false)).build());
        this.uppercase = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("include-uppercase-characters")).description("Whether the bypass text should include uppercase characters.")).visible(this.bypass::get)).defaultValue(true)).build());
        this.length = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("length")).description("Number of characters used to bypass anti spam.")).visible(this.bypass::get)).defaultValue(16)).sliderRange(1, 256).build());
    }

    @Override
    public void onActivate() {
        this.timer = this.delay.get();
        this.messageI = 0;
        this.splitNum = 0;
    }

    @EventHandler
    private void onScreenOpen(OpenScreenEvent event) {
        if (this.disableOnDisconnect.get().booleanValue() && event.screen instanceof DisconnectedScreen) {
            this.toggle();
        }
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (this.disableOnLeave.get().booleanValue()) {
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.messages.get().isEmpty()) {
            return;
        }
        if (this.timer <= 0) {
            if (this.text == null) {
                int i;
                if (this.random.get().booleanValue()) {
                    i = Utils.random(0, this.messages.get().size());
                } else {
                    if (this.messageI >= this.messages.get().size()) {
                        this.messageI = 0;
                    }
                    i = this.messageI++;
                }
                this.text = this.messages.get().get(i);
                if (this.bypass.get().booleanValue()) {
                    String bypass = RandomStringUtils.randomAlphabetic((int)this.length.get());
                    if (!this.uppercase.get().booleanValue()) {
                        bypass = bypass.toLowerCase();
                    }
                    this.text = this.text + " " + bypass;
                }
            }
            if (this.autoSplitMessages.get().booleanValue() && this.text.length() > this.splitLength.get()) {
                double length = this.text.length();
                int splits = (int)Math.ceil(length / (double)this.splitLength.get().intValue());
                int start = this.splitNum * this.splitLength.get();
                int end = Math.min(start + this.splitLength.get(), this.text.length());
                ChatUtils.sendPlayerMsg(this.text.substring(start, end));
                ++this.splitNum;
                this.splitNum %= splits;
                this.timer = this.autoSplitDelay.get();
                if (this.splitNum == 0) {
                    this.timer = this.delay.get();
                    this.text = null;
                }
            } else {
                if (this.text.length() > 256) {
                    this.text = this.text.substring(0, 256);
                }
                ChatUtils.sendPlayerMsg(this.text);
                this.timer = this.delay.get();
                this.text = null;
            }
        } else {
            --this.timer;
        }
    }
}

