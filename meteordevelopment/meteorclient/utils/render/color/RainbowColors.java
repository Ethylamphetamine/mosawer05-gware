/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.render.color;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.waypoints.Waypoint;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.meteorclient.utils.render.color.RainbowColor;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

public class RainbowColors {
    private static final List<Setting<SettingColor>> colorSettings = new UnorderedArrayList<Setting<SettingColor>>();
    private static final List<Setting<List<SettingColor>>> colorListSettings = new UnorderedArrayList<Setting<List<SettingColor>>>();
    private static final List<SettingColor> colors = new UnorderedArrayList<SettingColor>();
    private static final List<Runnable> listeners = new UnorderedArrayList<Runnable>();
    public static final RainbowColor GLOBAL = new RainbowColor();

    private RainbowColors() {
    }

    @PostInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(RainbowColors.class);
    }

    public static void addSetting(Setting<SettingColor> setting) {
        colorSettings.add(setting);
    }

    public static void addSettingList(Setting<List<SettingColor>> setting) {
        colorListSettings.add(setting);
    }

    public static void removeSetting(Setting<SettingColor> setting) {
        colorSettings.remove(setting);
    }

    public static void removeSettingList(Setting<List<SettingColor>> setting) {
        colorListSettings.remove(setting);
    }

    public static void add(SettingColor color) {
        colors.add(color);
    }

    public static void register(Runnable runnable) {
        listeners.add(runnable);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        GLOBAL.setSpeed(Config.get().rainbowSpeed.get() / 100.0);
        GLOBAL.getNext();
        for (Setting<SettingColor> setting : colorSettings) {
            if (setting.module != null && !setting.module.isActive()) continue;
            setting.get().update();
        }
        for (Setting<Object> setting : colorListSettings) {
            if (setting.module != null && !setting.module.isActive()) continue;
            for (SettingColor color : (List)setting.get()) {
                color.update();
            }
        }
        for (SettingColor settingColor : colors) {
            settingColor.update();
        }
        for (Waypoint waypoint : Waypoints.get()) {
            waypoint.color.get().update();
        }
        if (MeteorClient.mc.currentScreen instanceof WidgetScreen) {
            for (SettingGroup settingGroup : GuiThemes.get().settings) {
                for (Setting setting : settingGroup) {
                    if (!(setting instanceof ColorSetting)) continue;
                    ((SettingColor)setting.get()).update();
                }
            }
        }
        for (Runnable runnable : listeners) {
            runnable.run();
        }
    }
}

