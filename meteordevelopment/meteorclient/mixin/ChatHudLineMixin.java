/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.minecraft.client.gui.hud.ChatHudLine
 *  net.minecraft.text.Text
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.mixininterface.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ChatHudLine.class})
public abstract class ChatHudLineMixin
implements IChatHudLine {
    @Shadow
    @Final
    private Text comp_893;
    @Unique
    private int id;
    @Unique
    private GameProfile sender;

    @Override
    public String meteor$getText() {
        return this.comp_893.getString();
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
}

