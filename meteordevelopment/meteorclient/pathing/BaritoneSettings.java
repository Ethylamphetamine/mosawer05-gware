/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  baritone.api.BaritoneAPI
 *  baritone.api.Settings
 *  baritone.api.Settings$Setting
 *  baritone.api.utils.SettingsUtil
 *  net.minecraft.block.Block
 *  net.minecraft.item.Item
 */
package meteordevelopment.meteorclient.pathing;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.api.utils.SettingsUtil;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.pathing.IPathManager;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class BaritoneSettings
implements IPathManager.ISettings {
    private final Settings settings = new Settings();
    private Setting<Boolean> walkOnWater;
    private Setting<Boolean> walkOnLava;
    private Setting<Boolean> step;
    private Setting<Boolean> noFall;
    private static final Map<String, Double> SETTING_MAX_VALUES = new HashMap<String, Double>();
    private static Map<String, String> descriptions;

    public BaritoneSettings() {
        this.createWrappers();
    }

    @Override
    public Settings get() {
        return this.settings;
    }

    @Override
    public Setting<Boolean> getWalkOnWater() {
        return this.walkOnWater;
    }

    @Override
    public Setting<Boolean> getWalkOnLava() {
        return this.walkOnLava;
    }

    @Override
    public Setting<Boolean> getStep() {
        return this.step;
    }

    @Override
    public Setting<Boolean> getNoFall() {
        return this.noFall;
    }

    @Override
    public void save() {
        SettingsUtil.save((baritone.api.Settings)BaritoneAPI.getSettings());
    }

    private void createWrappers() {
        SettingGroup sgBool = this.settings.createGroup("Checkboxes");
        SettingGroup sgDouble = this.settings.createGroup("Numbers");
        SettingGroup sgInt = this.settings.createGroup("Whole Numbers");
        SettingGroup sgString = this.settings.createGroup("Strings");
        SettingGroup sgColor = this.settings.createGroup("Colors");
        SettingGroup sgBlockLists = this.settings.createGroup("Block Lists");
        SettingGroup sgItemLists = this.settings.createGroup("Item Lists");
        try {
            Class<?> klass = BaritoneAPI.getSettings().getClass();
            for (Field field : klass.getDeclaredFields()) {
                Setting<Comparable<Boolean>> wrapper;
                Object obj;
                if (Modifier.isStatic(field.getModifiers()) || !((obj = field.get(BaritoneAPI.getSettings())) instanceof Settings.Setting)) continue;
                Settings.Setting setting = (Settings.Setting)obj;
                Object value = setting.value;
                if (value instanceof Boolean) {
                    wrapper = sgBool.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue(Boolean.valueOf((Boolean)setting.defaultValue))).onChanged(aBoolean -> {
                        setting.value = aBoolean;
                    })).onModuleActivated(booleanSetting -> booleanSetting.set((Boolean)setting.value))).build());
                    switch (wrapper.name) {
                        case "assumeWalkOnWater": {
                            this.walkOnWater = wrapper;
                            break;
                        }
                        case "assumeWalkOnLava": {
                            this.walkOnLava = wrapper;
                            break;
                        }
                        case "assumeStep": {
                            this.step = wrapper;
                        }
                    }
                    continue;
                }
                if (value instanceof Double) {
                    sgDouble.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue((Double)setting.defaultValue).max(SETTING_MAX_VALUES.getOrDefault(setting.getName(), 10.0)).sliderMax(SETTING_MAX_VALUES.getOrDefault(setting.getName(), 10.0)).onChanged(aDouble -> {
                        setting.value = aDouble;
                    })).onModuleActivated(doubleSetting -> doubleSetting.set((Double)setting.value))).build());
                    continue;
                }
                if (value instanceof Float) {
                    sgDouble.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue(((Float)setting.defaultValue).doubleValue()).max(SETTING_MAX_VALUES.getOrDefault(setting.getName(), 10.0)).sliderMax(SETTING_MAX_VALUES.getOrDefault(setting.getName(), 10.0)).onChanged(aDouble -> {
                        setting.value = Float.valueOf(aDouble.floatValue());
                    })).onModuleActivated(doubleSetting -> doubleSetting.set(((Float)setting.value).doubleValue()))).build());
                    continue;
                }
                if (value instanceof Integer) {
                    wrapper = sgInt.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue(Integer.valueOf((Integer)setting.defaultValue))).onChanged(integer -> {
                        setting.value = integer;
                    })).onModuleActivated(integerSetting -> integerSetting.set((Integer)setting.value))).build());
                    if (!wrapper.name.equals("maxFallHeightNoWater")) continue;
                    this.noFall = ((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name(wrapper.name)).description(wrapper.description)).defaultValue(false)).onChanged(aBoolean -> wrapper.set((Comparable<Boolean>)(aBoolean != false ? 159159 : (Integer)wrapper.getDefaultValue())))).onModuleActivated(booleanSetting -> booleanSetting.set((Integer)wrapper.get() >= 255))).build();
                    continue;
                }
                if (value instanceof Long) {
                    sgInt.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue(((Long)setting.defaultValue).intValue())).onChanged(integer -> {
                        setting.value = integer.longValue();
                    })).onModuleActivated(integerSetting -> integerSetting.set(((Long)setting.value).intValue()))).build());
                    continue;
                }
                if (value instanceof String) {
                    sgString.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue((String)setting.defaultValue)).onChanged(string -> {
                        setting.value = string;
                    })).onModuleActivated(stringSetting -> stringSetting.set((String)setting.value))).build());
                    continue;
                }
                if (value instanceof Color) {
                    Color c = (Color)setting.value;
                    sgColor.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue(new SettingColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha())).onChanged(color -> {
                        setting.value = new Color(color.r, color.g, color.b, color.a);
                    })).onModuleActivated(colorSetting -> colorSetting.set(new SettingColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha())))).build());
                    continue;
                }
                if (!(value instanceof List)) continue;
                Type listType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                Type type = ((ParameterizedType)listType).getActualTypeArguments()[0];
                if (type == Block.class) {
                    sgBlockLists.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue((List)setting.defaultValue)).onChanged(blockList -> {
                        setting.value = blockList;
                    })).onModuleActivated(blockListSetting -> blockListSetting.set((List)setting.value))).build());
                    continue;
                }
                if (type != Item.class) continue;
                sgItemLists.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name(setting.getName())).description(BaritoneSettings.getDescription(setting.getName()))).defaultValue((List)setting.defaultValue)).onChanged(itemList -> {
                    setting.value = itemList;
                })).onModuleActivated(itemListSetting -> itemListSetting.set((List)setting.value))).build());
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDescription(String settingName, String description) {
        descriptions.put(settingName.toLowerCase(), description);
    }

    private static String getDescription(String settingName) {
        if (descriptions == null) {
            BaritoneSettings.loadDescriptions();
        }
        return descriptions.get(settingName.toLowerCase());
    }

    private static void loadDescriptions() {
        descriptions = new HashMap<String, String>();
        BaritoneSettings.addDescription("acceptableThrowawayItems", "Blocks that Baritone is allowed to place (as throwaway, for sneak bridging, pillaring, etc.)");
        BaritoneSettings.addDescription("allowBreak", "Allow Baritone to break blocks");
        BaritoneSettings.addDescription("allowBreakAnyway", "Blocks that baritone will be allowed to break even with allowBreak set to false");
        BaritoneSettings.addDescription("allowDiagonalAscend", "Allow diagonal ascending");
        BaritoneSettings.addDescription("allowDiagonalDescend", "Allow descending diagonally");
        BaritoneSettings.addDescription("allowDownward", "Allow mining the block directly beneath its feet");
        BaritoneSettings.addDescription("allowInventory", "Allow Baritone to move items in your inventory to your hotbar");
        BaritoneSettings.addDescription("allowJumpAt256", "If true, parkour is allowed to make jumps when standing on blocks at the maximum height, so player feet is y=256");
        BaritoneSettings.addDescription("allowOnlyExposedOres", "This will only allow baritone to mine exposed ores, can be used to stop ore obfuscators on servers that use them.");
        BaritoneSettings.addDescription("allowOnlyExposedOresDistance", "When allowOnlyExposedOres is enabled this is the distance around to search.");
        BaritoneSettings.addDescription("allowOvershootDiagonalDescend", "Is it okay to sprint through a descend followed by a diagonal? The player overshoots the landing, but not enough to fall off.");
        BaritoneSettings.addDescription("allowParkour", "You know what it is");
        BaritoneSettings.addDescription("allowParkourAscend", "This should be monetized it's so good");
        BaritoneSettings.addDescription("allowParkourPlace", "Actually pretty reliable.");
        BaritoneSettings.addDescription("allowPlace", "Allow Baritone to place blocks");
        BaritoneSettings.addDescription("allowSprint", "Allow Baritone to sprint");
        BaritoneSettings.addDescription("allowVines", "Enables some more advanced vine features.");
        BaritoneSettings.addDescription("allowWalkOnBottomSlab", "Slab behavior is complicated, disable this for higher path reliability.");
        BaritoneSettings.addDescription("allowWaterBucketFall", "Allow Baritone to fall arbitrary distances and place a water bucket beneath it.");
        BaritoneSettings.addDescription("antiCheatCompatibility", "Will cause some minor behavioral differences to ensure that Baritone works on anticheats.");
        BaritoneSettings.addDescription("assumeExternalAutoTool", "Disable baritone's auto-tool at runtime, but still assume that another mod will provide auto tool functionality");
        BaritoneSettings.addDescription("assumeSafeWalk", "Assume safe walk functionality; don't sneak on a backplace traverse.");
        BaritoneSettings.addDescription("assumeStep", "Assume step functionality; don't jump on an Ascend.");
        BaritoneSettings.addDescription("assumeWalkOnLava", "If you have Fire Resistance and Jesus then I guess you could turn this on lol");
        BaritoneSettings.addDescription("assumeWalkOnWater", "Allow Baritone to assume it can walk on still water just like any other block.");
        BaritoneSettings.addDescription("autoTool", "Automatically select the best available tool");
        BaritoneSettings.addDescription("avoidance", "Toggle the following 4 settings");
        BaritoneSettings.addDescription("avoidBreakingMultiplier", "this multiplies the break speed, if set above 1 it's \"encourage breaking\" instead");
        BaritoneSettings.addDescription("avoidUpdatingFallingBlocks", "If this setting is true, Baritone will never break a block that is adjacent to an unsupported falling block.");
        BaritoneSettings.addDescription("axisHeight", "The \"axis\" command (aka GoalAxis) will go to a axis, or diagonal axis, at this Y level.");
        BaritoneSettings.addDescription("backfill", "Fill in blocks behind you (stealth +100)");
        BaritoneSettings.addDescription("backtrackCostFavoringCoefficient", "Set to 1.0 to effectively disable this feature");
        BaritoneSettings.addDescription("blacklistClosestOnFailure", "When GetToBlockProcess or MineProcess fails to calculate a path, instead of just giving up, mark the closest instance of that block as \"unreachable\" and go towards the next closest.");
        BaritoneSettings.addDescription("blockBreakAdditionalPenalty", "This is just a tiebreaker to make it less likely to break blocks if it can avoid it.");
        BaritoneSettings.addDescription("blockPlacementPenalty", "It doesn't actually take twenty ticks to place a block, this cost is so high because we want to generally conserve blocks which might be limited.");
        BaritoneSettings.addDescription("blockReachDistance", "Block reach distance");
        BaritoneSettings.addDescription("blocksToAvoid", "Blocks that Baritone will attempt to avoid (Used in avoidance)");
        BaritoneSettings.addDescription("blocksToAvoidBreaking", "blocks that baritone shouldn't break, but can if it needs to.");
        BaritoneSettings.addDescription("blocksToDisallowBreaking", "Blocks that Baritone is not allowed to break");
        BaritoneSettings.addDescription("breakCorrectBlockPenaltyMultiplier", "Multiply the cost of breaking a block that's correct in the builder's schematic by this coefficient");
        BaritoneSettings.addDescription("breakFromAbove", "Allow standing above a block while mining it, in BuilderProcess");
        BaritoneSettings.addDescription("builderTickScanRadius", "Distance to scan every tick for updates.");
        BaritoneSettings.addDescription("buildIgnoreBlocks", "A list of blocks to be treated as if they're air.");
        BaritoneSettings.addDescription("buildIgnoreDirection", "If this is true, the builder will ignore directionality of certain blocks like glazed terracotta.");
        BaritoneSettings.addDescription("buildIgnoreExisting", "If this is true, the builder will treat all non-air blocks as correct.");
        BaritoneSettings.addDescription("buildInLayers", "Don't consider the next layer in builder until the current one is done");
        BaritoneSettings.addDescription("buildOnlySelection", "Only build the selected part of schematics");
        BaritoneSettings.addDescription("buildRepeat", "How far to move before repeating the build.");
        BaritoneSettings.addDescription("buildRepeatCount", "How many times to buildrepeat.");
        BaritoneSettings.addDescription("buildRepeatSneaky", "Don't notify schematics that they are moved.");
        BaritoneSettings.addDescription("buildSkipBlocks", "A list of blocks to be treated as correct.");
        BaritoneSettings.addDescription("buildSubstitutes", "A mapping of blocks to blocks to be built instead");
        BaritoneSettings.addDescription("buildValidSubstitutes", "A mapping of blocks to blocks treated as correct in their position.");
        BaritoneSettings.addDescription("cachedChunksExpirySeconds", "Cached chunks (regardless of if they're in RAM or saved to disk) expire and are deleted after this number of seconds -1 to disable");
        BaritoneSettings.addDescription("cachedChunksOpacity", "0.0f = not visible, fully transparent (instead of setting this to 0, turn off renderCachedChunks) 1.0f = fully opaque");
        BaritoneSettings.addDescription("cancelOnGoalInvalidation", "Cancel the current path if the goal has changed, and the path originally ended in the goal but doesn't anymore.");
        BaritoneSettings.addDescription("censorCoordinates", "Censor coordinates in goals and block positions");
        BaritoneSettings.addDescription("censorRanCommands", "Censor arguments to ran commands, to hide, for example, coordinates to #goal");
        BaritoneSettings.addDescription("chatControl", "Allow chat based control of Baritone.");
        BaritoneSettings.addDescription("chatControlAnyway", "Some clients like Impact try to force chatControl to off, so here's a second setting to do it anyway");
        BaritoneSettings.addDescription("chatDebug", "Print all the debug messages to chat");
        BaritoneSettings.addDescription("chunkCaching", "The big one.");
        BaritoneSettings.addDescription("colorBestPathSoFar", "The color of the best path so far");
        BaritoneSettings.addDescription("colorBlocksToBreak", "The color of the blocks to break");
        BaritoneSettings.addDescription("colorBlocksToPlace", "The color of the blocks to place");
        BaritoneSettings.addDescription("colorBlocksToWalkInto", "The color of the blocks to walk into");
        BaritoneSettings.addDescription("colorCurrentPath", "The color of the current path");
        BaritoneSettings.addDescription("colorGoalBox", "The color of the goal box");
        BaritoneSettings.addDescription("colorInvertedGoalBox", "The color of the goal box when it's inverted");
        BaritoneSettings.addDescription("colorMostRecentConsidered", "The color of the path to the most recent considered node");
        BaritoneSettings.addDescription("colorNextPath", "The color of the next path");
        BaritoneSettings.addDescription("colorSelection", "The color of all selections");
        BaritoneSettings.addDescription("colorSelectionPos1", "The color of the selection pos 1");
        BaritoneSettings.addDescription("colorSelectionPos2", "The color of the selection pos 2");
        BaritoneSettings.addDescription("considerPotionEffects", "For example, if you have Mining Fatigue or Haste, adjust the costs of breaking blocks accordingly.");
        BaritoneSettings.addDescription("costHeuristic", "This is the big A* setting.");
        BaritoneSettings.addDescription("costVerificationLookahead", "Stop 5 movements before anything that made the path COST_INF.");
        BaritoneSettings.addDescription("cutoffAtLoadBoundary", "After calculating a path (potentially through cached chunks), artificially cut it off to just the part that is entirely within currently loaded chunks.");
        BaritoneSettings.addDescription("desktopNotifications", "Desktop notifications");
        BaritoneSettings.addDescription("disableCompletionCheck", "Turn this on if your exploration filter is enormous, you don't want it to check if it's done, and you are just fine with it just hanging on completion");
        BaritoneSettings.addDescription("disconnectOnArrival", "Disconnect from the server upon arriving at your goal");
        BaritoneSettings.addDescription("distanceTrim", "Trim incorrect positions too far away, helps performance but hurts reliability in very large schematics");
        BaritoneSettings.addDescription("doBedWaypoints", "Allows baritone to save bed waypoints when interacting with beds");
        BaritoneSettings.addDescription("doDeathWaypoints", "Allows baritone to save death waypoints");
        BaritoneSettings.addDescription("echoCommands", "Echo commands to chat when they are run");
        BaritoneSettings.addDescription("enterPortal", "When running a goto towards a nether portal block, walk all the way into the portal instead of stopping one block before.");
        BaritoneSettings.addDescription("exploreChunkSetMinimumSize", "Take the 10 closest chunks, even if they aren't strictly tied for distance metric from origin.");
        BaritoneSettings.addDescription("exploreForBlocks", "When GetToBlock or non-legit Mine doesn't know any locations for the desired block, explore randomly instead of giving up.");
        BaritoneSettings.addDescription("exploreMaintainY", "Attempt to maintain Y coordinate while exploring");
        BaritoneSettings.addDescription("extendCacheOnThreshold", "When the cache scan gives less blocks than the maximum threshold (but still above zero), scan the main world too.");
        BaritoneSettings.addDescription("fadePath", "Start fading out the path at 20 movements ahead, and stop rendering it entirely 30 movements ahead.");
        BaritoneSettings.addDescription("failureTimeoutMS", "Pathing can never take longer than this, even if that means failing to find any path at all");
        BaritoneSettings.addDescription("followOffsetDirection", "The actual GoalNear is set in this direction from the entity you're following.");
        BaritoneSettings.addDescription("followOffsetDistance", "The actual GoalNear is set this distance away from the entity you're following");
        BaritoneSettings.addDescription("followRadius", "The radius (for the GoalNear) of how close to your target position you actually have to be");
        BaritoneSettings.addDescription("forceInternalMining", "When mining block of a certain type, try to mine two at once instead of one.");
        BaritoneSettings.addDescription("freeLook", "Move without having to force the client-sided rotations");
        BaritoneSettings.addDescription("goalBreakFromAbove", "As well as breaking from above, set a goal to up and to the side of all blocks to break.");
        BaritoneSettings.addDescription("goalRenderLineWidthPixels", "Line width of the goal when rendered, in pixels");
        BaritoneSettings.addDescription("incorrectSize", "The set of incorrect blocks can never grow beyond this size");
        BaritoneSettings.addDescription("internalMiningAirException", "Modification to the previous setting, only has effect if forceInternalMining is true If true, only apply the previous setting if the block adjacent to the goal isn't air.");
        BaritoneSettings.addDescription("itemSaver", "Stop using tools just before they are going to break.");
        BaritoneSettings.addDescription("itemSaverThreshold", "Durability to leave on the tool when using itemSaver");
        BaritoneSettings.addDescription("jumpPenalty", "Additional penalty for hitting the space bar (ascend, pillar, or parkour) because it uses hunger");
        BaritoneSettings.addDescription("layerHeight", "How high should the individual layers be?");
        BaritoneSettings.addDescription("layerOrder", "false = build from bottom to top");
        BaritoneSettings.addDescription("legitMine", "Disallow MineBehavior from using X-Ray to see where the ores are.");
        BaritoneSettings.addDescription("legitMineIncludeDiagonals", "Magically see ores that are separated diagonally from existing ores.");
        BaritoneSettings.addDescription("legitMineYLevel", "What Y level to go to for legit strip mining");
        BaritoneSettings.addDescription("logAsToast", "Shows popup message in the upper right corner, similarly to when you make an advancement");
        BaritoneSettings.addDescription("mapArtMode", "Build in map art mode, which makes baritone only care about the top block in each column");
        BaritoneSettings.addDescription("maxCachedWorldScanCount", "After finding this many instances of the target block in the cache, it will stop expanding outward the chunk search.");
        BaritoneSettings.addDescription("maxCostIncrease", "If a movement's cost increases by more than this amount between calculation and execution (due to changes in the environment / world), cancel and recalculate");
        BaritoneSettings.addDescription("maxFallHeightBucket", "How far are you allowed to fall onto solid ground (with a water bucket)? It's not that reliable, so I've set it below what would kill an unarmored player (23)");
        BaritoneSettings.addDescription("maxFallHeightNoWater", "How far are you allowed to fall onto solid ground (without a water bucket)? 3 won't deal any damage.");
        BaritoneSettings.addDescription("maxPathHistoryLength", "If we are more than 300 movements into the current path, discard the oldest segments, as they are no longer useful");
        BaritoneSettings.addDescription("mineDropLoiterDurationMSThanksLouca", "While mining, wait this number of milliseconds after mining an ore to see if it will drop an item instead of immediately going onto the next one");
        BaritoneSettings.addDescription("mineGoalUpdateInterval", "Rescan for the goal once every 5 ticks.");
        BaritoneSettings.addDescription("mineScanDroppedItems", "While mining, should it also consider dropped items of the correct type as a pathing destination (as well as ore blocks)?");
        BaritoneSettings.addDescription("minimumImprovementRepropagation", "Don't repropagate cost improvements below 0.01 ticks.");
        BaritoneSettings.addDescription("minYLevelWhileMining", "Sets the minimum y level whilst mining - set to 0 to turn off. if world has negative y values, subtract the min world height to get the value to put here");
        BaritoneSettings.addDescription("mobAvoidanceCoefficient", "Set to 1.0 to effectively disable this feature");
        BaritoneSettings.addDescription("mobAvoidanceRadius", "Distance to avoid mobs.");
        BaritoneSettings.addDescription("mobSpawnerAvoidanceCoefficient", "Set to 1.0 to effectively disable this feature");
        BaritoneSettings.addDescription("mobSpawnerAvoidanceRadius", "Distance to avoid mob spawners.");
        BaritoneSettings.addDescription("movementTimeoutTicks", "If a movement takes this many ticks more than its initial cost estimate, cancel it");
        BaritoneSettings.addDescription("notificationOnBuildFinished", "Desktop notification on build finished");
        BaritoneSettings.addDescription("notificationOnExploreFinished", "Desktop notification on explore finished");
        BaritoneSettings.addDescription("notificationOnFarmFail", "Desktop notification on farm fail");
        BaritoneSettings.addDescription("notificationOnMineFail", "Desktop notification on mine fail");
        BaritoneSettings.addDescription("notificationOnPathComplete", "Desktop notification on path complete");
        BaritoneSettings.addDescription("notifier", "The function that is called when Baritone will send a desktop notification.");
        BaritoneSettings.addDescription("okIfAir", "A list of blocks to become air");
        BaritoneSettings.addDescription("okIfWater", "Override builder's behavior to not attempt to correct blocks that are currently water");
        BaritoneSettings.addDescription("overshootTraverse", "If we overshoot a traverse and end up one block beyond the destination, mark it as successful anyway.");
        BaritoneSettings.addDescription("pathCutoffFactor", "Static cutoff factor.");
        BaritoneSettings.addDescription("pathCutoffMinimumLength", "Only apply static cutoff for paths of at least this length (in terms of number of movements)");
        BaritoneSettings.addDescription("pathHistoryCutoffAmount", "If the current path is too long, cut off this many movements from the beginning.");
        BaritoneSettings.addDescription("pathingMapDefaultSize", "Default size of the Long2ObjectOpenHashMap used in pathing");
        BaritoneSettings.addDescription("pathingMapLoadFactor", "Load factor coefficient for the Long2ObjectOpenHashMap used in pathing");
        BaritoneSettings.addDescription("pathingMaxChunkBorderFetch", "The maximum number of times it will fetch outside loaded or cached chunks before assuming that pathing has reached the end of the known area, and should therefore stop.");
        BaritoneSettings.addDescription("pathRenderLineWidthPixels", "Line width of the path when rendered, in pixels");
        BaritoneSettings.addDescription("pathThroughCachedOnly", "Exclusively use cached chunks for pathing");
        BaritoneSettings.addDescription("pauseMiningForFallingBlocks", "When breaking blocks for a movement, wait until all falling blocks have settled before continuing");
        BaritoneSettings.addDescription("planAheadFailureTimeoutMS", "Planning ahead while executing a segment can never take longer than this, even if that means failing to find any path at all");
        BaritoneSettings.addDescription("planAheadPrimaryTimeoutMS", "Planning ahead while executing a segment ends after this amount of time, but only if a path has been found");
        BaritoneSettings.addDescription("planningTickLookahead", "Start planning the next path once the remaining movements tick estimates sum up to less than this value");
        BaritoneSettings.addDescription("preferSilkTouch", "Always prefer silk touch tools over regular tools.");
        BaritoneSettings.addDescription("prefix", "The command prefix for chat control");
        BaritoneSettings.addDescription("prefixControl", "Whether or not to allow you to run Baritone commands with the prefix");
        BaritoneSettings.addDescription("primaryTimeoutMS", "Pathing ends after this amount of time, but only if a path has been found");
        BaritoneSettings.addDescription("pruneRegionsFromRAM", "On save, delete from RAM any cached regions that are more than 1024 blocks away from the player");
        BaritoneSettings.addDescription("randomLooking", "How many degrees to randomize the pitch and yaw every tick.");
        BaritoneSettings.addDescription("randomLooking113", "How many degrees to randomize the yaw every tick. Set to 0 to disable");
        BaritoneSettings.addDescription("renderCachedChunks", "Render cached chunks as semitransparent.");
        BaritoneSettings.addDescription("renderGoal", "Render the goal");
        BaritoneSettings.addDescription("renderGoalAnimated", "Render the goal as a sick animated thingy instead of just a box (also controls animation of GoalXZ if renderGoalXZBeacon is enabled)");
        BaritoneSettings.addDescription("renderGoalIgnoreDepth", "Ignore depth when rendering the goal");
        BaritoneSettings.addDescription("renderGoalXZBeacon", "Renders X/Z type Goals with the vanilla beacon beam effect.");
        BaritoneSettings.addDescription("renderPath", "Render the path");
        BaritoneSettings.addDescription("renderPathAsLine", "Render the path as a line instead of a frickin thingy");
        BaritoneSettings.addDescription("renderPathIgnoreDepth", "Ignore depth when rendering the path");
        BaritoneSettings.addDescription("renderSelection", "Render selections");
        BaritoneSettings.addDescription("renderSelectionBoxes", "Render selection boxes");
        BaritoneSettings.addDescription("renderSelectionBoxesIgnoreDepth", "Ignore depth when rendering the selection boxes (to break, to place, to walk into)");
        BaritoneSettings.addDescription("renderSelectionCorners", "Render selection corners");
        BaritoneSettings.addDescription("renderSelectionIgnoreDepth", "Ignore depth when rendering selections");
        BaritoneSettings.addDescription("repackOnAnyBlockChange", "Whenever a block changes, repack the whole chunk that it's in");
        BaritoneSettings.addDescription("replantCrops", "Replant normal Crops while farming and leave cactus and sugarcane to regrow");
        BaritoneSettings.addDescription("replantNetherWart", "Replant nether wart while farming.");
        BaritoneSettings.addDescription("rightClickContainerOnArrival", "When running a goto towards a container block (chest, ender chest, furnace, etc), right click and open it once you arrive.");
        BaritoneSettings.addDescription("rightClickSpeed", "How many ticks between right clicks are allowed.");
        BaritoneSettings.addDescription("schematicFallbackExtension", "The fallback used by the build command when no extension is specified.");
        BaritoneSettings.addDescription("schematicOrientationX", "When this setting is true, build a schematic with the highest X coordinate being the origin, instead of the lowest");
        BaritoneSettings.addDescription("schematicOrientationY", "When this setting is true, build a schematic with the highest Y coordinate being the origin, instead of the lowest");
        BaritoneSettings.addDescription("schematicOrientationZ", "When this setting is true, build a schematic with the highest Z coordinate being the origin, instead of the lowest");
        BaritoneSettings.addDescription("selectionLineWidth", "Line width of the goal when rendered, in pixels");
        BaritoneSettings.addDescription("selectionOpacity", "The opacity of the selection.");
        BaritoneSettings.addDescription("shortBaritonePrefix", "Use a short Baritone prefix [B] instead of [Baritone] when logging to chat");
        BaritoneSettings.addDescription("simplifyUnloadedYCoord", "If your goal is a GoalBlock in an unloaded chunk, assume it's far enough away that the Y coord doesn't matter yet, and replace it with a GoalXZ to the same place before calculating a path.");
        BaritoneSettings.addDescription("skipFailedLayers", "If a layer is unable to be constructed, just skip it.");
        BaritoneSettings.addDescription("slowPath", "For debugging, consider nodes much much slower");
        BaritoneSettings.addDescription("slowPathTimeDelayMS", "Milliseconds between each node");
        BaritoneSettings.addDescription("slowPathTimeoutMS", "The alternative timeout number when slowPath is on");
        BaritoneSettings.addDescription("splicePath", "When a new segment is calculated that doesn't overlap with the current one, but simply begins where the current segment ends, splice it on and make a longer combined path.");
        BaritoneSettings.addDescription("sprintAscends", "Sprint and jump a block early on ascends wherever possible");
        BaritoneSettings.addDescription("sprintInWater", "Continue sprinting while in water");
        BaritoneSettings.addDescription("startAtLayer", "Start building the schematic at a specific layer.");
        BaritoneSettings.addDescription("toaster", "The function that is called when Baritone will show a toast.");
        BaritoneSettings.addDescription("toastTimer", "The time of how long the message in the pop-up will display");
        BaritoneSettings.addDescription("useSwordToMine", "Use sword to mine.");
        BaritoneSettings.addDescription("verboseCommandExceptions", "Print out ALL command exceptions as a stack trace to stdout, even simple syntax errors");
        BaritoneSettings.addDescription("walkOnWaterOnePenalty", "Walking on water uses up hunger really quick, so penalize it");
        BaritoneSettings.addDescription("walkWhileBreaking", "Don't stop walking forward when you need to break blocks in your way");
        BaritoneSettings.addDescription("worldExploringChunkOffset", "While exploring the world, offset the closest unloaded chunk by this much in both axes.");
        BaritoneSettings.addDescription("yLevelBoxSize", "The size of the box that is rendered when the current goal is a GoalYLevel");
    }

    static {
        SETTING_MAX_VALUES.put("pathCutoffFactor", 1.0);
    }
}

