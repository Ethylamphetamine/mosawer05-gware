/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 */
package meteordevelopment.meteorclient.systems;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.accounts.Accounts;
import meteordevelopment.meteorclient.systems.config.AntiCheatConfig;
import meteordevelopment.meteorclient.systems.config.AntiCheatPacketHandler;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.macros.Macros;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import meteordevelopment.meteorclient.systems.proxies.Proxies;
import meteordevelopment.meteorclient.systems.rekit.RekitSystem;
import meteordevelopment.meteorclient.systems.waypoints.Waypoints;
import meteordevelopment.orbit.EventHandler;

public class Systems {
    private static final Map<Class<? extends System>, System<?>> systems = new Reference2ReferenceOpenHashMap();
    private static final List<Runnable> preLoadTasks = new ArrayList<Runnable>(1);

    public static void addPreLoadTask(Runnable task) {
        preLoadTasks.add(task);
    }

    public static void init() {
        Config config = new Config();
        System<?> configSystem = Systems.add(config);
        configSystem.init();
        configSystem.load();
        config.settings.registerColorSettings(null);
        Systems.add(new Modules());
        Systems.add(new Macros());
        Systems.add(new Friends());
        Systems.add(new Accounts());
        Systems.add(new Waypoints());
        Systems.add(new Profiles());
        Systems.add(new Proxies());
        Systems.add(new Hud());
        Systems.add(new AntiCheatConfig());
        Systems.add(new RekitSystem());
        MeteorClient.EVENT_BUS.subscribe(AntiCheatPacketHandler.INSTANCE);
        MeteorClient.EVENT_BUS.subscribe(Systems.class);
    }

    private static System<?> add(System<?> system) {
        systems.put(system.getClass(), system);
        MeteorClient.EVENT_BUS.subscribe(system);
        system.init();
        return system;
    }

    @EventHandler
    private static void onGameLeft(GameLeftEvent event) {
        Systems.save();
    }

    public static void save(File folder) {
        long start = java.lang.System.currentTimeMillis();
        MeteorClient.LOG.info("Saving");
        for (System<?> system : systems.values()) {
            system.save(folder);
        }
        MeteorClient.LOG.info("Saved in {} milliseconds.", (Object)(java.lang.System.currentTimeMillis() - start));
    }

    public static void save() {
        Systems.save(null);
    }

    public static void load(File folder) {
        long start = java.lang.System.currentTimeMillis();
        MeteorClient.LOG.info("Loading");
        for (Runnable runnable : preLoadTasks) {
            runnable.run();
        }
        for (System system : systems.values()) {
            system.load(folder);
        }
        MeteorClient.LOG.info("Loaded in {} milliseconds", (Object)(java.lang.System.currentTimeMillis() - start));
    }

    public static void load() {
        Systems.load(null);
    }

    public static <T extends System<?>> T get(Class<T> klass) {
        return (T)systems.get(klass);
    }
}

