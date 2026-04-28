/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 */
package meteordevelopment.meteorclient.systems.config;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.FontFaceSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class Config
extends System<Config> {
    public final Settings settings = new Settings();
    private final SettingGroup sgVisual = this.settings.createGroup("Visual");
    private final SettingGroup sgChat = this.settings.createGroup("Chat");
    private final SettingGroup sgMisc = this.settings.createGroup("Misc");
    public final Setting<Boolean> customFont = this.sgVisual.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-font")).description("Use a custom font.")).defaultValue(true)).build());
    public final Setting<FontFace> font = this.sgVisual.add(((FontFaceSetting.Builder)((FontFaceSetting.Builder)((FontFaceSetting.Builder)((FontFaceSetting.Builder)new FontFaceSetting.Builder().name("font")).description("Custom font to use.")).visible(this.customFont::get)).onChanged(Fonts::load)).build());
    public final Setting<Double> rainbowSpeed = this.sgVisual.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("rainbow-speed")).description("The global rainbow speed.")).defaultValue(0.5).range(0.0, 10.0).sliderMax(5.0).build());
    public final Setting<Boolean> titleScreenCredits = this.sgVisual.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("title-screen-credits")).description("Show Meteor credits on title screen")).defaultValue(true)).build());
    public final Setting<Boolean> titleScreenSplashes = this.sgVisual.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("title-screen-splashes")).description("Show Meteor splash texts on title screen")).defaultValue(true)).build());
    public final Setting<Boolean> customWindowTitle = this.sgVisual.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("custom-window-title")).description("Show custom text in the window title.")).defaultValue(false)).onModuleActivated(setting -> MeteorClient.mc.updateWindowTitle())).onChanged(value -> MeteorClient.mc.updateWindowTitle())).build());
    public final Setting<String> customWindowTitleText = this.sgVisual.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("window-title-text")).description("The text it displays in the window title.")).visible(this.customWindowTitle::get)).defaultValue("Minecraft {mc_version} - {meteor.name} {meteor.version}")).onChanged(value -> MeteorClient.mc.updateWindowTitle())).build());
    public final Setting<SettingColor> friendColor = this.sgVisual.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("friend-color")).description("The color used to show friends.")).defaultValue(new SettingColor(0, 255, 180)).build());
    public final Setting<SettingColor> enemyColor = this.sgVisual.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("enemy-color")).description("The color used to show enemy.")).defaultValue(new SettingColor(240, 10, 10)).build());
    public final Setting<String> prefix = this.sgChat.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("prefix")).description("Prefix.")).defaultValue(".")).build());
    public final Setting<Boolean> chatFeedback = this.sgChat.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-feedback")).description("Sends chat feedback when meteor performs certain actions.")).defaultValue(true)).build());
    public final Setting<Boolean> deleteChatFeedback = this.sgChat.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("delete-chat-feedback")).description("Delete previous matching chat feedback to keep chat clear.")).visible(this.chatFeedback::get)).defaultValue(true)).build());
    public final Setting<Integer> rotationHoldTicks = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("rotation-hold")).description("Hold long to hold server side rotation when not sending any packets.")).defaultValue(4)).build());
    public final Setting<Boolean> useTeamColor = this.sgMisc.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-team-color")).description("Uses player's team color for rendering things like esp and tracers.")).defaultValue(true)).build());
    public final Setting<Integer> moduleSearchCount = this.sgMisc.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("module-search-count")).description("Amount of modules and settings to be shown in the module search bar.")).defaultValue(8)).min(1).sliderMax(12).build());
    public final Setting<Boolean> moduleAliases = this.sgMisc.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("search-module-aliases")).description("Whether or not module aliases will be used in the module search bar.")).defaultValue(true)).build());
    public List<String> dontShowAgainPrompts = new ArrayList<String>();

    public Config() {
        super("config");
    }

    public static Config get() {
        return Systems.get(Config.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", (NbtElement)this.settings.toTag());
        tag.put("dontShowAgainPrompts", (NbtElement)this.listToTag(this.dontShowAgainPrompts));
        return tag;
    }

    @Override
    public Config fromTag(NbtCompound tag) {
        if (tag.contains("settings")) {
            this.settings.fromTag(tag.getCompound("settings"));
        }
        if (tag.contains("dontShowAgainPrompts")) {
            this.dontShowAgainPrompts = this.listFromTag(tag, "dontShowAgainPrompts");
        }
        return this;
    }

    private NbtList listToTag(List<String> list) {
        NbtList nbt = new NbtList();
        for (String item : list) {
            nbt.add((Object)NbtString.of((String)item));
        }
        return nbt;
    }

    private List<String> listFromTag(NbtCompound tag, String key) {
        ArrayList<String> list = new ArrayList<String>();
        for (NbtElement item : tag.getList(key, 8)) {
            list.add(item.asString());
        }
        return list;
    }
}

