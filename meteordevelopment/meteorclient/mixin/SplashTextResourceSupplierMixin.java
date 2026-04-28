/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.SplashTextRenderer
 *  net.minecraft.client.resource.SplashTextResourceSupplier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package meteordevelopment.meteorclient.mixin;

import java.util.List;
import java.util.Random;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SplashTextResourceSupplier.class})
public abstract class SplashTextResourceSupplierMixin {
    @Unique
    private boolean override = true;
    @Unique
    private static final Random random = new Random();
    @Unique
    private final List<String> meteorSplashes = SplashTextResourceSupplierMixin.getMeteorSplashes();

    @Inject(method={"get"}, at={@At(value="HEAD")}, cancellable=true)
    private void onApply(CallbackInfoReturnable<SplashTextRenderer> cir) {
        if (Config.get() == null || !Config.get().titleScreenSplashes.get().booleanValue()) {
            return;
        }
        if (this.override) {
            cir.setReturnValue((Object)new SplashTextRenderer(this.meteorSplashes.get(random.nextInt(this.meteorSplashes.size()))));
        }
        this.override = !this.override;
    }

    @Unique
    private static List<String> getMeteorSplashes() {
        return List.of("Meteor on Crack!", "Star Meteor Client on GitHub!", "Based utility mod.", "\u00a76MineGame159 \u00a7fbased god", "\u00a74meteorclient.com", "\u00a74Meteor on Crack!", "\u00a76Meteor on Crack!");
    }
}

