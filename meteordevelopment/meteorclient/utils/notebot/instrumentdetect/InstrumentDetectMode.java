/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.NoteBlock
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.state.property.Property
 */
package meteordevelopment.meteorclient.utils.notebot.instrumentdetect;

import meteordevelopment.meteorclient.utils.notebot.instrumentdetect.InstrumentDetectFunction;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Property;

public enum InstrumentDetectMode {
    BlockState((noteBlock, blockPos) -> (NoteBlockInstrument)noteBlock.get((Property)NoteBlock.INSTRUMENT)),
    BelowBlock((noteBlock, blockPos) -> MinecraftClient.getInstance().world.getBlockState(blockPos.down()).getInstrument());

    private final InstrumentDetectFunction instrumentDetectFunction;

    private InstrumentDetectMode(InstrumentDetectFunction instrumentDetectFunction) {
        this.instrumentDetectFunction = instrumentDetectFunction;
    }

    public InstrumentDetectFunction getInstrumentDetectFunction() {
        return this.instrumentDetectFunction;
    }
}

