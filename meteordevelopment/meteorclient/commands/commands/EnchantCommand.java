/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.gui.screen.ingame.InventoryScreen
 *  net.minecraft.command.CommandSource
 *  net.minecraft.enchantment.Enchantment
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.registry.entry.RegistryEntry
 *  net.minecraft.registry.entry.RegistryEntry$Reference
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.ToIntFunction;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.RegistryEntryReferenceArgumentType;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class EnchantCommand
extends Command {
    private static final SimpleCommandExceptionType NOT_IN_CREATIVE = new SimpleCommandExceptionType((Message)Text.literal((String)"You must be in creative mode to use this."));
    private static final SimpleCommandExceptionType NOT_HOLDING_ITEM = new SimpleCommandExceptionType((Message)Text.literal((String)"You need to hold some item to enchant."));

    public EnchantCommand() {
        super("enchant", "Enchants the item in your hand. REQUIRES Creative mode.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(EnchantCommand.literal("one").then(((RequiredArgumentBuilder)EnchantCommand.argument("enchantment", RegistryEntryReferenceArgumentType.enchantment()).then(EnchantCommand.literal("level").then(EnchantCommand.argument("level", IntegerArgumentType.integer()).executes(context -> {
            this.one((CommandContext<CommandSource>)context, enchantment -> (Integer)context.getArgument("level", Integer.class));
            return 1;
        })))).then(EnchantCommand.literal("max").executes(context -> {
            this.one((CommandContext<CommandSource>)context, Enchantment::getMaxLevel);
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)EnchantCommand.literal("all_possible").then(EnchantCommand.literal("level").then(EnchantCommand.argument("level", IntegerArgumentType.integer()).executes(context -> {
            this.all(true, enchantment -> (Integer)context.getArgument("level", Integer.class));
            return 1;
        })))).then(EnchantCommand.literal("max").executes(context -> {
            this.all(true, Enchantment::getMaxLevel);
            return 1;
        })));
        builder.then(((LiteralArgumentBuilder)EnchantCommand.literal("all").then(EnchantCommand.literal("level").then(EnchantCommand.argument("level", IntegerArgumentType.integer()).executes(context -> {
            this.all(false, enchantment -> (Integer)context.getArgument("level", Integer.class));
            return 1;
        })))).then(EnchantCommand.literal("max").executes(context -> {
            this.all(false, Enchantment::getMaxLevel);
            return 1;
        })));
        builder.then(EnchantCommand.literal("clear").executes(context -> {
            ItemStack itemStack = this.tryGetItemStack();
            Utils.clearEnchantments(itemStack);
            this.syncItem();
            return 1;
        }));
        builder.then(EnchantCommand.literal("remove").then(EnchantCommand.argument("enchantment", RegistryEntryReferenceArgumentType.enchantment()).executes(context -> {
            ItemStack itemStack = this.tryGetItemStack();
            RegistryEntry.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
            Utils.removeEnchantment(itemStack, (Enchantment)enchantment.comp_349());
            this.syncItem();
            return 1;
        })));
    }

    private void one(CommandContext<CommandSource> context, ToIntFunction<Enchantment> level) throws CommandSyntaxException {
        ItemStack itemStack = this.tryGetItemStack();
        RegistryEntry.Reference<Enchantment> enchantment = RegistryEntryReferenceArgumentType.getEnchantment(context, "enchantment");
        Utils.addEnchantment(itemStack, enchantment, level.applyAsInt((Enchantment)enchantment.comp_349()));
        this.syncItem();
    }

    private void all(boolean onlyPossible, ToIntFunction<Enchantment> level) throws CommandSyntaxException {
        ItemStack itemStack = this.tryGetItemStack();
        mc.getNetworkHandler().getRegistryManager().getOptionalWrapper(RegistryKeys.ENCHANTMENT).ifPresent(registry -> registry.streamEntries().forEach(enchantment -> {
            if (!onlyPossible || ((Enchantment)enchantment.comp_349()).isAcceptableItem(itemStack)) {
                Utils.addEnchantment(itemStack, (RegistryEntry<Enchantment>)enchantment, level.applyAsInt((Enchantment)enchantment.comp_349()));
            }
        }));
        this.syncItem();
    }

    private void syncItem() {
        mc.setScreen((Screen)new InventoryScreen((PlayerEntity)EnchantCommand.mc.player));
        mc.setScreen(null);
    }

    private ItemStack tryGetItemStack() throws CommandSyntaxException {
        if (!EnchantCommand.mc.player.isCreative()) {
            throw NOT_IN_CREATIVE.create();
        }
        ItemStack itemStack = this.getItemStack();
        if (itemStack == null) {
            throw NOT_HOLDING_ITEM.create();
        }
        return itemStack;
    }

    private ItemStack getItemStack() {
        ItemStack itemStack = EnchantCommand.mc.player.getMainHandStack();
        if (itemStack == null) {
            itemStack = EnchantCommand.mc.player.getOffHandStack();
        }
        return itemStack.isEmpty() ? null : itemStack;
    }
}

