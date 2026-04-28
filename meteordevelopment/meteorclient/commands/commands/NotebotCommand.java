/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  net.minecraft.command.CommandSource
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 *  net.minecraft.sound.SoundEvent
 *  net.minecraft.text.Text
 *  net.minecraft.util.Util
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.NotebotSongArgumentType;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class NotebotCommand
extends Command {
    private static final SimpleCommandExceptionType INVALID_SONG = new SimpleCommandExceptionType((Message)Text.literal((String)"Invalid song."));
    private static final DynamicCommandExceptionType INVALID_PATH = new DynamicCommandExceptionType(object -> Text.literal((String)"'%s' is not a valid path.".formatted(object)));
    int ticks = -1;
    private final Map<Integer, List<Note>> song = new HashMap<Integer, List<Note>>();

    public NotebotCommand() {
        super("notebot", "Allows you load notebot files", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(NotebotCommand.literal("help").executes(ctx -> {
            Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/meteor-client/wiki/Notebot-Guide");
            return 1;
        }));
        builder.then(NotebotCommand.literal("status").executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            this.info(notebot.getStatus(), new Object[0]);
            return 1;
        }));
        builder.then(NotebotCommand.literal("pause").executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.pause();
            return 1;
        }));
        builder.then(NotebotCommand.literal("resume").executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.pause();
            return 1;
        }));
        builder.then(NotebotCommand.literal("stop").executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.stop();
            return 1;
        }));
        builder.then(NotebotCommand.literal("randomsong").executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            notebot.playRandomSong();
            return 1;
        }));
        builder.then(NotebotCommand.literal("play").then(NotebotCommand.argument("song", NotebotSongArgumentType.create()).executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            Path songPath = (Path)ctx.getArgument("song", Path.class);
            if (songPath == null || !songPath.toFile().exists()) {
                throw INVALID_SONG.create();
            }
            notebot.loadSong(songPath.toFile());
            return 1;
        })));
        builder.then(NotebotCommand.literal("preview").then(NotebotCommand.argument("song", NotebotSongArgumentType.create()).executes(ctx -> {
            Notebot notebot = Modules.get().get(Notebot.class);
            Path songPath = (Path)ctx.getArgument("song", Path.class);
            if (songPath == null || !songPath.toFile().exists()) {
                throw INVALID_SONG.create();
            }
            notebot.previewSong(songPath.toFile());
            return 1;
        })));
        builder.then(NotebotCommand.literal("record").then(NotebotCommand.literal("start").executes(ctx -> {
            this.ticks = -1;
            this.song.clear();
            MeteorClient.EVENT_BUS.subscribe(this);
            this.info("Recording started", new Object[0]);
            return 1;
        })));
        builder.then(NotebotCommand.literal("record").then(NotebotCommand.literal("cancel").executes(ctx -> {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            this.info("Recording cancelled", new Object[0]);
            return 1;
        })));
        builder.then(NotebotCommand.literal("record").then(NotebotCommand.literal("save").then(NotebotCommand.argument("name", StringArgumentType.greedyString()).executes(ctx -> {
            String name = (String)ctx.getArgument("name", String.class);
            if (name == null || name.isEmpty()) {
                throw INVALID_PATH.create((Object)name);
            }
            Path notebotFolder = MeteorClient.FOLDER.toPath().resolve("notebot");
            Path path = notebotFolder.resolve(String.format("%s.txt", name)).normalize();
            if (!path.startsWith(notebotFolder)) {
                throw INVALID_PATH.create((Object)path);
            }
            this.saveRecording(path);
            return 1;
        }))));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (this.ticks == -1) {
            return;
        }
        ++this.ticks;
    }

    @EventHandler
    private void onReadPacket(PacketEvent.Receive event) {
        PlaySoundS2CPacket sound;
        Packet<?> packet = event.packet;
        if (packet instanceof PlaySoundS2CPacket && ((SoundEvent)(sound = (PlaySoundS2CPacket)packet).getSound().comp_349()).getId().getPath().contains("note_block")) {
            if (this.ticks == -1) {
                this.ticks = 0;
            }
            List notes = this.song.computeIfAbsent(this.ticks, tick -> new ArrayList());
            Note note = this.getNote(sound);
            if (note != null) {
                notes.add(note);
            }
        }
    }

    private void saveRecording(Path path) {
        if (this.song.isEmpty()) {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            return;
        }
        try {
            MeteorClient.EVENT_BUS.unsubscribe(this);
            FileWriter file = new FileWriter(path.toFile());
            for (Map.Entry<Integer, List<Note>> entry : this.song.entrySet()) {
                int tick = entry.getKey();
                List<Note> notes = entry.getValue();
                for (Note note : notes) {
                    NoteBlockInstrument instrument = note.getInstrument();
                    int noteLevel = note.getNoteLevel();
                    file.write(String.format("%d:%d:%d\n", tick, noteLevel, instrument.ordinal()));
                }
            }
            file.close();
            this.info("Song saved.", new Object[0]);
        }
        catch (IOException e) {
            this.info("Couldn't create the file.", new Object[0]);
            MeteorClient.EVENT_BUS.unsubscribe(this);
        }
    }

    private Note getNote(PlaySoundS2CPacket soundPacket) {
        float pitch = soundPacket.getPitch();
        int noteLevel = -1;
        for (int n = 0; n < 25; ++n) {
            if (!((double)((float)Math.pow(2.0, (double)(n - 12) / 12.0)) - 0.01 < (double)pitch) || !((double)((float)Math.pow(2.0, (double)(n - 12) / 12.0)) + 0.01 > (double)pitch)) continue;
            noteLevel = n;
            break;
        }
        if (noteLevel == -1) {
            this.error("Error while bruteforcing a note level! Sound: " + String.valueOf(soundPacket.getSound().comp_349()) + " Pitch: " + pitch, new Object[0]);
            return null;
        }
        NoteBlockInstrument instrument = this.getInstrumentFromSound((SoundEvent)soundPacket.getSound().comp_349());
        if (instrument == null) {
            this.error("Can't find the instrument from sound! Sound: " + String.valueOf(soundPacket.getSound().comp_349()), new Object[0]);
            return null;
        }
        return new Note(instrument, noteLevel);
    }

    private NoteBlockInstrument getInstrumentFromSound(SoundEvent sound) {
        String path = sound.getId().getPath();
        if (path.contains("harp")) {
            return NoteBlockInstrument.HARP;
        }
        if (path.contains("basedrum")) {
            return NoteBlockInstrument.BASEDRUM;
        }
        if (path.contains("snare")) {
            return NoteBlockInstrument.SNARE;
        }
        if (path.contains("hat")) {
            return NoteBlockInstrument.HAT;
        }
        if (path.contains("bass")) {
            return NoteBlockInstrument.BASS;
        }
        if (path.contains("flute")) {
            return NoteBlockInstrument.FLUTE;
        }
        if (path.contains("bell")) {
            return NoteBlockInstrument.BELL;
        }
        if (path.contains("guitar")) {
            return NoteBlockInstrument.GUITAR;
        }
        if (path.contains("chime")) {
            return NoteBlockInstrument.CHIME;
        }
        if (path.contains("xylophone")) {
            return NoteBlockInstrument.XYLOPHONE;
        }
        if (path.contains("iron_xylophone")) {
            return NoteBlockInstrument.IRON_XYLOPHONE;
        }
        if (path.contains("cow_bell")) {
            return NoteBlockInstrument.COW_BELL;
        }
        if (path.contains("didgeridoo")) {
            return NoteBlockInstrument.DIDGERIDOO;
        }
        if (path.contains("bit")) {
            return NoteBlockInstrument.BIT;
        }
        if (path.contains("banjo")) {
            return NoteBlockInstrument.BANJO;
        }
        if (path.contains("pling")) {
            return NoteBlockInstrument.PLING;
        }
        return null;
    }
}

