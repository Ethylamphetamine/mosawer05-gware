/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.entity.BeaconBlockEntity
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.BeaconScreen
 *  net.minecraft.client.gui.screen.ingame.BeaconScreen$EffectButtonWidget
 *  net.minecraft.client.gui.screen.ingame.HandledScreen
 *  net.minecraft.client.gui.widget.ClickableWidget
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.screen.BeaconScreenHandler
 *  net.minecraft.screen.ScreenHandler
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterBeacons;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BeaconScreen.class})
public abstract class BeaconScreenMixin
extends HandledScreen<BeaconScreenHandler> {
    @Shadow
    protected abstract <T extends ClickableWidget> void addButton(T var1);

    public BeaconScreenMixin(BeaconScreenHandler handler, PlayerInventory inventory, Text title) {
        super((ScreenHandler)handler, inventory, title);
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/ingame/BeaconScreen;addButton(Lnet/minecraft/client/gui/widget/ClickableWidget;)V", ordinal=1, shift=At.Shift.AFTER)}, cancellable=true)
    private void changeButtons(CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) {
            return;
        }
        List effects = BeaconBlockEntity.EFFECTS_BY_LEVEL.stream().flatMap(Collection::stream).toList();
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof BeaconScreen) {
            BeaconScreen beaconScreen = (BeaconScreen)screen;
            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 2; ++y) {
                    RegistryEntry effect = (RegistryEntry)effects.get(x * 2 + y);
                    int xMin = this.x + x * 25;
                    int yMin = this.y + y * 25;
                    BeaconScreen beaconScreen2 = beaconScreen;
                    Objects.requireNonNull(beaconScreen2);
                    this.addButton(new BeaconScreen.EffectButtonWidget(beaconScreen2, xMin + 27, yMin + 32, effect, true, -1));
                    BeaconScreen beaconScreen3 = beaconScreen;
                    Objects.requireNonNull(beaconScreen3);
                    BeaconScreen.EffectButtonWidget secondaryWidget = new BeaconScreen.EffectButtonWidget(beaconScreen3, xMin + 133, yMin + 32, effect, false, 3);
                    if (((BeaconScreenHandler)this.getScreenHandler()).getProperties() != 4) {
                        secondaryWidget.active = false;
                    }
                    this.addButton(secondaryWidget);
                }
            }
        }
        ci.cancel();
    }

    @Inject(method={"drawBackground"}, at={@At(value="TAIL")})
    private void onDrawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!Modules.get().get(BetterBeacons.class).isActive()) {
            return;
        }
        context.fill(this.x + 10, this.y + 7, this.x + 220, this.y + 98, -14606047);
    }
}

