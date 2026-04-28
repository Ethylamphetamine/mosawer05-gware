/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair
 *  it.unimi.dsi.fastutil.objects.ObjectIntPair
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.component.type.ItemEnchantmentsComponent
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.enchantment.EnchantmentHelper
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BedItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.SwordItem
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.registry.tag.EnchantmentTags
 *  net.minecraft.util.math.MathHelper
 *  org.joml.Matrix4fStack
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.ArrayList;
import java.util.Set;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnchantmentListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fStack;

public class CombatHud
extends HudElement {
    private static final Color GREEN = new Color(15, 255, 15);
    private static final Color RED = new Color(255, 15, 15);
    private static final Color BLACK = new Color(0, 0, 0, 255);
    public static final HudElementInfo<CombatHud> INFO = new HudElementInfo<CombatHud>(Hud.GROUP, "combat", "Displays information about your combat target.", CombatHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Setting<Double> range;
    private final Setting<Boolean> displayPing;
    private final Setting<Boolean> displayDistance;
    private final Setting<Set<RegistryKey<Enchantment>>> displayedEnchantments;
    private final Setting<SettingColor> backgroundColor;
    private final Setting<SettingColor> enchantmentTextColor;
    private final Setting<SettingColor> pingColor1;
    private final Setting<SettingColor> pingColor2;
    private final Setting<SettingColor> pingColor3;
    private final Setting<SettingColor> distColor1;
    private final Setting<SettingColor> distColor2;
    private final Setting<SettingColor> distColor3;
    private final Setting<SettingColor> healthColor1;
    private final Setting<SettingColor> healthColor2;
    private final Setting<SettingColor> healthColor3;
    private PlayerEntity playerEntity;

    public CombatHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).onChanged(aDouble -> this.calculateSize())).build());
        this.range = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("range")).description("The range to target players.")).defaultValue(100.0).min(1.0).sliderMax(200.0).build());
        this.displayPing = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ping")).description("Shows the player's ping.")).defaultValue(true)).build());
        this.displayDistance = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("distance")).description("Shows the distance between you and the player.")).defaultValue(true)).build());
        this.displayedEnchantments = this.sgGeneral.add(((EnchantmentListSetting.Builder)((EnchantmentListSetting.Builder)new EnchantmentListSetting.Builder().name("displayed-enchantments")).description("The enchantments that are shown on nametags.")).vanillaDefaults().build());
        this.backgroundColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color of background.")).defaultValue(new SettingColor(0, 0, 0, 64)).build());
        this.enchantmentTextColor = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("enchantment-color")).description("Color of enchantment text.")).defaultValue(new SettingColor(255, 255, 255)).build());
        this.pingColor1 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-stage-1")).description("Color of ping text when under 75.")).defaultValue(new SettingColor(15, 255, 15)).visible(this.displayPing::get)).build());
        this.pingColor2 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-stage-2")).description("Color of ping text when between 75 and 200.")).defaultValue(new SettingColor(255, 150, 15)).visible(this.displayPing::get)).build());
        this.pingColor3 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("ping-stage-3")).description("Color of ping text when over 200.")).defaultValue(new SettingColor(255, 15, 15)).visible(this.displayPing::get)).build());
        this.distColor1 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-stage-1")).description("The color when a player is within 10 blocks of you.")).defaultValue(new SettingColor(255, 15, 15)).visible(this.displayDistance::get)).build());
        this.distColor2 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-stage-2")).description("The color when a player is within 50 blocks of you.")).defaultValue(new SettingColor(255, 150, 15)).visible(this.displayDistance::get)).build());
        this.distColor3 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("distance-stage-3")).description("The color when a player is greater then 50 blocks away from you.")).defaultValue(new SettingColor(15, 255, 15)).visible(this.displayDistance::get)).build());
        this.healthColor1 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("health-stage-1")).description("The color on the left of the health gradient.")).defaultValue(new SettingColor(255, 15, 15)).build());
        this.healthColor2 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("health-stage-2")).description("The color in the middle of the health gradient.")).defaultValue(new SettingColor(255, 150, 15)).build());
        this.healthColor3 = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("health-stage-3")).description("The color on the right of the health gradient.")).defaultValue(new SettingColor(15, 255, 15)).build());
        this.calculateSize();
    }

    private void calculateSize() {
        this.setSize(175.0 * this.scale.get(), 95.0 * this.scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        renderer.post(() -> {
            double x = this.x;
            double y = this.y;
            Color primaryColor = TextHud.getSectionColor(0);
            Color secondaryColor = TextHud.getSectionColor(1);
            this.playerEntity = this.isInEditor() ? MeteorClient.mc.player : TargetUtils.getPlayerTarget(this.range.get(), SortPriority.LowestDistance);
            if (this.playerEntity == null && !this.isInEditor()) {
                return;
            }
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
            if (this.playerEntity == null) {
                if (this.isInEditor()) {
                    renderer.line(x, y, x + (double)this.getWidth(), y + (double)this.getHeight(), Color.GRAY);
                    renderer.line(x + (double)this.getWidth(), y, x, y + (double)this.getHeight(), Color.GRAY);
                    Renderer2D.COLOR.render(null);
                }
                return;
            }
            Renderer2D.COLOR.render(null);
            InventoryScreen.drawEntity((DrawContext)renderer.drawContext, (int)((int)x), (int)((int)y), (int)((int)(x + 25.0 * this.scale.get())), (int)((int)(y + 66.0 * this.scale.get())), (int)((int)(30.0 * this.scale.get())), (float)0.0f, (float)(-MathHelper.wrapDegrees((float)(this.playerEntity.prevYaw + (this.playerEntity.getYaw() - this.playerEntity.prevYaw) * MeteorClient.mc.getRenderTickCounter().getTickDelta(true)))), (float)(-this.playerEntity.getPitch()), (LivingEntity)this.playerEntity);
            x += 50.0 * this.scale.get();
            y += 5.0 * this.scale.get();
            String breakText = " | ";
            String nameText = this.playerEntity.getName().getString();
            Color nameColor = PlayerUtils.getPlayerColor(this.playerEntity, primaryColor);
            int ping = EntityUtils.getPing(this.playerEntity);
            String pingText = ping + "ms";
            Color pingColor = ping <= 75 ? (Color)this.pingColor1.get() : (ping <= 200 ? (Color)this.pingColor2.get() : (Color)this.pingColor3.get());
            double dist = 0.0;
            if (!this.isInEditor()) {
                dist = (double)Math.round((double)MeteorClient.mc.player.distanceTo((Entity)this.playerEntity) * 100.0) / 100.0;
            }
            String distText = dist + "m";
            Color distColor = dist <= 10.0 ? (Color)this.distColor1.get() : (dist <= 50.0 ? (Color)this.distColor2.get() : (Color)this.distColor3.get());
            String friendText = "Unknown";
            Color friendColor = primaryColor;
            if (Friends.get().isFriend(this.playerEntity)) {
                friendText = "Friend";
                friendColor = Config.get().friendColor.get();
            } else {
                boolean naked = true;
                for (int position = 3; position >= 0; --position) {
                    ItemStack itemStack = this.getItem(position);
                    if (itemStack.isEmpty()) continue;
                    naked = false;
                }
                if (naked) {
                    friendText = "Naked";
                    friendColor = GREEN;
                } else {
                    boolean threat = false;
                    for (int position = 5; position >= 0; --position) {
                        ItemStack itemStack = this.getItem(position);
                        if (!(itemStack.getItem() instanceof SwordItem) && itemStack.getItem() != Items.END_CRYSTAL && itemStack.getItem() != Items.RESPAWN_ANCHOR && !(itemStack.getItem() instanceof BedItem)) continue;
                        threat = true;
                    }
                    if (threat) {
                        friendText = "Threat";
                        friendColor = RED;
                    }
                }
            }
            TextRenderer.get().begin(0.45 * this.scale.get(), false, true);
            double breakWidth = TextRenderer.get().getWidth(breakText);
            double pingWidth = TextRenderer.get().getWidth(pingText);
            double friendWidth = TextRenderer.get().getWidth(friendText);
            TextRenderer.get().render(nameText, x, y, nameColor != null ? nameColor : primaryColor);
            TextRenderer.get().render(friendText, x, y += TextRenderer.get().getHeight(), friendColor);
            if (this.displayPing.get().booleanValue()) {
                TextRenderer.get().render(breakText, x + friendWidth, y, secondaryColor);
                TextRenderer.get().render(pingText, x + friendWidth + breakWidth, y, pingColor);
                if (this.displayDistance.get().booleanValue()) {
                    TextRenderer.get().render(breakText, x + friendWidth + breakWidth + pingWidth, y, secondaryColor);
                    TextRenderer.get().render(distText, x + friendWidth + breakWidth + pingWidth + breakWidth, y, distColor);
                }
            } else if (this.displayDistance.get().booleanValue()) {
                TextRenderer.get().render(breakText, x + friendWidth, y, secondaryColor);
                TextRenderer.get().render(distText, x + friendWidth + breakWidth, y, distColor);
            }
            TextRenderer.get().end();
            y += 10.0 * this.scale.get();
            int slot = 5;
            Matrix4fStack matrices = RenderSystem.getModelViewStack();
            matrices.pushMatrix();
            matrices.scale(this.scale.get().floatValue(), this.scale.get().floatValue(), 1.0f);
            x /= this.scale.get().doubleValue();
            y /= this.scale.get().doubleValue();
            TextRenderer.get().begin(0.35, false, true);
            for (int position = 0; position < 6; ++position) {
                double armorX = x + (double)(position * 20);
                double armorY = y;
                ItemStack itemStack = this.getItem(slot);
                renderer.item(itemStack, (int)(armorX * this.scale.get()), (int)(armorY * this.scale.get()), this.scale.get().floatValue(), true);
                armorY += 18.0;
                ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments((ItemStack)itemStack);
                ArrayList<ObjectIntImmutablePair> enchantmentsToShow = new ArrayList<ObjectIntImmutablePair>();
                for (Object2IntMap.Entry entry : enchantments.getEnchantmentEntries()) {
                    if (!((RegistryEntry)entry.getKey()).matches(this.displayedEnchantments.get()::contains)) continue;
                    enchantmentsToShow.add(new ObjectIntImmutablePair((Object)((RegistryEntry)entry.getKey()), entry.getIntValue()));
                }
                for (ObjectIntPair objectIntPair : enchantmentsToShow) {
                    String enchantName = Utils.getEnchantSimpleName((RegistryEntry<Enchantment>)((RegistryEntry)objectIntPair.left()), 3) + " " + objectIntPair.rightInt();
                    double enchX = armorX + 8.0 - TextRenderer.get().getWidth(enchantName) / 2.0;
                    TextRenderer.get().render(enchantName, enchX, armorY, ((RegistryEntry)objectIntPair.left()).isIn(EnchantmentTags.CURSE) ? RED : (Color)this.enchantmentTextColor.get());
                    armorY += TextRenderer.get().getHeight();
                }
                --slot;
            }
            TextRenderer.get().end();
            y = (int)((double)this.y + 75.0 * this.scale.get());
            x = this.x;
            x /= this.scale.get().doubleValue();
            y /= this.scale.get().doubleValue();
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.boxLines(x += 5.0, y += 5.0, 165.0, 11.0, BLACK);
            Renderer2D.COLOR.render(null);
            x += 2.0;
            y += 2.0;
            float maxHealth = this.playerEntity.getMaxHealth();
            int maxAbsorb = 16;
            int maxTotal = (int)(maxHealth + (float)maxAbsorb);
            int totalHealthWidth = (int)(161.0f * maxHealth / (float)maxTotal);
            int totalAbsorbWidth = 161 * maxAbsorb / maxTotal;
            float f = this.playerEntity.getHealth();
            float absorb = this.playerEntity.getAbsorptionAmount();
            double healthPercent = f / maxHealth;
            double absorbPercent = absorb / (float)maxAbsorb;
            int healthWidth = (int)((double)totalHealthWidth * healthPercent);
            int absorbWidth = (int)((double)totalAbsorbWidth * absorbPercent);
            Renderer2D.COLOR.begin();
            Renderer2D.COLOR.quad(x, y, healthWidth, 7.0, this.healthColor1.get(), this.healthColor2.get(), this.healthColor2.get(), this.healthColor1.get());
            Renderer2D.COLOR.quad(x + (double)healthWidth, y, absorbWidth, 7.0, this.healthColor2.get(), this.healthColor3.get(), this.healthColor3.get(), this.healthColor2.get());
            Renderer2D.COLOR.render(null);
            matrices.popMatrix();
        });
    }

    private ItemStack getItem(int i) {
        if (this.isInEditor()) {
            return switch (i) {
                case 0 -> Items.END_CRYSTAL.getDefaultStack();
                case 1 -> Items.NETHERITE_BOOTS.getDefaultStack();
                case 2 -> Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 3 -> Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 4 -> Items.NETHERITE_HELMET.getDefaultStack();
                case 5 -> Items.TOTEM_OF_UNDYING.getDefaultStack();
                default -> ItemStack.EMPTY;
            };
        }
        if (this.playerEntity == null) {
            return ItemStack.EMPTY;
        }
        return switch (i) {
            case 4 -> this.playerEntity.getOffHandStack();
            case 5 -> this.playerEntity.getMainHandStack();
            default -> this.playerEntity.getInventory().getArmorStack(i);
        };
    }
}

