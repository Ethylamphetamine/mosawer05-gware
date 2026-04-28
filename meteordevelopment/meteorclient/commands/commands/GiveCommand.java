/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.command.argument.ItemStackArgumentType
 *  net.minecraft.item.ItemStack
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

public class GiveCommand
extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType((Message)Text.literal((String)"You must be in creative mode to use this."));
    private static final SimpleCommandExceptionType NO_SPACE = new SimpleCommandExceptionType((Message)Text.literal((String)"No space in hotbar."));

    public GiveCommand() {
        super("give", "Gives you any item.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(((RequiredArgumentBuilder)GiveCommand.argument("item", ItemStackArgumentType.itemStack((CommandRegistryAccess)REGISTRY_ACCESS)).executes(context -> {
            if (!GiveCommand.mc.player.getAbilities().creativeMode) {
                throw NOT_IN_CREATIVE.create();
            }
            ItemStack item = ItemStackArgumentType.getItemStackArgument((CommandContext)context, (String)"item").createStack(1, false);
            FindItemResult fir = InvUtils.find(ItemStack::isEmpty, 0, 8);
            if (!fir.found()) {
                throw NO_SPACE.create();
            }
            mc.getNetworkHandler().sendPacket((Packet)new CreativeInventoryActionC2SPacket(36 + fir.slot(), item));
            return 1;
        })).then(GiveCommand.argument("number", IntegerArgumentType.integer()).executes(context -> {
            if (!GiveCommand.mc.player.getAbilities().creativeMode) {
                throw NOT_IN_CREATIVE.create();
            }
            ItemStack item = ItemStackArgumentType.getItemStackArgument((CommandContext)context, (String)"item").createStack(IntegerArgumentType.getInteger((CommandContext)context, (String)"number"), false);
            FindItemResult fir = InvUtils.find(ItemStack::isEmpty, 0, 8);
            if (!fir.found()) {
                throw NO_SPACE.create();
            }
            mc.getNetworkHandler().sendPacket((Packet)new CreativeInventoryActionC2SPacket(36 + fir.slot(), item));
            return 1;
        })));
    }
}

