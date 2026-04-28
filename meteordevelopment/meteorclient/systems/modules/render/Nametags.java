/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.enchantment.Enchantments
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.ItemEntity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.TntEntity
 *  net.minecraft.entity.decoration.ItemFrameEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.entity.vehicle.TntMinecartEntity
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.registry.tag.EnchantmentTags
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.GameMode
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.joml.Vector3d;

public class Nametags
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPlayers;
    private final SettingGroup sgItems;
    private final SettingGroup sgRender;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> scale;
    private final Setting<Boolean> ignoreSelf;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> ignoreBots;
    private final Setting<Boolean> culling;
    private final Setting<Double> maxCullRange;
    private final Setting<Integer> maxCullCount;
    private final Setting<Boolean> displayHealth;
    private final Setting<Boolean> displayTotemPops;
    private final Setting<Boolean> displayGameMode;
    private final Setting<Boolean> displayDistance;
    private final Setting<Boolean> displayPing;
    private final Setting<Boolean> displayItems;
    private final Setting<Boolean> showTrackedHotbar;
    private final Setting<Keybind> showHotbarKey;
    private final Setting<Integer> hotbarOffhandMargin;
    private final Setting<Boolean> showNormalItemsWithHotbar;
    private final Setting<Boolean> ignoreEmpty;
    private final Setting<Durability> itemDurability;
    private final Setting<Boolean> displayEnchants;
    private final Setting<Set<RegistryKey<Enchantment>>> shownEnchantments;
    private final Setting<Position> enchantPos;
    private final Setting<Integer> enchantLength;
    private final Setting<Double> enchantTextScale;
    private final Setting<Boolean> itemCount;
    private final Setting<SettingColor> background;
    private final Setting<SettingColor> nameColor;
    private final Setting<SettingColor> totemPopsColorColor;
    private final Setting<SettingColor> pingColor;
    private final Setting<SettingColor> gamemodeColor;
    private final Setting<DistanceColorMode> distanceColorMode;
    private final Setting<SettingColor> distanceColor;
    private final Color WHITE;
    private final Color RED;
    private final Color AMBER;
    private final Color GREEN;
    private final Color GOLD;
    private final Vector3d pos;
    private final double[] itemWidths;
    private final List<ItemStack> items;
    private final Map<UUID, ItemStack[]> trackedHotbars;
    private final Map<UUID, Integer> lastTrackedSlots;
    private final List<Entity> entityList;
    private boolean showHotbarKeyPressed;
    private boolean showHotbarToggled;

    public Nametags() {
        super(Categories.Render, "nametags", "Displays customizable nametags above players, items and other entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPlayers = this.settings.createGroup("Players");
        this.sgItems = this.settings.createGroup("Items");
        this.sgRender = this.settings.createGroup("Render");
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Select entities to draw nametags on.")).defaultValue(EntityType.PLAYER, EntityType.ITEM).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the nametag.")).defaultValue(1.1).min(0.1).build());
        this.ignoreSelf = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-self")).description("Ignore yourself when in third person or freecam.")).defaultValue(true)).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Ignore rendering nametags for friends.")).defaultValue(false)).build());
        this.ignoreBots = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-bots")).description("Only render non-bot nametags.")).defaultValue(true)).build());
        this.culling = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("culling")).description("Only render a certain number of nametags at a certain distance.")).defaultValue(false)).build());
        this.maxCullRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("culling-range")).description("Only render nametags within this distance of your player.")).defaultValue(20.0).min(0.0).sliderMax(200.0).visible(this.culling::get)).build());
        this.maxCullCount = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("culling-count")).description("Only render this many nametags.")).defaultValue(50)).min(1).sliderRange(1, 100).visible(this.culling::get)).build());
        this.displayHealth = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("health")).description("Shows the player's health.")).defaultValue(true)).build());
        this.displayTotemPops = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-totem-pops")).description("Shows the player's totem pops.")).defaultValue(true)).build());
        this.displayGameMode = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("gamemode")).description("Shows the player's GameMode.")).defaultValue(false)).build());
        this.displayDistance = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance")).description("Shows the distance between you and the player.")).defaultValue(false)).build());
        this.displayPing = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ping")).description("Shows the player's ping.")).defaultValue(true)).build());
        this.displayItems = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("items")).description("Displays armor and hand items above the name tags.")).defaultValue(true)).build());
        this.showTrackedHotbar = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-hotbar")).description("Enables tracked hotbar rendering and hotbar settings.")).defaultValue(true)).visible(this.displayItems::get)).build());
        this.showHotbarKey = this.sgPlayers.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("show-hotbar-key")).description("Displays the tracked hotbar instead of armor when pressed.")).visible(() -> this.displayItems.get() != false && this.showTrackedHotbar.get() != false)).build());
        this.hotbarOffhandMargin = this.sgPlayers.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("hotbar-offhand-margin")).description("Spacing in pixels between offhand (index 0) and hotbar (indices 1-9).")).defaultValue(12)).range(0, 64).sliderRange(0, 32).visible(() -> this.displayItems.get() != false && this.showTrackedHotbar.get() != false)).build());
        this.showNormalItemsWithHotbar = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-normal-items-with-hotbar")).description("Also render the normal items row above the hotbar row when the hotbar is shown.")).defaultValue(false)).visible(() -> this.displayItems.get() != false && this.showTrackedHotbar.get() != false)).build());
        this.ignoreEmpty = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-empty-slots")).description("Doesn't add spacing where an empty item stack would be.")).defaultValue(true)).visible(this.displayItems::get)).build());
        this.itemDurability = this.sgPlayers.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("durability")).description("Displays item durability as either a total, percentage, or neither.")).defaultValue(Durability.None)).visible(this.displayItems::get)).build());
        this.displayEnchants = this.sgPlayers.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("display-enchants")).description("Displays item enchantments on the items.")).defaultValue(false)).visible(this.displayItems::get)).build());
        this.shownEnchantments = this.sgPlayers.add(((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)new EnchantmentListSetting.Builder().name("shown-enchantments")).description("The enchantments that are shown on nametags.")).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).defaultValue(Enchantments.PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.PROJECTILE_PROTECTION).build());
        this.enchantPos = this.sgPlayers.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("enchantment-position")).description("Where the enchantments are rendered.")).defaultValue(Position.Above)).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).build());
        this.enchantLength = this.sgPlayers.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("enchant-name-length")).description("The length enchantment names are trimmed to.")).defaultValue(3)).range(1, 5).sliderRange(1, 5).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).build());
        this.enchantTextScale = this.sgPlayers.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("enchant-text-scale")).description("The scale of the enchantment text.")).defaultValue(1.0).range(0.1, 2.0).sliderRange(0.1, 2.0).visible(() -> this.displayItems.get() != false && this.displayEnchants.get() != false)).build());
        this.itemCount = this.sgItems.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-count")).description("Displays the number of items in the stack.")).defaultValue(true)).build());
        this.background = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("The color of the nametag background.")).defaultValue(new SettingColor(0, 0, 0, 75)).build());
        this.nameColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("name-color")).description("The color of the nametag names.")).defaultValue(new SettingColor()).build());
        this.totemPopsColorColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("totem-pop-color")).description("The color of the nametag totem pops.")).defaultValue(new SettingColor(225, 120, 20)).build());
        this.pingColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-color")).description("The color of the nametag ping.")).defaultValue(new SettingColor(20, 170, 170)).visible(this.displayPing::get)).build());
        this.gamemodeColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("gamemode-color")).description("The color of the nametag gamemode.")).defaultValue(new SettingColor(232, 185, 35)).visible(this.displayGameMode::get)).build());
        this.distanceColorMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("distance-color-mode")).description("The mode to color the nametag distance with.")).defaultValue(DistanceColorMode.Gradient)).visible(this.displayDistance::get)).build());
        this.distanceColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-color")).description("The color of the nametag distance.")).defaultValue(new SettingColor(150, 150, 150)).visible(() -> this.displayDistance.get() != false && this.distanceColorMode.get() == DistanceColorMode.Flat)).build());
        this.WHITE = new Color(255, 255, 255);
        this.RED = new Color(255, 25, 25);
        this.AMBER = new Color(255, 105, 25);
        this.GREEN = new Color(25, 252, 25);
        this.GOLD = new Color(232, 185, 35);
        this.pos = new Vector3d();
        this.itemWidths = new double[10];
        this.items = new ArrayList<ItemStack>();
        this.trackedHotbars = new HashMap<UUID, ItemStack[]>();
        this.lastTrackedSlots = new HashMap<UUID, Integer>();
        this.entityList = new ArrayList<Entity>();
        this.showHotbarKeyPressed = false;
        this.showHotbarToggled = false;
    }

    private static String ticksToTime(int ticks) {
        if (ticks > 72000) {
            int h = ticks / 20 / 3600;
            return h + " h";
        }
        if (ticks > 1200) {
            int m = ticks / 20 / 60;
            return m + " m";
        }
        int s = ticks / 20;
        int ms = ticks % 20 / 2;
        return s + "." + ms + " s";
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        boolean isPressed = this.showHotbarKey.get().isPressed();
        if (isPressed && !this.showHotbarKeyPressed) {
            this.showHotbarToggled = !this.showHotbarToggled;
        }
        this.showHotbarKeyPressed = isPressed;
        this.entityList.clear();
        boolean freecamNotActive = !Modules.get().isActive(Freecam.class);
        boolean notThirdPerson = this.mc.options.getPerspective().isFirstPerson();
        Vec3d cameraPos = this.mc.gameRenderer.getCamera().getPos();
        for (Entity entity : this.mc.world.getEntities()) {
            EntityType type = entity.getType();
            if (!this.entities.get().contains(type)) continue;
            if (type == EntityType.PLAYER) {
                PlayerEntity player = (PlayerEntity)entity;
                if ((this.ignoreSelf.get().booleanValue() || freecamNotActive && notThirdPerson) && entity == this.mc.player || EntityUtils.getGameMode(player) == null && this.ignoreBots.get().booleanValue() || Friends.get().isFriend(player) && this.ignoreFriends.get().booleanValue()) continue;
            }
            if (this.culling.get().booleanValue() && !PlayerUtils.isWithinCamera(entity, (double)this.maxCullRange.get())) continue;
            this.entityList.add(entity);
        }
        this.entityList.sort(Comparator.comparing(e -> e.squaredDistanceTo(cameraPos)));
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        int count = this.getRenderCount();
        boolean shadow = Config.get().customFont.get();
        for (int i = count - 1; i > -1; --i) {
            Entity entity = this.entityList.get(i);
            Utils.set(this.pos, entity, event.tickDelta);
            this.pos.add(0.0, this.getHeight(entity), 0.0);
            EntityType type = entity.getType();
            if (!NametagUtils.to2D(this.pos, this.scale.get())) continue;
            if (type == EntityType.PLAYER) {
                PlayerEntity player = (PlayerEntity)entity;
                UUID uuid = player.getUuid();
                if (player.isDead() || player.getHealth() <= 0.0f) {
                    this.lastTrackedSlots.remove(uuid);
                    this.trackedHotbars.remove(uuid);
                } else {
                    ItemStack[] trackedHotbar = this.trackedHotbars.computeIfAbsent(uuid, id -> {
                        Object[] newArr = new ItemStack[10];
                        Arrays.fill(newArr, ItemStack.EMPTY);
                        return newArr;
                    });
                    ArrayList<ItemStack> currentHotbar = new ArrayList<ItemStack>(9);
                    for (int s = 0; s < 9; ++s) {
                        currentHotbar.add((ItemStack)player.getInventory().main.get(s));
                    }
                    HashSet<Item> insertedThisTick = new HashSet<Item>();
                    for (ItemStack stack : currentHotbar) {
                        if (stack.isEmpty()) continue;
                        Item item = stack.getItem();
                        boolean found = false;
                        for (int j = 1; j <= 9; ++j) {
                            if (trackedHotbar[j].isEmpty() || trackedHotbar[j].getItem() != item) continue;
                            trackedHotbar[j] = stack.copy();
                            found = true;
                            break;
                        }
                        if (found || !insertedThisTick.add(item)) continue;
                        int prev = this.lastTrackedSlots.getOrDefault(uuid, 1);
                        int index = prev + 1;
                        if (index > 9) {
                            index = 1;
                        }
                        trackedHotbar[index] = stack.copy();
                        this.lastTrackedSlots.put(uuid, index);
                    }
                    ItemStack offhand = player.getOffHandStack();
                    ItemStack itemStack = trackedHotbar[0] = offhand.isEmpty() ? ItemStack.EMPTY : offhand.copy();
                }
            }
            if (type == EntityType.PLAYER) {
                this.renderNametagPlayer(event, (PlayerEntity)entity, shadow);
                continue;
            }
            if (type == EntityType.ITEM) {
                this.renderNametagItem(((ItemEntity)entity).getStack(), shadow);
                continue;
            }
            if (type == EntityType.ITEM_FRAME) {
                this.renderNametagItem(((ItemFrameEntity)entity).getHeldItemStack(), shadow);
                continue;
            }
            if (type == EntityType.TNT) {
                this.renderTntNametag(Nametags.ticksToTime(((TntEntity)entity).getFuse()), shadow);
                continue;
            }
            if (type == EntityType.TNT_MINECART && ((TntMinecartEntity)entity).isPrimed()) {
                this.renderTntNametag(Nametags.ticksToTime(((TntMinecartEntity)entity).getFuseTicks()), shadow);
                continue;
            }
            if (entity instanceof LivingEntity) {
                this.renderGenericLivingNametag((LivingEntity)entity, shadow);
                continue;
            }
            this.renderGenericNametag(entity, shadow);
        }
    }

    private int getRenderCount() {
        int count = this.culling.get() != false ? this.maxCullCount.get().intValue() : this.entityList.size();
        count = MathHelper.clamp((int)count, (int)0, (int)this.entityList.size());
        return count;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(this.getRenderCount());
    }

    private double getHeight(Entity entity) {
        double height = entity.getEyeHeight(entity.getPose());
        height = entity.getType() == EntityType.ITEM || entity.getType() == EntityType.ITEM_FRAME ? (height += 0.2) : (height += 0.5);
        return height;
    }

    private void renderNametagPlayer(Render2DEvent event, PlayerEntity player, boolean shadow) {
        boolean renderPlayerDistance;
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos, event.drawContext);
        GameMode gm = EntityUtils.getGameMode(player);
        Object gmText = "BOT";
        if (gm != null) {
            gmText = switch (gm) {
                default -> throw new MatchException(null, null);
                case GameMode.SPECTATOR -> "Sp";
                case GameMode.SURVIVAL -> "S";
                case GameMode.CREATIVE -> "C";
                case GameMode.ADVENTURE -> "A";
            };
        }
        gmText = "[" + (String)gmText + "] ";
        Color nameColor = PlayerUtils.getPlayerColor(player, this.nameColor.get());
        String name = player == this.mc.player ? Modules.get().get(NameProtect.class).getName(player.getName().getString()) : player.getName().getString();
        float absorption = player.getAbsorptionAmount();
        int health = Math.round(player.getHealth() + absorption);
        double healthPercentage = (float)health / (player.getMaxHealth() + absorption);
        String healthText = " " + health;
        Color healthColor = healthPercentage <= 0.333 ? this.RED : (healthPercentage <= 0.666 ? this.AMBER : this.GREEN);
        String totemPopsText = " " + -MeteorClient.INFO.getPops((Entity)player);
        int ping = EntityUtils.getPing(player);
        String pingText = " [" + ping + "ms]";
        double dist = (double)Math.round(PlayerUtils.distanceToCamera((Entity)player) * 10.0) / 10.0;
        String distText = " " + dist + "m";
        double gmWidth = text.getWidth((String)gmText, shadow);
        double nameWidth = text.getWidth(name, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double totemPopsWidth = text.getWidth(totemPopsText, shadow);
        double pingWidth = text.getWidth(pingText, shadow);
        double distWidth = text.getWidth(distText, shadow);
        double width = nameWidth;
        boolean bl = renderPlayerDistance = player != this.mc.cameraEntity || Modules.get().isActive(Freecam.class);
        if (this.displayHealth.get().booleanValue()) {
            width += healthWidth;
        }
        if (this.displayTotemPops.get().booleanValue() && MeteorClient.INFO.getPops((Entity)player) > 0) {
            width += totemPopsWidth;
        }
        if (this.displayGameMode.get().booleanValue()) {
            width += gmWidth;
        }
        if (this.displayPing.get().booleanValue()) {
            width += pingWidth;
        }
        if (this.displayDistance.get().booleanValue() && renderPlayerDistance) {
            width += distWidth;
        }
        double widthHalf = width / 2.0;
        double heightDown = text.getHeight(shadow);
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        if (this.displayGameMode.get().booleanValue()) {
            hX = text.render((String)gmText, hX, hY, this.gamemodeColor.get(), shadow);
        }
        hX = text.render(name, hX, hY, nameColor, shadow);
        if (this.displayHealth.get().booleanValue()) {
            hX = text.render(healthText, hX, hY, healthColor, shadow);
        }
        if (this.displayTotemPops.get().booleanValue() && MeteorClient.INFO.getPops((Entity)player) > 0) {
            hX = text.render(totemPopsText, hX, hY, this.totemPopsColorColor.get(), shadow);
        }
        if (this.displayPing.get().booleanValue()) {
            hX = text.render(pingText, hX, hY, this.pingColor.get(), shadow);
        }
        if (this.displayDistance.get().booleanValue() && renderPlayerDistance) {
            switch (this.distanceColorMode.get().ordinal()) {
                case 1: {
                    text.render(distText, hX, hY, this.distanceColor.get(), shadow);
                    break;
                }
                case 0: {
                    text.render(distText, hX, hY, EntityUtils.getColorFromDistance((Entity)player), shadow);
                }
            }
        }
        text.end();
        if (this.displayItems.get().booleanValue()) {
            boolean showBoth;
            boolean hotbarEnabled = this.showTrackedHotbar.get();
            boolean showHotbar = hotbarEnabled && this.showHotbarToggled;
            boolean bl2 = showBoth = showHotbar && this.showNormalItemsWithHotbar.get() != false;
            if (showBoth) {
                double enchantX;
                Color encColor;
                String nm;
                double addY;
                double enchantY;
                double aW;
                Object2IntOpenHashMap toShow;
                ItemEnchantmentsComponent ench;
                Color damageColor;
                String damageText;
                ItemStack stack;
                int i;
                String nm2;
                RegistryEntry enc;
                int size;
                ItemStack s;
                int i2;
                ArrayList<ItemStack> normalRow = new ArrayList<ItemStack>(6);
                normalRow.add(player.getMainHandStack());
                normalRow.add((ItemStack)player.getInventory().armor.get(3));
                normalRow.add((ItemStack)player.getInventory().armor.get(2));
                normalRow.add((ItemStack)player.getInventory().armor.get(1));
                normalRow.add((ItemStack)player.getInventory().armor.get(0));
                normalRow.add(player.getOffHandStack());
                ItemStack[] tracked = this.trackedHotbars.get(player.getUuid());
                ArrayList<ItemStack> hotbarRow = new ArrayList<ItemStack>(10);
                if (tracked != null) {
                    Collections.addAll(hotbarRow, tracked);
                } else {
                    for (int i3 = 0; i3 < 10; ++i3) {
                        hotbarRow.add(ItemStack.EMPTY);
                    }
                }
                double[] widthsNorm = new double[normalRow.size()];
                double[] widthsHB = new double[hotbarRow.size()];
                boolean hasNorm = false;
                boolean hasHB = false;
                int maxEnchantCountNorm = 0;
                int maxEnchantCountHB = 0;
                for (i2 = 0; i2 < normalRow.size(); ++i2) {
                    s = (ItemStack)normalRow.get(i2);
                    if (!this.ignoreEmpty.get().booleanValue() || !s.isEmpty()) {
                        widthsNorm[i2] = 32.0;
                    }
                    if (!s.isEmpty()) {
                        hasNorm = true;
                    }
                    if (!this.displayEnchants.get().booleanValue()) continue;
                    ItemEnchantmentsComponent ench2 = EnchantmentHelper.getEnchantments((ItemStack)s);
                    size = 0;
                    Object object = ench2.getEnchantmentEntries().iterator();
                    while (object.hasNext()) {
                        Map.Entry e = (Map.Entry)object.next();
                        enc = (RegistryEntry)e.getKey();
                        if (enc.getKey().isPresent() && !this.shownEnchantments.get().contains(enc.getKey().get())) continue;
                        nm2 = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)enc, this.enchantLength.get()) + " " + String.valueOf(e.getValue());
                        widthsNorm[i2] = Math.max(widthsNorm[i2], TextRenderer.get().getWidth(nm2, shadow) / 2.0);
                        ++size;
                    }
                    maxEnchantCountNorm = Math.max(maxEnchantCountNorm, size);
                }
                for (i2 = 0; i2 < hotbarRow.size(); ++i2) {
                    s = (ItemStack)hotbarRow.get(i2);
                    if (!this.ignoreEmpty.get().booleanValue() || !s.isEmpty()) {
                        double extra = i2 == 0 ? (double)this.hotbarOffhandMargin.get().intValue() : 0.0;
                        widthsHB[i2] = 32.0 + extra;
                    }
                    if (!s.isEmpty()) {
                        hasHB = true;
                    }
                    if (!this.displayEnchants.get().booleanValue()) continue;
                    ItemEnchantmentsComponent ench3 = EnchantmentHelper.getEnchantments((ItemStack)s);
                    size = 0;
                    for (Map.Entry e : ench3.getEnchantmentEntries()) {
                        enc = (RegistryEntry)e.getKey();
                        if (enc.getKey().isPresent() && !this.shownEnchantments.get().contains(enc.getKey().get())) continue;
                        nm2 = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)enc, this.enchantLength.get()) + " " + String.valueOf(e.getValue());
                        widthsHB[i2] = Math.max(widthsHB[i2], TextRenderer.get().getWidth(nm2, shadow) / 2.0);
                        ++size;
                    }
                    maxEnchantCountHB = Math.max(maxEnchantCountHB, size);
                }
                double totalNormW = 0.0;
                double totalHBW = 0.0;
                for (double wv : widthsNorm) {
                    totalNormW += wv;
                }
                for (double wv : widthsHB) {
                    totalHBW += wv;
                }
                double totalHeightItems = 0.0;
                if (hasNorm) {
                    totalHeightItems += 32.0;
                }
                if (hasHB) {
                    totalHeightItems += (double)((totalHeightItems > 0.0 ? 2 : 0) + 32);
                }
                double yTop = -heightDown - 7.0 - totalHeightItems;
                if (hasNorm) {
                    double x = -totalNormW / 2.0;
                    double y = yTop;
                    for (i = 0; i < normalRow.size(); ++i) {
                        stack = (ItemStack)normalRow.get(i);
                        if (!(this.ignoreEmpty.get().booleanValue() && stack.isEmpty() || this.isMobHead(stack))) {
                            RenderUtils.drawItem(event.drawContext, stack, (int)x, (int)y, 2.0f, true);
                        }
                        if (stack.isDamageable() && this.itemDurability.get() != Durability.None) {
                            text.begin(0.75, false, true);
                            damageText = switch (this.itemDurability.get().ordinal()) {
                                case 2 -> String.format("%.0f%%", Float.valueOf((float)(stack.getMaxDamage() - stack.getDamage()) * 100.0f / (float)stack.getMaxDamage()));
                                case 1 -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                                default -> "err";
                            };
                            damageColor = new Color(stack.getItem().getItemBarColor(stack));
                            text.render(damageText, (int)x, (int)y, damageColor.a(255), true);
                            text.end();
                        }
                        if (maxEnchantCountNorm > 0 && this.displayEnchants.get().booleanValue()) {
                            text.begin(0.5 * this.enchantTextScale.get(), false, true);
                            ench = EnchantmentHelper.getEnchantments((ItemStack)stack);
                            toShow = new Object2IntOpenHashMap();
                            for (Map.Entry e : ench.getEnchantmentEntries()) {
                                RegistryEntry enc2 = (RegistryEntry)e.getKey();
                                if (!enc2.matches(this.shownEnchantments.get()::contains)) continue;
                                toShow.put((Object)enc2, (Integer)e.getValue());
                            }
                            aW = widthsNorm[i];
                            enchantY = 0.0;
                            addY = switch (this.enchantPos.get().ordinal()) {
                                default -> throw new MatchException(null, null);
                                case 0 -> -((double)(toShow.size() + 1) * text.getHeight(shadow));
                                case 1 -> (32.0 - (double)toShow.size() * text.getHeight(shadow)) / 2.0;
                            };
                            for (Object2IntMap.Entry e : Object2IntMaps.fastIterable((Object2IntMap)toShow)) {
                                nm = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)((RegistryEntry)e.getKey()), this.enchantLength.get()) + " " + e.getIntValue();
                                encColor = this.WHITE;
                                if (((RegistryEntry)e.getKey()).isIn(EnchantmentTags.CURSE)) {
                                    encColor = this.RED;
                                }
                                enchantX = switch (this.enchantPos.get().ordinal()) {
                                    default -> throw new MatchException(null, null);
                                    case 0 -> x + aW / 2.0 - text.getWidth(nm, shadow) / 2.0;
                                    case 1 -> x + (aW - text.getWidth(nm, shadow)) / 2.0;
                                };
                                text.render(nm, enchantX, y + addY + enchantY, encColor, shadow);
                                enchantY += text.getHeight(shadow);
                            }
                            text.end();
                        }
                        x += widthsNorm[i];
                    }
                }
                if (hasHB) {
                    double y = yTop + (double)(hasNorm ? 34 : 0);
                    double x = -totalHBW / 2.0;
                    for (i = 0; i < hotbarRow.size(); ++i) {
                        stack = (ItemStack)hotbarRow.get(i);
                        if (!(this.ignoreEmpty.get().booleanValue() && stack.isEmpty() || this.isMobHead(stack))) {
                            RenderUtils.drawItem(event.drawContext, stack, (int)x, (int)y, 2.0f, true);
                        }
                        if (stack.isDamageable() && this.itemDurability.get() != Durability.None) {
                            text.begin(0.75, false, true);
                            damageText = switch (this.itemDurability.get().ordinal()) {
                                case 2 -> String.format("%.0f%%", Float.valueOf((float)(stack.getMaxDamage() - stack.getDamage()) * 100.0f / (float)stack.getMaxDamage()));
                                case 1 -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                                default -> "err";
                            };
                            damageColor = new Color(stack.getItem().getItemBarColor(stack));
                            text.render(damageText, (int)x, (int)y, damageColor.a(255), true);
                            text.end();
                        }
                        if (maxEnchantCountHB > 0 && this.displayEnchants.get().booleanValue()) {
                            text.begin(0.5 * this.enchantTextScale.get(), false, true);
                            ench = EnchantmentHelper.getEnchantments((ItemStack)stack);
                            toShow = new Object2IntOpenHashMap();
                            for (Map.Entry e : ench.getEnchantmentEntries()) {
                                RegistryEntry enc3 = (RegistryEntry)e.getKey();
                                if (!enc3.matches(this.shownEnchantments.get()::contains)) continue;
                                toShow.put((Object)enc3, (Integer)e.getValue());
                            }
                            aW = widthsHB[i];
                            enchantY = 0.0;
                            addY = switch (this.enchantPos.get().ordinal()) {
                                default -> throw new MatchException(null, null);
                                case 0 -> -((double)(toShow.size() + 1) * text.getHeight(shadow));
                                case 1 -> (32.0 - (double)toShow.size() * text.getHeight(shadow)) / 2.0;
                            };
                            for (Object2IntMap.Entry e : Object2IntMaps.fastIterable((Object2IntMap)toShow)) {
                                nm = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)((RegistryEntry)e.getKey()), this.enchantLength.get()) + " " + e.getIntValue();
                                encColor = this.WHITE;
                                if (((RegistryEntry)e.getKey()).isIn(EnchantmentTags.CURSE)) {
                                    encColor = this.RED;
                                }
                                enchantX = switch (this.enchantPos.get().ordinal()) {
                                    default -> throw new MatchException(null, null);
                                    case 0 -> x + aW / 2.0 - text.getWidth(nm, shadow) / 2.0;
                                    case 1 -> x + (aW - text.getWidth(nm, shadow)) / 2.0;
                                };
                                text.render(nm, enchantX, y + addY + enchantY, encColor, shadow);
                                enchantY += text.getHeight(shadow);
                            }
                            text.end();
                        }
                        x += widthsHB[i];
                    }
                    int selectedSlot = -1;
                    ItemStack currentHand = player.getMainHandStack();
                    if (!currentHand.isEmpty()) {
                        for (int i4 = 1; i4 <= 9; ++i4) {
                            if (((ItemStack)hotbarRow.get(i4)).isEmpty() || ((ItemStack)hotbarRow.get(i4)).getItem() != currentHand.getItem()) continue;
                            selectedSlot = i4;
                            break;
                        }
                    }
                    if (selectedSlot == -1) {
                        selectedSlot = MathHelper.clamp((int)(player.getInventory().selectedSlot + 1), (int)1, (int)9);
                    }
                    double selectedSlotX = -totalHBW / 2.0;
                    for (int i5 = 0; i5 < selectedSlot; ++i5) {
                        selectedSlotX += widthsHB[i5];
                    }
                    if (!this.ignoreEmpty.get().booleanValue() || selectedSlot >= 1 && selectedSlot <= 9 && !((ItemStack)hotbarRow.get(selectedSlot)).isEmpty()) {
                        Renderer2D.COLOR.begin();
                        float quadX = (float)(selectedSlotX - 1.0);
                        float quadY = (float)(y - 1.0);
                        float quadW = 34.0f;
                        float quadH = 34.0f;
                        Renderer2D.COLOR.quad(quadX, quadY, quadW, 1.0, Color.WHITE);
                        Renderer2D.COLOR.quad(quadX, quadY + quadH - 1.0f, quadW, 1.0, Color.WHITE);
                        Renderer2D.COLOR.quad(quadX, quadY + 1.0f, 1.0, quadH - 2.0f, Color.WHITE);
                        Renderer2D.COLOR.quad(quadX + quadW - 1.0f, quadY + 1.0f, 1.0, quadH - 2.0f, Color.WHITE);
                        Renderer2D.COLOR.render(null);
                    }
                }
            } else {
                this.items.clear();
                if (hotbarEnabled && showHotbar) {
                    ItemStack[] trackedHotbar = this.trackedHotbars.get(player.getUuid());
                    if (trackedHotbar != null) {
                        Collections.addAll(this.items, trackedHotbar);
                    } else {
                        for (int i = 0; i < 10; ++i) {
                            this.items.add(ItemStack.EMPTY);
                        }
                    }
                } else {
                    this.items.add(player.getMainHandStack());
                    this.items.add((ItemStack)player.getInventory().armor.get(3));
                    this.items.add((ItemStack)player.getInventory().armor.get(2));
                    this.items.add((ItemStack)player.getInventory().armor.get(1));
                    this.items.add((ItemStack)player.getInventory().armor.get(0));
                    this.items.add(player.getOffHandStack());
                }
                int itemCount = this.items.size();
                Arrays.fill(this.itemWidths, 0.0);
                boolean hasItems = false;
                int maxEnchantCount = 0;
                for (int i = 0; i < itemCount; ++i) {
                    ItemStack itemStack = this.items.get(i);
                    if (hotbarEnabled && showHotbar) {
                        if (!this.ignoreEmpty.get().booleanValue() || !itemStack.isEmpty()) {
                            double extraSpacing = i == 0 ? (double)this.hotbarOffhandMargin.get().intValue() : 0.0;
                            this.itemWidths[i] = 32.0 + extraSpacing;
                        }
                    } else if (!(this.itemWidths[i] != 0.0 || this.ignoreEmpty.get().booleanValue() && itemStack.isEmpty())) {
                        this.itemWidths[i] = 32.0;
                    }
                    if (!itemStack.isEmpty()) {
                        hasItems = true;
                    }
                    if (!this.displayEnchants.get().booleanValue()) continue;
                    ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments((ItemStack)itemStack);
                    int size = 0;
                    for (Map.Entry entry : enchantments.getEnchantmentEntries()) {
                        RegistryEntry enchantment = (RegistryEntry)entry.getKey();
                        if (enchantment.getKey().isPresent() && !this.shownEnchantments.get().contains(enchantment.getKey().get())) continue;
                        String enchantName = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)enchantment, this.enchantLength.get()) + " " + String.valueOf(entry.getValue());
                        this.itemWidths[i] = Math.max(this.itemWidths[i], text.getWidth(enchantName, shadow) / 2.0);
                        ++size;
                    }
                    maxEnchantCount = Math.max(maxEnchantCount, size);
                }
                double itemsHeight = hasItems ? 32 : 0;
                double itemWidthTotal = 0.0;
                for (int i = 0; i < itemCount; ++i) {
                    itemWidthTotal += this.itemWidths[i];
                }
                double itemWidthHalf = itemWidthTotal / 2.0;
                double y = -heightDown - 7.0 - itemsHeight;
                double x = -itemWidthHalf;
                for (int i = 0; i < itemCount; ++i) {
                    ItemStack stack = this.items.get(i);
                    if (!(hotbarEnabled && showHotbar && this.ignoreEmpty.get().booleanValue() && stack.isEmpty() || this.isMobHead(stack))) {
                        RenderUtils.drawItem(event.drawContext, stack, (int)x, (int)y, 2.0f, true);
                    }
                    if (stack.isDamageable() && this.itemDurability.get() != Durability.None) {
                        text.begin(0.75, false, true);
                        String damageText = switch (this.itemDurability.get().ordinal()) {
                            case 2 -> String.format("%.0f%%", Float.valueOf((float)(stack.getMaxDamage() - stack.getDamage()) * 100.0f / (float)stack.getMaxDamage()));
                            case 1 -> Integer.toString(stack.getMaxDamage() - stack.getDamage());
                            default -> "err";
                        };
                        Color damageColor = new Color(stack.getItem().getItemBarColor(stack));
                        text.render(damageText, (int)x, (int)y, damageColor.a(255), true);
                        text.end();
                    }
                    if (maxEnchantCount > 0 && this.displayEnchants.get().booleanValue()) {
                        text.begin(0.5 * this.enchantTextScale.get(), false, true);
                        ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments((ItemStack)stack);
                        Object2IntOpenHashMap enchantmentsToShow = new Object2IntOpenHashMap();
                        for (Map.Entry entry : enchantments.getEnchantmentEntries()) {
                            RegistryEntry enchantment = (RegistryEntry)entry.getKey();
                            if (!enchantment.matches(this.shownEnchantments.get()::contains)) continue;
                            enchantmentsToShow.put((Object)enchantment, (Integer)entry.getValue());
                        }
                        double aW = this.itemWidths[i];
                        double enchantY = 0.0;
                        double addY = switch (this.enchantPos.get().ordinal()) {
                            default -> throw new MatchException(null, null);
                            case 0 -> -((double)(enchantmentsToShow.size() + 1) * text.getHeight(shadow));
                            case 1 -> (itemsHeight - (double)enchantmentsToShow.size() * text.getHeight(shadow)) / 2.0;
                        };
                        for (Object2IntMap.Entry entry : Object2IntMaps.fastIterable((Object2IntMap)enchantmentsToShow)) {
                            String enchantName = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)((RegistryEntry)entry.getKey()), this.enchantLength.get()) + " " + entry.getIntValue();
                            Color enchantColor = this.WHITE;
                            if (((RegistryEntry)entry.getKey()).isIn(EnchantmentTags.CURSE)) {
                                enchantColor = this.RED;
                            }
                            double enchantX = switch (this.enchantPos.get().ordinal()) {
                                default -> throw new MatchException(null, null);
                                case 0 -> x + aW / 2.0 - text.getWidth(enchantName, shadow) / 2.0;
                                case 1 -> x + (aW - text.getWidth(enchantName, shadow)) / 2.0;
                            };
                            text.render(enchantName, enchantX, y + addY + enchantY, enchantColor, shadow);
                            enchantY += text.getHeight(shadow);
                        }
                        text.end();
                    }
                    x += this.itemWidths[i];
                }
                if (hotbarEnabled && showHotbar) {
                    int selectedSlot = -1;
                    ItemStack currentHand = player.getMainHandStack();
                    if (!this.items.isEmpty() && !currentHand.isEmpty()) {
                        for (int i = 1; i <= 9; ++i) {
                            if (i >= this.items.size() || this.items.get(i).isEmpty() || this.items.get(i).getItem() != currentHand.getItem()) continue;
                            selectedSlot = i;
                            break;
                        }
                    }
                    if (selectedSlot == -1) {
                        selectedSlot = MathHelper.clamp((int)(player.getInventory().selectedSlot + 1), (int)1, (int)9);
                    }
                    double selectedSlotX = -itemWidthHalf;
                    for (int i = 0; i < selectedSlot; ++i) {
                        selectedSlotX += this.itemWidths[i];
                    }
                    double itemSize = 32.0;
                    if (!this.ignoreEmpty.get().booleanValue() || selectedSlot >= 1 && selectedSlot <= 9 && selectedSlot < this.items.size() && !this.items.get(selectedSlot).isEmpty()) {
                        Renderer2D.COLOR.begin();
                        float quadX = (float)(selectedSlotX - 1.0);
                        float quadY = (float)(y - 1.0);
                        float quadW = (float)(itemSize + 2.0);
                        float quadH = (float)(itemSize + 2.0);
                        Renderer2D.COLOR.quad(quadX, quadY, quadW, 1.0, Color.WHITE);
                        Renderer2D.COLOR.quad(quadX, quadY + quadH - 1.0f, quadW, 1.0, Color.WHITE);
                        Renderer2D.COLOR.quad(quadX, quadY + 1.0f, 1.0, quadH - 2.0f, Color.WHITE);
                        Renderer2D.COLOR.quad(quadX + quadW - 1.0f, quadY + 1.0f, 1.0, quadH - 2.0f, Color.WHITE);
                        Renderer2D.COLOR.render(null);
                    }
                }
            }
        } else if (this.displayEnchants.get().booleanValue()) {
            this.displayEnchants.set(false);
        }
        NametagUtils.end(event.drawContext);
    }

    private void renderNametagItem(ItemStack stack, boolean shadow) {
        if (stack.isEmpty()) {
            return;
        }
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String name = Names.get(stack);
        String count = " x" + stack.getCount();
        double nameWidth = text.getWidth(name, shadow);
        double countWidth = text.getWidth(count, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth;
        if (this.itemCount.get().booleanValue()) {
            width += countWidth;
        }
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        hX = text.render(name, hX, hY, this.nameColor.get(), shadow);
        if (this.itemCount.get().booleanValue()) {
            text.render(count, hX, hY, this.GOLD, shadow);
        }
        text.end();
        NametagUtils.end();
    }

    private void renderGenericLivingNametag(LivingEntity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        Object nameText = entity.getType().getName().getString();
        nameText = (String)nameText + " ";
        float absorption = entity.getAbsorptionAmount();
        int health = Math.round(entity.getHealth() + absorption);
        double healthPercentage = (float)health / (entity.getMaxHealth() + absorption);
        String healthText = String.valueOf(health);
        Color healthColor = healthPercentage <= 0.333 ? this.RED : (healthPercentage <= 0.666 ? this.AMBER : this.GREEN);
        double nameWidth = text.getWidth((String)nameText, shadow);
        double healthWidth = text.getWidth(healthText, shadow);
        double heightDown = text.getHeight(shadow);
        double width = nameWidth + healthWidth;
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        hX = text.render((String)nameText, hX, hY, this.nameColor.get(), shadow);
        text.render(healthText, hX, hY, healthColor, shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderGenericNametag(Entity entity, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        String nameText = entity.getType().getName().getString();
        double nameWidth = text.getWidth(nameText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = nameWidth / 2.0;
        this.drawBg(-widthHalf, -heightDown, nameWidth, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(nameText, hX, hY, this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private void renderTntNametag(String fuseText, boolean shadow) {
        TextRenderer text = TextRenderer.get();
        NametagUtils.begin(this.pos);
        double width = text.getWidth(fuseText, shadow);
        double heightDown = text.getHeight(shadow);
        double widthHalf = width / 2.0;
        this.drawBg(-widthHalf, -heightDown, width, heightDown);
        text.beginBig();
        double hX = -widthHalf;
        double hY = -heightDown;
        text.render(fuseText, hX, hY, this.nameColor.get(), shadow);
        text.end();
        NametagUtils.end();
    }

    private void drawBg(double x, double y, double width, double height) {
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad((float)(x - 1.0), (float)(y - 1.0), (float)(width + 2.0), (float)(height + 2.0), this.background.get());
        Renderer2D.COLOR.render(null);
    }

    public boolean excludeBots() {
        return this.ignoreBots.get();
    }

    public boolean playerNametags() {
        return this.isActive() && this.entities.get().contains(EntityType.PLAYER);
    }

    private boolean isMobHead(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.DRAGON_HEAD || item == Items.PLAYER_HEAD || item == Items.ZOMBIE_HEAD || item == Items.CREEPER_HEAD || item == Items.SKELETON_SKULL || item == Items.WITHER_SKELETON_SKULL || item == Items.PIGLIN_HEAD;
    }

    public static enum Durability {
        None,
        Total,
        Percentage;

    }

    public static enum Position {
        Above,
        OnTop;

    }

    public static enum DistanceColorMode {
        Gradient,
        Flat;

    }
}

