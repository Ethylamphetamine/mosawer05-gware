/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.CreditsScreen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.AddServerScreen
 *  net.minecraft.client.gui.screen.multiplayer.ConnectScreen
 *  net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen
 *  net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
 *  net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen
 *  net.minecraft.client.gui.screen.option.ChatOptionsScreen
 *  net.minecraft.client.gui.screen.option.ControlsOptionsScreen
 *  net.minecraft.client.gui.screen.option.LanguageOptionsScreen
 *  net.minecraft.client.gui.screen.option.OptionsScreen
 *  net.minecraft.client.gui.screen.option.SkinOptionsScreen
 *  net.minecraft.client.gui.screen.option.SoundOptionsScreen
 *  net.minecraft.client.gui.screen.option.VideoOptionsScreen
 *  net.minecraft.client.gui.screen.pack.PackScreen
 *  net.minecraft.client.gui.screen.world.CreateWorldScreen
 *  net.minecraft.client.gui.screen.world.EditGameRulesScreen
 *  net.minecraft.client.gui.screen.world.EditWorldScreen
 *  net.minecraft.client.gui.screen.world.LevelLoadingScreen
 *  net.minecraft.client.gui.screen.world.SelectWorldScreen
 *  net.minecraft.client.realms.gui.screen.RealmsScreen
 *  net.minecraft.util.Pair
 *  net.minecraft.util.Util
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.discordipc.RichPresence;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.util.Pair;
import net.minecraft.util.Util;

