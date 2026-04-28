/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  org.apache.commons.io.FilenameUtils
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.utils.notebot.decoder;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.utils.notebot.NotebotUtils;
import meteordevelopment.meteorclient.utils.notebot.decoder.NBSSongDecoder;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoder;
import meteordevelopment.meteorclient.utils.notebot.decoder.TextSongDecoder;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import net.minecraft.block.enums.NoteBlockInstrument;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

public class SongDecoders {
    private static final Map<String, SongDecoder> decoders = new HashMap<String, SongDecoder>();

    public static void registerDecoder(String extension, SongDecoder songDecoder) {
        decoders.put(extension, songDecoder);
    }

    public static SongDecoder getDecoder(File file) {
        return decoders.get(FilenameUtils.getExtension((String)file.getName()));
    }

    public static boolean hasDecoder(File file) {
        return decoders.containsKey(FilenameUtils.getExtension((String)file.getName()));
    }

    public static boolean hasDecoder(Path path) {
        return SongDecoders.hasDecoder(path.toFile());
    }

    @NotNull
    public static Song parse(File file) throws Exception {
        if (!SongDecoders.hasDecoder(file)) {
            throw new IllegalStateException("Decoder for this file does not exists!");
        }
        SongDecoder decoder = SongDecoders.getDecoder(file);
        Song song = decoder.parse(file);
        SongDecoders.fixSong(song);
        song.finishLoading();
        return song;
    }

    private static void fixSong(Song song) {
        Notebot notebot = Modules.get().get(Notebot.class);
        Iterator iterator = song.getNotesMap().entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            int tick = (Integer)entry.getKey();
            Note note = (Note)entry.getValue();
            int n = note.getNoteLevel();
            if (n < 0 || n > 24) {
                if (notebot.roundOutOfRange.get().booleanValue()) {
                    note.setNoteLevel(n < 0 ? 0 : 24);
                } else {
                    notebot.warning("Note at tick %d out of range.", tick);
                    iterator.remove();
                    continue;
                }
            }
            if (notebot.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
                NoteBlockInstrument newInstrument = notebot.getMappedInstrument(note.getInstrument());
                if (newInstrument == null) continue;
                note.setInstrument(newInstrument);
                continue;
            }
            note.setInstrument(null);
        }
    }

    static {
        SongDecoders.registerDecoder("nbs", new NBSSongDecoder());
        SongDecoders.registerDecoder("txt", new TextSongDecoder());
    }
}

