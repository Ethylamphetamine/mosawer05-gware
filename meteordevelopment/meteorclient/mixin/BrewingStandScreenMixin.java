/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.BrewingStandScreen
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.screen.BrewingStandScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.AutoBrewer;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BrewingStandScreen.class})
public abstract class BrewingStandScreenMixin
extends HandledScreen<BrewingStandScreenHandler> {
    public BrewingStandScreenMixin(BrewingStandScreenHandler container, PlayerInventory playerInventory, Text name) {
        super((ScreenHandler)container, playerInventory, name);
    }

    public void handledScreenTick() {
        super.handledScreenTick();
        if (Modules.get().isActive(AutoBrewer.class)) {
            Modules.get().get(AutoBrewer.class).tick((BrewingStandScreenHandler)this.handler);
        }
    }

    public void close() {
        if (Modules.get().isActive(AutoBrewer.class)) {
            Modules.get().get(AutoBrewer.class).onBrewingStandClose();
        }
        super.close();
    }
}

