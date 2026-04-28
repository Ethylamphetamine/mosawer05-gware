/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.settings;

import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class BlockPosSetting
extends Setting<BlockPos> {
    public BlockPosSetting(String name, String description, BlockPos defaultValue, Consumer<BlockPos> onChanged, Consumer<Setting<BlockPos>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected BlockPos parseImpl(String str) {
        List<String> values = List.of(str.split(","));
        if (values.size() != 3) {
            return null;
        }
        BlockPos bp = null;
        try {
            bp = new BlockPos(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)), Integer.parseInt(values.get(2)));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        return bp;
    }

    @Override
    protected boolean isValueValid(BlockPos value) {
        return true;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        tag.putIntArray("value", new int[]{((BlockPos)this.value).getX(), ((BlockPos)this.value).getY(), ((BlockPos)this.value).getZ()});
        return tag;
    }

    @Override
    protected BlockPos load(NbtCompound tag) {
        int[] value = tag.getIntArray("value");
        this.set(new BlockPos(value[0], value[1], value[2]));
        return (BlockPos)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, BlockPos, BlockPosSetting> {
        public Builder() {
            super(new BlockPos(0, 0, 0));
        }

        @Override
        public BlockPosSetting build() {
            return new BlockPosSetting(this.name, this.description, (BlockPos)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible);
        }
    }
}

