/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Category {
    public final String name;
    public final ItemStack icon;
    private final int nameHash;

    public Category(String name, ItemStack icon) {
        this.name = name;
        this.nameHash = name.hashCode();
        this.icon = icon == null ? Items.AIR.getDefaultStack() : icon;
    }

    public Category(String name) {
        this(name, null);
    }

    public String toString() {
        return this.name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Category category = (Category)o;
        return this.nameHash == category.nameHash;
    }

    public int hashCode() {
        return this.nameHash;
    }
}

