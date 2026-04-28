/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 */
package meteordevelopment.meteorclient.utils.notebot.song;

import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import meteordevelopment.meteorclient.utils.notebot.song.Note;

public class Song {
    private final Multimap<Integer, Note> notesMap;
    private int lastTick;
    private final String title;
    private final String author;
    private final Set<Note> requirements = new HashSet<Note>();
    private boolean finishedLoading = false;

    public Song(Multimap<Integer, Note> notesMap, String title, String author) {
        this.notesMap = notesMap;
        this.title = title;
        this.author = author;
    }

    public void finishLoading() {
        if (this.finishedLoading) {
            throw new IllegalStateException("Song has already finished loading!");
        }
        this.lastTick = (Integer)Collections.max(this.notesMap.keySet());
        this.notesMap.values().stream().distinct().forEach(this.requirements::add);
        this.finishedLoading = true;
    }

    public Multimap<Integer, Note> getNotesMap() {
        return this.notesMap;
    }

    public Set<Note> getRequirements() {
        if (!this.finishedLoading) {
            throw new IllegalStateException("Song is still loading!");
        }
        return this.requirements;
    }

    public int getLastTick() {
        if (!this.finishedLoading) {
            throw new IllegalStateException("Song is still loading!");
        }
        return this.lastTick;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }
}

