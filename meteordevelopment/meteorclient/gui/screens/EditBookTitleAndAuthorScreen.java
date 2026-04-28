/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.BookScreen$Contents
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.WrittenBookContentComponent
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket
 *  net.minecraft.text.RawFilteredPair
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.gui.screens;

import java.util.ArrayList;
import java.util.Optional;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.util.Hand;

public class EditBookTitleAndAuthorScreen
extends WindowScreen {
    private final ItemStack itemStack;
    private final Hand hand;

    public EditBookTitleAndAuthorScreen(GuiTheme theme, ItemStack itemStack, Hand hand) {
        super(theme, "Edit title & author");
        this.itemStack = itemStack;
        this.hand = hand;
    }

    @Override
    public void initWidgets() {
        WTable t = this.add(this.theme.table()).expandX().widget();
        t.add(this.theme.label("Title"));
        WTextBox title = t.add(this.theme.textBox((String)((WrittenBookContentComponent)this.itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT)).comp_2419().get(MeteorClient.mc.shouldFilterText()))).minWidth(220.0).expandX().widget();
        t.row();
        t.add(this.theme.label("Author"));
        WTextBox author = t.add(this.theme.textBox(((WrittenBookContentComponent)this.itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT)).comp_2420())).minWidth(220.0).expandX().widget();
        t.row();
        t.add(this.theme.button((String)"Done")).expandX().widget().action = () -> {
            WrittenBookContentComponent component = (WrittenBookContentComponent)this.itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            WrittenBookContentComponent newComponent = new WrittenBookContentComponent(RawFilteredPair.of((Object)title.get()), author.get(), component.comp_2421(), component.comp_2422(), component.comp_2423());
            this.itemStack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, (Object)newComponent);
            BookScreen.Contents contents = new BookScreen.Contents(((WrittenBookContentComponent)this.itemStack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT)).getPages(MeteorClient.mc.shouldFilterText()));
            ArrayList<String> pages = new ArrayList<String>(contents.getPageCount());
            for (int i = 0; i < contents.getPageCount(); ++i) {
                pages.add(contents.getPage(i).getString());
            }
            MeteorClient.mc.getNetworkHandler().sendPacket((Packet)new BookUpdateC2SPacket(this.hand == Hand.MAIN_HAND ? MeteorClient.mc.player.getInventory().selectedSlot : 40, pages, Optional.of(title.get())));
            this.close();
        };
    }
}

