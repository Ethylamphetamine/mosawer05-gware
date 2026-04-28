/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.settings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockDataSetting<T extends ICopyable<T> & ISerializable<T> & IBlockData<T>>
extends Setting<Map<Block, T>> {
    public final IGetter<T> defaultData;

    public BlockDataSetting(String name, String description, Map<Block, T> defaultValue, Consumer<Map<Block, T>> onChanged, Consumer<Setting<Map<Block, T>>> onModuleActivated, IGetter<T> defaultData, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.defaultData = defaultData;
    }

    @Override
    public void resetImpl() {
        this.value = new HashMap((Map)this.defaultValue);
    }

    @Override
    protected Map<Block, T> parseImpl(String str) {
        return new HashMap(0);
    }

    @Override
    protected boolean isValueValid(Map<Block, T> value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (Block block : ((Map)this.get()).keySet()) {
            valueTag.put(Registries.BLOCK.getId((Object)block).toString(), (NbtElement)((ISerializable)((Object)((ICopyable)((Map)this.get()).get(block)))).toTag());
        }
        tag.put("value", (NbtElement)valueTag);
        return tag;
    }

    @Override
    protected Map<Block, T> load(NbtCompound tag) {
        ((Map)this.get()).clear();
        NbtCompound valueTag = tag.getCompound("value");
        for (String key : valueTag.getKeys()) {
            ((Map)this.get()).put((Block)Registries.BLOCK.get(Identifier.of((String)key)), (ICopyable)((ISerializable)((ICopyable)this.defaultData.get()).copy()).fromTag(valueTag.getCompound(key)));
        }
        return (Map)this.get();
    }

    public static class Builder<T extends ICopyable<T> & ISerializable<T> & IBlockData<T>>
    extends Setting.SettingBuilder<Builder<T>, Map<Block, T>, BlockDataSetting<T>> {
        private IGetter<T> defaultData;

        public Builder() {
            super(new HashMap(0));
        }

        public Builder<T> defaultData(IGetter<T> defaultData) {
            this.defaultData = defaultData;
            return this;
        }

        @Override
        public BlockDataSetting<T> build() {
            return new BlockDataSetting<T>(this.name, this.description, (Map)this.defaultValue, this.onChanged, this.onModuleActivated, this.defaultData, this.visible);
        }
    }
}

