/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.LongSortedSet
 *  net.minecraft.world.entity.EntityLike
 *  net.minecraft.world.entity.EntityTrackingSection
 *  net.minecraft.world.entity.SectionedEntityCache
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SectionedEntityCache.class})
public interface SectionedEntityCacheAccessor {
    @Accessor(value="trackedPositions")
    public LongSortedSet getTrackedPositions();

    @Accessor(value="trackingSections")
    public <T extends EntityLike> Long2ObjectMap<EntityTrackingSection<T>> getTrackingSections();
}

