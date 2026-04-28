/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 */
package meteordevelopment.meteorclient.gui.utils;

import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;

public class WindowConfig
implements ISerializable<WindowConfig> {
    public boolean expanded = true;
    public double x = -1.0;
    public double y = -1.0;

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("expanded", this.expanded);
        tag.putDouble("x", this.x);
        tag.putDouble("y", this.y);
        return tag;
    }

    @Override
    public WindowConfig fromTag(NbtCompound tag) {
        this.expanded = tag.getBoolean("expanded");
        this.x = tag.getDouble("x");
        this.y = tag.getDouble("y");
        return this;
    }
}

