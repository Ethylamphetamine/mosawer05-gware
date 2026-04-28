/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.collection.TypeFilterableList
 *  net.minecraft.world.entity.EntityTrackingSection
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.world.entity.EntityTrackingSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityTrackingSection.class})
public interface EntityTrackingSectionAccessor {
    @Accessor(value="collection")
    public <T> TypeFilterableList<T> getCollection();
}

