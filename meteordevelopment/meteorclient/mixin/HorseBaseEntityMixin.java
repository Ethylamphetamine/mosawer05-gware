/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.passive.AbstractHorseEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IHorseBaseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={AbstractHorseEntity.class})
public abstract class HorseBaseEntityMixin
implements IHorseBaseEntity {
    @Shadow
    protected abstract void setHorseFlag(int var1, boolean var2);

    @Override
    public void setSaddled(boolean saddled) {
        this.setHorseFlag(4, saddled);
    }
}

