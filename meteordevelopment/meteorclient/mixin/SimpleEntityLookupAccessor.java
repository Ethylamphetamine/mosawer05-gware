/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityLike
 *  net.minecraft.world.entity.SectionedEntityCache
 *  net.minecraft.world.entity.SimpleEntityLookup
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package meteordevelopment.meteorclient.mixin;

import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.entity.SimpleEntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={SimpleEntityLookup.class})
public interface SimpleEntityLookupAccessor {
    @Accessor(value="cache")
    public <T extends EntityLike> SectionedEntityCache<T> getCache();
}

