/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ListMultimap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.MultimapBuilder
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  org.apache.commons.io.FilenameUtils
 */
package meteordevelopment.meteorclient.utils.notebot.decoder;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoder;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import net.minecraft.block.enums.NoteBlockInstrument;
import org.apache.commons.io.FilenameUtils;

public class TextSongDecoder
extends SongDecoder {
    @Override
    public Song parse(File file) throws Exception {
        List<String> data = Files.readAllLines(file.toPath());
        ListMultimap notesMap = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        String title = FilenameUtils.getBaseName((String)file.getName());
        String author = "Unknown";
        for (int lineNumber = 0; lineNumber < data.size(); ++lineNumber) {
            int val;
            int key;
            int type;
            block6: {
                String line = data.get(lineNumber);
                if (line.startsWith("// Name: ")) {
                    title = line.substring(9);
                    continue;
                }
                if (line.startsWith("// Author: ")) {
                    author = line.substring(11);
                    continue;
                }
                if (line.isEmpty()) continue;
                String[] parts = data.get(lineNumber).split(":");
                if (parts.length < 2) {
                    this.notebot.warning("Malformed line %d", lineNumber);
                    continue;
                }
                type = 0;
                try {
                    key = Integer.parseInt(parts[0]);
                    val = Integer.parseInt(parts[1]);
                    if (parts.length <= 2) break block6;
                    type = Integer.parseInt(parts[2]);
                }
                catch (NumberFormatException e) {
                    this.notebot.warning("Invalid character at line %d", lineNumber);
                    continue;
                }
            }
            Note note = new Note(NoteBlockInstrument.values()[type], val);
            notesMap.put((Object)key, (Object)note);
        }
        return new Song((Multimap<Integer, Note>)notesMap, title, author);
    }
}

