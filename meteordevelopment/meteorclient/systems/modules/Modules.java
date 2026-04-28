/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.registry.Registry
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.SimpleRegistry
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.registry.entry.RegistryEntry$Reference
 *  net.minecraft.registry.entry.RegistryEntryList$Named
 *  net.minecraft.registry.tag.TagKey
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.random.Random
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.systems.modules;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.ActiveModulesChangedEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.AntiAnvil;
import meteordevelopment.meteorclient.systems.modules.combat.AntiBed;
import meteordevelopment.meteorclient.systems.modules.combat.AntiDigDown;
import meteordevelopment.meteorclient.systems.modules.combat.AntiMace;
import meteordevelopment.meteorclient.systems.modules.combat.ArrowDodge;
import meteordevelopment.meteorclient.systems.modules.combat.AutoAnvil;
import meteordevelopment.meteorclient.systems.modules.combat.AutoArmor;
import meteordevelopment.meteorclient.systems.modules.combat.AutoEXP;
import meteordevelopment.meteorclient.systems.modules.combat.AutoMine;
import meteordevelopment.meteorclient.systems.modules.combat.AutoTrap;
import meteordevelopment.meteorclient.systems.modules.combat.AutoWeapon;
import meteordevelopment.meteorclient.systems.modules.combat.AutoWeb;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.BowAimbot;
import meteordevelopment.meteorclient.systems.modules.combat.BowSpam;
import meteordevelopment.meteorclient.systems.modules.combat.Burrow;
import meteordevelopment.meteorclient.systems.modules.combat.Criticals;
import meteordevelopment.meteorclient.systems.modules.combat.EChestBlocker;
import meteordevelopment.meteorclient.systems.modules.combat.ForceSwim;
import meteordevelopment.meteorclient.systems.modules.combat.Hitboxes;
import meteordevelopment.meteorclient.systems.modules.combat.HoleFiller;
import meteordevelopment.meteorclient.systems.modules.combat.KeyPearl;
import meteordevelopment.meteorclient.systems.modules.combat.KeyPot;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.combat.LogoutTrap;
import meteordevelopment.meteorclient.systems.modules.combat.MaceAura;
import meteordevelopment.meteorclient.systems.modules.combat.Offhand;
import meteordevelopment.meteorclient.systems.modules.combat.PacketDebug;
import meteordevelopment.meteorclient.systems.modules.combat.PearlBlocker;
import meteordevelopment.meteorclient.systems.modules.combat.PearlPhase;
import meteordevelopment.meteorclient.systems.modules.combat.Quiver;
import meteordevelopment.meteorclient.systems.modules.combat.SelfAnvil;
import meteordevelopment.meteorclient.systems.modules.combat.SelfWeb;
import meteordevelopment.meteorclient.systems.modules.combat.SpongeAura;
import meteordevelopment.meteorclient.systems.modules.combat.Surround;
import meteordevelopment.meteorclient.systems.modules.combat.SwordAura;
import meteordevelopment.meteorclient.systems.modules.combat.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.systems.modules.combat.newAutocrystal.NewAutoCrystal;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import meteordevelopment.meteorclient.systems.modules.misc.AutoLog;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import meteordevelopment.meteorclient.systems.modules.misc.AutoRespawn;
import meteordevelopment.meteorclient.systems.modules.misc.BetterBeacons;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.systems.modules.misc.BookBot;
import meteordevelopment.meteorclient.systems.modules.misc.DebugModule;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.systems.modules.misc.FriendNotify;
import meteordevelopment.meteorclient.systems.modules.misc.InventoryTweaks;
import meteordevelopment.meteorclient.systems.modules.misc.KillNotify;
import meteordevelopment.meteorclient.systems.modules.misc.MessageAura;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.systems.modules.misc.Notifier;
import meteordevelopment.meteorclient.systems.modules.misc.PacketCanceller;
import meteordevelopment.meteorclient.systems.modules.misc.PacketSaver;
import meteordevelopment.meteorclient.systems.modules.misc.ServerSpoof;
import meteordevelopment.meteorclient.systems.modules.misc.SoundBlocker;
import meteordevelopment.meteorclient.systems.modules.misc.Spam;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.systems.modules.movement.AirJump;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.systems.modules.movement.AntiAFK;
import meteordevelopment.meteorclient.systems.modules.movement.AntiVoid;
import meteordevelopment.meteorclient.systems.modules.movement.AutoCrawl;
import meteordevelopment.meteorclient.systems.modules.movement.AutoJump;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWalk;
import meteordevelopment.meteorclient.systems.modules.movement.AutoWasp;
import meteordevelopment.meteorclient.systems.modules.movement.Blink;
import meteordevelopment.meteorclient.systems.modules.movement.BoatFly;
import meteordevelopment.meteorclient.systems.modules.movement.ClickTP;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraBoost;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraFakeFly;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraLaunch;
import meteordevelopment.meteorclient.systems.modules.movement.ElytraSpeed;
import meteordevelopment.meteorclient.systems.modules.movement.EntityControl;
import meteordevelopment.meteorclient.systems.modules.movement.EntitySpeed;
import meteordevelopment.meteorclient.systems.modules.movement.FastClimb;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.movement.GUIMove;
import meteordevelopment.meteorclient.systems.modules.movement.GrimDisabler;
import meteordevelopment.meteorclient.systems.modules.movement.HighJump;
import meteordevelopment.meteorclient.systems.modules.movement.Jesus;
import meteordevelopment.meteorclient.systems.modules.movement.LongJump;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.systems.modules.movement.NoFall;
import meteordevelopment.meteorclient.systems.modules.movement.NoJumpDelay;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.movement.Parkour;
import meteordevelopment.meteorclient.systems.modules.movement.ReverseStep;
import meteordevelopment.meteorclient.systems.modules.movement.SafeWalk;
import meteordevelopment.meteorclient.systems.modules.movement.Scaffold;
import meteordevelopment.meteorclient.systems.modules.movement.Slippy;
import meteordevelopment.meteorclient.systems.modules.movement.Sneak;
import meteordevelopment.meteorclient.systems.modules.movement.Spider;
import meteordevelopment.meteorclient.systems.modules.movement.Sprint;
import meteordevelopment.meteorclient.systems.modules.movement.Step;
import meteordevelopment.meteorclient.systems.modules.movement.TridentBoost;
import meteordevelopment.meteorclient.systems.modules.movement.Velocity;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.systems.modules.movement.speed.Speed;
import meteordevelopment.meteorclient.systems.modules.player.AntiHunger;
import meteordevelopment.meteorclient.systems.modules.player.AutoClicker;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoFish;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.systems.modules.player.AutoMend;
import meteordevelopment.meteorclient.systems.modules.player.AutoRekit;
import meteordevelopment.meteorclient.systems.modules.player.AutoReplenish;
import meteordevelopment.meteorclient.systems.modules.player.AutoTool;
import meteordevelopment.meteorclient.systems.modules.player.BreakDelay;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.player.EXPThrower;
import meteordevelopment.meteorclient.systems.modules.player.FakePlayer;
import meteordevelopment.meteorclient.systems.modules.player.FastUse;
import meteordevelopment.meteorclient.systems.modules.player.GhostHand;
import meteordevelopment.meteorclient.systems.modules.player.InstantRebreak;
import meteordevelopment.meteorclient.systems.modules.player.LiquidInteract;
import meteordevelopment.meteorclient.systems.modules.player.MiddleClickExtra;
import meteordevelopment.meteorclient.systems.modules.player.Multitask;
import meteordevelopment.meteorclient.systems.modules.player.NoInteract;
import meteordevelopment.meteorclient.systems.modules.player.NoMiningTrace;
import meteordevelopment.meteorclient.systems.modules.player.NoRotate;
import meteordevelopment.meteorclient.systems.modules.player.OffhandCrash;
import meteordevelopment.meteorclient.systems.modules.player.Portals;
import meteordevelopment.meteorclient.systems.modules.player.PotionSaver;
import meteordevelopment.meteorclient.systems.modules.player.PotionSpoof;
import meteordevelopment.meteorclient.systems.modules.player.Reach;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.player.SilentMine;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import meteordevelopment.meteorclient.systems.modules.render.BetterTab;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.render.BlockSelection;
import meteordevelopment.meteorclient.systems.modules.render.Blur;
import meteordevelopment.meteorclient.systems.modules.render.BossStack;
import meteordevelopment.meteorclient.systems.modules.render.Breadcrumbs;
import meteordevelopment.meteorclient.systems.modules.render.BreakIndicators;
import meteordevelopment.meteorclient.systems.modules.render.CameraTweaks;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.systems.modules.render.CityESP;
import meteordevelopment.meteorclient.systems.modules.render.ESP;
import meteordevelopment.meteorclient.systems.modules.render.EntityOwner;
import meteordevelopment.meteorclient.systems.modules.render.FakeItem;
import meteordevelopment.meteorclient.systems.modules.render.FreeLook;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.systems.modules.render.Fullbright;
import meteordevelopment.meteorclient.systems.modules.render.HandView;
import meteordevelopment.meteorclient.systems.modules.render.HoleESP;
import meteordevelopment.meteorclient.systems.modules.render.ItemHighlight;
import meteordevelopment.meteorclient.systems.modules.render.ItemPhysics;
import meteordevelopment.meteorclient.systems.modules.render.LightOverlay;
import meteordevelopment.meteorclient.systems.modules.render.LogoutSpots;
import meteordevelopment.meteorclient.systems.modules.render.Nametags;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.render.PhaseESP;
import meteordevelopment.meteorclient.systems.modules.render.PopChams;
import meteordevelopment.meteorclient.systems.modules.render.StorageESP;
import meteordevelopment.meteorclient.systems.modules.render.TimeChanger;
import meteordevelopment.meteorclient.systems.modules.render.Tracers;
import meteordevelopment.meteorclient.systems.modules.render.Trail;
import meteordevelopment.meteorclient.systems.modules.render.Trajectories;
import meteordevelopment.meteorclient.systems.modules.render.TunnelESP;
import meteordevelopment.meteorclient.systems.modules.render.UnfocusedCPU;
import meteordevelopment.meteorclient.systems.modules.render.VoidESP;
import meteordevelopment.meteorclient.systems.modules.render.WallHack;
import meteordevelopment.meteorclient.systems.modules.render.WaypointsModule;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.systems.modules.render.Zoom;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.marker.Marker;
import meteordevelopment.meteorclient.systems.modules.world.AirPlace;
import meteordevelopment.meteorclient.systems.modules.world.Ambience;
import meteordevelopment.meteorclient.systems.modules.world.AutoBreed;
import meteordevelopment.meteorclient.systems.modules.world.AutoBrewer;
import meteordevelopment.meteorclient.systems.modules.world.AutoMount;
import meteordevelopment.meteorclient.systems.modules.world.AutoNametag;
import meteordevelopment.meteorclient.systems.modules.world.AutoPortal;
import meteordevelopment.meteorclient.systems.modules.world.AutoShearer;
import meteordevelopment.meteorclient.systems.modules.world.AutoSign;
import meteordevelopment.meteorclient.systems.modules.world.AutoSmelter;
import meteordevelopment.meteorclient.systems.modules.world.BuildHeight;
import meteordevelopment.meteorclient.systems.modules.world.Collisions;
import meteordevelopment.meteorclient.systems.modules.world.EChestFarmer;
import meteordevelopment.meteorclient.systems.modules.world.EndermanLook;
import meteordevelopment.meteorclient.systems.modules.world.Excavator;
import meteordevelopment.meteorclient.systems.modules.world.Filler;
import meteordevelopment.meteorclient.systems.modules.world.Flamethrower;
import meteordevelopment.meteorclient.systems.modules.world.HighwayBuilder;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.systems.modules.world.LiquidFiller;
import meteordevelopment.meteorclient.systems.modules.world.MapAura;
import meteordevelopment.meteorclient.systems.modules.world.MountBypass;
import meteordevelopment.meteorclient.systems.modules.world.NoGhostBlocks;
import meteordevelopment.meteorclient.systems.modules.world.Nuker;
import meteordevelopment.meteorclient.systems.modules.world.SourceFiller;
import meteordevelopment.meteorclient.systems.modules.world.SpawnProofer;
import meteordevelopment.meteorclient.systems.modules.world.StashFinder;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.systems.modules.world.VeinMiner;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.ValueComparableMap;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Modules
extends System<Modules> {
    public static final ModuleRegistry REGISTRY = new ModuleRegistry();
    private static final List<Category> CATEGORIES = new ArrayList<Category>();
    private final List<Module> modules = new ArrayList<Module>();
    private final Map<Class<? extends Module>, Module> moduleInstances = new Reference2ReferenceOpenHashMap();
    private final Map<Category, List<Module>> groups = new Reference2ReferenceOpenHashMap();
    private final List<Module> active = new ArrayList<Module>();
    private Module moduleToBind;
    private boolean awaitingKeyRelease = false;

    public Modules() {
        super("modules");
    }

    public static Modules get() {
        return Systems.get(Modules.class);
    }

    @Override
    public void init() {
        this.initCombat();
        this.initPlayer();
        this.initMovement();
        this.initRender();
        this.initWorld();
        this.initMisc();
    }

    @Override
    public void load(File folder) {
        for (Module module : this.modules) {
            for (SettingGroup group : module.settings) {
                for (Setting<?> setting : group) {
                    setting.reset();
                }
            }
        }
        super.load(folder);
    }

    public void sortModules() {
        for (List<Module> modules : this.groups.values()) {
            modules.sort(Comparator.comparing(o -> o.title));
        }
        this.modules.sort(Comparator.comparing(o -> o.title));
    }

    public static void registerCategory(Category category) {
        if (!Categories.REGISTERING) {
            throw new RuntimeException("Modules.registerCategory - Cannot register category outside of onRegisterCategories callback.");
        }
        CATEGORIES.add(category);
    }

    public static Iterable<Category> loopCategories() {
        return CATEGORIES;
    }

    @Deprecated(forRemoval=true)
    public static Category getCategoryByHash(int hash) {
        for (Category category : CATEGORIES) {
            if (category.hashCode() != hash) continue;
            return category;
        }
        return null;
    }

    public <T extends Module> T get(Class<T> klass) {
        return (T)this.moduleInstances.get(klass);
    }

    public Module get(String name) {
        for (Module module : this.moduleInstances.values()) {
            if (!module.name.equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public boolean isActive(Class<? extends Module> klass) {
        Module module = this.get(klass);
        return module != null && module.isActive();
    }

    public List<Module> getGroup(Category category) {
        return this.groups.computeIfAbsent(category, category1 -> new ArrayList());
    }

    public Collection<Module> getAll() {
        return this.moduleInstances.values();
    }

    public List<Module> getList() {
        return this.modules;
    }

    public int getCount() {
        return this.moduleInstances.values().size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<Module> getActive() {
        List<Module> list = this.active;
        synchronized (list) {
            return this.active;
        }
    }

    public Map<Module, Integer> searchTitles(String text) {
        ValueComparableMap<Module, Integer> modules = new ValueComparableMap<Module, Integer>(Comparator.naturalOrder());
        for (Module module : this.moduleInstances.values()) {
            int score = Utils.searchLevenshteinDefault(module.title, text, false);
            if (Config.get().moduleAliases.get().booleanValue()) {
                for (String alias : module.aliases) {
                    int aliasScore = Utils.searchLevenshteinDefault(alias, text, false);
                    if (aliasScore >= score) continue;
                    score = aliasScore;
                }
            }
            modules.put(module, modules.getOrDefault(module, 0) + score);
        }
        return modules;
    }

    public Set<Module> searchSettingTitles(String text) {
        ValueComparableMap modules = new ValueComparableMap(Comparator.naturalOrder());
        for (Module module : this.moduleInstances.values()) {
            int lowest = Integer.MAX_VALUE;
            for (SettingGroup sg : module.settings) {
                for (Setting<?> setting : sg) {
                    int score = Utils.searchLevenshteinDefault(setting.title, text, false);
                    if (score >= lowest) continue;
                    lowest = score;
                }
            }
            modules.put(module, modules.getOrDefault(module, 0) + lowest);
        }
        return modules.keySet();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void addActive(Module module) {
        List<Module> list = this.active;
        synchronized (list) {
            if (!this.active.contains(module)) {
                this.active.add(module);
                MeteorClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void removeActive(Module module) {
        List<Module> list = this.active;
        synchronized (list) {
            if (this.active.remove(module)) {
                MeteorClient.EVENT_BUS.post(ActiveModulesChangedEvent.get());
            }
        }
    }

    public void setModuleToBind(Module moduleToBind) {
        this.moduleToBind = moduleToBind;
    }

    public void awaitKeyRelease() {
        this.awaitingKeyRelease = true;
    }

    public boolean isBinding() {
        return this.moduleToBind != null;
    }

    @EventHandler(priority=200)
    private void onKeyBinding(KeyEvent event) {
        if (event.action == KeyAction.Release && this.onBinding(true, event.key, event.modifiers)) {
            event.cancel();
        }
    }

    @EventHandler(priority=200)
    private void onButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && this.onBinding(false, event.button, 0)) {
            event.cancel();
        }
    }

    private boolean onBinding(boolean isKey, int value, int modifiers) {
        if (!this.isBinding()) {
            return false;
        }
        if (this.awaitingKeyRelease) {
            if (!isKey || value != 257 && value != 335) {
                return false;
            }
            this.awaitingKeyRelease = false;
            return false;
        }
        if (this.moduleToBind.keybind.canBindTo(isKey, value, modifiers)) {
            this.moduleToBind.keybind.set(isKey, value, modifiers);
            this.moduleToBind.info("Bound to (highlight)%s(default).", this.moduleToBind.keybind);
        } else if (value == 256) {
            this.moduleToBind.keybind.set(Keybind.none());
            this.moduleToBind.info("Removed bind.", new Object[0]);
        } else {
            return false;
        }
        MeteorClient.EVENT_BUS.post(ModuleBindChangedEvent.get(this.moduleToBind));
        this.moduleToBind = null;
        return true;
    }

    @EventHandler(priority=100)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Repeat) {
            return;
        }
        this.onAction(true, event.key, event.modifiers, event.action == KeyAction.Press);
    }

    @EventHandler(priority=100)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Repeat) {
            return;
        }
        this.onAction(false, event.button, 0, event.action == KeyAction.Press);
    }

    private void onAction(boolean isKey, int value, int modifiers, boolean isPress) {
        if (MeteorClient.mc.currentScreen != null || Input.isKeyPressed(292)) {
            return;
        }
        for (Module module : this.moduleInstances.values()) {
            if (!module.keybind.matches(isKey, value, modifiers) || !isPress) continue;
            module.toggle();
            module.sendToggledMsg();
        }
    }

    @EventHandler(priority=201)
    private void onOpenScreen(OpenScreenEvent event) {
        if (!Utils.canUpdate()) {
            return;
        }
        for (Module module : this.moduleInstances.values()) {
            if (!module.toggleOnBindRelease || !module.isActive()) continue;
            module.toggle();
            module.sendToggledMsg();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        List<Module> list = this.active;
        synchronized (list) {
            for (Module module : this.modules) {
                if (!module.isActive() || module.runInMainMenu) continue;
                MeteorClient.EVENT_BUS.subscribe(module);
                module.onActivate();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        List<Module> list = this.active;
        synchronized (list) {
            for (Module module : this.modules) {
                if (!module.isActive() || module.runInMainMenu) continue;
                MeteorClient.EVENT_BUS.unsubscribe(module);
                module.onDeactivate();
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        for (Module module : this.moduleInstances.values()) {
            if (!module.toggleOnBindRelease) continue;
            if (module.keybind.isPressed() && !(MeteorClient.mc.currentScreen instanceof ChatScreen)) {
                if (module.isActive()) continue;
                module.toggle();
                continue;
            }
            if (!module.isActive()) continue;
            module.toggle();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void disableAll() {
        List<Module> list = this.active;
        synchronized (list) {
            for (Module module : this.modules) {
                if (!module.isActive()) continue;
                module.toggle();
            }
        }
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtList modulesTag = new NbtList();
        for (Module module : this.getAll()) {
            NbtCompound moduleTag = module.toTag();
            if (moduleTag == null) continue;
            modulesTag.add((Object)moduleTag);
        }
        tag.put("modules", (NbtElement)modulesTag);
        return tag;
    }

    @Override
    public Modules fromTag(NbtCompound tag) {
        this.disableAll();
        NbtList modulesTag = tag.getList("modules", 10);
        for (NbtElement moduleTagI : modulesTag) {
            NbtCompound moduleTag = (NbtCompound)moduleTagI;
            Module module = this.get(moduleTag.getString("name"));
            if (module == null) continue;
            module.fromTag(moduleTag);
        }
        return this;
    }

    public void add(Module module) {
        if (!CATEGORIES.contains(module.category)) {
            throw new RuntimeException("Modules.addModule - Module's category was not registered.");
        }
        AtomicReference removedModule = new AtomicReference();
        if (this.moduleInstances.values().removeIf(module1 -> {
            if (module1.name.equals(module.name)) {
                removedModule.set(module1);
                module1.settings.unregisterColorSettings();
                return true;
            }
            return false;
        })) {
            this.getGroup(((Module)removedModule.get()).category).remove(removedModule.get());
        }
        this.moduleInstances.put(module.getClass(), module);
        this.modules.add(module);
        this.getGroup(module.category).add(module);
        module.settings.registerColorSettings(module);
    }

    private static Module hidden(Module module) {
        module.hidden = true;
        return module;
    }

    private void initCombat() {
        this.add(new AnchorAura());
        this.add(Modules.hidden(new AntiAnvil()));
        this.add(Modules.hidden(new AntiBed()));
        this.add(Modules.hidden(new ArrowDodge()));
        this.add(Modules.hidden(new AutoAnvil()));
        this.add(new AutoArmor());
        this.add(new AutoEXP());
        this.add(new AutoTrap());
        this.add(Modules.hidden(new AutoWeapon()));
        this.add(Modules.hidden(new AutoWeb()));
        this.add(Modules.hidden(new BedAura()));
        this.add(Modules.hidden(new BowAimbot()));
        this.add(Modules.hidden(new BowSpam()));
        this.add(Modules.hidden(new Burrow()));
        this.add(Modules.hidden(new Criticals()));
        this.add(Modules.hidden(new Hitboxes()));
        this.add(Modules.hidden(new HoleFiller()));
        this.add(Modules.hidden(new KillAura()));
        this.add(new Offhand());
        this.add(Modules.hidden(new Quiver()));
        this.add(Modules.hidden(new SelfAnvil()));
        this.add(Modules.hidden(new SelfWeb()));
        this.add(new Surround());
        this.add(new AntiMace());
        this.add(new EChestBlocker());
        this.add(new MaceAura());
        this.add(new LogoutTrap());
        this.add(new PacketDebug());
        this.add(new AutoCrystal());
        this.add(new NewAutoCrystal());
        this.add(new AutoMine());
        this.add(new ForceSwim());
        this.add(new SwordAura());
        this.add(new AntiDigDown());
    }

    private void initPlayer() {
        this.add(Modules.hidden(new AntiHunger()));
        this.add(new AutoEat());
        this.add(new AutoClicker());
        this.add(Modules.hidden(new AutoFish()));
        this.add(new AutoGap());
        this.add(Modules.hidden(new AutoMend()));
        this.add(new AutoReplenish());
        this.add(Modules.hidden(new AutoTool()));
        this.add(Modules.hidden(new BreakDelay()));
        this.add(new ChestSwap());
        this.add(Modules.hidden(new EXPThrower()));
        this.add(new FakePlayer());
        this.add(new FastUse());
        this.add(new GhostHand());
        this.add(Modules.hidden(new InstantRebreak()));
        this.add(Modules.hidden(new LiquidInteract()));
        this.add(new MiddleClickExtra());
        this.add(new Multitask());
        this.add(new NoInteract());
        this.add(new NoMiningTrace());
        this.add(new NoRotate());
        this.add(Modules.hidden(new OffhandCrash()));
        this.add(new Portals());
        this.add(Modules.hidden(new PotionSaver()));
        this.add(Modules.hidden(new PotionSpoof()));
        this.add(Modules.hidden(new Reach()));
        this.add(new Rotation());
        this.add(Modules.hidden(new SpeedMine()));
        this.add(new PearlPhase());
        this.add(new KeyPearl());
        this.add(new PearlBlocker());
        this.add(new SpongeAura());
        this.add(new AutoRekit());
        this.add(new KeyPot());
    }

    private void initMovement() {
        this.add(Modules.hidden(new AirJump()));
        this.add(Modules.hidden(new Anchor()));
        this.add(new AntiAFK());
        this.add(Modules.hidden(new AntiVoid()));
        this.add(new AutoCrawl());
        this.add(Modules.hidden(new AutoJump()));
        this.add(new AutoWalk());
        this.add(Modules.hidden(new AutoWasp()));
        this.add(Modules.hidden(new Blink()));
        this.add(Modules.hidden(new BoatFly()));
        this.add(new ClickTP());
        this.add(Modules.hidden(new ElytraBoost()));
        this.add(Modules.hidden(new ElytraSpeed()));
        this.add(new ElytraFly());
        this.add(new ElytraLaunch());
        this.add(Modules.hidden(new EntityControl()));
        this.add(Modules.hidden(new EntitySpeed()));
        this.add(Modules.hidden(new FastClimb()));
        this.add(Modules.hidden(new Flight()));
        this.add(new GUIMove());
        this.add(Modules.hidden(new HighJump()));
        this.add(Modules.hidden(new Jesus()));
        this.add(Modules.hidden(new LongJump()));
        this.add(Modules.hidden(new NoFall()));
        this.add(new NoSlow());
        this.add(new Parkour());
        this.add(Modules.hidden(new ReverseStep()));
        this.add(Modules.hidden(new SafeWalk()));
        this.add(Modules.hidden(new Scaffold()));
        this.add(Modules.hidden(new Slippy()));
        this.add(new Sneak());
        this.add(new Speed());
        this.add(Modules.hidden(new Spider()));
        this.add(new Sprint());
        this.add(Modules.hidden(new Step()));
        this.add(Modules.hidden(new TridentBoost()));
        this.add(new Velocity());
        this.add(new ElytraFakeFly());
        this.add(new MovementFix());
        this.add(Modules.hidden(new GrimDisabler()));
        this.add(new NoJumpDelay());
    }

    private void initRender() {
        this.add(new BetterTooltips());
        this.add(Modules.hidden(new BlockSelection()));
        this.add(Modules.hidden(new BossStack()));
        this.add(Modules.hidden(new Breadcrumbs()));
        this.add(new BreakIndicators());
        this.add(new CameraTweaks());
        this.add(new Chams());
        this.add(Modules.hidden(new CityESP()));
        this.add(new EntityOwner());
        this.add(new ESP());
        this.add(new Freecam());
        this.add(new FreeLook());
        this.add(new Fullbright());
        this.add(new HandView());
        this.add(new HoleESP());
        this.add(new ItemPhysics());
        this.add(new ItemHighlight());
        this.add(new LightOverlay());
        this.add(new LogoutSpots());
        this.add(new Marker());
        this.add(new Nametags());
        this.add(new NoRender());
        this.add(new BlockESP());
        this.add(new StorageESP());
        this.add(new TimeChanger());
        this.add(new Tracers());
        this.add(Modules.hidden(new Trail()));
        this.add(new Trajectories());
        this.add(new UnfocusedCPU());
        this.add(new VoidESP());
        this.add(new WallHack());
        this.add(new WaypointsModule());
        this.add(new Xray());
        this.add(new Zoom());
        this.add(new Blur());
        this.add(new PopChams());
        this.add(new TunnelESP());
        this.add(new BetterTab());
        this.add(new PhaseESP());
        this.add(new FakeItem());
    }

    private void initWorld() {
        this.add(Modules.hidden(new AirPlace()));
        this.add(new Ambience());
        this.add(Modules.hidden(new AutoBreed()));
        this.add(Modules.hidden(new AutoBrewer()));
        this.add(Modules.hidden(new AutoMount()));
        this.add(Modules.hidden(new AutoNametag()));
        this.add(Modules.hidden(new AutoShearer()));
        this.add(Modules.hidden(new AutoSign()));
        this.add(Modules.hidden(new AutoSmelter()));
        this.add(Modules.hidden(new BuildHeight()));
        this.add(Modules.hidden(new Collisions()));
        this.add(Modules.hidden(new EChestFarmer()));
        this.add(new EndermanLook());
        this.add(Modules.hidden(new Flamethrower()));
        this.add(new HighwayBuilder());
        this.add(new LiquidFiller());
        this.add(new MountBypass());
        this.add(new NoGhostBlocks());
        this.add(new Nuker());
        this.add(new SilentMine());
        this.add(new StashFinder());
        this.add(new SpawnProofer());
        this.add(Modules.hidden(new Timer()));
        this.add(Modules.hidden(new VeinMiner()));
        this.add(new Filler());
        this.add(new SourceFiller());
        this.add(new MapAura());
        this.add(new AutoPortal());
        if (BaritoneUtils.IS_AVAILABLE) {
            this.add(new Excavator());
            this.add(new InfinityMiner());
        }
    }

    private void initMisc() {
        this.add(new Swarm());
        this.add(new AntiPacketKick());
        this.add(new AutoLog());
        this.add(new AutoReconnect());
        this.add(new AutoRespawn());
        this.add(new BetterBeacons());
        this.add(new BetterChat());
        this.add(new BookBot());
        this.add(new DiscordPresence());
        this.add(new InventoryTweaks());
        this.add(new MessageAura());
        this.add(new NameProtect());
        this.add(new Notebot());
        this.add(new Notifier());
        this.add(new PacketCanceller());
        this.add(new ServerSpoof());
        this.add(new SoundBlocker());
        this.add(new Spam());
        this.add(new KillNotify());
        this.add(new PacketSaver());
        this.add(new DebugModule());
        this.add(new FriendNotify());
    }

    public static class ModuleRegistry
    extends SimpleRegistry<Module> {
        public ModuleRegistry() {
            super(RegistryKey.ofRegistry((Identifier)MeteorClient.identifier("modules")), Lifecycle.stable());
        }

        public int size() {
            return Modules.get().getAll().size();
        }

        public Identifier getId(Module entry) {
            return null;
        }

        public Optional<RegistryKey<Module>> getKey(Module entry) {
            return Optional.empty();
        }

        public int getRawId(Module entry) {
            return 0;
        }

        public Module get(RegistryKey<Module> key) {
            return null;
        }

        public Module get(Identifier id) {
            return null;
        }

        public Lifecycle getLifecycle() {
            return null;
        }

        public Set<Identifier> getIds() {
            return null;
        }

        public boolean containsId(Identifier id) {
            return false;
        }

        @Nullable
        public Module get(int index) {
            return null;
        }

        @NotNull
        public Iterator<Module> iterator() {
            return new ModuleIterator();
        }

        public boolean contains(RegistryKey<Module> key) {
            return false;
        }

        public Set<Map.Entry<RegistryKey<Module>, Module>> getEntrySet() {
            return null;
        }

        public Set<RegistryKey<Module>> getKeys() {
            return null;
        }

        public Optional<RegistryEntry.Reference<Module>> getRandom(Random random) {
            return Optional.empty();
        }

        public Registry<Module> freeze() {
            return null;
        }

        public RegistryEntry.Reference<Module> createEntry(Module value) {
            return null;
        }

        public Optional<RegistryEntry.Reference<Module>> getEntry(int rawId) {
            return Optional.empty();
        }

        public Optional<RegistryEntry.Reference<Module>> getEntry(RegistryKey<Module> key) {
            return Optional.empty();
        }

        public Stream<RegistryEntry.Reference<Module>> streamEntries() {
            return null;
        }

        public Optional<RegistryEntryList.Named<Module>> getEntryList(TagKey<Module> tag) {
            return Optional.empty();
        }

        public RegistryEntryList.Named<Module> getOrCreateEntryList(TagKey<Module> tag) {
            return null;
        }

        public Stream<Pair<TagKey<Module>, RegistryEntryList.Named<Module>>> streamTagsAndEntries() {
            return null;
        }

        public Stream<TagKey<Module>> streamTags() {
            return null;
        }

        public void clearTags() {
        }

        public void populateTags(Map<TagKey<Module>, List<RegistryEntry<Module>>> tagEntries) {
        }

        private static class ModuleIterator
        implements Iterator<Module> {
            private final Iterator<Module> iterator = Modules.get().getAll().iterator();

            private ModuleIterator() {
            }

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public Module next() {
                return this.iterator.next();
            }
        }
    }
}

