/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.ArrayList;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

public class ToggleCommand
extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module.", "t");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(((LiteralArgumentBuilder)ToggleCommand.literal("all").then(ToggleCommand.literal("on").executes(context -> {
            new ArrayList<Module>(Modules.get().getAll()).forEach(module -> {
                if (!module.isActive()) {
                    module.toggle();
                }
            });
            Hud.get().active = true;
            return 1;
        }))).then(ToggleCommand.literal("off").executes(context -> {
            new ArrayList<Module>(Modules.get().getActive()).forEach(Module::toggle);
            Hud.get().active = false;
            return 1;
        })))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)ToggleCommand.argument("module", ModuleArgumentType.create()).executes(context -> {
            Module m = ModuleArgumentType.get(context);
            m.toggle();
            m.sendToggledMsg();
            return 1;
        })).then(ToggleCommand.literal("on").executes(context -> {
            Module m = ModuleArgumentType.get(context);
            if (!m.isActive()) {
                m.toggle();
            }
            return 1;
        }))).then(ToggleCommand.literal("off").executes(context -> {
            Module m = ModuleArgumentType.get(context);
            if (m.isActive()) {
                m.toggle();
            }
            return 1;
        })))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)ToggleCommand.literal("hud").executes(context -> {
            Hud.get().active = !Hud.get().active;
            return 1;
        })).then(ToggleCommand.literal("on").executes(context -> {
            Hud.get().active = true;
            return 1;
        }))).then(ToggleCommand.literal("off").executes(context -> {
            Hud.get().active = false;
            return 1;
        })));
    }
}

