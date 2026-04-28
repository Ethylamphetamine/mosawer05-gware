/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  net.minecraft.block.Block
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.client.resource.language.I18n
 *  net.minecraft.client.sound.WeightedSoundSet
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.particle.ParticleEffect
 *  net.minecraft.particle.ParticleType
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.StringHelper
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.utils.misc;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ResourcePacksReloadedEvent;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

public class Names {
    private static final Map<StatusEffect, String> statusEffectNames = new Reference2ObjectOpenHashMap(16);
    private static final Map<Item, String> itemNames = new Reference2ObjectOpenHashMap(128);
    private static final Map<Block, String> blockNames = new Reference2ObjectOpenHashMap(128);
    private static final Map<RegistryKey<Enchantment>, String> enchantmentKeyNames = new WeakHashMap<RegistryKey<Enchantment>, String>(16);
    private static final Map<RegistryEntry<Enchantment>, String> enchantmentEntryNames = new Reference2ObjectOpenHashMap(16);
    private static final Map<EntityType<?>, String> entityTypeNames = new Reference2ObjectOpenHashMap(64);
    private static final Map<ParticleType<?>, String> particleTypesNames = new Reference2ObjectOpenHashMap(64);
    private static final Map<Identifier, String> soundNames = new HashMap<Identifier, String>(64);

    private Names() {
    }

    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Names.class);
    }

    @EventHandler
    private static void onResourcePacksReloaded(ResourcePacksReloadedEvent event) {
        statusEffectNames.clear();
        itemNames.clear();
        blockNames.clear();
        enchantmentEntryNames.clear();
        entityTypeNames.clear();
        particleTypesNames.clear();
        soundNames.clear();
    }

    public static String get(StatusEffect effect) {
        return statusEffectNames.computeIfAbsent(effect, effect1 -> StringHelper.stripTextFormat((String)I18n.translate((String)effect1.getTranslationKey(), (Object[])new Object[0])));
    }

    public static String get(Item item) {
        return itemNames.computeIfAbsent(item, item1 -> StringHelper.stripTextFormat((String)I18n.translate((String)item1.getTranslationKey(), (Object[])new Object[0])));
    }

    public static String get(Block block) {
        return blockNames.computeIfAbsent(block, block1 -> StringHelper.stripTextFormat((String)I18n.translate((String)block1.getTranslationKey(), (Object[])new Object[0])));
    }

    public static String get(RegistryKey<Enchantment> enchantment) {
        return enchantmentKeyNames.computeIfAbsent(enchantment, enchantment1 -> Optional.ofNullable(MinecraftClient.getInstance().getNetworkHandler()).map(ClientPlayNetworkHandler::getRegistryManager).flatMap(registryManager -> registryManager.getOptional(RegistryKeys.ENCHANTMENT)).flatMap(registry -> registry.getEntry(enchantment)).map(Names::get).orElseGet(() -> {
            String key = "enchantment." + enchantment1.getValue().toTranslationKey();
            String translated = I18n.translate((String)key, (Object[])new Object[0]);
            return translated == key ? enchantment1.getValue().toString() : translated;
        }));
    }

    public static String get(RegistryEntry<Enchantment> enchantment) {
        return enchantmentEntryNames.computeIfAbsent(enchantment, enchantment1 -> StringHelper.stripTextFormat((String)((Enchantment)enchantment.comp_349()).comp_2686().getString()));
    }

    public static String get(EntityType<?> entityType) {
        return entityTypeNames.computeIfAbsent(entityType, entityType1 -> StringHelper.stripTextFormat((String)I18n.translate((String)entityType1.getTranslationKey(), (Object[])new Object[0])));
    }

    public static String get(ParticleType<?> type) {
        if (!(type instanceof ParticleEffect)) {
            return "";
        }
        return particleTypesNames.computeIfAbsent(type, effect1 -> StringUtils.capitalize((String)Registries.PARTICLE_TYPE.getId((Object)type).getPath().replace("_", " ")));
    }

    public static String getSoundName(Identifier id) {
        return soundNames.computeIfAbsent(id, identifier -> {
            WeightedSoundSet soundSet = MeteorClient.mc.getSoundManager().get(identifier);
            if (soundSet == null) {
                return identifier.getPath();
            }
            Text text = soundSet.getSubtitle();
            if (text == null) {
                return identifier.getPath();
            }
            return StringHelper.stripTextFormat((String)text.getString());
        });
    }

    public static String get(ItemStack stack) {
        return stack.getName().getString();
    }
}

