/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ListMultimap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.MultimapBuilder
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.utils.notebot.decoder;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoder;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import net.minecraft.block.enums.NoteBlockInstrument;
import org.jetbrains.annotations.NotNull;

public class NBSSongDecoder
extends SongDecoder {
    public static final int NOTE_OFFSET = 33;

    @Override
    @NotNull
    public Song parse(File songFile) throws Exception {
        return this.parse(new FileInputStream(songFile));
    }

    @NotNull
    private Song parse(InputStream inputStream) throws Exception {
        short jumpTicks;
        ListMultimap notesMap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        short length = NBSSongDecoder.readShort(dataInputStream);
        byte nbsversion = 0;
        if (length == 0) {
            nbsversion = dataInputStream.readByte();
            dataInputStream.readByte();
            if (nbsversion >= 3) {
                length = NBSSongDecoder.readShort(dataInputStream);
            }
        }
        NBSSongDecoder.readShort(dataInputStream);
        String title = NBSSongDecoder.readString(dataInputStream);
        String author = NBSSongDecoder.readString(dataInputStream);
        NBSSongDecoder.readString(dataInputStream);
        NBSSongDecoder.readString(dataInputStream);
        float speed = (float)NBSSongDecoder.readShort(dataInputStream) / 100.0f;
        dataInputStream.readBoolean();
        dataInputStream.readByte();
        dataInputStream.readByte();
        NBSSongDecoder.readInt(dataInputStream);
        NBSSongDecoder.readInt(dataInputStream);
        NBSSongDecoder.readInt(dataInputStream);
        NBSSongDecoder.readInt(dataInputStream);
        NBSSongDecoder.readInt(dataInputStream);
        NBSSongDecoder.readString(dataInputStream);
        if (nbsversion >= 4) {
            dataInputStream.readByte();
            dataInputStream.readByte();
            NBSSongDecoder.readShort(dataInputStream);
        }
        double tick = -1.0;
        while ((jumpTicks = NBSSongDecoder.readShort(dataInputStream)) != 0) {
            short jumpLayers;
            tick += (double)((float)jumpTicks * (20.0f / speed));
            int layer = -1;
            while ((jumpLayers = NBSSongDecoder.readShort(dataInputStream)) != 0) {
                NoteBlockInstrument inst;
                layer = (short)(layer + jumpLayers);
                byte instrument = dataInputStream.readByte();
                byte key = dataInputStream.readByte();
                if (nbsversion >= 4) {
                    dataInputStream.readUnsignedByte();
                    dataInputStream.readUnsignedByte();
                    NBSSongDecoder.readShort(dataInputStream);
                }
                if ((inst = NBSSongDecoder.fromNBSInstrument(instrument)) == null) continue;
                Note note = new Note(inst, key - 33);
                NBSSongDecoder.setNote((int)Math.round(tick), note, (Multimap<Integer, Note>)notesMap);
            }
        }
        return new Song((Multimap<Integer, Note>)notesMap, title, author);
    }

    private static void setNote(int ticks, Note note, Multimap<Integer, Note> notesMap) {
        notesMap.put((Object)ticks, (Object)note);
    }

    private static short readShort(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        return (short)(byte1 + (byte2 << 8));
    }

    private static int readInt(DataInputStream dataInputStream) throws IOException {
        int byte1 = dataInputStream.readUnsignedByte();
        int byte2 = dataInputStream.readUnsignedByte();
        int byte3 = dataInputStream.readUnsignedByte();
        int byte4 = dataInputStream.readUnsignedByte();
        return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
    }

    private static String readString(DataInputStream dataInputStream) throws IOException {
        int length = NBSSongDecoder.readInt(dataInputStream);
        if (length < 0) {
            throw new EOFException("Length can't be negative! Length: " + length);
        }
        if (length > dataInputStream.available()) {
            throw new EOFException("Can't read string that is larger than a buffer! Length: " + length + " Readable Bytes Length: " + dataInputStream.available());
        }
        StringBuilder builder = new StringBuilder(length);
        while (length > 0) {
            char c = (char)dataInputStream.readByte();
            if (c == '\r') {
                c = ' ';
            }
            builder.append(c);
            --length;
        }
        return builder.toString();
    }

    private static NoteBlockInstrument fromNBSInstrument(int instrument) {
        return switch (instrument) {
            case 0 -> NoteBlockInstrument.HARP;
            case 1 -> NoteBlockInstrument.BASS;
            case 2 -> NoteBlockInstrument.BASEDRUM;
            case 3 -> NoteBlockInstrument.SNARE;
            case 4 -> NoteBlockInstrument.HAT;
            case 5 -> NoteBlockInstrument.GUITAR;
            case 6 -> NoteBlockInstrument.FLUTE;
            case 7 -> NoteBlockInstrument.BELL;
            case 8 -> NoteBlockInstrument.CHIME;
            case 9 -> NoteBlockInstrument.XYLOPHONE;
            case 10 -> NoteBlockInstrument.IRON_XYLOPHONE;
            case 11 -> NoteBlockInstrument.COW_BELL;
            case 12 -> NoteBlockInstrument.DIDGERIDOO;
            case 13 -> NoteBlockInstrument.BIT;
            case 14 -> NoteBlockInstrument.BANJO;
            case 15 -> NoteBlockInstrument.PLING;
            default -> null;
        };
    }
}

