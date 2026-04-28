/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.tabs;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.builtin.AntiCheatTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.EnemiesTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.FriendsTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.GuiTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.HudTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.MacrosTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.ModulesTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.PathManagerTab;
import meteordevelopment.meteorclient.gui.tabs.builtin.ProfilesTab;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.utils.PreInit;

public class Tabs {
    private static final List<Tab> tabs = new ArrayList<Tab>();

    private Tabs() {
    }

    @PreInit(dependencies={PathManagers.class})
    public static void init() {
        Tabs.add(new ModulesTab());
        Tabs.add(new ConfigTab());
        Tabs.add(new GuiTab());
        Tabs.add(new HudTab());
        Tabs.add(new FriendsTab());
        Tabs.add(new EnemiesTab());
        Tabs.add(new MacrosTab());
        Tabs.add(new ProfilesTab());
        Tabs.add(new AntiCheatTab());
        if (PathManagers.get().getSettings().get().sizeGroups() > 0) {
            Tabs.add(new PathManagerTab());
        }
    }

    public static void add(Tab tab) {
        tabs.add(tab);
    }

    public static List<Tab> get() {
        return tabs;
    }
}

