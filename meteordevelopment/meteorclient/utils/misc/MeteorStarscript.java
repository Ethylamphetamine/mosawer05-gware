/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.pathing.goals.Goal
 *  baritone.api.process.IBaritoneProcess
 *  net.minecraft.SharedConstants
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.client.network.PlayerListEntry
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket$Mode
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.stat.Stat
 *  net.minecraft.stat.Stats
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.InvalidIdentifierException
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.hit.HitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.biome.Biome
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.utils.misc;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.process.IBaritoneProcess;
import java.lang.runtime.SwitchBootstraps;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.pathing.BaritoneUtils;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CPSUtils;
import meteordevelopment.meteorclient.utils.misc.HorizontalDirection;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.Dimension;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.Section;
import meteordevelopment.starscript.StandardLib;
import meteordevelopment.starscript.Starscript;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.Error;
import meteordevelopment.starscript.utils.StarscriptError;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.StringUtils;

public class MeteorStarscript {
    public static Starscript ss = new Starscript();
    private static final BlockPos.Mutable BP = new BlockPos.Mutable();
    private static final StringBuilder SB = new StringBuilder();
    private static long lastRequestedStatsTime = 0L;

    @PreInit(dependencies={PathManagers.class})
    public static void init() {
        StandardLib.init(ss);
        ss.set("mc_version", SharedConstants.getGameVersion().getName());
        ss.set("fps", () -> Value.number(MinecraftClientAccessor.getFps()));
        ss.set("ping", MeteorStarscript::ping);
        ss.set("time", () -> Value.string(LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))));
        ss.set("cps", () -> Value.number(CPSUtils.getCpsAverage()));
        ss.set("meteor", new ValueMap().set("name", MeteorClient.NAME).set("version", (String)(MeteorClient.VERSION != null ? (MeteorClient.DEV_BUILD.isEmpty() ? MeteorClient.VERSION.toString() : String.valueOf(MeteorClient.VERSION) + " " + MeteorClient.DEV_BUILD) : "")).set("modules", () -> Value.number(Modules.get().getAll().size())).set("active_modules", () -> Value.number(Modules.get().getActive().size())).set("is_module_active", MeteorStarscript::isModuleActive).set("get_module_info", MeteorStarscript::getModuleInfo).set("get_module_setting", MeteorStarscript::getModuleSetting).set("prefix", MeteorStarscript::getMeteorPrefix));
        if (BaritoneUtils.IS_AVAILABLE) {
            ss.set("baritone", new ValueMap().set("is_pathing", () -> Value.bool(BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing())).set("distance_to_goal", MeteorStarscript::baritoneDistanceToGoal).set("process", MeteorStarscript::baritoneProcess).set("process_name", MeteorStarscript::baritoneProcessName).set("eta", MeteorStarscript::baritoneETA));
        }
        ss.set("camera", new ValueMap().set("pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(false, true)).set("x", () -> Value.number(MeteorClient.mc.gameRenderer.getCamera().getPos().x)).set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getCamera().getPos().y)).set("z", () -> Value.number(MeteorClient.mc.gameRenderer.getCamera().getPos().z))).set("opposite_dim_pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(true, true)).set("x", () -> MeteorStarscript.oppositeX(true)).set("y", () -> Value.number(MeteorClient.mc.gameRenderer.getCamera().getPos().y)).set("z", () -> MeteorStarscript.oppositeZ(true))).set("yaw", () -> MeteorStarscript.yaw(true)).set("pitch", () -> MeteorStarscript.pitch(true)).set("direction", () -> MeteorStarscript.direction(true)));
        ss.set("player", new ValueMap().set("_toString", () -> Value.string(MeteorClient.mc.getSession().getUsername())).set("health", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getHealth() : 0.0)).set("absorption", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getAbsorptionAmount() : 0.0)).set("hunger", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.getHungerManager().getFoodLevel() : 0.0)).set("speed", () -> Value.number(Utils.getPlayerSpeed().horizontalLength())).set("speed_all", new ValueMap().set("_toString", () -> Value.string(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().toString() : "")).set("x", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().x : 0.0)).set("y", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().y : 0.0)).set("z", () -> Value.number(MeteorClient.mc.player != null ? Utils.getPlayerSpeed().z : 0.0))).set("breaking_progress", () -> Value.number(MeteorClient.mc.interactionManager != null ? (double)((ClientPlayerInteractionManagerAccessor)MeteorClient.mc.interactionManager).getBreakingProgress() : 0.0)).set("biome", MeteorStarscript::biome).set("dimension", () -> Value.string(PlayerUtils.getDimension().name())).set("opposite_dimension", () -> Value.string(PlayerUtils.getDimension().opposite().name())).set("gamemode", () -> PlayerUtils.getGameMode() != null ? Value.string(StringUtils.capitalize((String)PlayerUtils.getGameMode().getName())) : Value.null_()).set("pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(false, false)).set("x", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() : 0.0)).set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0)).set("z", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() : 0.0))).set("opposite_dim_pos", new ValueMap().set("_toString", () -> MeteorStarscript.posString(true, false)).set("x", () -> MeteorStarscript.oppositeX(false)).set("y", () -> Value.number(MeteorClient.mc.player != null ? MeteorClient.mc.player.getY() : 0.0)).set("z", () -> MeteorStarscript.oppositeZ(false))).set("yaw", () -> MeteorStarscript.yaw(false)).set("pitch", () -> MeteorStarscript.pitch(false)).set("direction", () -> MeteorStarscript.direction(false)).set("hand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getMainHandStack()) : Value.null_()).set("offhand", () -> MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getOffHandStack()) : Value.null_()).set("hand_or_offhand", MeteorStarscript::handOrOffhand).set("get_item", MeteorStarscript::getItem).set("count_items", MeteorStarscript::countItems).set("xp", new ValueMap().set("level", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.experienceLevel : 0.0)).set("progress", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.experienceProgress : 0.0)).set("total", () -> Value.number(MeteorClient.mc.player != null ? (double)MeteorClient.mc.player.totalExperience : 0.0))).set("has_potion_effect", MeteorStarscript::hasPotionEffect).set("get_potion_effect", MeteorStarscript::getPotionEffect).set("get_stat", MeteorStarscript::getStat));
        ss.set("crosshair_target", new ValueMap().set("type", MeteorStarscript::crosshairType).set("value", MeteorStarscript::crosshairValue));
        ss.set("server", new ValueMap().set("_toString", () -> Value.string(Utils.getWorldName())).set("tps", () -> Value.number(TickRate.INSTANCE.getTickRate())).set("time", () -> Value.string(Utils.getWorldTime())).set("player_count", () -> Value.number(MeteorClient.mc.getNetworkHandler() != null ? (double)MeteorClient.mc.getNetworkHandler().getPlayerList().size() : 0.0)).set("difficulty", () -> Value.string(MeteorClient.mc.world != null ? MeteorClient.mc.world.getDifficulty().getName() : "")));
    }

    public static Script compile(String source) {
        Parser.Result result = Parser.parse(source);
        if (result.hasErrors()) {
            for (Error error : result.errors) {
                MeteorStarscript.printChatError(error);
            }
            return null;
        }
        return Compiler.compile(result);
    }

    public static Section runSection(Script script, StringBuilder sb) {
        try {
            return ss.run(script, sb);
        }
        catch (StarscriptError error) {
            MeteorStarscript.printChatError(error);
            return null;
        }
    }

    public static String run(Script script, StringBuilder sb) {
        Section section = MeteorStarscript.runSection(script, sb);
        return section != null ? section.toString() : null;
    }

    public static Section runSection(Script script) {
        return MeteorStarscript.runSection(script, new StringBuilder());
    }

    public static String run(Script script) {
        return MeteorStarscript.run(script, new StringBuilder());
    }

    public static void printChatError(int i, Error error) {
        String caller = MeteorStarscript.getCallerName();
        if (caller != null) {
            if (i != -1) {
                ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s (from %s)", i, error.character, Character.valueOf(error.ch), error.message, caller);
            } else {
                ChatUtils.errorPrefix("Starscript", "%d '%c': %s (from %s)", error.character, Character.valueOf(error.ch), error.message, caller);
            }
        } else if (i != -1) {
            ChatUtils.errorPrefix("Starscript", "%d, %d '%c': %s", i, error.character, Character.valueOf(error.ch), error.message);
        } else {
            ChatUtils.errorPrefix("Starscript", "%d '%c': %s", error.character, Character.valueOf(error.ch), error.message);
        }
    }

    public static void printChatError(Error error) {
        MeteorStarscript.printChatError(-1, error);
    }

    public static void printChatError(StarscriptError e) {
        String caller = MeteorStarscript.getCallerName();
        if (caller != null) {
            ChatUtils.errorPrefix("Starscript", "%s (from %s)", e.getMessage(), caller);
        } else {
            ChatUtils.errorPrefix("Starscript", "%s", e.getMessage());
        }
    }

    private static String getCallerName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length == 0) {
            return null;
        }
        for (int i = 1; i < elements.length; ++i) {
            String name = elements[i].getClassName();
            if (name.startsWith(Starscript.class.getPackageName()) || name.equals(MeteorStarscript.class.getName())) continue;
            return name.substring(name.lastIndexOf(46) + 1);
        }
        return null;
    }

    private static Value hasPotionEffect(Starscript ss, int argCount) {
        if (argCount < 1) {
            ss.error("player.has_potion_effect() requires 1 argument, got %d.", argCount);
        }
        if (MeteorClient.mc.player == null) {
            return Value.bool(false);
        }
        Identifier name = MeteorStarscript.popIdentifier(ss, "First argument to player.has_potion_effect() needs to a string.");
        Optional effect = Registries.STATUS_EFFECT.getEntry(name);
        if (effect.isEmpty()) {
            return Value.bool(false);
        }
        StatusEffectInstance effectInstance = MeteorClient.mc.player.getStatusEffect((RegistryEntry)effect.get());
        return Value.bool(effectInstance != null);
    }

    private static Value getPotionEffect(Starscript ss, int argCount) {
        if (argCount < 1) {
            ss.error("player.get_potion_effect() requires 1 argument, got %d.", argCount);
        }
        if (MeteorClient.mc.player == null) {
            return Value.null_();
        }
        Identifier name = MeteorStarscript.popIdentifier(ss, "First argument to player.get_potion_effect() needs to a string.");
        Optional effect = Registries.STATUS_EFFECT.getEntry(name);
        if (effect.isEmpty()) {
            return Value.null_();
        }
        StatusEffectInstance effectInstance = MeteorClient.mc.player.getStatusEffect((RegistryEntry)effect.get());
        if (effectInstance == null) {
            return Value.null_();
        }
        return MeteorStarscript.wrap(effectInstance);
    }

    private static Value getStat(Starscript ss, int argCount) {
        if (argCount < 1) {
            ss.error("player.get_stat() requires 1 argument, got %d.", argCount);
        }
        if (MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        long time = System.currentTimeMillis();
        if ((double)(time - lastRequestedStatsTime) / 1000.0 >= 1.0 && MeteorClient.mc.getNetworkHandler() != null) {
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
            lastRequestedStatsTime = time;
        }
        String type = argCount > 1 ? ss.popString("First argument to player.get_stat() needs to be a string.") : "custom";
        Identifier name = MeteorStarscript.popIdentifier(ss, (argCount > 1 ? "Second" : "First") + " argument to player.get_stat() needs to be a string.");
        Stat stat = switch (type) {
            case "mined" -> Stats.MINED.getOrCreateStat((Object)((Block)Registries.BLOCK.get(name)));
            case "crafted" -> Stats.CRAFTED.getOrCreateStat((Object)((Item)Registries.ITEM.get(name)));
            case "used" -> Stats.USED.getOrCreateStat((Object)((Item)Registries.ITEM.get(name)));
            case "broken" -> Stats.BROKEN.getOrCreateStat((Object)((Item)Registries.ITEM.get(name)));
            case "picked_up" -> Stats.PICKED_UP.getOrCreateStat((Object)((Item)Registries.ITEM.get(name)));
            case "dropped" -> Stats.DROPPED.getOrCreateStat((Object)((Item)Registries.ITEM.get(name)));
            case "killed" -> Stats.KILLED.getOrCreateStat((Object)((EntityType)Registries.ENTITY_TYPE.get(name)));
            case "killed_by" -> Stats.KILLED_BY.getOrCreateStat((Object)((EntityType)Registries.ENTITY_TYPE.get(name)));
            case "custom" -> {
                name = (Identifier)Registries.CUSTOM_STAT.get(name);
                if (name != null) {
                    yield Stats.CUSTOM.getOrCreateStat((Object)name);
                }
                yield null;
            }
            default -> null;
        };
        return Value.number(stat != null ? (double)MeteorClient.mc.player.getStatHandler().getStat(stat) : 0.0);
    }

    private static Value getModuleInfo(Starscript ss, int argCount) {
        Module module;
        if (argCount != 1) {
            ss.error("meteor.get_module_info() requires 1 argument, got %d.", argCount);
        }
        if ((module = Modules.get().get(ss.popString("First argument to meteor.get_module_info() needs to be a string."))) != null && module.isActive()) {
            String info = module.getInfoString();
            return Value.string(info == null ? "" : info);
        }
        return Value.string("");
    }

    private static Value getModuleSetting(Starscript ss, int argCount) {
        Object value;
        Setting<?> setting;
        if (argCount != 2) {
            ss.error("meteor.get_module_setting() requires 2 arguments, got %d.", argCount);
        }
        String settingName = ss.popString("Second argument to meteor.get_module_setting() needs to be a string.");
        String moduleName = ss.popString("First argument to meteor.get_module_setting() needs to be a string.");
        Module module = Modules.get().get(moduleName);
        if (module == null) {
            ss.error("Unable to get module %s for meteor.get_module_setting()", moduleName);
        }
        if ((setting = module.settings.get(settingName)) == null) {
            ss.error("Unable to get setting %s for module %s for meteor.get_module_setting()", settingName, moduleName);
        }
        Object obj = value = setting.get();
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Double.class, Integer.class, Boolean.class, List.class}, obj, n)) {
            case 0 -> {
                Double d = (Double)obj;
                yield Value.number(d);
            }
            case 1 -> {
                Integer i = (Integer)obj;
                yield Value.number(i.intValue());
            }
            case 2 -> {
                Boolean b = (Boolean)obj;
                yield Value.bool(b);
            }
            case 3 -> {
                List list = (List)obj;
                yield Value.number(list.size());
            }
            default -> Value.string(value.toString());
        };
    }

    private static Value isModuleActive(Starscript ss, int argCount) {
        Module module;
        if (argCount != 1) {
            ss.error("meteor.is_module_active() requires 1 argument, got %d.", argCount);
        }
        return Value.bool((module = Modules.get().get(ss.popString("First argument to meteor.is_module_active() needs to be a string."))) != null && module.isActive());
    }

    private static Value getItem(Starscript ss, int argCount) {
        int i;
        if (argCount != 1) {
            ss.error("player.get_item() requires 1 argument, got %d.", argCount);
        }
        if ((i = (int)ss.popNumber("First argument to player.get_item() needs to be a number.")) < 0) {
            ss.error("First argument to player.get_item() needs to be a non-negative integer.", i);
        }
        return MeteorClient.mc.player != null ? MeteorStarscript.wrap(MeteorClient.mc.player.getInventory().getStack(i)) : Value.null_();
    }

    private static Value countItems(Starscript ss, int argCount) {
        String idRaw;
        Identifier id;
        if (argCount != 1) {
            ss.error("player.count_items() requires 1 argument, got %d.", argCount);
        }
        if ((id = Identifier.tryParse((String)(idRaw = ss.popString("First argument to player.count_items() needs to be a string.")))) == null) {
            return Value.number(0.0);
        }
        Item item = (Item)Registries.ITEM.get(id);
        if (item == Items.AIR || MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        int count = 0;
        for (int i = 0; i < MeteorClient.mc.player.getInventory().size(); ++i) {
            ItemStack itemStack = MeteorClient.mc.player.getInventory().getStack(i);
            if (itemStack.getItem() != item) continue;
            count += itemStack.getCount();
        }
        return Value.number(count);
    }

    private static Value getMeteorPrefix() {
        if (Config.get() == null) {
            return Value.null_();
        }
        return Value.string(Config.get().prefix.get());
    }

    private static Value baritoneProcess() {
        Optional process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
        return Value.string(process.isEmpty() ? "" : ((IBaritoneProcess)process.get()).displayName0());
    }

    private static Value baritoneProcessName() {
        Optional process = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingControlManager().mostRecentInControl();
        if (process.isEmpty()) {
            return Value.string("");
        }
        String className = ((IBaritoneProcess)process.get()).getClass().getSimpleName();
        if (className.endsWith("Process")) {
            className = className.substring(0, className.length() - 7);
        }
        SB.append(className);
        int i = 0;
        for (int j = 0; j < className.length(); ++j) {
            if (j > 0 && Character.isUpperCase(className.charAt(j))) {
                SB.insert(i, ' ');
                ++i;
            }
            ++i;
        }
        String name = SB.toString();
        SB.setLength(0);
        return Value.string(name);
    }

    private static Value baritoneETA() {
        if (MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        Optional ticksTillGoal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().estimatedTicksToGoal();
        return ticksTillGoal.map(aDouble -> Value.number(aDouble / 20.0)).orElseGet(() -> Value.number(0.0));
    }

    private static Value oppositeX(boolean camera) {
        double x = camera ? MeteorClient.mc.gameRenderer.getCamera().getPos().x : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getX() : 0.0);
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension == Dimension.Overworld) {
            x /= 8.0;
        } else if (dimension == Dimension.Nether) {
            x *= 8.0;
        }
        return Value.number(x);
    }

    private static Value oppositeZ(boolean camera) {
        double z = camera ? MeteorClient.mc.gameRenderer.getCamera().getPos().z : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getZ() : 0.0);
        Dimension dimension = PlayerUtils.getDimension();
        if (dimension == Dimension.Overworld) {
            z /= 8.0;
        } else if (dimension == Dimension.Nether) {
            z *= 8.0;
        }
        return Value.number(z);
    }

    private static Value yaw(boolean camera) {
        float yaw = camera ? MeteorClient.mc.gameRenderer.getCamera().getYaw() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getYaw() : 0.0f);
        if ((yaw %= 360.0f) < 0.0f) {
            yaw += 360.0f;
        }
        if (yaw > 180.0f) {
            yaw -= 360.0f;
        }
        return Value.number(yaw);
    }

    private static Value pitch(boolean camera) {
        float pitch = camera ? MeteorClient.mc.gameRenderer.getCamera().getPitch() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getPitch() : 0.0f);
        if ((pitch %= 360.0f) < 0.0f) {
            pitch += 360.0f;
        }
        if (pitch > 180.0f) {
            pitch -= 360.0f;
        }
        return Value.number(pitch);
    }

    private static Value direction(boolean camera) {
        float yaw = camera ? MeteorClient.mc.gameRenderer.getCamera().getYaw() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getYaw() : 0.0f);
        return MeteorStarscript.wrap(HorizontalDirection.get(yaw));
    }

    private static Value biome() {
        if (MeteorClient.mc.player == null || MeteorClient.mc.world == null) {
            return Value.string("");
        }
        BP.set(MeteorClient.mc.player.getX(), MeteorClient.mc.player.getY(), MeteorClient.mc.player.getZ());
        Identifier id = MeteorClient.mc.world.getRegistryManager().get(RegistryKeys.BIOME).getId((Object)((Biome)MeteorClient.mc.world.getBiome((BlockPos)BP).comp_349()));
        if (id == null) {
            return Value.string("Unknown");
        }
        return Value.string(Arrays.stream(id.getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" ")));
    }

    private static Value handOrOffhand() {
        if (MeteorClient.mc.player == null) {
            return Value.null_();
        }
        ItemStack itemStack = MeteorClient.mc.player.getMainHandStack();
        if (itemStack.isEmpty()) {
            itemStack = MeteorClient.mc.player.getOffHandStack();
        }
        return itemStack != null ? MeteorStarscript.wrap(itemStack) : Value.null_();
    }

    private static Value ping() {
        if (MeteorClient.mc.getNetworkHandler() == null || MeteorClient.mc.player == null) {
            return Value.number(0.0);
        }
        PlayerListEntry playerListEntry = MeteorClient.mc.getNetworkHandler().getPlayerListEntry(MeteorClient.mc.player.getUuid());
        return Value.number(playerListEntry != null ? (double)playerListEntry.getLatency() : 0.0);
    }

    private static Value baritoneDistanceToGoal() {
        Goal goal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getGoal();
        return Value.number(goal != null && MeteorClient.mc.player != null ? goal.heuristic(MeteorClient.mc.player.getBlockPos()) : 0.0);
    }

    private static Value posString(boolean opposite, boolean camera) {
        Vec3d pos = camera ? MeteorClient.mc.gameRenderer.getCamera().getPos() : (MeteorClient.mc.player != null ? MeteorClient.mc.player.getPos() : Vec3d.ZERO);
        double x = pos.x;
        double z = pos.z;
        if (opposite) {
            Dimension dimension = PlayerUtils.getDimension();
            if (dimension == Dimension.Overworld) {
                x /= 8.0;
                z /= 8.0;
            } else if (dimension == Dimension.Nether) {
                x *= 8.0;
                z *= 8.0;
            }
        }
        return MeteorStarscript.posString(x, pos.y, z);
    }

    private static Value posString(double x, double y, double z) {
        return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z));
    }

    private static Value crosshairType() {
        if (MeteorClient.mc.crosshairTarget == null) {
            return Value.string("miss");
        }
        return Value.string(switch (MeteorClient.mc.crosshairTarget.getType()) {
            default -> throw new MatchException(null, null);
            case HitResult.Type.MISS -> "miss";
            case HitResult.Type.BLOCK -> "block";
            case HitResult.Type.ENTITY -> "entity";
        });
    }

    private static Value crosshairValue() {
        if (MeteorClient.mc.world == null || MeteorClient.mc.crosshairTarget == null) {
            return Value.null_();
        }
        if (MeteorClient.mc.crosshairTarget.getType() == HitResult.Type.MISS) {
            return Value.string("");
        }
        HitResult hitResult = MeteorClient.mc.crosshairTarget;
        if (hitResult instanceof BlockHitResult) {
            BlockHitResult hit = (BlockHitResult)hitResult;
            return MeteorStarscript.wrap(hit.getBlockPos(), MeteorClient.mc.world.getBlockState(hit.getBlockPos()));
        }
        return MeteorStarscript.wrap(((EntityHitResult)MeteorClient.mc.crosshairTarget).getEntity());
    }

    public static Identifier popIdentifier(Starscript ss, String errorMessage) {
        try {
            return Identifier.of((String)ss.popString(errorMessage));
        }
        catch (InvalidIdentifierException e) {
            ss.error(e.getMessage(), new Object[0]);
            return null;
        }
    }

    public static Value wrap(ItemStack itemStack) {
        String name = itemStack.isEmpty() ? "" : Names.get(itemStack.getItem());
        int durability = 0;
        if (!itemStack.isEmpty() && itemStack.isDamageable()) {
            durability = itemStack.getMaxDamage() - itemStack.getDamage();
        }
        return Value.map(new ValueMap().set("_toString", Value.string(itemStack.getCount() <= 1 ? name : String.format("%s %dx", name, itemStack.getCount()))).set("name", Value.string(name)).set("id", Value.string(Registries.ITEM.getId((Object)itemStack.getItem()).toString())).set("count", Value.number(itemStack.getCount())).set("durability", Value.number(durability)).set("max_durability", Value.number(itemStack.getMaxDamage())));
    }

    public static Value wrap(BlockPos blockPos, BlockState blockState) {
        return Value.map(new ValueMap().set("_toString", Value.string(Names.get(blockState.getBlock()))).set("id", Value.string(Registries.BLOCK.getId((Object)blockState.getBlock()).toString())).set("pos", Value.map(new ValueMap().set("_toString", MeteorStarscript.posString(blockPos.getX(), blockPos.getY(), blockPos.getZ())).set("x", Value.number(blockPos.getX())).set("y", Value.number(blockPos.getY())).set("z", Value.number(blockPos.getZ())))));
    }

    public static Value wrap(Entity entity) {
        double d;
        double d2;
        LivingEntity e;
        ValueMap valueMap = new ValueMap().set("_toString", Value.string(entity.getName().getString())).set("id", Value.string(Registries.ENTITY_TYPE.getId((Object)entity.getType()).toString()));
        if (entity instanceof LivingEntity) {
            e = (LivingEntity)entity;
            d2 = e.getHealth();
        } else {
            d2 = 0.0;
        }
        ValueMap valueMap2 = valueMap.set("health", Value.number(d2));
        if (entity instanceof LivingEntity) {
            e = (LivingEntity)entity;
            d = e.getAbsorptionAmount();
        } else {
            d = 0.0;
        }
        return Value.map(valueMap2.set("absorption", Value.number(d)).set("pos", Value.map(new ValueMap().set("_toString", MeteorStarscript.posString(entity.getX(), entity.getY(), entity.getZ())).set("x", Value.number(entity.getX())).set("y", Value.number(entity.getY())).set("z", Value.number(entity.getZ())))));
    }

    public static Value wrap(HorizontalDirection dir) {
        return Value.map(new ValueMap().set("_toString", Value.string(dir.name + " " + dir.axis)).set("name", Value.string(dir.name)).set("axis", Value.string(dir.axis)));
    }

    public static Value wrap(StatusEffectInstance effectInstance) {
        return Value.map(new ValueMap().set("duration", effectInstance.getDuration()).set("level", effectInstance.getAmplifier() + 1));
    }
}

