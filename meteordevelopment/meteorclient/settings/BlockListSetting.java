/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockListSetting
extends Setting<List<Block>> {
    public final Predicate<Block> filter;

    public BlockListSetting(String name, String description, List<Block> defaultValue, Consumer<List<Block>> onChanged, Consumer<Setting<List<Block>>> onModuleActivated, Predicate<Block> filter, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    public void resetImpl() {
        this.value = new ArrayList((Collection)this.defaultValue);
    }

    @Override
    protected List<Block> parseImpl(String str) {
        String[] values = str.split(",");
        ArrayList<Block> blocks = new ArrayList<Block>(values.length);
        try {
            for (String value : values) {
                Block block = (Block)BlockListSetting.parseId(Registries.BLOCK, value);
                if (block == null || this.filter != null && !this.filter.test(block)) continue;
                blocks.add(block);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return blocks;
    }

    @Override
    protected boolean isValueValid(List<Block> value) {
        return true;
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.BLOCK.getIds();
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtList valueTag = new NbtList();
        for (Block block : (List)this.get()) {
            valueTag.add((Object)NbtString.of((String)Registries.BLOCK.getId((Object)block).toString()));
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    protected List<Block> load(NbtCompound tag) {
        ((List)this.get()).clear();
        NbtList valueTag = tag.getList("value", 8);
        for (NbtElement tagI : valueTag) {
            Block block = (Block)Registries.BLOCK.get(Identifier.of((String)tagI.asString()));
            if (this.filter != null && !this.filter.test(block)) continue;
            ((List)this.get()).add(block);
        }
        return (List)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, List<Block>, BlockListSetting> {
        private Predicate<Block> filter;

        public Builder() {
            super(new ArrayList(0));
        }

        @Override
        public Builder defaultValue(Block ... defaults) {
            return (Builder)this.defaultValue(defaults != null ? Arrays.asList(defaults) : new ArrayList());
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BlockListSetting build() {
            return new BlockListSetting(this.name, this.description, (List)this.defaultValue, this.onChanged, this.onModuleActivated, this.filter, this.visible);
        }
    }
}

