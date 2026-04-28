/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.ObjectIterators
 *  net.minecraft.block.entity.BlockEntityType
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.registry.Registries
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
package meteordevelopment.meteorclient.settings;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
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

public class StorageBlockListSetting
extends Setting<List<BlockEntityType<?>>> {
    public static final BlockEntityType<?>[] STORAGE_BLOCKS = new BlockEntityType[]{BlockEntityType.BARREL, BlockEntityType.BLAST_FURNACE, BlockEntityType.BREWING_STAND, BlockEntityType.CAMPFIRE, BlockEntityType.CHEST, BlockEntityType.CHISELED_BOOKSHELF, BlockEntityType.CRAFTER, BlockEntityType.DISPENSER, BlockEntityType.DECORATED_POT, BlockEntityType.DROPPER, BlockEntityType.ENDER_CHEST, BlockEntityType.FURNACE, BlockEntityType.HOPPER, BlockEntityType.SHULKER_BOX, BlockEntityType.SMOKER, BlockEntityType.TRAPPED_CHEST};
    public static final Registry<BlockEntityType<?>> REGISTRY = new SRegistry();

    public StorageBlockListSetting(String name, String description, List<BlockEntityType<?>> defaultValue, Consumer<List<BlockEntityType<?>>> onChanged, Consumer<Setting<List<BlockEntityType<?>>>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<BlockEntityType<?>> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList blocks = new ArrayList(values.length);
        try {
            for (String value : values) {
                BlockEntityType block = (BlockEntityType)StorageBlockListSetting.parseId(Registries.BLOCK_ENTITY_TYPE, value);
                if (block == null) continue;
                blocks.add(block);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return blocks;
    }

    @Override
    protected boolean isValueValid(List<BlockEntityType<?>> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.BLOCK_ENTITY_TYPE.getIds();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (BlockEntityType type : (List)this.get()) {
            Identifier id = Registries.BLOCK_ENTITY_TYPE.getId((Object)type);
            if (id == null) continue;
            valueTag.add((Object)NbtString.of((String)id.toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    public List<BlockEntityType<?>> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            BlockEntityType type = (BlockEntityType)Registries.BLOCK_ENTITY_TYPE.get(Identifier.of((String)tagI.asString()));
            if (type == null) continue;
            ((List)this.get()).add(type);
        }
        return (List)this.get();
    }

    private static class SRegistry
    extends SimpleRegistry<BlockEntityType<?>> {
        public SRegistry() {
            super(RegistryKey.ofRegistry((Identifier)MeteorClient.identifier("storage-blocks")), Lifecycle.stable());
        }

        public int size() {
            return STORAGE_BLOCKS.length;
        }

        @Nullable
        public Identifier getId(BlockEntityType<?> entry) {
            return null;
        }

        public Optional<RegistryKey<BlockEntityType<?>>> getKey(BlockEntityType<?> entry) {
            return Optional.empty();
        }

        public int getRawId(@Nullable BlockEntityType<?> entry) {
            return 0;
        }

        @Nullable
        public BlockEntityType<?> get(@Nullable RegistryKey<BlockEntityType<?>> key) {
            return null;
        }

        @Nullable
        public BlockEntityType<?> get(@Nullable Identifier id) {
            return null;
        }

        public Lifecycle getLifecycle() {
            return null;
        }

        public Set<Identifier> getIds() {
            return null;
        }

        public BlockEntityType<?> getOrThrow(int index) {
            return (BlockEntityType)super.getOrThrow(index);
        }

        public boolean containsId(Identifier id) {
            return false;
        }

        @Nullable
        public BlockEntityType<?> get(int index) {
            return null;
        }

        @NotNull
        public Iterator<BlockEntityType<?>> iterator() {
            return ObjectIterators.wrap((Object[])STORAGE_BLOCKS);
        }

        public boolean contains(RegistryKey<BlockEntityType<?>> key) {
            return false;
        }

        public Set<Map.Entry<RegistryKey<BlockEntityType<?>>, BlockEntityType<?>>> getEntrySet() {
            return null;
        }

        public Optional<RegistryEntry.Reference<BlockEntityType<?>>> getRandom(Random random) {
            return Optional.empty();
        }

        public Registry<BlockEntityType<?>> freeze() {
            return null;
        }

        public RegistryEntry.Reference<BlockEntityType<?>> createEntry(BlockEntityType<?> value) {
            return null;
        }

        public Optional<RegistryEntry.Reference<BlockEntityType<?>>> getEntry(int rawId) {
            return Optional.empty();
        }

        public Optional<RegistryEntry.Reference<BlockEntityType<?>>> getEntry(RegistryKey<BlockEntityType<?>> key) {
            return Optional.empty();
        }

        public Stream<RegistryEntry.Reference<BlockEntityType<?>>> streamEntries() {
            return null;
        }

        public Optional<RegistryEntryList.Named<BlockEntityType<?>>> getEntryList(TagKey<BlockEntityType<?>> tag) {
            return Optional.empty();
        }

        public RegistryEntryList.Named<BlockEntityType<?>> getOrCreateEntryList(TagKey<BlockEntityType<?>> tag) {
            return null;
        }

        public Stream<Pair<TagKey<BlockEntityType<?>>, RegistryEntryList.Named<BlockEntityType<?>>>> streamTagsAndEntries() {
            return null;
        }

        public Stream<TagKey<BlockEntityType<?>>> streamTags() {
            return null;
        }

        public void clearTags() {
        }

        public void populateTags(Map<TagKey<BlockEntityType<?>>, List<RegistryEntry<BlockEntityType<?>>>> tagEntries) {
        }

        public Set<RegistryKey<BlockEntityType<?>>> getKeys() {
            return null;
        }
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<BlockEntityType<?>>, StorageBlockListSetting> {
        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(BlockEntityType<?> ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        @Override
        public StorageBlockListSetting build() {
            return new StorageBlockListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

