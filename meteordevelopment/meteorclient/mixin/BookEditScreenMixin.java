/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.BookEditScreen
 *  net.minecraft.client.gui.widget.ButtonWidget$Builder
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtSizeTracker
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.text.Text
 *  org.lwjgl.glfw.GLFW
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BookEditScreen.class})
public abstract class BookEditScreenMixin
extends Screen {
    @Shadow
    @Final
    private List<String> pages;
    @Shadow
    private int currentPage;
    @Shadow
    private boolean dirty;

    public BookEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void updateButtons();

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Copy"), button -> {
            NbtList listTag = new NbtList();
            this.pages.stream().map(NbtString::of).forEach(arg_0 -> listTag.add(arg_0));
            NbtCompound tag = new NbtCompound();
            tag.put("pages", (NbtElement)listTag);
            tag.putInt("currentPage", this.currentPage);
            FastByteArrayOutputStream bytes = new FastByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream((OutputStream)bytes);
            try {
                NbtIo.write((NbtElement)tag, (DataOutput)out);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                GLFW.glfwSetClipboardString((long)MeteorClient.mc.getWindow().getHandle(), (CharSequence)Base64.getEncoder().encodeToString(bytes.array));
            }
            catch (OutOfMemoryError exception) {
                GLFW.glfwSetClipboardString((long)MeteorClient.mc.getWindow().getHandle(), (CharSequence)exception.toString());
            }
        }).position(4, 4).size(120, 20).build());
        this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Paste"), button -> {
            byte[] bytes;
            String clipboard = GLFW.glfwGetClipboardString((long)MeteorClient.mc.getWindow().getHandle());
            if (clipboard == null) {
                return;
            }
            try {
                bytes = Base64.getDecoder().decode(clipboard);
            }
            catch (IllegalArgumentException ignored) {
                return;
            }
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
            try {
                NbtCompound tag = NbtIo.readCompressed((InputStream)in, (NbtSizeTracker)NbtSizeTracker.ofUnlimitedBytes());
                NbtList listTag = tag.getList("pages", 8).copy();
                this.pages.clear();
                for (int i = 0; i < listTag.size(); ++i) {
                    this.pages.add(listTag.getString(i));
                }
                if (this.pages.isEmpty()) {
                    this.pages.add("");
                }
                this.currentPage = tag.getInt("currentPage");
                this.dirty = true;
                this.updateButtons();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).position(4, 26).size(120, 20).build());
    }
}

