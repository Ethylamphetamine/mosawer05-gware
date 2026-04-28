/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.sound.PositionedSoundInstance
 *  net.minecraft.client.sound.SoundInstance
 *  net.minecraft.client.toast.Toast
 *  net.minecraft.client.toast.Toast$Visibility
 *  net.minecraft.client.toast.ToastManager
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.sound.SoundEvent
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.text.TextColor
 *  net.minecraft.util.Identifier
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeteorToast
implements Toast {
    public static final int TITLE_COLOR = Color.fromRGBA(145, 61, 226, 255);
    public static final int TEXT_COLOR = Color.fromRGBA(220, 220, 220, 255);
    private static final Identifier TEXTURE = Identifier.of((String)"textures/gui/sprites/toast/advancement.png");
    private ItemStack icon;
    private Text title;
    private Text text;
    private boolean justUpdated = true;
    private boolean playedSound;
    private long start;
    private long duration;

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text, long duration) {
        this.icon = item != null ? item.getDefaultStack() : null;
        this.title = Text.literal((String)title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TITLE_COLOR)));
        this.text = text != null ? Text.literal((String)text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TEXT_COLOR))) : null;
        this.duration = duration;
    }

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text) {
        this.icon = item != null ? item.getDefaultStack() : null;
        this.title = Text.literal((String)title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TITLE_COLOR)));
        this.text = text != null ? Text.literal((String)text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TEXT_COLOR))) : null;
        this.duration = 6000L;
    }

    public Toast.Visibility draw(DrawContext context, ToastManager toastManager, long currentTime) {
        if (this.justUpdated) {
            this.start = currentTime;
            this.justUpdated = false;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        context.drawTexture(TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        int x = this.icon != null ? 28 : 12;
        int titleY = 12;
        if (this.text != null) {
            context.drawText(MeteorClient.mc.textRenderer, this.title, x, 18, TITLE_COLOR, false);
            titleY = 7;
        }
        context.drawText(MeteorClient.mc.textRenderer, this.title, x, titleY, TITLE_COLOR, false);
        if (this.icon != null) {
            context.drawItem(this.icon, 8, 8);
        }
        if (!this.playedSound) {
            MeteorClient.mc.getSoundManager().play(this.getSound());
            this.playedSound = true;
        }
        return currentTime - this.start >= this.duration ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public void setIcon(@Nullable Item item) {
        this.icon = item != null ? item.getDefaultStack() : null;
        this.justUpdated = true;
    }

    public void setTitle(@NotNull String title) {
        this.title = Text.literal((String)title).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TITLE_COLOR)));
        this.justUpdated = true;
    }

    public void setText(@Nullable String text) {
        this.text = text != null ? Text.literal((String)text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TEXT_COLOR))) : null;
        this.justUpdated = true;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        this.justUpdated = true;
    }

    public SoundInstance getSound() {
        return PositionedSoundInstance.master((SoundEvent)((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_CHIME.comp_349()), (float)1.2f, (float)1.0f);
    }
}

