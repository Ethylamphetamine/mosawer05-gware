/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.enums.NoteBlockInstrument
 */
package meteordevelopment.meteorclient.utils.notebot.song;

import java.util.Objects;
import net.minecraft.block.enums.NoteBlockInstrument;

public class Note {
    private NoteBlockInstrument instrument;
    private int noteLevel;

    public Note(NoteBlockInstrument instrument, int noteLevel) {
        this.instrument = instrument;
        this.noteLevel = noteLevel;
    }

    public NoteBlockInstrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(NoteBlockInstrument instrument) {
        this.instrument = instrument;
    }

    public int getNoteLevel() {
        return this.noteLevel;
    }

    public void setNoteLevel(int noteLevel) {
        this.noteLevel = noteLevel;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Note note = (Note)o;
        return this.instrument == note.instrument && this.noteLevel == note.noteLevel;
    }

    public int hashCode() {
        return Objects.hash(this.instrument, this.noteLevel);
    }

    public String toString() {
        return "Note{instrument=" + String.valueOf(this.getInstrument()) + ", noteLevel=" + this.getNoteLevel() + "}";
    }
}

