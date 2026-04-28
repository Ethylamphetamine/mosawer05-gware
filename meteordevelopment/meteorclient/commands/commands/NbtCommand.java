/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.serialization.DataResult
 *  net.minecraft.command.CommandSource
 *  net.minecraft.command.EntityDataObject
 *  net.minecraft.command.argument.NbtPathArgumentType$NbtPath
 *  net.minecraft.command.argument.RegistryKeyArgumentType
 *  net.minecraft.component.Component
 *  net.minecraft.component.ComponentChanges
 *  net.minecraft.component.ComponentChanges$Builder
 *  net.minecraft.component.ComponentMap
 *  net.minecraft.component.ComponentMapImpl
 *  net.minecraft.component.ComponentType
 *  net.minecraft.entity.Entity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtHelper
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket
 *  net.minecraft.registry.Registries
 *  net.minecraft.registry.Registry
 *  net.minecraft.registry.RegistryKey
 *  net.minecraft.registry.RegistryKeys
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.HoverEvent
 *  net.minecraft.text.HoverEvent$Action
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ComponentMapArgumentType;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.component.Component;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class NbtCommand
extends Command {
    private static final DynamicCommandExceptionType MALFORMED_ITEM_EXCEPTION = new DynamicCommandExceptionType(error -> Text.stringifiedTranslatable((String)"arguments.item.malformed", (Object[])new Object[]{error}));
    private final Text copyButton = Text.literal((String)"NBT").setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE).withClickEvent((ClickEvent)new MeteorClickEvent(ClickEvent.Action.RUN_COMMAND, this.toString("copy"))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (Object)Text.literal((String)"Copy the NBT data to your clipboard."))));

    public NbtCommand() {
        super("nbt", "Modifies NBT data for an item, example: .nbt add {display:{Name:'{\"text\":\"$cRed Name\"}'}}", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(NbtCommand.literal("add").then(NbtCommand.argument("component", ComponentMapArgumentType.componentMap(REGISTRY_ACCESS)).executes(ctx -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getMainHandStack();
            if (this.validBasic(stack)) {
                ComponentMap itemComponents = stack.getComponents();
                ComponentMap newComponents = ComponentMapArgumentType.getComponentMap(ctx, "component");
                ComponentMap testComponents = ComponentMap.of((ComponentMap)itemComponents, (ComponentMap)newComponents);
                DataResult dataResult = ItemStack.validateComponents((ComponentMap)testComponents);
                dataResult.getOrThrow(arg_0 -> ((DynamicCommandExceptionType)MALFORMED_ITEM_EXCEPTION).create(arg_0));
                stack.applyComponentsFrom(testComponents);
                this.setStack(stack);
            }
            return 1;
        })));
        builder.then(NbtCommand.literal("set").then(NbtCommand.argument("component", ComponentMapArgumentType.componentMap(REGISTRY_ACCESS)).executes(ctx -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getMainHandStack();
            if (this.validBasic(stack)) {
                ComponentMap components = ComponentMapArgumentType.getComponentMap(ctx, "component");
                ComponentMapImpl stackComponents = (ComponentMapImpl)stack.getComponents();
                DataResult dataResult = ItemStack.validateComponents((ComponentMap)components);
                dataResult.getOrThrow(arg_0 -> ((DynamicCommandExceptionType)MALFORMED_ITEM_EXCEPTION).create(arg_0));
                ComponentChanges.Builder changesBuilder = ComponentChanges.builder();
                Set types = stackComponents.getTypes();
                for (Component entry : components) {
                    changesBuilder.add(entry);
                    types.remove(entry.comp_2443());
                }
                for (ComponentType type : types) {
                    changesBuilder.remove(type);
                }
                stackComponents.applyChanges(changesBuilder.build());
                this.setStack(stack);
            }
            return 1;
        })));
        builder.then(NbtCommand.literal("remove").then((ArgumentBuilder)((RequiredArgumentBuilder)NbtCommand.argument("component", RegistryKeyArgumentType.registryKey((RegistryKey)RegistryKeys.DATA_COMPONENT_TYPE)).executes(ctx -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getMainHandStack();
            if (this.validBasic(stack)) {
                RegistryKey componentTypeKey = (RegistryKey)ctx.getArgument("component", RegistryKey.class);
                ComponentType componentType = (ComponentType)Registries.DATA_COMPONENT_TYPE.get(componentTypeKey);
                ComponentMapImpl components = (ComponentMapImpl)stack.getComponents();
                components.applyChanges(ComponentChanges.builder().remove(componentType).build());
                this.setStack(stack);
            }
            return 1;
        })).suggests((ctx, suggestionsBuilder) -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getMainHandStack();
            if (stack != ItemStack.EMPTY) {
                ComponentMap components = stack.getComponents();
                String remaining = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
                CommandSource.forEachMatching(components.getTypes().stream().map(arg_0 -> ((Registry)Registries.DATA_COMPONENT_TYPE).getEntry(arg_0)).toList(), (String)remaining, entry -> {
                    if (entry.getKey().isPresent()) {
                        return ((RegistryKey)entry.getKey().get()).getValue();
                    }
                    return null;
                }, entry -> {
                    ComponentType dataComponentType = (ComponentType)entry.comp_349();
                    if (dataComponentType.getCodec() != null && entry.getKey().isPresent()) {
                        suggestionsBuilder.suggest(((RegistryKey)entry.getKey().get()).getValue().toString());
                    }
                });
            }
            return suggestionsBuilder.buildFuture();
        })));
        builder.then(NbtCommand.literal("get").executes(context -> {
            EntityDataObject dataCommandObject = new EntityDataObject((Entity)NbtCommand.mc.player);
            NbtPathArgumentType.NbtPath handPath = NbtPathArgumentType.NbtPath.parse((String)"SelectedItem");
            MutableText text = Text.empty().append(this.copyButton);
            try {
                List nbtElement = handPath.get((NbtElement)dataCommandObject.getNbt());
                if (!nbtElement.isEmpty()) {
                    text.append(" ").append(NbtHelper.toPrettyPrintedText((NbtElement)((NbtElement)nbtElement.getFirst())));
                }
            }
            catch (CommandSyntaxException e) {
                text.append("{}");
            }
            this.info((Text)text);
            return 1;
        }));
        builder.then(NbtCommand.literal("copy").executes(context -> {
            EntityDataObject dataCommandObject = new EntityDataObject((Entity)NbtCommand.mc.player);
            NbtPathArgumentType.NbtPath handPath = NbtPathArgumentType.NbtPath.parse((String)"SelectedItem");
            MutableText text = Text.empty().append(this.copyButton);
            String nbt = "{}";
            try {
                List nbtElement = handPath.get((NbtElement)dataCommandObject.getNbt());
                if (!nbtElement.isEmpty()) {
                    text.append(" ").append(NbtHelper.toPrettyPrintedText((NbtElement)((NbtElement)nbtElement.getFirst())));
                    nbt = ((NbtElement)nbtElement.getFirst()).toString();
                }
            }
            catch (CommandSyntaxException e) {
                text.append("{}");
            }
            NbtCommand.mc.keyboard.setClipboard(nbt);
            text.append(" data copied!");
            this.info((Text)text);
            return 1;
        }));
        builder.then(NbtCommand.literal("count").then(NbtCommand.argument("count", IntegerArgumentType.integer((int)-127, (int)127)).executes(context -> {
            ItemStack stack = NbtCommand.mc.player.getInventory().getMainHandStack();
            if (this.validBasic(stack)) {
                int count = IntegerArgumentType.getInteger((CommandContext)context, (String)"count");
                stack.setCount(count);
                this.setStack(stack);
                this.info("Set mainhand stack count to %s.", count);
            }
            return 1;
        })));
    }

    private void setStack(ItemStack stack) {
        NbtCommand.mc.player.networkHandler.sendPacket((Packet)new CreativeInventoryActionC2SPacket(36 + NbtCommand.mc.player.getInventory().selectedSlot, stack));
    }

    private boolean validBasic(ItemStack stack) {
        if (!NbtCommand.mc.player.getAbilities().creativeMode) {
            this.error("Creative mode only.", new Object[0]);
            return false;
        }
        if (stack == ItemStack.EMPTY) {
            this.error("You must hold an item in your main hand.", new Object[0]);
            return false;
        }
        return true;
    }
}

