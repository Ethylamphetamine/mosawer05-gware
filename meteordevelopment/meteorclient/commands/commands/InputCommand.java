/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.resource.language.I18n
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandSource;

public class InputCommand
extends Command {
    private static final List<KeypressHandler> activeHandlers = new ArrayList<KeypressHandler>();
    private static final List<Pair<KeyBinding, String>> holdKeys = List.of(new Pair((Object)InputCommand.mc.options.forwardKey, (Object)"forwards"), new Pair((Object)InputCommand.mc.options.backKey, (Object)"backwards"), new Pair((Object)InputCommand.mc.options.leftKey, (Object)"left"), new Pair((Object)InputCommand.mc.options.rightKey, (Object)"right"), new Pair((Object)InputCommand.mc.options.jumpKey, (Object)"jump"), new Pair((Object)InputCommand.mc.options.sneakKey, (Object)"sneak"), new Pair((Object)InputCommand.mc.options.sprintKey, (Object)"sprint"), new Pair((Object)InputCommand.mc.options.useKey, (Object)"use"), new Pair((Object)InputCommand.mc.options.attackKey, (Object)"attack"));
    private static final List<Pair<KeyBinding, String>> pressKeys = List.of(new Pair((Object)InputCommand.mc.options.swapHandsKey, (Object)"swap"), new Pair((Object)InputCommand.mc.options.dropKey, (Object)"drop"));

    public InputCommand() {
        super("input", "Keyboard input simulation.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        for (Pair<KeyBinding, String> keyBinding : holdKeys) {
            builder.then(InputCommand.literal((String)keyBinding.getSecond()).then(InputCommand.argument("ticks", IntegerArgumentType.integer((int)1)).executes(context -> {
                activeHandlers.add(new KeypressHandler((KeyBinding)keyBinding.getFirst(), (Integer)context.getArgument("ticks", Integer.class)));
                return 1;
            })));
        }
        for (Pair<KeyBinding, String> keyBinding : pressKeys) {
            builder.then(InputCommand.literal((String)keyBinding.getSecond()).executes(context -> {
                InputCommand.press((KeyBinding)keyBinding.getFirst());
                return 1;
            }));
        }
        for (KeyBinding keyBinding : InputCommand.mc.options.hotbarKeys) {
            builder.then(InputCommand.literal(keyBinding.getTranslationKey().substring(4)).executes(context -> {
                InputCommand.press(keyBinding);
                return 1;
            }));
        }
        builder.then(InputCommand.literal("clear").executes(ctx -> {
            if (activeHandlers.isEmpty()) {
                this.warning("No active keypress handlers.", new Object[0]);
            } else {
                this.info("Cleared all keypress handlers.", new Object[0]);
                activeHandlers.forEach(MeteorClient.EVENT_BUS::unsubscribe);
                activeHandlers.clear();
            }
            return 1;
        }));
        builder.then(InputCommand.literal("list").executes(ctx -> {
            if (activeHandlers.isEmpty()) {
                this.warning("No active keypress handlers.", new Object[0]);
            } else {
                this.info("Active keypress handlers: ", new Object[0]);
                for (int i = 0; i < activeHandlers.size(); ++i) {
                    KeypressHandler handler = activeHandlers.get(i);
                    this.info("(highlight)%d(default) - (highlight)%s %d(default) ticks left out of (highlight)%d(default).", i, I18n.translate((String)handler.key.getTranslationKey(), (Object[])new Object[0]), handler.ticks, handler.totalTicks);
                }
            }
            return 1;
        }));
        builder.then(InputCommand.literal("remove").then(InputCommand.argument("index", IntegerArgumentType.integer((int)0)).executes(ctx -> {
            int index = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"index");
            if (index >= activeHandlers.size()) {
                this.warning("Index out of range.", new Object[0]);
            } else {
                this.info("Removed keypress handler.", new Object[0]);
                MeteorClient.EVENT_BUS.unsubscribe(activeHandlers.get(index));
                activeHandlers.remove(index);
            }
            return 1;
        })));
    }

    private static void press(KeyBinding keyBinding) {
        KeyBindingAccessor accessor = (KeyBindingAccessor)keyBinding;
        accessor.meteor$setTimesPressed(accessor.meteor$getTimesPressed() + 1);
    }

    private static class KeypressHandler {
        private final KeyBinding key;
        private final int totalTicks;
        private int ticks;

        public KeypressHandler(KeyBinding key, int ticks) {
            this.key = key;
            this.totalTicks = ticks;
            this.ticks = ticks;
            MeteorClient.EVENT_BUS.subscribe(this);
        }

        @EventHandler
        private void onTick(TickEvent.Post event) {
            if (this.ticks-- > 0) {
                this.key.setPressed(true);
            } else {
                this.key.setPressed(false);
                MeteorClient.EVENT_BUS.unsubscribe(this);
                activeHandlers.remove(this);
            }
        }
    }
}

