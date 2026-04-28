/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.BannerPatternsComponent
 *  net.minecraft.component.type.BannerPatternsComponent$Builder
 *  net.minecraft.component.type.BlockStateComponent
 *  net.minecraft.component.type.FoodComponent
 *  net.minecraft.component.type.MapIdComponent
 *  net.minecraft.component.type.NbtComponent
 *  net.minecraft.component.type.SuspiciousStewEffectsComponent
 *  net.minecraft.component.type.SuspiciousStewEffectsComponent$StewEffect
 *  net.minecraft.component.type.WritableBookContentComponent
 *  net.minecraft.component.type.WrittenBookContentComponent
 *  net.minecraft.entity.Bucketable
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffectUtil
 *  net.minecraft.item.BannerItem
 *  net.minecraft.item.BannerPatternItem
 *  net.minecraft.item.EntityBucketItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.registry.RegistryWrapper$WrapperLookup
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.RawFilteredPair
 *  net.minecraft.text.Text
 *  net.minecraft.util.DyeColor
 *  net.minecraft.util.Formatting
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.DataOutput;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.events.game.ItemStackTooltipEvent;
import meteordevelopment.meteorclient.events.render.TooltipDataEvent;
import meteordevelopment.meteorclient.mixin.EntityAccessor;
import meteordevelopment.meteorclient.mixin.EntityBucketItemAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ByteCountDataOutput;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.EChestMemory;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.tooltip.BannerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.BookTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.ContainerTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.EntityTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.MapTooltipComponent;
import meteordevelopment.meteorclient.utils.tooltip.TextTooltipComponent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

