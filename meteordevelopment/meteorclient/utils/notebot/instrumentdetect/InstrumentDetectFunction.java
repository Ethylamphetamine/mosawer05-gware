/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.enums.NoteBlockInstrument
 *  net.minecraft.util.math.BlockPos
 */
package meteordevelopment.meteorclient.utils.notebot.instrumentdetect;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.util.math.BlockPos;

public interface InstrumentDetectFunction {
    public NoteBlockInstrument detectInstrument(BlockState var1, BlockPos var2);
}

