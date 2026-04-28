/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.commands.BindCommand;
import meteordevelopment.meteorclient.commands.commands.BindsCommand;
import meteordevelopment.meteorclient.commands.commands.CommandsCommand;
import meteordevelopment.meteorclient.commands.commands.DamageCommand;
import meteordevelopment.meteorclient.commands.commands.DisconnectCommand;
import meteordevelopment.meteorclient.commands.commands.DismountCommand;
import meteordevelopment.meteorclient.commands.commands.DropCommand;
import meteordevelopment.meteorclient.commands.commands.EnchantCommand;
import meteordevelopment.meteorclient.commands.commands.EnderChestCommand;
import meteordevelopment.meteorclient.commands.commands.EnemyCommand;
import meteordevelopment.meteorclient.commands.commands.FakePlayerCommand;
import meteordevelopment.meteorclient.commands.commands.FovCommand;
import meteordevelopment.meteorclient.commands.commands.FriendCommand;
import meteordevelopment.meteorclient.commands.commands.GamemodeCommand;
import meteordevelopment.meteorclient.commands.commands.GiveCommand;
import meteordevelopment.meteorclient.commands.commands.HClipCommand;
import meteordevelopment.meteorclient.commands.commands.InputCommand;
import meteordevelopment.meteorclient.commands.commands.InventoryCommand;
import meteordevelopment.meteorclient.commands.commands.LocateCommand;
import meteordevelopment.meteorclient.commands.commands.MacroCommand;
import meteordevelopment.meteorclient.commands.commands.ModulesCommand;
import meteordevelopment.meteorclient.commands.commands.NameHistoryCommand;
import meteordevelopment.meteorclient.commands.commands.NbtCommand;
import meteordevelopment.meteorclient.commands.commands.NotebotCommand;
import meteordevelopment.meteorclient.commands.commands.PeekCommand;
import meteordevelopment.meteorclient.commands.commands.ProfilesCommand;
import meteordevelopment.meteorclient.commands.commands.ReloadCommand;
import meteordevelopment.meteorclient.commands.commands.ResetCommand;
import meteordevelopment.meteorclient.commands.commands.RotationCommand;
import meteordevelopment.meteorclient.commands.commands.SaveKitCommand;
import meteordevelopment.meteorclient.commands.commands.SaveMapCommand;
import meteordevelopment.meteorclient.commands.commands.SayCommand;
import meteordevelopment.meteorclient.commands.commands.ServerCommand;
import meteordevelopment.meteorclient.commands.commands.SettingCommand;
import meteordevelopment.meteorclient.commands.commands.SpectateCommand;
import meteordevelopment.meteorclient.commands.commands.SwarmCommand;
import meteordevelopment.meteorclient.commands.commands.ToggleCommand;
import meteordevelopment.meteorclient.commands.commands.VClipCommand;
import meteordevelopment.meteorclient.commands.commands.WaspCommand;
import meteordevelopment.meteorclient.commands.commands.WaypointCommand;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.utils.PostInit;
import net.minecraft.command.CommandSource;

public class Commands {
    public static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher();
    public static final List<Command> COMMANDS = new ArrayList<Command>();

    @PostInit(dependencies={PathManagers.class})
    public static void init() {
        Commands.add(new VClipCommand());
        Commands.add(new HClipCommand());
        Commands.add(new DismountCommand());
        Commands.add(new DisconnectCommand());
        Commands.add(new DamageCommand());
        Commands.add(new DropCommand());
        Commands.add(new EnchantCommand());
        Commands.add(new FakePlayerCommand());
        Commands.add(new FriendCommand());
        Commands.add(new EnemyCommand());
        Commands.add(new CommandsCommand());
        Commands.add(new InventoryCommand());
        Commands.add(new NbtCommand());
        Commands.add(new NotebotCommand());
        Commands.add(new PeekCommand());
        Commands.add(new EnderChestCommand());
        Commands.add(new ProfilesCommand());
        Commands.add(new ReloadCommand());
        Commands.add(new ResetCommand());
        Commands.add(new SayCommand());
        Commands.add(new ServerCommand());
        Commands.add(new SwarmCommand());
        Commands.add(new ToggleCommand());
        Commands.add(new SettingCommand());
        Commands.add(new SpectateCommand());
        Commands.add(new GamemodeCommand());
        Commands.add(new SaveMapCommand());
        Commands.add(new MacroCommand());
        Commands.add(new ModulesCommand());
        Commands.add(new BindsCommand());
        Commands.add(new GiveCommand());
        Commands.add(new NameHistoryCommand());
        Commands.add(new BindCommand());
        Commands.add(new FovCommand());
        Commands.add(new RotationCommand());
        Commands.add(new WaypointCommand());
        Commands.add(new InputCommand());
        Commands.add(new WaspCommand());
        Commands.add(new LocateCommand());
        Commands.add(new SaveKitCommand());
        COMMANDS.sort(Comparator.comparing(Command::getName));
    }

    public static void add(Command command) {
        COMMANDS.removeIf(existing -> existing.getName().equals(command.getName()));
        command.registerTo(DISPATCHER);
        COMMANDS.add(command);
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        DISPATCHER.execute(message, (Object)MeteorClient.mc.getNetworkHandler().getCommandSource());
    }

    public static Command get(String name) {
        for (Command command : COMMANDS) {
            if (!command.getName().equals(name)) continue;
            return command;
        }
        return null;
    }
}