public class BetterTooltips
extends Module {
    public static final Color ECHEST_COLOR = new Color(0, 50, 50);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPreviews;
    private final SettingGroup sgOther;
    private final SettingGroup sgHideFlags;
    private final Setting<DisplayWhen> displayWhen;
    private final Setting<Keybind> keybind;
    private final Setting<Boolean> middleClickOpen;
    private final Setting<Boolean> pauseInCreative;
    private final Setting<Boolean> shulkers;
    private final Setting<Boolean> shulkerCompactTooltip;
    public final Setting<Boolean> echest;
    private final Setting<Boolean> maps;
    public final Setting<Double> mapsScale;
    private final Setting<Boolean> books;
    private final Setting<Boolean> banners;
    private final Setting<Boolean> entitiesInBuckets;
    public final Setting<Boolean> byteSize;
    private final Setting<Boolean> statusEffects;
    private final Setting<Boolean> beehive;
    public final Setting<Boolean> tooltip;
    public final Setting<Boolean> enchantments;
    public final Setting<Boolean> modifiers;
    public final Setting<Boolean> unbreakable;
    public final Setting<Boolean> canDestroy;
    public final Setting<Boolean> canPlaceOn;
    public final Setting<Boolean> additional;
    public final Setting<Boolean> dye;
    public final Setting<Boolean> upgrades;
    private boolean updateTooltips;
    private static final ItemStack[] ITEMS = new ItemStack[27];

    public BetterTooltips() {
        super(Categories.Render, "better-tooltips", "Displays more useful tooltips for certain items.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPreviews = this.settings.createGroup("Previews");
        this.sgOther = this.settings.createGroup("Other");
        this.sgHideFlags = this.settings.createGroup("Hide Flags");
        this.displayWhen = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("display-when")).description("When to display previews.")).defaultValue(DisplayWhen.Keybind)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.keybind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("keybind")).description("The bind for keybind mode.")).defaultValue(Keybind.fromKey(342))).visible(() -> this.displayWhen.get() == DisplayWhen.Keybind)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.middleClickOpen = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("middle-click-open")).description("Opens a GUI window with the inventory of the storage block or book when you middle click the item.")).defaultValue(true)).build());
        this.pauseInCreative = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-in-creative")).description("Pauses middle click open while the player is in creative mode.")).defaultValue(true)).visible(this.middleClickOpen::get)).build());
        this.shulkers = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("containers")).description("Shows a preview of a containers when hovering over it in an inventory.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.shulkerCompactTooltip = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("compact-shulker-tooltip")).description("Compacts the lines of the shulker tooltip.")).defaultValue(true)).build());
        this.echest = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("echests")).description("Shows a preview of your echest when hovering over it in an inventory.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.maps = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("maps")).description("Shows a preview of a map when hovering over it in an inventory.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.mapsScale = this.sgPreviews.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("map-scale")).description("The scale of the map preview.")).defaultValue(1.0).min(0.001).sliderMax(1.0).visible(this.maps::get)).build());
        this.books = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("books")).description("Shows contents of a book when hovering over it in an inventory.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.banners = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("banners")).description("Shows banners' patterns when hovering over it in an inventory. Also works with shields.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.entitiesInBuckets = this.sgPreviews.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("entities-in-buckets")).description("Shows entities in buckets when hovering over it in an inventory.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.byteSize = this.sgOther.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("byte-size")).description("Displays an item's size in bytes in the tooltip.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.statusEffects = this.sgOther.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("status-effects")).description("Adds list of status effects to tooltips of food items.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.beehive = this.sgOther.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("beehive")).description("Displays information about a beehive or bee nest.")).defaultValue(true)).onChanged(value -> {
            this.updateTooltips = true;
        })).build());
        this.tooltip = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tooltip")).description("Show the tooltip when it's hidden.")).defaultValue(false)).build());
        this.enchantments = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("enchantments")).description("Show enchantments when it's hidden.")).defaultValue(false)).build());
        this.modifiers = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("modifiers")).description("Show item modifiers when it's hidden.")).defaultValue(false)).build());
        this.unbreakable = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unbreakable")).description("Show \"Unbreakable\" tag when it's hidden.")).defaultValue(false)).build());
        this.canDestroy = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("can-destroy")).description("Show \"CanDestroy\" tag when it's hidden.")).defaultValue(false)).build());
        this.canPlaceOn = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("can-place-on")).description("Show \"CanPlaceOn\" tag when it's hidden.")).defaultValue(false)).build());
        this.additional = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("additional")).description("Show potion effects, firework status, book author, etc when it's hidden.")).defaultValue(false)).build());
        this.dye = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("dye")).description("Show dyed item tags when it's hidden.")).defaultValue(false)).build());
        this.upgrades = this.sgHideFlags.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("armor-trim")).description("Show armor trims when it's hidden.")).defaultValue(false)).build());
        this.updateTooltips = false;
    }

    @EventHandler
    private void appendTooltip(ItemStackTooltipEvent event) {
        if (!this.tooltip.get().booleanValue() && event.list().isEmpty()) {
            this.appendPreviewTooltipText(event, false);
            return;
        }
        if (this.statusEffects.get().booleanValue()) {
            if (event.itemStack().getItem() == Items.SUSPICIOUS_STEW) {
                SuspiciousStewEffectsComponent stewEffectsComponent = (SuspiciousStewEffectsComponent)event.itemStack().get(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS);
                if (stewEffectsComponent != null) {
                    for (SuspiciousStewEffectsComponent.StewEffect effectTag : stewEffectsComponent.comp_2416()) {
                        StatusEffectInstance effect = new StatusEffectInstance(effectTag.comp_1838(), effectTag.comp_1839(), 0);
                        event.appendStart((Text)this.getStatusText(effect));
                    }
                }
            } else {
                FoodComponent food = (FoodComponent)event.itemStack().get(DataComponentTypes.FOOD);
                if (food != null) {
                    food.comp_2495().forEach(e -> event.appendStart((Text)this.getStatusText(e.comp_2496())));
                }
            }
        }
        if (this.beehive.get().booleanValue() && (event.itemStack().getItem() == Items.BEEHIVE || event.itemStack().getItem() == Items.BEE_NEST)) {
            List bees;
            BlockStateComponent blockStateComponent = (BlockStateComponent)event.itemStack().get(DataComponentTypes.BLOCK_STATE);
            if (blockStateComponent != null) {
                String level = (String)blockStateComponent.comp_2381().get("honey_level");
                event.appendStart((Text)Text.literal((String)String.format("%sHoney level: %s%s%s.", Formatting.GRAY, Formatting.YELLOW, level, Formatting.GRAY)));
            }
            if ((bees = (List)event.itemStack().get(DataComponentTypes.BEES)) != null) {
                event.appendStart((Text)Text.literal((String)String.format("%sBees: %s%d%s.", Formatting.GRAY, Formatting.YELLOW, bees.size(), Formatting.GRAY)));
            }
        }
        if (this.byteSize.get().booleanValue()) {
            try {
                event.itemStack().encode((RegistryWrapper.WrapperLookup)this.mc.player.getRegistryManager()).write((DataOutput)ByteCountDataOutput.INSTANCE);
                int byteCount = ByteCountDataOutput.INSTANCE.getCount();
                ByteCountDataOutput.INSTANCE.reset();
                String count = byteCount >= 1024 ? String.format("%.2f kb", Float.valueOf((float)byteCount / 1024.0f)) : String.format("%d bytes", byteCount);
                event.appendEnd((Text)Text.literal((String)count).formatted(Formatting.GRAY));
            }
            catch (Exception e2) {
                event.appendEnd((Text)Text.literal((String)"Error getting bytes.").formatted(Formatting.RED));
            }
        }
        this.appendPreviewTooltipText(event, true);
    }

    /*
     * Enabled aggressive block sorting
     */
    @EventHandler
    private void getTooltipData(TooltipDataEvent event) {
        if (this.previewShulkers() && Utils.hasItems(event.itemStack)) {
            Utils.getItemsInContainerItem(event.itemStack, ITEMS);
            event.tooltipData = new ContainerTooltipComponent(ITEMS, Utils.getShulkerColor(event.itemStack));
            return;
        }
        if (event.itemStack.getItem() == Items.ENDER_CHEST && this.previewEChest()) {
            event.tooltipData = EChestMemory.isKnown() ? new ContainerTooltipComponent((ItemStack[])EChestMemory.ITEMS.toArray((Object[])new ItemStack[27]), ECHEST_COLOR) : new TextTooltipComponent((Text)Text.literal((String)"Unknown ender chest inventory.").formatted(Formatting.DARK_RED));
            return;
        }
        if (event.itemStack.getItem() == Items.FILLED_MAP && this.previewMaps()) {
            MapIdComponent mapIdComponent = (MapIdComponent)event.itemStack.get(DataComponentTypes.MAP_ID);
            if (mapIdComponent == null) return;
            event.tooltipData = new MapTooltipComponent(mapIdComponent.comp_2315());
            return;
        }
        if ((event.itemStack.getItem() == Items.WRITABLE_BOOK || event.itemStack.getItem() == Items.WRITTEN_BOOK) && this.previewBooks()) {
            Text page = this.getFirstPage(event.itemStack);
            if (page == null) return;
            event.tooltipData = new BookTooltipComponent(page);
            return;
        }
        if (event.itemStack.getItem() instanceof BannerItem && this.previewBanners()) {
            event.tooltipData = new BannerTooltipComponent(event.itemStack);
            return;
        }
        Item page = event.itemStack.getItem();
        if (page instanceof BannerPatternItem) {
            BannerPatternItem bannerPatternItem = (BannerPatternItem)page;
            if (this.previewBanners()) {
                event.tooltipData = new BannerTooltipComponent(DyeColor.GRAY, this.createBannerPatternsComponent(bannerPatternItem));
                return;
            }
        }
        if (event.itemStack.getItem() == Items.SHIELD && this.previewBanners()) {
            if (event.itemStack.get(DataComponentTypes.BASE_COLOR) == null) {
                if (((BannerPatternsComponent)event.itemStack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, (Object)BannerPatternsComponent.DEFAULT)).comp_2428().isEmpty()) return;
            }
            event.tooltipData = this.createBannerFromShield(event.itemStack);
            return;
        }
        page = event.itemStack.getItem();
        if (!(page instanceof EntityBucketItem)) return;
        EntityBucketItem bucketItem = (EntityBucketItem)page;
        if (!this.previewEntities()) return;
        EntityType<?> type = ((EntityBucketItemAccessor)bucketItem).getEntityType();
        Entity entity = type.create((World)this.mc.world);
        if (entity == null) return;
        ((Bucketable)entity).copyDataFromNbt(((NbtComponent)event.itemStack.get(DataComponentTypes.BUCKET_ENTITY_DATA)).copyNbt());
        ((EntityAccessor)entity).setInWater(true);
        event.tooltipData = new EntityTooltipComponent(entity);
    }

    public void applyCompactShulkerTooltip(ItemStack shulkerItem, List<Text> tooltip) {
        if (shulkerItem.contains(DataComponentTypes.CONTAINER_LOOT)) {
            tooltip.add((Text)Text.literal((String)"???????"));
        }
        if (Utils.hasItems(shulkerItem)) {
            Utils.getItemsInContainerItem(shulkerItem, ITEMS);
            Object2IntOpenHashMap counts = new Object2IntOpenHashMap();
            for (ItemStack item : ITEMS) {
                if (item.isEmpty()) continue;
                int count = counts.getInt((Object)item.getItem());
                counts.put((Object)item.getItem(), count + item.getCount());
            }
            counts.keySet().stream().sorted(Comparator.comparingInt(arg_0 -> BetterTooltips.lambda$applyCompactShulkerTooltip$13((Object2IntMap)counts, arg_0))).limit(5L).forEach(arg_0 -> BetterTooltips.lambda$applyCompactShulkerTooltip$14((Object2IntMap)counts, tooltip, arg_0));
            if (counts.size() > 5) {
                tooltip.add((Text)Text.translatable((String)"container.shulkerBox.more", (Object[])new Object[]{counts.size() - 5}).formatted(Formatting.ITALIC));
            }
        }
    }

    private void appendPreviewTooltipText(ItemStackTooltipEvent event, boolean spacer) {
        if (!this.isPressed() && (this.shulkers.get() != false && Utils.hasItems(event.itemStack()) || event.itemStack().getItem() == Items.ENDER_CHEST && this.echest.get() != false || event.itemStack().getItem() == Items.FILLED_MAP && this.maps.get() != false || event.itemStack().getItem() == Items.WRITABLE_BOOK && this.books.get() != false || event.itemStack().getItem() == Items.WRITTEN_BOOK && this.books.get() != false || event.itemStack().getItem() instanceof EntityBucketItem && this.entitiesInBuckets.get() != false || event.itemStack().getItem() instanceof BannerItem && this.banners.get() != false || event.itemStack().getItem() instanceof BannerPatternItem && this.banners.get() != false || event.itemStack().getItem() == Items.SHIELD && this.banners.get().booleanValue())) {
            if (spacer) {
                event.appendEnd((Text)Text.literal((String)""));
            }
            event.appendEnd((Text)Text.literal((String)("Hold " + String.valueOf(Formatting.YELLOW) + String.valueOf(this.keybind) + String.valueOf(Formatting.RESET) + " to preview")));
        }
    }

    private MutableText getStatusText(StatusEffectInstance effect) {
        MutableText text = Text.translatable((String)effect.getTranslationKey());
        if (effect.getAmplifier() != 0) {
            text.append(String.format(" %d (%s)", effect.getAmplifier() + 1, StatusEffectUtil.getDurationText((StatusEffectInstance)effect, (float)1.0f, (float)this.mc.world.getTickManager().getTickRate()).getString()));
        } else {
            text.append(String.format(" (%s)", StatusEffectUtil.getDurationText((StatusEffectInstance)effect, (float)1.0f, (float)this.mc.world.getTickManager().getTickRate()).getString()));
        }
        if (((StatusEffect)effect.getEffectType().comp_349()).isBeneficial()) {
            return text.formatted(Formatting.BLUE);
        }
        return text.formatted(Formatting.RED);
    }

    private Text getFirstPage(ItemStack bookItem) {
        if (bookItem.get(DataComponentTypes.WRITABLE_BOOK_CONTENT) != null) {
            List pages = ((WritableBookContentComponent)bookItem.get(DataComponentTypes.WRITABLE_BOOK_CONTENT)).comp_2422();
            if (pages.isEmpty()) {
                return null;
            }
            return Text.literal((String)((String)((RawFilteredPair)pages.getFirst()).get(false)));
        }
        if (bookItem.get(DataComponentTypes.WRITTEN_BOOK_CONTENT) != null) {
            List pages = ((WrittenBookContentComponent)bookItem.get(DataComponentTypes.WRITTEN_BOOK_CONTENT)).comp_2422();
            if (pages.isEmpty()) {
                return null;
            }
            return (Text)((RawFilteredPair)pages.getFirst()).get(false);
        }
        return null;
    }

    private BannerPatternsComponent createBannerPatternsComponent(BannerPatternItem item) {
        return new BannerPatternsComponent.Builder().add(this.mc.player.getRegistryManager().getWrapperOrThrow(RegistryKeys.BANNER_PATTERN).getOrThrow(item.getPattern()).get(0), DyeColor.WHITE).build();
    }

    private BannerTooltipComponent createBannerFromShield(ItemStack shieldItem) {
        DyeColor dyeColor2 = (DyeColor)shieldItem.getOrDefault(DataComponentTypes.BASE_COLOR, (Object)DyeColor.WHITE);
        BannerPatternsComponent bannerPatternsComponent = (BannerPatternsComponent)shieldItem.getOrDefault(DataComponentTypes.BANNER_PATTERNS, (Object)BannerPatternsComponent.DEFAULT);
        return new BannerTooltipComponent(dyeColor2, bannerPatternsComponent);
    }

    public boolean middleClickOpen() {
        return this.isActive() && this.middleClickOpen.get() != false && (this.pauseInCreative.get() == false || !this.mc.player.isInCreativeMode());
    }

    public boolean previewShulkers() {
        return this.isActive() && this.isPressed() && this.shulkers.get() != false;
    }

    public boolean shulkerCompactTooltip() {
        return this.isActive() && this.shulkerCompactTooltip.get() != false;
    }

    private boolean previewEChest() {
        return this.isPressed() && this.echest.get() != false;
    }

    private boolean previewMaps() {
        return this.isPressed() && this.maps.get() != false;
    }

    private boolean previewBooks() {
        return this.isPressed() && this.books.get() != false;
    }

    private boolean previewBanners() {
        return this.isPressed() && this.banners.get() != false;
    }

    private boolean previewEntities() {
        return this.isPressed() && this.entitiesInBuckets.get() != false;
    }

    private boolean isPressed() {
        return this.keybind.get().isPressed() && this.displayWhen.get() == DisplayWhen.Keybind || this.displayWhen.get() == DisplayWhen.Always;
    }

    public boolean updateTooltips() {
        if (this.updateTooltips && this.isActive()) {
            this.updateTooltips = false;
            return true;
        }
        return false;
    }

    private static /* synthetic */ void lambda$applyCompactShulkerTooltip$14(Object2IntMap counts, List tooltip, Item item) {
        MutableText mutableText = item.getName().copyContentOnly();
        mutableText.append((Text)Text.literal((String)" x").append(String.valueOf(counts.getInt((Object)item))).formatted(Formatting.GRAY));
        tooltip.add(mutableText);
    }

    private static /* synthetic */ int lambda$applyCompactShulkerTooltip$13(Object2IntMap counts, Item value) {
        return -counts.getInt((Object)value);
    }

    public static enum DisplayWhen {
        Keybind,
        Always;

    }
}

