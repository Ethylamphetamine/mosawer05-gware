/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.MultimapBuilder
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.NoteBlock
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.sound.SoundEvent
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.state.property.Property
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 *  org.apache.commons.io.FilenameUtils
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3d
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.notebot.NotebotUtils;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoders;
import meteordevelopment.meteorclient.utils.notebot.instrumentdetect.InstrumentDetectMode;
import meteordevelopment.meteorclient.utils.notebot.song.Note;
import meteordevelopment.meteorclient.utils.notebot.song.Song;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class Notebot
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgNoteMap;
    private final SettingGroup sgRender;
    public final Setting<Integer> tickDelay;
    public final Setting<Integer> concurrentTuneBlocks;
    public final Setting<NotebotUtils.NotebotMode> mode;
    public final Setting<InstrumentDetectMode> instrumentDetectMode;
    public final Setting<Boolean> polyphonic;
    public final Setting<Boolean> autoRotate;
    public final Setting<Boolean> autoPlay;
    public final Setting<Boolean> roundOutOfRange;
    public final Setting<Boolean> swingArm;
    public final Setting<Integer> checkNoteblocksAgainDelay;
    public final Setting<Boolean> renderText;
    public final Setting<Boolean> renderBoxes;
    public final Setting<ShapeMode> shapeMode;
    public final Setting<SettingColor> untunedSideColor;
    public final Setting<SettingColor> untunedLineColor;
    public final Setting<SettingColor> tunedSideColor;
    public final Setting<SettingColor> tunedLineColor;
    public final Setting<SettingColor> tuneHitSideColor;
    private final Setting<SettingColor> tuneHitLineColor;
    public final Setting<SettingColor> scannedNoteblockSideColor;
    private final Setting<SettingColor> scannedNoteblockLineColor;
    public final Setting<Double> noteTextScale;
    public final Setting<Boolean> showScannedNoteblocks;
    private CompletableFuture<Song> loadingSongFuture;
    private Song song;
    private final Map<Note, BlockPos> noteBlockPositions;
    private final Multimap<Note, BlockPos> scannedNoteblocks;
    private final List<BlockPos> clickedBlocks;
    private Stage stage;
    private PlayingMode playingMode;
    private boolean isPlaying;
    private int currentTick;
    private int ticks;
    private WLabel status;
    private boolean anyNoteblockTuned;
    private final Map<BlockPos, Integer> tuneHits;
    private int waitTicks;

    public Notebot() {
        super(Categories.Misc, "notebot", "Plays noteblock nicely");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgNoteMap = this.settings.createGroup("Note Map", false);
        this.sgRender = this.settings.createGroup("Render", true);
        this.tickDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tick-delay")).description("The delay when loading a song.")).defaultValue(1)).sliderRange(1, 20).min(1).build());
        this.concurrentTuneBlocks = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("concurrent-tune-blocks")).description("How many noteblocks can be tuned at the same time. On Paper it is recommended to set it to 1 to avoid bugs.")).defaultValue(1)).min(1).sliderRange(1, 20).build());
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Select mode of notebot")).defaultValue(NotebotUtils.NotebotMode.ExactInstruments)).build());
        this.instrumentDetectMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("instrument-detect-mode")).description("Select an instrument detect mode. Can be useful when server has a plugin that modifies noteblock state (e.g ItemsAdder) but noteblock can still play the right note")).defaultValue(InstrumentDetectMode.BlockState)).build());
        this.polyphonic = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("polyphonic")).description("Whether or not to allow multiple notes to be played at the same time")).defaultValue(true)).build());
        this.autoRotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-rotate")).description("Should client look at note block when it wants to hit it")).defaultValue(true)).build());
        this.autoPlay = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-play")).description("Auto plays random songs")).defaultValue(false)).build());
        this.roundOutOfRange = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("round-out-of-range")).description("Rounds out of range notes")).defaultValue(false)).build());
        this.swingArm = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swing-arm")).description("Should swing arm on hit")).defaultValue(true)).build());
        this.checkNoteblocksAgainDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("check-noteblocks-again-delay")).description("How much delay should be between end of tuning and checking again")).defaultValue(10)).min(1).sliderRange(1, 20).build());
        this.renderText = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-text")).description("Whether or not to render the text above noteblocks.")).defaultValue(true)).build());
        this.renderBoxes = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-boxes")).description("Whether or not to render the outline around the noteblocks.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.untunedSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("untuned-side-color")).description("The color of the sides of the untuned blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 10)).build());
        this.untunedLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("untuned-line-color")).description("The color of the lines of the untuned blocks being rendered.")).defaultValue(new SettingColor(204, 0, 0, 255)).build());
        this.tunedSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("tuned-side-color")).description("The color of the sides of the tuned blocks being rendered.")).defaultValue(new SettingColor(0, 204, 0, 10)).build());
        this.tunedLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("tuned-line-color")).description("The color of the lines of the tuned blocks being rendered.")).defaultValue(new SettingColor(0, 204, 0, 255)).build());
        this.tuneHitSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("hit-side-color")).description("The color of the sides being rendered on noteblock tune hit.")).defaultValue(new SettingColor(255, 153, 0, 10)).build());
        this.tuneHitLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("hit-line-color")).description("The color of the lines being rendered on noteblock tune hit.")).defaultValue(new SettingColor(255, 153, 0, 255)).build());
        this.scannedNoteblockSideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("scanned-noteblock-side-color")).description("The color of the sides of the scanned noteblocks being rendered.")).defaultValue(new SettingColor(255, 255, 0, 30)).build());
        this.scannedNoteblockLineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("scanned-noteblock-line-color")).description("The color of the lines of the scanned noteblocks being rendered.")).defaultValue(new SettingColor(255, 255, 0, 255)).build());
        this.noteTextScale = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("note-text-scale")).description("The scale.")).defaultValue(1.5).min(0.0).build());
        this.showScannedNoteblocks = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("show-scanned-noteblocks")).description("Show scanned Noteblocks")).defaultValue(false)).build());
        this.loadingSongFuture = null;
        this.noteBlockPositions = new HashMap<Note, BlockPos>();
        this.scannedNoteblocks = MultimapBuilder.linkedHashKeys().arrayListValues().build();
        this.clickedBlocks = new ArrayList<BlockPos>();
        this.stage = Stage.None;
        this.playingMode = PlayingMode.None;
        this.isPlaying = false;
        this.currentTick = 0;
        this.ticks = 0;
        this.anyNoteblockTuned = false;
        this.tuneHits = new HashMap<BlockPos, Integer>();
        this.waitTicks = -1;
        for (NoteBlockInstrument inst : NoteBlockInstrument.values()) {
            NotebotUtils.OptionalInstrument optionalInstrument = NotebotUtils.OptionalInstrument.fromMinecraftInstrument(inst);
            if (optionalInstrument == null) continue;
            this.sgNoteMap.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name(this.beautifyText(inst.name()))).defaultValue(optionalInstrument)).visible(() -> this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments)).build());
        }
    }

    @Override
    public String getInfoString() {
        if (this.stage == Stage.None) {
            return "None";
        }
        return this.playingMode.toString() + " | " + this.stage.toString();
    }

    @Override
    public void onActivate() {
        this.ticks = 0;
        this.resetVariables();
    }

    private void resetVariables() {
        if (this.loadingSongFuture != null) {
            this.loadingSongFuture.cancel(true);
            this.loadingSongFuture = null;
        }
        this.clickedBlocks.clear();
        this.tuneHits.clear();
        this.anyNoteblockTuned = false;
        this.currentTick = 0;
        this.playingMode = PlayingMode.None;
        this.isPlaying = false;
        this.stage = Stage.None;
        this.song = null;
        this.noteBlockPositions.clear();
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (!this.renderBoxes.get().booleanValue()) {
            return;
        }
        if (this.stage != Stage.SetUp && this.stage != Stage.Tune && this.stage != Stage.WaitingToCheckNoteblocks && !this.isPlaying) {
            return;
        }
        if (this.showScannedNoteblocks.get().booleanValue()) {
            for (BlockPos blockPos : this.scannedNoteblocks.values()) {
                double x1 = blockPos.getX();
                double y1 = blockPos.getY();
                double z1 = blockPos.getZ();
                double x2 = blockPos.getX() + 1;
                double y2 = blockPos.getY() + 1;
                double z2 = blockPos.getZ() + 1;
                event.renderer.box(x1, y1, z1, x2, y2, z2, this.scannedNoteblockSideColor.get(), this.scannedNoteblockLineColor.get(), this.shapeMode.get(), 0);
            }
        } else {
            for (Map.Entry<Note, BlockPos> entry : this.noteBlockPositions.entrySet()) {
                Color lineColor;
                Color sideColor;
                Note note = entry.getKey();
                BlockPos blockPos = entry.getValue();
                BlockState state = this.mc.world.getBlockState(blockPos);
                if (state.getBlock() != Blocks.NOTE_BLOCK) continue;
                int level = (Integer)state.get((Property)NoteBlock.NOTE);
                double x1 = blockPos.getX();
                double y1 = blockPos.getY();
                double z1 = blockPos.getZ();
                double x2 = blockPos.getX() + 1;
                double y2 = blockPos.getY() + 1;
                double z2 = blockPos.getZ() + 1;
                if (this.clickedBlocks.contains(blockPos)) {
                    sideColor = this.tuneHitSideColor.get();
                    lineColor = this.tuneHitLineColor.get();
                } else if (note.getNoteLevel() == level) {
                    sideColor = this.tunedSideColor.get();
                    lineColor = this.tunedLineColor.get();
                } else {
                    sideColor = this.untunedSideColor.get();
                    lineColor = this.untunedLineColor.get();
                }
                event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor, lineColor, this.shapeMode.get(), 0);
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!this.renderText.get().booleanValue()) {
            return;
        }
        if (this.stage != Stage.SetUp && this.stage != Stage.Tune && this.stage != Stage.WaitingToCheckNoteblocks && !this.isPlaying) {
            return;
        }
        Vector3d pos = new Vector3d();
        for (BlockPos blockPos : this.noteBlockPositions.values()) {
            BlockState state = this.mc.world.getBlockState(blockPos);
            if (state.getBlock() != Blocks.NOTE_BLOCK) continue;
            double x = (double)blockPos.getX() + 0.5;
            double y = blockPos.getY() + 1;
            double z = (double)blockPos.getZ() + 0.5;
            pos.set(x, y, z);
            String levelText = String.valueOf(state.get((Property)NoteBlock.NOTE));
            String tuneHitsText = null;
            if (this.tuneHits.containsKey(blockPos)) {
                tuneHitsText = " -" + String.valueOf(this.tuneHits.get(blockPos));
            }
            if (!NametagUtils.to2D(pos, this.noteTextScale.get(), true)) continue;
            TextRenderer text = TextRenderer.get();
            NametagUtils.begin(pos);
            text.beginBig();
            double xScreen = text.getWidth(levelText) / 2.0;
            if (tuneHitsText != null) {
                xScreen += text.getWidth(tuneHitsText) / 2.0;
            }
            double hX = text.render(levelText, -xScreen, 0.0, Color.GREEN);
            if (tuneHitsText != null) {
                text.render(tuneHitsText, hX, 0.0, Color.RED);
            }
            text.end();
            NametagUtils.end();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        ++this.ticks;
        this.clickedBlocks.clear();
        if (this.stage == Stage.WaitingToCheckNoteblocks) {
            --this.waitTicks;
            if (this.waitTicks == 0) {
                this.waitTicks = -1;
                this.info("Checking noteblocks again...", new Object[0]);
                this.setupTuneHitsMap();
                this.stage = Stage.Tune;
            }
        } else if (this.stage == Stage.SetUp) {
            this.scanForNoteblocks();
            if (this.scannedNoteblocks.isEmpty()) {
                this.error("Can't find any nearby noteblock!", new Object[0]);
                this.stop();
                return;
            }
            this.setupNoteblocksMap();
            if (this.noteBlockPositions.isEmpty()) {
                this.error("Can't find any valid noteblock to play song.", new Object[0]);
                this.stop();
                return;
            }
            this.setupTuneHitsMap();
            this.stage = Stage.Tune;
        } else if (this.stage == Stage.Tune) {
            this.tune();
        } else if (this.stage == Stage.Playing) {
            if (!this.isPlaying) {
                return;
            }
            if (this.mc.player == null || this.currentTick > this.song.getLastTick()) {
                this.onSongEnd();
                return;
            }
            if (this.song.getNotesMap().containsKey((Object)this.currentTick)) {
                if (this.playingMode == PlayingMode.Preview) {
                    this.onTickPreview();
                } else {
                    if (this.mc.player.getAbilities().creativeMode) {
                        this.error("You need to be in survival mode.", new Object[0]);
                        this.stop();
                        return;
                    }
                    this.onTickPlay();
                }
            }
            ++this.currentTick;
            this.updateStatus();
        }
    }

    private void setupNoteblocksMap() {
        this.noteBlockPositions.clear();
        ArrayList<Note> uniqueNotesToUse = new ArrayList<Note>(this.song.getRequirements());
        HashMap incorrectNoteBlocks = new HashMap();
        for (Map.Entry entry : this.scannedNoteblocks.asMap().entrySet()) {
            Note note2 = (Note)entry.getKey();
            ArrayList noteblocks = new ArrayList((Collection)entry.getValue());
            if (uniqueNotesToUse.contains(note2)) {
                this.noteBlockPositions.put(note2, (BlockPos)noteblocks.removeFirst());
                uniqueNotesToUse.remove(note2);
            }
            if (noteblocks.isEmpty()) continue;
            if (!incorrectNoteBlocks.containsKey(note2.getInstrument())) {
                incorrectNoteBlocks.put(note2.getInstrument(), new ArrayList());
            }
            ((List)incorrectNoteBlocks.get(note2.getInstrument())).addAll(noteblocks);
        }
        block1: for (Map.Entry entry : incorrectNoteBlocks.entrySet()) {
            List positions = (List)entry.getValue();
            if (this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
                NoteBlockInstrument inst = (NoteBlockInstrument)entry.getKey();
                List foundNotes = uniqueNotesToUse.stream().filter(note -> note.getInstrument() == inst).collect(Collectors.toList());
                if (foundNotes.isEmpty()) continue;
                for (BlockPos pos : positions) {
                    if (foundNotes.isEmpty()) continue block1;
                    Note note3 = (Note)foundNotes.removeFirst();
                    this.noteBlockPositions.put(note3, pos);
                    uniqueNotesToUse.remove(note3);
                }
                continue;
            }
            for (BlockPos pos : positions) {
                if (uniqueNotesToUse.isEmpty()) continue block1;
                Note note4 = (Note)uniqueNotesToUse.removeFirst();
                this.noteBlockPositions.put(note4, pos);
            }
        }
        if (!uniqueNotesToUse.isEmpty()) {
            for (Note note5 : uniqueNotesToUse) {
                this.warning("Missing note: " + String.valueOf(note5.getInstrument()) + ", " + note5.getNoteLevel(), new Object[0]);
            }
            this.warning(uniqueNotesToUse.size() + " missing notes!", new Object[0]);
        }
    }

    private void setupTuneHitsMap() {
        this.tuneHits.clear();
        for (Map.Entry<Note, BlockPos> entry : this.noteBlockPositions.entrySet()) {
            BlockPos blockPos;
            BlockState blockState;
            int currentLevel;
            int targetLevel = entry.getKey().getNoteLevel();
            if (targetLevel == (currentLevel = ((Integer)(blockState = this.mc.world.getBlockState(blockPos = entry.getValue())).get((Property)NoteBlock.NOTE)).intValue())) continue;
            this.tuneHits.put(blockPos, Notebot.calcNumberOfHits(currentLevel, targetLevel));
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WTable table = theme.table();
        WButton openSongGUI = table.add(theme.button("Open Song GUI")).expandX().minWidth(100.0).widget();
        openSongGUI.action = () -> this.mc.setScreen((Screen)theme.notebotSongs());
        table.row();
        WButton alignCenter = table.add(theme.button("Align Center")).expandX().minWidth(100.0).widget();
        alignCenter.action = () -> {
            if (this.mc.player == null) {
                return;
            }
            Vec3d pos = Vec3d.ofBottomCenter((Vec3i)this.mc.player.getBlockPos());
            this.mc.player.setPosition(pos.x, this.mc.player.getY(), pos.z);
        };
        table.row();
        this.status = table.add(theme.label(this.getStatus())).expandCellX().widget();
        WButton pause = table.add(theme.button(this.isPlaying ? "Pause" : "Resume")).right().widget();
        pause.action = () -> {
            this.pause();
            pause.set(this.isPlaying ? "Pause" : "Resume");
            this.updateStatus();
        };
        WButton stop = table.add(theme.button("Stop")).right().widget();
        stop.action = this::stop;
        return table;
    }

    public String getStatus() {
        if (!this.isActive()) {
            return "Module disabled.";
        }
        if (this.song == null) {
            return "No song loaded.";
        }
        if (this.isPlaying) {
            return String.format("Playing song. %d/%d", this.currentTick, this.song.getLastTick());
        }
        if (this.stage == Stage.Playing) {
            return "Ready to play.";
        }
        if (this.stage == Stage.SetUp || this.stage == Stage.Tune || this.stage == Stage.WaitingToCheckNoteblocks) {
            return "Setting up the noteblocks.";
        }
        return String.format("Stage: %s.", this.stage.toString());
    }

    public void play() {
        if (this.mc.player == null) {
            return;
        }
        if (this.mc.player.getAbilities().creativeMode && this.playingMode != PlayingMode.Preview) {
            this.error("You need to be in survival mode.", new Object[0]);
        } else if (this.stage == Stage.Playing) {
            this.isPlaying = true;
            this.info("Playing.", new Object[0]);
        } else {
            this.error("No song loaded.", new Object[0]);
        }
    }

    public void pause() {
        if (!this.isActive()) {
            this.toggle();
        }
        if (this.isPlaying) {
            this.info("Pausing.", new Object[0]);
            this.isPlaying = false;
        } else {
            this.info("Resuming.", new Object[0]);
            this.isPlaying = true;
        }
    }

    public void stop() {
        this.info("Stopping.", new Object[0]);
        this.disable();
        this.updateStatus();
    }

    public void onSongEnd() {
        if (this.autoPlay.get().booleanValue() && this.playingMode != PlayingMode.Preview) {
            this.playRandomSong();
        } else {
            this.stop();
        }
    }

    public void playRandomSong() {
        File[] files = MeteorClient.FOLDER.toPath().resolve("notebot").toFile().listFiles();
        if (files == null) {
            return;
        }
        File randomSong = files[ThreadLocalRandom.current().nextInt(files.length)];
        if (SongDecoders.hasDecoder(randomSong)) {
            this.loadSong(randomSong);
        } else {
            this.playRandomSong();
        }
    }

    public void disable() {
        this.resetVariables();
        if (!this.isActive()) {
            this.toggle();
        }
    }

    public void loadSong(File file) {
        if (!this.isActive()) {
            this.toggle();
        }
        this.resetVariables();
        this.playingMode = PlayingMode.Noteblocks;
        if (!this.loadFileToMap(file, () -> {
            this.stage = Stage.SetUp;
        })) {
            this.onSongEnd();
            return;
        }
        this.updateStatus();
    }

    public void previewSong(File file) {
        if (!this.isActive()) {
            this.toggle();
        }
        this.resetVariables();
        this.playingMode = PlayingMode.Preview;
        this.loadFileToMap(file, () -> {
            this.stage = Stage.Playing;
            this.play();
        });
        this.updateStatus();
    }

    public boolean loadFileToMap(File file, Runnable callback) {
        if (!file.exists() || !file.isFile()) {
            this.error("File not found", new Object[0]);
            return false;
        }
        if (!SongDecoders.hasDecoder(file)) {
            this.error("File is in wrong format. Decoder not found.", new Object[0]);
            return false;
        }
        this.info("Loading song \"%s\".", FilenameUtils.getBaseName((String)file.getName()));
        this.loadingSongFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return SongDecoders.parse(file);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.loadingSongFuture.completeOnTimeout(null, 60L, TimeUnit.SECONDS);
        this.stage = Stage.LoadingSong;
        long time1 = System.currentTimeMillis();
        this.loadingSongFuture.whenComplete((song, ex) -> {
            if (ex == null) {
                if (song == null) {
                    this.error("Loading song '" + FilenameUtils.getBaseName((String)file.getName()) + "' timed out.", new Object[0]);
                    this.onSongEnd();
                    return;
                }
                this.song = song;
                long time2 = System.currentTimeMillis();
                long diff = time2 - time1;
                this.info("Song '" + FilenameUtils.getBaseName((String)file.getName()) + "' has been loaded to the memory! Took " + diff + "ms", new Object[0]);
                callback.run();
            } else if (ex instanceof CancellationException) {
                this.error("Loading song '" + FilenameUtils.getBaseName((String)file.getName()) + "' was cancelled.", new Object[0]);
            } else {
                this.error("An error occurred while loading song '" + FilenameUtils.getBaseName((String)file.getName()) + "'. See the logs for more details", new Object[0]);
                MeteorClient.LOG.error("An error occurred while loading song '" + FilenameUtils.getBaseName((String)file.getName()) + "'", ex);
                this.onSongEnd();
            }
        });
        return true;
    }

    private void scanForNoteblocks() {
        if (this.mc.interactionManager == null || this.mc.world == null || this.mc.player == null) {
            return;
        }
        this.scannedNoteblocks.clear();
        int min = (int)(-this.mc.player.getBlockInteractionRange()) - 2;
        int max = (int)this.mc.player.getBlockInteractionRange() + 2;
        for (int y = min; y < max; ++y) {
            for (int x = min; x < max; ++x) {
                for (int z = min; z < max; ++z) {
                    BlockPos pos = this.mc.player.getBlockPos().add(x, y + 1, z);
                    BlockState blockState = this.mc.world.getBlockState(pos);
                    if (blockState.getBlock() != Blocks.NOTE_BLOCK || !this.mc.player.canInteractWithBlockAt(pos, 1.0) || !this.isValidScanSpot(pos)) continue;
                    Note note = NotebotUtils.getNoteFromNoteBlock(blockState, pos, this.mode.get(), this.instrumentDetectMode.get().getInstrumentDetectFunction());
                    this.scannedNoteblocks.put((Object)note, (Object)pos);
                }
            }
        }
    }

    private void onTickPreview() {
        for (Note note : this.song.getNotesMap().get((Object)this.currentTick)) {
            if (this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
                this.mc.player.playSound((SoundEvent)note.getInstrument().getSound().comp_349(), 2.0f, (float)Math.pow(2.0, (double)(note.getNoteLevel() - 12) / 12.0));
                continue;
            }
            this.mc.player.playSound((SoundEvent)SoundEvents.BLOCK_NOTE_BLOCK_HARP.comp_349(), 2.0f, (float)Math.pow(2.0, (double)(note.getNoteLevel() - 12) / 12.0));
        }
    }

    private void tune() {
        if (this.tuneHits.isEmpty()) {
            if (this.anyNoteblockTuned) {
                this.anyNoteblockTuned = false;
                this.waitTicks = this.checkNoteblocksAgainDelay.get();
                this.stage = Stage.WaitingToCheckNoteblocks;
                this.info("Delaying check for noteblocks", new Object[0]);
            } else {
                this.stage = Stage.Playing;
                this.info("Loading done.", new Object[0]);
                this.play();
            }
            return;
        }
        if (this.ticks < this.tickDelay.get()) {
            return;
        }
        this.tuneBlocks();
        this.ticks = 0;
    }

    private void tuneBlocks() {
        if (this.mc.world == null || this.mc.player == null) {
            this.disable();
        }
        if (this.swingArm.get().booleanValue()) {
            this.mc.player.swingHand(Hand.MAIN_HAND);
        }
        int iterations = 0;
        Iterator<Map.Entry<BlockPos, Integer>> iterator = this.tuneHits.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            BlockPos pos = entry.getKey();
            int hitsNumber = entry.getValue();
            if (this.autoRotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos), 100, () -> this.tuneNoteblockWithPackets(pos));
            } else {
                this.tuneNoteblockWithPackets(pos);
            }
            this.clickedBlocks.add(pos);
            entry.setValue(--hitsNumber);
            if (hitsNumber == 0) {
                iterator.remove();
            }
            if (++iterations != this.concurrentTuneBlocks.get()) continue;
            return;
        }
    }

    private void tuneNoteblockWithPackets(BlockPos pos) {
        this.mc.player.networkHandler.sendPacket((Packet)new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Vec3d.ofCenter((Vec3i)pos), Direction.DOWN, pos, false), 0));
        this.anyNoteblockTuned = true;
    }

    public void updateStatus() {
        if (this.status != null) {
            this.status.set(this.getStatus());
        }
    }

    private static int calcNumberOfHits(int from, int to) {
        if (from > to) {
            return 25 - from + to;
        }
        return to - from;
    }

    private void onTickPlay() {
        Collection notes = this.song.getNotesMap().get((Object)this.currentTick);
        if (!notes.isEmpty()) {
            BlockPos firstPos;
            Optional firstNote;
            if (this.autoRotate.get().booleanValue() && (firstNote = notes.stream().findFirst()).isPresent() && (firstPos = this.noteBlockPositions.get(firstNote.get())) != null) {
                Rotations.rotate(Rotations.getYaw(firstPos), Rotations.getPitch(firstPos));
            }
            if (this.swingArm.get().booleanValue()) {
                this.mc.player.swingHand(Hand.MAIN_HAND);
            }
            for (Note note : notes) {
                BlockPos pos = this.noteBlockPositions.get(note);
                if (pos == null) {
                    return;
                }
                if (this.polyphonic.get().booleanValue()) {
                    this.playRotate(pos);
                    continue;
                }
                this.playRotate(pos);
            }
        }
    }

    private void playRotate(BlockPos pos) {
        if (this.mc.interactionManager == null) {
            return;
        }
        try {
            this.mc.player.networkHandler.sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, 0));
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
    }

    private boolean isValidScanSpot(BlockPos pos) {
        if (this.mc.world.getBlockState(pos).getBlock() != Blocks.NOTE_BLOCK) {
            return false;
        }
        return this.mc.world.getBlockState(pos.up()).isAir();
    }

    @Nullable
    public NoteBlockInstrument getMappedInstrument(@NotNull NoteBlockInstrument inst) {
        if (this.mode.get() == NotebotUtils.NotebotMode.ExactInstruments) {
            NotebotUtils.OptionalInstrument optionalInstrument = (NotebotUtils.OptionalInstrument)((Object)this.sgNoteMap.getByIndex(inst.ordinal()).get());
            return optionalInstrument.toMinecraftInstrument();
        }
        return inst;
    }

    private String beautifyText(String text) {
        text = text.toLowerCase(Locale.ROOT);
        String[] arr = text.split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
        }
        return sb.toString().trim();
    }

    public static enum Stage {
        None,
        LoadingSong,
        SetUp,
        Tune,
        WaitingToCheckNoteblocks,
        Playing;

    }

    public static enum PlayingMode {
        None,
        Preview,
        Noteblocks;

    }
}

