/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockSetting
extends Setting<Block> {
    public final Predicate<Block> filter;

    public BlockSetting(String name, String description, Block defaultValue, Consumer<Block> onChanged, Consumer<Setting<Block>> onModuleActivated, IVisible visible, Predicate<Block> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
    }

    @Override
    protected Block parseImpl(String str) {
        return (Block)BlockSetting.parseId(Registries.BLOCK, str);
    }

    @Override
    protected boolean isValueValid(Block value) {
        return this.filter == null || this.filter.test(value);
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.BLOCK.getIds();
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.putString("value", Registries.BLOCK.getId((Object)((Block)this.get())).toString());
        return tag;
    }

    @Override
    protected Block load(NbtCompound tag) {
        this.value = Registries.BLOCK.get(Identifier.of((String)tag.getString("value")));
        if (this.filter != null && !this.filter.test((Block)this.value)) {
            for (Block block : Registries.BLOCK) {
                if (!this.filter.test(block)) continue;
                this.value = block;
                break;
            }
        }
        return (Block)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Block, BlockSetting> {
        private Predicate<Block> filter;

        public Builder() {
            super(null);
        }

        public Builder filter(Predicate<Block> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public BlockSetting build() {
            return new BlockSetting(this.name, this.description, (Block)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.filter);
        }
    }
}

