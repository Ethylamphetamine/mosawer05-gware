/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.client.network.ClientPlayerEntity
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.command.argument.ItemStackArgumentType
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class DropCommand
extends Command {
    private static final SimpleCommandExceptionType NOT_SPECTATOR = new SimpleCommandExceptionType((Message)Text.literal((String)"Can't drop items while in spectator."));
    private static final SimpleCommandExceptionType NO_SUCH_ITEM = new SimpleCommandExceptionType((Message)Text.literal((String)"Could not find an item with that name!"));

    public DropCommand() {
        super("drop", "Automatically drops specified items.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(DropCommand.literal("hand").executes(context -> this.drop(player -> player.dropSelectedItem(true))));
        builder.then(DropCommand.literal("offhand").executes(context -> this.drop(player -> InvUtils.drop().slotOffhand())));
        builder.then(DropCommand.literal("hotbar").executes(context -> this.drop(player -> {
            for (int i = 0; i < 9; ++i) {
                InvUtils.drop().slotHotbar(i);
            }
        })));
        builder.then(DropCommand.literal("inventory").executes(context -> this.drop(player -> {
            for (int i = 9; i < player.getInventory().main.size(); ++i) {
                InvUtils.drop().slotMain(i - 9);
            }
        })));
        builder.then(DropCommand.literal("all").executes(context -> this.drop(player -> {
            for (int i = 0; i < player.getInventory().size(); ++i) {
                InvUtils.drop().slot(i);
            }
            InvUtils.drop().slotOffhand();
        })));
        builder.then(DropCommand.literal("armor").executes(context -> this.drop(player -> {
            for (int i = 0; i < player.getInventory().armor.size(); ++i) {
                InvUtils.drop().slotArmor(i);
            }
        })));
        builder.then(DropCommand.argument("item", ItemStackArgumentType.itemStack((CommandRegistryAccess)REGISTRY_ACCESS)).executes(context -> this.drop(player -> {
            ItemStack stack = ItemStackArgumentType.getItemStackArgument((CommandContext)context, (String)"item").createStack(1, false);
            if (stack == null || stack.getItem() == Items.AIR) {
                throw NO_SUCH_ITEM.create();
            }
            for (int i = 0; i < player.getInventory().size(); ++i) {
                if (stack.getItem() != player.getInventory().getStack(i).getItem()) continue;
                InvUtils.drop().slot(i);
            }
        })));
    }

    private int drop(PlayerConsumer consumer) throws CommandSyntaxException {
        if (DropCommand.mc.player.isSpectator()) {
            throw NOT_SPECTATOR.create();
        }
        consumer.accept(DropCommand.mc.player);
        return 1;
    }

    @FunctionalInterface
    private static interface PlayerConsumer {
        public void accept(ClientPlayerEntity var1) throws CommandSyntaxException;
    }
}

