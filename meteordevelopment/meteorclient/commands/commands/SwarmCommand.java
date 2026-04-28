/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair
 *  it.unimi.dsi.fastutil.objects.ObjectIntPair
 *  net.minecraft.command.CommandRegistryAccess
 *  net.minecraft.command.CommandSource
 *  net.minecraft.command.argument.BlockStateArgument
 *  net.minecraft.command.argument.BlockStateArgumentType
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.text.ClickEvent
 *  net.minecraft.text.ClickEvent$Action
 *  net.minecraft.text.Style
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.math.BlockPos
 *  org.jetbrains.annotations.Nullable
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import java.util.List;
import java.util.Random;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.commands.arguments.PlayerArgumentType;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.Swarm;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmWorker;
import meteordevelopment.meteorclient.systems.modules.world.InfinityMiner;
import meteordevelopment.meteorclient.utils.misc.text.MeteorClickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class SwarmCommand
extends Command {
    private static final SimpleCommandExceptionType SWARM_NOT_ACTIVE = new SimpleCommandExceptionType((Message)Text.literal((String)"The swarm module must be active to use this command."));
    @Nullable
    private ObjectIntPair<String> pendingConnection;

    public SwarmCommand() {
        super("swarm", "Sends commands to connected swarm workers.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(SwarmCommand.literal("disconnect").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (!swarm.isActive()) {
                throw SWARM_NOT_ACTIVE.create();
            }
            swarm.close();
            return 1;
        }));
        builder.then(((LiteralArgumentBuilder)SwarmCommand.literal("join").then(SwarmCommand.argument("ip", StringArgumentType.string()).then(SwarmCommand.argument("port", IntegerArgumentType.integer((int)0, (int)65535)).executes(context -> {
            String ip = StringArgumentType.getString((CommandContext)context, (String)"ip");
            int port = IntegerArgumentType.getInteger((CommandContext)context, (String)"port");
            this.pendingConnection = new ObjectIntImmutablePair((Object)ip, port);
            this.info("Are you sure you want to connect to '%s:%s'?", ip, port);
            this.info((Text)Text.literal((String)"Click here to confirm").setStyle(Style.EMPTY.withFormatting(new Formatting[]{Formatting.UNDERLINE, Formatting.GREEN}).withClickEvent((ClickEvent)new MeteorClickEvent(ClickEvent.Action.RUN_COMMAND, ".swarm join confirm"))));
            return 1;
        })))).then(SwarmCommand.literal("confirm").executes(ctx -> {
            if (this.pendingConnection == null) {
                this.error("No pending swarm connections.", new Object[0]);
                return 1;
            }
            Swarm swarm = Modules.get().get(Swarm.class);
            if (!swarm.isActive()) {
                swarm.toggle();
            }
            swarm.close();
            swarm.mode.set(Swarm.Mode.Worker);
            swarm.worker = new SwarmWorker((String)this.pendingConnection.left(), this.pendingConnection.rightInt());
            this.pendingConnection = null;
            try {
                this.info("Connected to (highlight)%s.", swarm.worker.getConnection());
            }
            catch (NullPointerException e) {
                this.error("Error connecting to swarm host.", new Object[0]);
                swarm.close();
                swarm.toggle();
            }
            return 1;
        })));
        builder.then(SwarmCommand.literal("connections").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    if (swarm.host.getConnectionCount() > 0) {
                        ChatUtils.info("--- Swarm Connections (highlight)(%s/%s)(default) ---", swarm.host.getConnectionCount(), swarm.host.getConnections().length);
                        for (int i = 0; i < swarm.host.getConnections().length; ++i) {
                            SwarmConnection connection = swarm.host.getConnections()[i];
                            if (connection == null) continue;
                            ChatUtils.info("(highlight)Worker %s(default): %s.", i, connection.getConnection());
                        }
                    } else {
                        this.warning("No active connections", new Object[0]);
                    }
                } else if (swarm.isWorker()) {
                    this.info("Connected to (highlight)%s", swarm.worker.getConnection());
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        }));
        builder.then(((LiteralArgumentBuilder)SwarmCommand.literal("follow").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput() + " " + SwarmCommand.mc.player.getName().getString());
                } else if (swarm.isWorker()) {
                    this.error("The follow host command must be used by the host.", new Object[0]);
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })).then(SwarmCommand.argument("player", PlayerArgumentType.create()).executes(context -> {
            PlayerEntity playerEntity = PlayerArgumentType.get(context);
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker() && playerEntity != null) {
                    PathManagers.get().follow(entity -> entity.getName().getString().equalsIgnoreCase(playerEntity.getName().getString()));
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })));
        builder.then(SwarmCommand.literal("goto").then(SwarmCommand.argument("x", IntegerArgumentType.integer()).then(SwarmCommand.argument("z", IntegerArgumentType.integer()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    int x = IntegerArgumentType.getInteger((CommandContext)context, (String)"x");
                    int z = IntegerArgumentType.getInteger((CommandContext)context, (String)"z");
                    PathManagers.get().moveTo(new BlockPos(x, 0, z), true);
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)SwarmCommand.literal("infinity-miner").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    this.runInfinityMiner();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })).then(((RequiredArgumentBuilder)SwarmCommand.argument("target", BlockStateArgumentType.blockState((CommandRegistryAccess)REGISTRY_ACCESS)).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    Modules.get().get(InfinityMiner.class).targetBlocks.set(List.of(((BlockStateArgument)context.getArgument("target", BlockStateArgument.class)).getBlockState().getBlock()));
                    this.runInfinityMiner();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })).then(SwarmCommand.argument("repair", BlockStateArgumentType.blockState((CommandRegistryAccess)REGISTRY_ACCESS)).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    Modules.get().get(InfinityMiner.class).targetBlocks.set(List.of(((BlockStateArgument)context.getArgument("target", BlockStateArgument.class)).getBlockState().getBlock()));
                    Modules.get().get(InfinityMiner.class).repairBlocks.set(List.of(((BlockStateArgument)context.getArgument("repair", BlockStateArgument.class)).getBlockState().getBlock()));
                    this.runInfinityMiner();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })))).then(SwarmCommand.literal("logout").then(SwarmCommand.argument("logout", BoolArgumentType.bool()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    Modules.get().get(InfinityMiner.class).logOut.set(BoolArgumentType.getBool((CommandContext)context, (String)"logout"));
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })))).then(SwarmCommand.literal("walkhome").then(SwarmCommand.argument("walkhome", BoolArgumentType.bool()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    Modules.get().get(InfinityMiner.class).walkHome.set(BoolArgumentType.getBool((CommandContext)context, (String)"walkhome"));
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        }))));
        builder.then(SwarmCommand.literal("mine").then(SwarmCommand.argument("block", BlockStateArgumentType.blockState((CommandRegistryAccess)REGISTRY_ACCESS)).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    swarm.worker.target = ((BlockStateArgument)context.getArgument("block", BlockStateArgument.class)).getBlockState().getBlock();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })));
        builder.then(SwarmCommand.literal("toggle").then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)SwarmCommand.argument("module", ModuleArgumentType.create()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    Module module = ModuleArgumentType.get(context);
                    module.toggle();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })).then(SwarmCommand.literal("on").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                Module m;
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker() && !(m = ModuleArgumentType.get(context)).isActive()) {
                    m.toggle();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        }))).then(SwarmCommand.literal("off").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                Module m;
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker() && (m = ModuleArgumentType.get(context)).isActive()) {
                    m.toggle();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        }))));
        builder.then(((LiteralArgumentBuilder)SwarmCommand.literal("scatter").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    this.scatter(100);
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })).then(SwarmCommand.argument("radius", IntegerArgumentType.integer()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    this.scatter(IntegerArgumentType.getInteger((CommandContext)context, (String)"radius"));
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })));
        builder.then(SwarmCommand.literal("stop").executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    PathManagers.get().stop();
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        }));
        builder.then(SwarmCommand.literal("exec").then(SwarmCommand.argument("command", StringArgumentType.greedyString()).executes(context -> {
            Swarm swarm = Modules.get().get(Swarm.class);
            if (swarm.isActive()) {
                if (swarm.isHost()) {
                    swarm.host.sendMessage(context.getInput());
                } else if (swarm.isWorker()) {
                    ChatUtils.sendPlayerMsg(StringArgumentType.getString((CommandContext)context, (String)"command"));
                }
            } else {
                throw SWARM_NOT_ACTIVE.create();
            }
            return 1;
        })));
    }

    private void runInfinityMiner() {
        InfinityMiner infinityMiner = Modules.get().get(InfinityMiner.class);
        if (infinityMiner.isActive()) {
            infinityMiner.toggle();
        }
        if (!infinityMiner.isActive()) {
            infinityMiner.toggle();
        }
    }

    private void scatter(int radius) {
        Random random = new Random();
        double a = random.nextDouble() * 2.0 * Math.PI;
        double r = (double)radius * Math.sqrt(random.nextDouble());
        double x = SwarmCommand.mc.player.getX() + r * Math.cos(a);
        double z = SwarmCommand.mc.player.getZ() + r * Math.sin(a);
        PathManagers.get().stop();
        PathManagers.get().moveTo(new BlockPos((int)x, 0, (int)z), true);
    }
}

