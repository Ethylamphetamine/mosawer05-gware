/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.systems.VertexSorter
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Reference2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.ShulkerBoxBlock
 *  net.minecraft.block.entity.BlockEntity
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.TitleScreen
 *  net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
 *  net.minecraft.client.gui.screen.world.SelectWorldScreen
 *  net.minecraft.client.resource.ResourceReloadLogger$ReloadState
 *  net.minecraft.component.ComponentMap
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.component.type.ItemEnchantmentsComponent$Builder
 *  net.minecraft.component.type.NbtComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.CrossbowItem
 *  net.minecraft.item.EggItem
 *  net.minecraft.item.EnderPearlItem
 *  net.minecraft.item.ExperienceBottleItem
 *  net.minecraft.item.FishingRodItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.LingeringPotionItem
 *  net.minecraft.item.SnowballItem
 *  net.minecraft.item.SplashPotionItem
 *  net.minecraft.item.TridentItem
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.util.DyeColor
 *  net.minecraft.util.collection.DefaultedList
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.chunk.Chunk
 *  org.apache.commons.io.IOUtils
 *  org.apache.commons.lang3.StringUtils
 *  org.jetbrains.annotations.Range
 *  org.joml.Matrix4f
 *  org.joml.Vector3d
 *  org.lwjgl.glfw.GLFW
 */
package meteordevelopment.meteorclient.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.mixin.ContainerComponentAccessor;
import meteordevelopment.meteorclient.mixin.MinecraftClientAccessor;
import meteordevelopment.meteorclient.mixin.MinecraftServerAccessor;
import meteordevelopment.meteorclient.mixin.ReloadStateAccessor;
import meteordevelopment.meteorclient.mixin.ResourceReloadLoggerAccessor;
import meteordevelopment.meteorclient.mixininterface.IMinecraftClient;
import meteordevelopment.meteorclient.settings.StatusEffectAmplifierMapSetting;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.render.PeekScreen;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.world.BlockEntityIterator;
import meteordevelopment.meteorclient.utils.world.ChunkIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Range;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

public class Utils {
    public static final Pattern FILE_NAME_INVALID_CHARS_PATTERN = Pattern.compile("[\\s\\\\/:*?\"<>|]");
    public static final Color WHITE = new Color(255, 255, 255);
    private static final Random random = new Random();
    public static boolean firstTimeTitleScreen = true;
    public static boolean isReleasingTrident;
    public static boolean rendering3D;
    public static double frameTime;
    public static Screen screenToOpen;
    public static VertexSorter vertexSorter;

