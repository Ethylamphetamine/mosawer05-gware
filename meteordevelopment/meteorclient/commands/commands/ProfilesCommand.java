/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.minecraft.command.CommandSource
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ProfileArgumentType;
import meteordevelopment.meteorclient.systems.profiles.Profile;
import meteordevelopment.meteorclient.systems.profiles.Profiles;
import net.minecraft.command.CommandSource;

public class ProfilesCommand
extends Command {
    public ProfilesCommand() {
        super("profiles", "Loads and saves profiles.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(ProfilesCommand.literal("load").then(ProfilesCommand.argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);
            if (profile != null) {
                profile.load();
                this.info("Loaded profile (highlight)%s(default).", profile.name.get());
            }
            return 1;
        })));
        builder.then(ProfilesCommand.literal("save").then(ProfilesCommand.argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);
            if (profile != null) {
                profile.save();
                this.info("Saved profile (highlight)%s(default).", profile.name.get());
            }
            return 1;
        })));
        builder.then(ProfilesCommand.literal("delete").then(ProfilesCommand.argument("profile", ProfileArgumentType.create()).executes(context -> {
            Profile profile = ProfileArgumentType.get(context);
            if (profile != null) {
                Profiles.get().remove(profile);
                this.info("Deleted profile (highlight)%s(default).", profile.name.get());
            }
            return 1;
        })));
    }
}

