/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.gui.hud.ChatHudLine$Visible
 *  net.minecraft.text.OrderedText
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.mixininterface.IChatHudLineVisible;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ChatHudLine.Visible.class})
public abstract class ChatHudLineVisibleMixin
implements IChatHudLineVisible {
    @Shadow
    @Final
    private OrderedText comp_896;
    @Unique
    private int id;
    @Unique
    private GameProfile sender;
    @Unique
    private boolean startOfEntry;

    @Override
    public String meteor$getText() {
        StringBuilder sb = new StringBuilder();
        this.comp_896.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return sb.toString();
    }

    @Override
    public int meteor$getId() {
        return this.id;
    }

    @Override
    public void meteor$setId(int id) {
        this.id = id;
    }

    @Override
    public GameProfile meteor$getSender() {
        return this.sender;
    }

    @Override
    public void meteor$setSender(GameProfile profile) {
        this.sender = profile;
    }

    @Override
    public boolean meteor$isStartOfEntry() {
        return this.startOfEntry;
    }

    @Override
    public void meteor$setStartOfEntry(boolean start) {
        this.startOfEntry = start;
    }
}

