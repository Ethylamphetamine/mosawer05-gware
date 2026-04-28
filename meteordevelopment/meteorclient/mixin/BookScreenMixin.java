/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
 *  net.minecraft.client.gui.Element
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.BookScreen
 *  net.minecraft.client.gui.screen.ingame.BookScreen$Contents
 *  net.minecraft.client.gui.widget.ButtonWidget$Builder
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtIo
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.nbt.NbtString
 *  net.minecraft.text.Text
 *  net.minecraft.util.Hand
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.EditBookTitleAndAuthorScreen;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BookScreen.class})
public abstract class BookScreenMixin
extends Screen {
    @Shadow
    private BookScreen.Contents contents;
    @Shadow
    private int pageIndex;

    public BookScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo info) {
        this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Copy"), button -> {
            NbtList listTag = new NbtList();
            for (int i = 0; i < this.contents.getPageCount(); ++i) {
                listTag.add((Object)NbtString.of((String)this.contents.getPage(i).getString()));
            }
            NbtCompound tag = new NbtCompound();
            tag.put("pages", (NbtElement)listTag);
            tag.putInt("currentPage", this.pageIndex);
            FastByteArrayOutputStream bytes = new FastByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream((OutputStream)bytes);
            try {
                NbtIo.write((NbtElement)tag, (DataOutput)out);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            String encoded = Base64.getEncoder().encodeToString(bytes.array);
            long available = MemoryStack.stackGet().getPointer();
            long size = MemoryUtil.memLengthUTF8((CharSequence)encoded, (boolean)true);
            if (size > available) {
                ChatUtils.error("Could not copy to clipboard: Out of memory.", new Object[0]);
            } else {
                GLFW.glfwSetClipboardString((long)MeteorClient.mc.getWindow().getHandle(), (CharSequence)encoded);
            }
        }).position(4, 4).size(120, 20).build());
        ItemStack itemStack = MeteorClient.mc.player.getMainHandStack();
        Hand hand = Hand.MAIN_HAND;
        if (itemStack.getItem() != Items.WRITTEN_BOOK) {
            itemStack = MeteorClient.mc.player.getOffHandStack();
            hand = Hand.OFF_HAND;
        }
        if (itemStack.getItem() != Items.WRITTEN_BOOK) {
            return;
        }
        ItemStack book = itemStack;
        Hand hand2 = hand;
        this.addDrawableChild((Element)new ButtonWidget.Builder((Text)Text.literal((String)"Edit title & author"), button -> MeteorClient.mc.setScreen((Screen)new EditBookTitleAndAuthorScreen(GuiThemes.get(), book, hand2))).position(4, 26).size(120, 20).build());
    }
}

