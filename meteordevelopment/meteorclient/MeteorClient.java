/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.loader.api.FabricLoader
 *  net.fabricmc.loader.api.ModContainer
 *  net.fabricmc.loader.api.metadata.ModMetadata
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.util.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package meteordevelopment.meteorclient;

import java.io.File;
import java.lang.invoke.MethodHandles;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.managers.BlockPlacementManager;
import meteordevelopment.meteorclient.systems.managers.InformationManager;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.ReflectInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Version;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeteorClient
implements ClientModInitializer {
    public static final String MOD_ID = "meteor-client";
    public static final ModMetadata MOD_META;
    public static final String NAME;
    public static final Version VERSION;
    public static final String DEV_BUILD;
    public static MeteorClient INSTANCE;
    public static MeteorAddon ADDON;
    public static MinecraftClient mc;
    public static final IEventBus EVENT_BUS;
    public static final File FOLDER;
    public static final Logger LOG;
    public static RotationManager ROTATION;
    public static BlockPlacementManager BLOCK;
    public static InformationManager INFO;
    public static SwapManager SWAP;
    private boolean wasWidgetScreen;
    private boolean wasHudHiddenRoot;

    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }
        LOG.info("Initializing {}", (Object)NAME);
        mc = MinecraftClient.getInstance();
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
            Systems.addPreLoadTask(() -> Modules.get().get(DiscordPresence.class).toggle());
        }
        AddonManager.init();
        AddonManager.ADDONS.forEach(addon -> {
            try {
                EVENT_BUS.registerLambdaFactory(addon.getPackage(), (lookupInMethod, klass) -> (MethodHandles.Lookup)lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
            }
            catch (AbstractMethodError e) {
                throw new RuntimeException("Addon \"%s\" is too old and cannot be ran.".formatted(addon.name), e);
            }
        });
        ReflectInit.registerPackages();
        ReflectInit.init(PreInit.class);
        Categories.init();
        Systems.init();
        EVENT_BUS.subscribe(this);
        AddonManager.ADDONS.forEach(MeteorAddon::onInitialize);
        Modules.get().sortModules();
        Systems.load();
        ReflectInit.init(PostInit.class);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));
        ROTATION = new RotationManager();
        BLOCK = new BlockPlacementManager();
        INFO = new InformationManager();
        SWAP = new SwapManager();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (MeteorClient.mc.currentScreen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.wasPressed()) {
            mc.setScreen((Screen)new ChatScreen(Config.get().prefix.get()));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesKey(event.key, 0)) {
            this.toggleGui();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.button)) {
            this.toggleGui();
        }
    }

    private void toggleGui() {
        if (Utils.canCloseGui()) {
            MeteorClient.mc.currentScreen.close();
        } else if (Utils.canOpenGui()) {
            Tabs.get().getFirst().openScreen(GuiThemes.get());
        }
    }

    @EventHandler(priority=-200)
    private void onOpenScreen(OpenScreenEvent event) {
        boolean hideHud = GuiThemes.get().hideHUD();
        if (hideHud) {
            if (!this.wasWidgetScreen) {
                this.wasHudHiddenRoot = MeteorClient.mc.options.hudHidden;
            }
            if (event.screen instanceof WidgetScreen) {
                MeteorClient.mc.options.hudHidden = true;
            } else if (!this.wasHudHiddenRoot) {
                MeteorClient.mc.options.hudHidden = false;
            }
        }
        this.wasWidgetScreen = event.screen instanceof WidgetScreen;
    }

    public static Identifier identifier(String path) {
        return Identifier.of((String)MOD_ID, (String)path);
    }

    static {
        EVENT_BUS = new EventBus();
        FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
        MOD_META = ((ModContainer)FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow()).getMetadata();
        NAME = MOD_META.getName();
        LOG = LoggerFactory.getLogger((String)NAME);
        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) {
            versionString = versionString.split("-")[0];
        }
        if (versionString.equals("${version}")) {
            versionString = "0.0.0";
        }
        VERSION = new Version(versionString);
        DEV_BUILD = MOD_META.getCustomValue("meteor-client:devbuild").getAsString();
    }
}

