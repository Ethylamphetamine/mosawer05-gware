/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.screen.slot.Slot
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.ISlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets={"net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen$CreativeSlot"})
public abstract class CreativeSlotMixin
implements ISlot {
    @Shadow
    @Final
    Slot slot;

    @Override
    public int getId() {
        return this.slot.id;
    }

    @Override
    public int getIndex() {
        return this.slot.getIndex();
    }
}