public class DiscordPresence
extends Module {
    private final SettingGroup sgLine1;
    private final SettingGroup sgLine2;
    private final Setting<List<String>> line1Strings;
    private final Setting<Integer> line1UpdateDelay;
    private final Setting<SelectMode> line1SelectMode;
    private final Setting<List<String>> line2Strings;
    private final Setting<Integer> line2UpdateDelay;
    private final Setting<SelectMode> line2SelectMode;
    private static final RichPresence rpc = new RichPresence();
    private SmallImage currentSmallImage;
    private int ticks;
    private boolean forceUpdate;
    private boolean lastWasInMainMenu;
    private final List<Script> line1Scripts;
    private int line1Ticks;
    private int line1I;
    private final List<Script> line2Scripts;
    private int line2Ticks;
    private int line2I;
    public static final List<Pair<String, String>> customStates = new ArrayList<Pair<String, String>>();

    public DiscordPresence() {
        super(Categories.Misc, "discord-presence", "Displays Meteor as your presence on Discord.");
        this.sgLine1 = this.settings.createGroup("Line 1");
        this.sgLine2 = this.settings.createGroup("Line 2");
        this.line1Strings = this.sgLine1.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("line-1-messages")).description("Messages used for the first line.")).defaultValue("{player}", "{server}").onChanged(strings -> this.recompileLine1())).renderer(StarscriptTextBoxRenderer.class).build());
        this.line1UpdateDelay = this.sgLine1.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("line-1-update-delay")).description("How fast to update the first line in ticks.")).defaultValue(200)).min(10).sliderRange(10, 200).build());
        this.line1SelectMode = this.sgLine1.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("line-1-select-mode")).description("How to select messages for the first line.")).defaultValue(SelectMode.Sequential)).build());
        this.line2Strings = this.sgLine2.add(((StringListSetting.Builder)((StringListSetting.Builder)((StringListSetting.Builder)new StringListSetting.Builder().name("line-2-messages")).description("Messages used for the second line.")).defaultValue("Meteor on Crack!", "{round(server.tps, 1)} TPS", "Playing on {server.difficulty} difficulty.", "{server.player_count} Players online").onChanged(strings -> this.recompileLine2())).renderer(StarscriptTextBoxRenderer.class).build());
        this.line2UpdateDelay = this.sgLine2.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("line-2-update-delay")).description("How fast to update the second line in ticks.")).defaultValue(60)).min(10).sliderRange(10, 200).build());
        this.line2SelectMode = this.sgLine2.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("line-2-select-mode")).description("How to select messages for the second line.")).defaultValue(SelectMode.Sequential)).build());
        this.line1Scripts = new ArrayList<Script>();
        this.line2Scripts = new ArrayList<Script>();
        this.runInMainMenu = true;
    }

    public static void registerCustomState(String packageName, String state) {
        for (Pair<String, String> pair : customStates) {
            if (!((String)pair.getLeft()).equals(packageName)) continue;
            pair.setRight((Object)state);
            return;
        }
        customStates.add((Pair<String, String>)new Pair((Object)packageName, (Object)state));
    }

    public static void unregisterCustomState(String packageName) {
        customStates.removeIf(pair -> ((String)pair.getLeft()).equals(packageName));
    }

    @Override
    public void onActivate() {
        DiscordIPC.start(835240968533049424L, null);
        rpc.setStart(System.currentTimeMillis() / 1000L);
        Object largeText = "%s %s".formatted(MeteorClient.NAME, MeteorClient.VERSION);
        if (!MeteorClient.DEV_BUILD.isEmpty()) {
            largeText = (String)largeText + " Dev Build: " + MeteorClient.DEV_BUILD;
        }
        rpc.setLargeImage("meteor_client", (String)largeText);
        this.currentSmallImage = SmallImage.Snail;
        this.recompileLine1();
        this.recompileLine2();
        this.ticks = 0;
        this.line1Ticks = 0;
        this.line2Ticks = 0;
        this.lastWasInMainMenu = false;
        this.line1I = 0;
        this.line2I = 0;
    }

    @Override
    public void onDeactivate() {
        DiscordIPC.stop();
    }

    private void recompile(List<String> messages, List<Script> scripts) {
        scripts.clear();
        for (String message : messages) {
            Script script = MeteorStarscript.compile(message);
            if (script == null) continue;
            scripts.add(script);
        }
        this.forceUpdate = true;
    }

    private void recompileLine1() {
        this.recompile(this.line1Strings.get(), this.line1Scripts);
    }

    private void recompileLine2() {
        this.recompile(this.line2Strings.get(), this.line2Scripts);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean update = false;
        if (this.ticks >= 200 || this.forceUpdate) {
            this.currentSmallImage = this.currentSmallImage.next();
            this.currentSmallImage.apply();
            update = true;
            this.ticks = 0;
        } else {
            ++this.ticks;
        }
        if (Utils.canUpdate()) {
            String message;
            int i;
            if (this.line1Ticks >= this.line1UpdateDelay.get() || this.forceUpdate) {
                if (!this.line1Scripts.isEmpty()) {
                    i = Utils.random(0, this.line1Scripts.size());
                    if (this.line1SelectMode.get() == SelectMode.Sequential) {
                        if (this.line1I >= this.line1Scripts.size()) {
                            this.line1I = 0;
                        }
                        i = this.line1I++;
                    }
                    if ((message = MeteorStarscript.run(this.line1Scripts.get(i))) != null) {
                        rpc.setDetails(message);
                    }
                }
                update = true;
                this.line1Ticks = 0;
            } else {
                ++this.line1Ticks;
            }
            if (this.line2Ticks >= this.line2UpdateDelay.get() || this.forceUpdate) {
                if (!this.line2Scripts.isEmpty()) {
                    i = Utils.random(0, this.line2Scripts.size());
                    if (this.line2SelectMode.get() == SelectMode.Sequential) {
                        if (this.line2I >= this.line2Scripts.size()) {
                            this.line2I = 0;
                        }
                        i = this.line2I++;
                    }
                    if ((message = MeteorStarscript.run(this.line2Scripts.get(i))) != null) {
                        rpc.setState(message);
                    }
                }
                update = true;
                this.line2Ticks = 0;
            } else {
                ++this.line2Ticks;
            }
        } else if (!this.lastWasInMainMenu) {
            rpc.setDetails(MeteorClient.NAME + " " + String.valueOf(MeteorClient.DEV_BUILD.isEmpty() ? MeteorClient.VERSION : String.valueOf(MeteorClient.VERSION) + " " + MeteorClient.DEV_BUILD));
            if (this.mc.currentScreen instanceof TitleScreen) {
                rpc.setState("Looking at title screen");
            } else if (this.mc.currentScreen instanceof SelectWorldScreen) {
                rpc.setState("Selecting world");
            } else if (this.mc.currentScreen instanceof CreateWorldScreen || this.mc.currentScreen instanceof EditGameRulesScreen) {
                rpc.setState("Creating world");
            } else if (this.mc.currentScreen instanceof EditWorldScreen) {
                rpc.setState("Editing world");
            } else if (this.mc.currentScreen instanceof LevelLoadingScreen) {
                rpc.setState("Loading world");
            } else if (this.mc.currentScreen instanceof MultiplayerScreen) {
                rpc.setState("Selecting server");
            } else if (this.mc.currentScreen instanceof AddServerScreen) {
                rpc.setState("Adding server");
            } else if (this.mc.currentScreen instanceof ConnectScreen || this.mc.currentScreen instanceof DirectConnectScreen) {
                rpc.setState("Connecting to server");
            } else if (this.mc.currentScreen instanceof WidgetScreen) {
                rpc.setState("Browsing Meteor's GUI");
            } else if (this.mc.currentScreen instanceof OptionsScreen || this.mc.currentScreen instanceof SkinOptionsScreen || this.mc.currentScreen instanceof SoundOptionsScreen || this.mc.currentScreen instanceof VideoOptionsScreen || this.mc.currentScreen instanceof ControlsOptionsScreen || this.mc.currentScreen instanceof LanguageOptionsScreen || this.mc.currentScreen instanceof ChatOptionsScreen || this.mc.currentScreen instanceof PackScreen || this.mc.currentScreen instanceof AccessibilityOptionsScreen) {
                rpc.setState("Changing options");
            } else if (this.mc.currentScreen instanceof CreditsScreen) {
                rpc.setState("Reading credits");
            } else if (this.mc.currentScreen instanceof RealmsScreen) {
                rpc.setState("Browsing Realms");
            } else {
                boolean setState = false;
                if (this.mc.currentScreen != null) {
                    String className = this.mc.currentScreen.getClass().getName();
                    for (Pair<String, String> pair : customStates) {
                        if (!className.startsWith((String)pair.getLeft())) continue;
                        rpc.setState((String)pair.getRight());
                        setState = true;
                        break;
                    }
                }
                if (!setState) {
                    rpc.setState("In main menu");
                }
            }
            update = true;
        }
        if (update) {
            DiscordIPC.setActivity(rpc);
        }
        this.forceUpdate = false;
        this.lastWasInMainMenu = !Utils.canUpdate();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) {
            this.lastWasInMainMenu = false;
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WButton help = theme.button("Open documentation.");
        help.action = () -> Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/meteor-client/wiki/Starscript");
        return help;
    }

    static {
        DiscordPresence.registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
        DiscordPresence.registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
    }

    public static enum SelectMode {
        Random,
        Sequential;

    }

    private static enum SmallImage {
        MineGame("minegame", "MineGame159"),
        Snail("seasnail", "seasnail8169");

        private final String key;
        private final String text;

        private SmallImage(String key, String text) {
            this.key = key;
            this.text = text;
        }

        void apply() {
            rpc.setSmallImage(this.key, this.text);
        }

        SmallImage next() {
            if (this == MineGame) {
                return Snail;
            }
            return MineGame;
        }
    }
}