    private Utils() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Utils.class);
    }

    @EventHandler
    private static void onTick(TickEvent.Post event) {
        if (screenToOpen != null && MeteorClient.mc.currentScreen == null) {
            MeteorClient.mc.setScreen(screenToOpen);
            screenToOpen = null;
        }
    }

    public static Vec3d getPlayerSpeed() {
        if (MeteorClient.mc.player == null) {
            return Vec3d.ZERO;
        }
        double tX = MeteorClient.mc.player.getX() - MeteorClient.mc.player.prevX;
        double tY = MeteorClient.mc.player.getY() - MeteorClient.mc.player.prevY;
        double tZ = MeteorClient.mc.player.getZ() - MeteorClient.mc.player.prevZ;
        Timer timer = Modules.get().get(Timer.class);
        if (timer.isActive()) {
            tX *= timer.getMultiplier();
            tY *= timer.getMultiplier();
            tZ *= timer.getMultiplier();
        }
        return new Vec3d(tX *= 20.0, tY *= 20.0, tZ *= 20.0);
    }

    public static String getWorldTime() {
        if (MeteorClient.mc.world == null) {
            return "00:00";
        }
        int ticks = (int)(MeteorClient.mc.world.getTimeOfDay() % 24000L);
        if ((ticks += 6000) > 24000) {
            ticks -= 24000;
        }
        return String.format("%02d:%02d", ticks / 1000, (int)((double)(ticks % 1000) / 1000.0 * 60.0));
    }

    public static Iterable<Chunk> chunks(boolean onlyWithLoadedNeighbours) {
        return () -> new ChunkIterator(onlyWithLoadedNeighbours);
    }

    public static Iterable<Chunk> chunks() {
        return Utils.chunks(false);
    }

    public static Iterable<BlockEntity> blockEntities() {
        return BlockEntityIterator::new;
    }

    public static void getEnchantments(ItemStack itemStack, Object2IntMap<RegistryEntry<Enchantment>> enchantments) {
        enchantments.clear();
        if (!itemStack.isEmpty()) {
            Set itemEnchantments = itemStack.getItem() == Items.ENCHANTED_BOOK ? ((ItemEnchantmentsComponent)itemStack.get(DataComponentTypes.STORED_ENCHANTMENTS)).getEnchantmentEntries() : itemStack.getEnchantments().getEnchantmentEntries();
            for (Object2IntMap.Entry entry : itemEnchantments) {
                enchantments.put((Object)((RegistryEntry)entry.getKey()), entry.getIntValue());
            }
        }
    }

    public static int getEnchantmentLevel(ItemStack itemStack, RegistryKey<Enchantment> enchantment) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        Utils.getEnchantments(itemStack, (Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments);
        return Utils.getEnchantmentLevel((Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable(itemEnchantments)) {
            if (!((RegistryEntry)entry.getKey()).matchesKey(enchantment)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    @SafeVarargs
    public static boolean hasEnchantments(ItemStack itemStack, RegistryKey<Enchantment> ... enchantments) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        Utils.getEnchantments(itemStack, (Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments);
        for (RegistryKey<Enchantment> enchantment : enchantments) {
            if (Utils.hasEnchantment((Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments, enchantment)) continue;
            return false;
        }
        return true;
    }

    public static boolean hasEnchantment(ItemStack itemStack, RegistryKey<Enchantment> enchantmentKey) {
        if (itemStack.isEmpty()) {
            return false;
        }
        Object2IntArrayMap itemEnchantments = new Object2IntArrayMap();
        Utils.getEnchantments(itemStack, (Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments);
        return Utils.hasEnchantment((Object2IntMap<RegistryEntry<Enchantment>>)itemEnchantments, enchantmentKey);
    }

    private static boolean hasEnchantment(Object2IntMap<RegistryEntry<Enchantment>> itemEnchantments, RegistryKey<Enchantment> enchantmentKey) {
        for (RegistryEntry enchantment : itemEnchantments.keySet()) {
            if (!enchantment.matchesKey(enchantmentKey)) continue;
            return true;
        }
        return false;
    }

    public static int getRenderDistance() {
        return Math.max((Integer)MeteorClient.mc.options.getViewDistance().getValue(), ((ClientPlayNetworkHandlerAccessor)MeteorClient.mc.getNetworkHandler()).getChunkLoadDistance());
    }

    public static int getWindowWidth() {
        return MeteorClient.mc.getWindow().getFramebufferWidth();
    }

    public static int getWindowHeight() {
        return MeteorClient.mc.getWindow().getFramebufferHeight();
    }

    public static void unscaledProjection() {
        vertexSorter = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix((Matrix4f)new Matrix4f().setOrtho(0.0f, (float)MeteorClient.mc.getWindow().getFramebufferWidth(), (float)MeteorClient.mc.getWindow().getFramebufferHeight(), 0.0f, 1000.0f, 21000.0f), (VertexSorter)VertexSorter.BY_Z);
        rendering3D = false;
    }

    public static void scaledProjection() {
        RenderSystem.setProjectionMatrix((Matrix4f)new Matrix4f().setOrtho(0.0f, (float)((double)MeteorClient.mc.getWindow().getFramebufferWidth() / MeteorClient.mc.getWindow().getScaleFactor()), (float)((double)MeteorClient.mc.getWindow().getFramebufferHeight() / MeteorClient.mc.getWindow().getScaleFactor()), 0.0f, 1000.0f, 21000.0f), (VertexSorter)vertexSorter);
        rendering3D = true;
    }

    public static Vec3d vec3d(BlockPos pos) {
        return new Vec3d((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
    }

    public static boolean openContainer(ItemStack itemStack, ItemStack[] contents, boolean pause) {
        if (Utils.hasItems(itemStack) || itemStack.getItem() == Items.ENDER_CHEST) {
            Utils.getItemsInContainerItem(itemStack, contents);
            if (pause) {
                screenToOpen = new PeekScreen(itemStack, contents);
            } else {
                MeteorClient.mc.setScreen((Screen)new PeekScreen(itemStack, contents));
            }
            return true;
        }
        return false;
    }

    public static void getItemsInContainerItem(ItemStack itemStack, ItemStack[] items) {
        block5: {
            NbtComponent nbt2;
            ComponentMap components;
            block4: {
                if (itemStack.getItem() == Items.ENDER_CHEST) {
                    for (int i = 0; i < EChestMemory.ITEMS.size(); ++i) {
                        items[i] = (ItemStack)EChestMemory.ITEMS.get(i);
                    }
                    return;
                }
                Arrays.fill(items, ItemStack.EMPTY);
                components = itemStack.getComponents();
                if (!components.contains(DataComponentTypes.CONTAINER)) break block4;
                ContainerComponentAccessor container = (ContainerComponentAccessor)components.get(DataComponentTypes.CONTAINER);
                DefaultedList<ItemStack> stacks = container.getStacks();
                for (int i = 0; i < stacks.size(); ++i) {
                    if (i < 0 || i >= items.length) continue;
                    items[i] = (ItemStack)stacks.get(i);
                }
                break block5;
            }
            if (!components.contains(DataComponentTypes.BLOCK_ENTITY_DATA) || !(nbt2 = (NbtComponent)components.get(DataComponentTypes.BLOCK_ENTITY_DATA)).contains("Items")) break block5;
            NbtList nbt3 = (NbtList)nbt2.getNbt().get("Items");
            for (int i = 0; i < nbt3.size(); ++i) {
                byte slot = nbt3.getCompound(i).getByte("Slot");
                if (slot < 0 || slot >= items.length) continue;
                items[slot] = ItemStack.fromNbtOrEmpty((RegistryWrapper.WrapperLookup)MeteorClient.mc.player.getRegistryManager(), (NbtCompound)nbt3.getCompound(i));
            }
        }
    }

    public static Color getShulkerColor(ItemStack shulkerItem) {
        Item item = shulkerItem.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            Block block = blockItem.getBlock();
            if (block == Blocks.ENDER_CHEST) {
                return BetterTooltips.ECHEST_COLOR;
            }
            if (block instanceof ShulkerBoxBlock) {
                ShulkerBoxBlock shulkerBlock = (ShulkerBoxBlock)block;
                DyeColor dye = shulkerBlock.getColor();
                if (dye == null) {
                    return WHITE;
                }
                int color = dye.getEntityColor();
                return new Color((float)(color >> 16 & 0xFF), (float)(color >> 8 & 0xFF), (float)(color & 0xFF), 1.0f);
            }
        }
        return WHITE;
    }

    public static boolean hasItems(ItemStack itemStack) {
        ContainerComponentAccessor container = (ContainerComponentAccessor)itemStack.get(DataComponentTypes.CONTAINER);
        if (container != null && !container.getStacks().isEmpty()) {
            return true;
        }
        NbtCompound compoundTag = ((NbtComponent)itemStack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, (Object)NbtComponent.DEFAULT)).getNbt();
        return compoundTag != null && compoundTag.contains("Items", 9);
    }

    public static Reference2IntMap<StatusEffect> createStatusEffectMap() {
        return new Reference2IntArrayMap(StatusEffectAmplifierMapSetting.EMPTY_STATUS_EFFECT_MAP);
    }

    public static String getEnchantSimpleName(RegistryEntry<Enchantment> enchantment, int length) {
        String name = Names.get(enchantment);
        return name.length() > length ? name.substring(0, length) : name;
    }

    public static boolean searchTextDefault(String text, String filter, boolean caseSensitive) {
        return Utils.searchInWords(text, filter) > 0 || Utils.searchLevenshteinDefault(text, filter, caseSensitive) < text.length() / 2;
    }

    public static int searchLevenshteinDefault(String text, String filter, boolean caseSensitive) {
        return Utils.levenshteinDistance(caseSensitive ? filter : filter.toLowerCase(Locale.ROOT), caseSensitive ? text : text.toLowerCase(Locale.ROOT), 1, 8, 8);
    }

    public static int searchInWords(String text, String filter) {
        String[] words;
        if (filter.isEmpty()) {
            return 1;
        }
        int wordsFound = 0;
        text = text.toLowerCase(Locale.ROOT);
        for (String word : words = filter.toLowerCase(Locale.ROOT).split(" ")) {
            if (!text.contains(word)) {
                return 0;
            }
            wordsFound += StringUtils.countMatches((CharSequence)text, (CharSequence)word);
        }
        return wordsFound;
    }

    public static int levenshteinDistance(String from, String to, int insCost, int subCost, int delCost) {
        int i;
        int textLength = from.length();
        int filterLength = to.length();
        if (textLength == 0) {
            return filterLength * insCost;
        }
        if (filterLength == 0) {
            return textLength * delCost;
        }
        int[][] d = new int[textLength + 1][filterLength + 1];
        for (i = 0; i <= textLength; ++i) {
            d[i][0] = i * delCost;
        }
        for (int j = 0; j <= filterLength; ++j) {
            d[0][j] = j * insCost;
        }
        for (i = 1; i <= textLength; ++i) {
            for (int j = 1; j <= filterLength; ++j) {
                int sCost = d[i - 1][j - 1] + (from.charAt(i - 1) == to.charAt(j - 1) ? 0 : subCost);
                int dCost = d[i - 1][j] + delCost;
                int iCost = d[i][j - 1] + insCost;
                d[i][j] = Math.min(Math.min(dCost, iCost), sCost);
            }
        }
        return d[textLength][filterLength];
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return dX * dX + dY * dY + dZ * dZ;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dX = x2 - x1;
        double dY = y2 - y1;
        double dZ = z2 - z1;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
    }

    public static String getFileWorldName() {
        return FILE_NAME_INVALID_CHARS_PATTERN.matcher(Utils.getWorldName()).replaceAll("_");
    }

    public static String getWorldName() {
        if (MeteorClient.mc.isInSingleplayer()) {
            if (MeteorClient.mc.world == null) {
                return "";
            }
            File folder = ((MinecraftServerAccessor)MeteorClient.mc.getServer()).getSession().getWorldDirectory(MeteorClient.mc.world.getRegistryKey()).toFile();
            if (folder.toPath().relativize(MeteorClient.mc.runDirectory.toPath()).getNameCount() != 2) {
                folder = folder.getParentFile();
            }
            return folder.getName();
        }
        if (MeteorClient.mc.getCurrentServerEntry() != null) {
            return MeteorClient.mc.getCurrentServerEntry().isRealm() ? "realms" : MeteorClient.mc.getCurrentServerEntry().address;
        }
        return "";
    }

    public static String nameToTitle(String name) {
        return Arrays.stream(name.split("-")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public static String titleToName(String title) {
        return title.replace(" ", "-").toLowerCase(Locale.ROOT);
    }

    public static String getKeyName(int key) {
        return switch (key) {
            case -1 -> "Unknown";
            case 256 -> "Esc";
            case 96 -> "Grave Accent";
            case 161 -> "World 1";
            case 162 -> "World 2";
            case 283 -> "Print Screen";
            case 284 -> "Pause";
            case 260 -> "Insert";
            case 261 -> "Delete";
            case 268 -> "Home";
            case 266 -> "Page Up";
            case 267 -> "Page Down";
            case 269 -> "End";
            case 258 -> "Tab";
            case 341 -> "Left Control";
            case 345 -> "Right Control";
            case 342 -> "Left Alt";
            case 346 -> "Right Alt";
            case 340 -> "Left Shift";
            case 344 -> "Right Shift";
            case 265 -> "Arrow Up";
            case 264 -> "Arrow Down";
            case 263 -> "Arrow Left";
            case 262 -> "Arrow Right";
            case 39 -> "Apostrophe";
            case 259 -> "Backspace";
            case 280 -> "Caps Lock";
            case 348 -> "Menu";
            case 343 -> "Left Super";
            case 347 -> "Right Super";
            case 257 -> "Enter";
            case 335 -> "Numpad Enter";
            case 282 -> "Num Lock";
            case 32 -> "Space";
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 302 -> "F13";
            case 303 -> "F14";
            case 304 -> "F15";
            case 305 -> "F16";
            case 306 -> "F17";
            case 307 -> "F18";
            case 308 -> "F19";
            case 309 -> "F20";
            case 310 -> "F21";
            case 311 -> "F22";
            case 312 -> "F23";
            case 313 -> "F24";
            case 314 -> "F25";
            default -> {
                String keyName = GLFW.glfwGetKeyName((int)key, (int)0);
                if (keyName == null) {
                    yield "Unknown";
                }
                yield StringUtils.capitalize((String)keyName);
            }
        };
    }

    public static String getButtonName(int button) {
        return switch (button) {
            case -1 -> "Unknown";
            case 0 -> "Mouse Left";
            case 1 -> "Mouse Right";
            case 2 -> "Mouse Middle";
            default -> "Mouse " + button;
        };
    }

    public static byte[] readBytes(InputStream in) {
        try {
            byte[] byArray = in.readAllBytes();
            return byArray;
        }
        catch (IOException e) {
            MeteorClient.LOG.error("Error reading from stream.", (Throwable)e);
            byte[] byArray = new byte[]{};
            return byArray;
        }
        finally {
            IOUtils.closeQuietly((InputStream)in);
        }
    }

    public static boolean canUpdate() {
        return MeteorClient.mc != null && MeteorClient.mc.world != null && MeteorClient.mc.player != null;
    }

    public static boolean canOpenGui() {
        if (Utils.canUpdate()) {
            return MeteorClient.mc.currentScreen == null;
        }
        return MeteorClient.mc.currentScreen instanceof TitleScreen || MeteorClient.mc.currentScreen instanceof MultiplayerScreen || MeteorClient.mc.currentScreen instanceof SelectWorldScreen;
    }

    public static boolean canCloseGui() {
        return MeteorClient.mc.currentScreen instanceof TabScreen;
    }

    public static int random(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static double random(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static void leftClick() {
        MeteorClient.mc.options.attackKey.setPressed(true);
        ((MinecraftClientAccessor)MeteorClient.mc).leftClick();
        MeteorClient.mc.options.attackKey.setPressed(false);
    }

    public static void rightClick() {
        ((IMinecraftClient)MeteorClient.mc).meteor_client$rightClick();
    }

    public static boolean isShulker(Item item) {
        return item == Items.SHULKER_BOX || item == Items.WHITE_SHULKER_BOX || item == Items.ORANGE_SHULKER_BOX || item == Items.MAGENTA_SHULKER_BOX || item == Items.LIGHT_BLUE_SHULKER_BOX || item == Items.YELLOW_SHULKER_BOX || item == Items.LIME_SHULKER_BOX || item == Items.PINK_SHULKER_BOX || item == Items.GRAY_SHULKER_BOX || item == Items.LIGHT_GRAY_SHULKER_BOX || item == Items.CYAN_SHULKER_BOX || item == Items.PURPLE_SHULKER_BOX || item == Items.BLUE_SHULKER_BOX || item == Items.BROWN_SHULKER_BOX || item == Items.GREEN_SHULKER_BOX || item == Items.RED_SHULKER_BOX || item == Items.BLACK_SHULKER_BOX;
    }

    public static boolean isThrowable(Item item) {
        return item instanceof ExperienceBottleItem || item instanceof BowItem || item instanceof CrossbowItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof FishingRodItem || item instanceof TridentItem;
    }

    public static void addEnchantment(ItemStack itemStack, RegistryEntry<Enchantment> enchantment, int level) {
        ItemEnchantmentsComponent.Builder b = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments((ItemStack)itemStack));
        b.add(enchantment, level);
        EnchantmentHelper.set((ItemStack)itemStack, (ItemEnchantmentsComponent)b.build());
    }

    public static void clearEnchantments(ItemStack itemStack) {
        EnchantmentHelper.apply((ItemStack)itemStack, components -> components.remove(a -> true));
    }

    public static void removeEnchantment(ItemStack itemStack, Enchantment enchantment) {
        EnchantmentHelper.apply((ItemStack)itemStack, components -> components.remove(enchantment1 -> ((Enchantment)enchantment1.comp_349()).equals((Object)enchantment)));
    }

    public static Color lerp(Color first, Color second, @Range(from=0L, to=1L) float v) {
        return new Color((int)((float)first.r * (1.0f - v) + (float)second.r * v), (int)((float)first.g * (1.0f - v) + (float)second.g * v), (int)((float)first.b * (1.0f - v) + (float)second.b * v));
    }

    public static boolean isLoading() {
        ResourceReloadLogger.ReloadState state = ((ResourceReloadLoggerAccessor)((MinecraftClientAccessor)MeteorClient.mc).getResourceReloadLogger()).getReloadState();
        return state == null || !((ReloadStateAccessor)state).isFinished();
    }

    public static int parsePort(String full) {
        int port;
        if (full == null || full.isBlank() || !full.contains(":")) {
            return -1;
        }
        try {
            port = Integer.parseInt(full.substring(full.lastIndexOf(58) + 1, full.length() - 1));
        }
        catch (NumberFormatException ignored) {
            port = -1;
        }
        return port;
    }

    public static String parseAddress(String full) {
        if (full == null || full.isBlank() || !full.contains(":")) {
            return full;
        }
        return full.substring(0, full.lastIndexOf(58));
    }

    public static boolean resolveAddress(String address) {
        if (address == null || address.isBlank()) {
            return false;
        }
        int port = Utils.parsePort(address);
        if (port == -1) {
            port = 25565;
        } else {
            address = Utils.parseAddress(address);
        }
        return Utils.resolveAddress(address, port);
    }

    public static boolean resolveAddress(String address, int port) {
        if (port <= 0 || port > 65535 || address == null || address.isBlank()) {
            return false;
        }
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);
        return !socketAddress.isUnresolved();
    }

    public static Vector3d set(Vector3d vec, Vec3d v) {
        vec.x = v.x;
        vec.y = v.y;
        vec.z = v.z;
        return vec;
    }

    public static Vector3d set(Vector3d vec, Entity entity, double tickDelta) {
        vec.x = MathHelper.lerp((double)tickDelta, (double)entity.lastRenderX, (double)entity.getX());
        vec.y = MathHelper.lerp((double)tickDelta, (double)entity.lastRenderY, (double)entity.getY());
        vec.z = MathHelper.lerp((double)tickDelta, (double)entity.lastRenderZ, (double)entity.getZ());
        return vec;
    }

    public static boolean nameFilter(String text, char character) {
        return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z' || character >= '0' && character <= '9' || character == '_' || character == '-' || character == '.' || character == ' ';
    }

    public static boolean ipFilter(String text, char character) {
        if (text.contains(":") && character == ':') {
            return false;
        }
        return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z' || character >= '0' && character <= '9' || character == '.' || character == '-';
    }

    static {
        rendering3D = true;
    }
}

