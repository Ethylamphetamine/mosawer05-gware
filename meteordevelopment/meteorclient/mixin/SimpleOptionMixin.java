/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.option.SimpleOption
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package meteordevelopment.meteorclient.mixin;

import java.util.Objects;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.mixininterface.ISimpleOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={SimpleOption.class})
public abstract class SimpleOptionMixin
implements ISimpleOption {
    @Shadow
    Object value;
    @Shadow
    @Final
    private Consumer<Object> changeCallback;

    @Override
    public void set(Object value) {
        if (!MinecraftClient.getInstance().isRunning()) {
            this.value = value;
        } else if (!Objects.equals(this.value, value)) {
            this.value = value;
            this.changeCallback.accept(this.value);
        }
    }
}

