/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.command.CommandSource
 *  net.minecraft.entity.decoration.ItemFrameEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class PeekCommand
extends Command {
    private static final ItemStack[] ITEMS = new ItemStack[27];
    private static final SimpleCommandExceptionType CANT_PEEK = new SimpleCommandExceptionType((Message)Text.literal((String)"You must be holding a storage block or looking at an item frame."));

    public PeekCommand() {
        super("peek", "Lets you see what's inside storage block items.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (Utils.openContainer(PeekCommand.mc.player.getMainHandStack(), ITEMS, true)) {
                return 1;
            }
            if (Utils.openContainer(PeekCommand.mc.player.getOffHandStack(), ITEMS, true)) {
                return 1;
            }
            if (PeekCommand.mc.targetedEntity instanceof ItemFrameEntity && Utils.openContainer(((ItemFrameEntity)PeekCommand.mc.targetedEntity).getHeldItemStack(), ITEMS, true)) {
                return 1;
            }
            throw CANT_PEEK.create();
        });
    }
}

