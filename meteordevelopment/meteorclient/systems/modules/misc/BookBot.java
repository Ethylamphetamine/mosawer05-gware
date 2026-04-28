/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextHandler$WidthRetriever
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.WritableBookContentComponent
 *  net.minecraft.component.type.WrittenBookContentComponent
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.RawFilteredPair
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Optional;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.mixin.TextHandlerAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.font.TextHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class BookBot
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Integer> pages;
    private final Setting<Boolean> onlyAscii;
    private final Setting<Integer> delay;
    private final Setting<Boolean> sign;
    private final Setting<String> name;
    private final Setting<Boolean> count;
    private File file;
    private final PointerBuffer filters;
    private int delayTimer;
    private int bookCount;
    private Random random;

    public BookBot() {
        super(Categories.Misc, "book-bot", "Automatically writes in books.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("What kind of text to write.")).defaultValue(Mode.Random)).build());
        this.pages = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("pages")).description("The number of pages to write per book.")).defaultValue(50)).range(1, 100).sliderRange(1, 100).visible(() -> this.mode.get() != Mode.File)).build());
        this.onlyAscii = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ascii-only")).description("Only uses the characters in the ASCII charset.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Random)).build());
        this.delay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("delay")).description("The amount of delay between writing books.")).defaultValue(20)).min(1).sliderRange(1, 200).build());
        this.sign = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sign")).description("Whether to sign the book.")).defaultValue(true)).build());
        this.name = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("name")).description("The name you want to give your books.")).defaultValue("Meteor on Crack!")).visible(this.sign::get)).build());
        this.count = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("append-count")).description("Whether to append the number of the book to the title.")).defaultValue(true)).visible(this.sign::get)).build());
        this.file = new File(MeteorClient.FOLDER, "bookbot.txt");
        if (!this.file.exists()) {
            this.file = null;
        }
        this.filters = BufferUtils.createPointerBuffer((int)1);
        ByteBuffer txtFilter = MemoryUtil.memASCII((CharSequence)"*.txt");
        this.filters.put(txtFilter);
        this.filters.rewind();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList list = theme.horizontalList();
        WButton selectFile = list.add(theme.button("Select File")).widget();
        WLabel fileName = list.add(theme.label(this.file != null && this.file.exists() ? this.file.getName() : "No file selected.")).widget();
        selectFile.action = () -> {
            String path = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"Select File", (CharSequence)new File(MeteorClient.FOLDER, "bookbot.txt").getAbsolutePath(), (PointerBuffer)this.filters, null, (boolean)false);
            if (path != null) {
                this.file = new File(path);
                fileName.set(this.file.getName());
            }
        };
        return list;
    }

    @Override
    public void onActivate() {
        if (!(this.file != null && this.file.exists() || this.mode.get() != Mode.File)) {
            this.info("No file selected, please select a file in the GUI.", new Object[0]);
            this.toggle();
            return;
        }
        this.random = new Random();
        this.delayTimer = this.delay.get();
        this.bookCount = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Predicate<ItemStack> bookPredicate = i -> {
            WritableBookContentComponent component = (WritableBookContentComponent)i.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);
            return i.getItem() == Items.WRITABLE_BOOK && (component != null || component.comp_2422().isEmpty());
        };
        FindItemResult writableBook = InvUtils.find(bookPredicate);
        if (!writableBook.found()) {
            this.toggle();
            return;
        }
        if (!InvUtils.testInMainHand(bookPredicate)) {
            InvUtils.move().from(writableBook.slot()).toHotbar(this.mc.player.getInventory().selectedSlot);
            return;
        }
        if (this.delayTimer > 0) {
            --this.delayTimer;
            return;
        }
        this.delayTimer = this.delay.get();
        if (this.mode.get() == Mode.Random) {
            int origin = this.onlyAscii.get() != false ? 33 : 2048;
            int bound = this.onlyAscii.get() != false ? 126 : 0x10FFFF;
            this.writeBook(this.random.ints(origin, bound).filter(i -> !Character.isWhitespace(i) && i != 13 && i != 10).iterator());
        } else if (this.mode.get() == Mode.File) {
            if (!(this.file != null && this.file.exists() || this.mode.get() != Mode.File)) {
                this.info("No file selected, please select a file in the GUI.", new Object[0]);
                this.toggle();
                return;
            }
            if (this.file.length() == 0L) {
                MutableText message = Text.literal((String)"");
                message.append((Text)Text.literal((String)"The bookbot file is empty! ").formatted(Formatting.RED));
                message.append((Text)Text.literal((String)"Click here to edit it.").setStyle(Style.EMPTY.withFormatting(new Formatting[]{Formatting.UNDERLINE, Formatting.RED}).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, this.file.getAbsolutePath()))));
                this.info((Text)message);
                this.toggle();
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file));){
                String line;
                StringBuilder file = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    file.append(line).append('\n');
                }
                reader.close();
                this.writeBook(file.toString().chars().iterator());
            }
            catch (IOException ignored) {
                this.error("Failed to read the file.", new Object[0]);
            }
        }
    }

    private void writeBook(PrimitiveIterator.OfInt chars) {
        ArrayList<String> pages = new ArrayList<String>();
        ArrayList<RawFilteredPair> filteredPages = new ArrayList<RawFilteredPair>();
        TextHandler.WidthRetriever widthRetriever = ((TextHandlerAccessor)this.mc.textRenderer.getTextHandler()).getWidthRetriever();
        int maxPages = this.mode.get() == Mode.File ? 100 : this.pages.get();
        int pageIndex = 0;
        int lineIndex = 0;
        StringBuilder page = new StringBuilder();
        float lineWidth = 0.0f;
        while (chars.hasNext()) {
            int c = chars.nextInt();
            if (c == 13 || c == 10) {
                page.append('\n');
                lineWidth = 0.0f;
                ++lineIndex;
            } else {
                float charWidth = widthRetriever.getWidth(c, Style.EMPTY);
                if (lineWidth + charWidth > 114.0f) {
                    page.append('\n');
                    lineWidth = charWidth;
                    if (++lineIndex != 14) {
                        page.appendCodePoint(c);
                    }
                } else {
                    if (lineWidth == 0.0f && c == 32) continue;
                    lineWidth += charWidth;
                    page.appendCodePoint(c);
                }
            }
            if (lineIndex != 14) continue;
            filteredPages.add(RawFilteredPair.of((Object)Text.of((String)page.toString())));
            pages.add(page.toString());
            page.setLength(0);
            lineIndex = 0;
            if (++pageIndex == maxPages) break;
            if (c == 13 || c == 10) continue;
            page.appendCodePoint(c);
        }
        if (!page.isEmpty() && pageIndex != maxPages) {
            filteredPages.add(RawFilteredPair.of((Object)Text.of((String)page.toString())));
            pages.add(page.toString());
        }
        Object title = this.name.get();
        if (this.count.get().booleanValue() && this.bookCount != 0) {
            title = (String)title + " #" + this.bookCount;
        }
        this.mc.player.getMainHandStack().set(DataComponentTypes.WRITTEN_BOOK_CONTENT, (Object)new WrittenBookContentComponent(RawFilteredPair.of((Object)title), this.mc.player.getGameProfile().getName(), 0, filteredPages, true));
        this.mc.player.networkHandler.sendPacket((Packet)new BookUpdateC2SPacket(this.mc.player.getInventory().selectedSlot, pages, this.sign.get() != false ? Optional.of(title) : Optional.empty()));
        ++this.bookCount;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = super.toTag();
        if (this.file != null && this.file.exists()) {
            tag.putString("file", this.file.getAbsolutePath());
        }
        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        if (tag.contains("file")) {
            this.file = new File(tag.getString("file"));
        }
        return super.fromTag(tag);
    }

    public static enum Mode {
        File,
        Random;

    }
}

