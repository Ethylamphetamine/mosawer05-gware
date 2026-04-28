/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  net.minecraft.command.CommandSource
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.DirectionArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class RotationCommand
extends Command {
    public RotationCommand() {
        super("rotation", "Modifies your rotation.", new String[0]);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        ((LiteralArgumentBuilder)builder.then(((LiteralArgumentBuilder)RotationCommand.literal("set").then(RotationCommand.argument("direction", DirectionArgumentType.create()).executes(context -> {
            RotationCommand.mc.player.setPitch((float)(((Direction)context.getArgument("direction", Direction.class)).getVector().getY() * -90));
            RotationCommand.mc.player.setYaw(((Direction)context.getArgument("direction", Direction.class)).asRotation());
            return 1;
        }))).then(((RequiredArgumentBuilder)RotationCommand.argument("pitch", FloatArgumentType.floatArg((float)-90.0f, (float)90.0f)).executes(context -> {
            RotationCommand.mc.player.setPitch(((Float)context.getArgument("pitch", Float.class)).floatValue());
            return 1;
        })).then(RotationCommand.argument("yaw", FloatArgumentType.floatArg((float)-180.0f, (float)180.0f)).executes(context -> {
            RotationCommand.mc.player.setPitch(((Float)context.getArgument("pitch", Float.class)).floatValue());
            RotationCommand.mc.player.setYaw(((Float)context.getArgument("yaw", Float.class)).floatValue());
            return 1;
        }))))).then(RotationCommand.literal("add").then(((RequiredArgumentBuilder)RotationCommand.argument("pitch", FloatArgumentType.floatArg((float)-90.0f, (float)90.0f)).executes(context -> {
            float pitch = RotationCommand.mc.player.getPitch() + ((Float)context.getArgument("pitch", Float.class)).floatValue();
            RotationCommand.mc.player.setPitch(pitch >= 0.0f ? Math.min(pitch, 90.0f) : Math.max(pitch, -90.0f));
            return 1;
        })).then(RotationCommand.argument("yaw", FloatArgumentType.floatArg((float)-180.0f, (float)180.0f)).executes(context -> {
            float pitch = RotationCommand.mc.player.getPitch() + ((Float)context.getArgument("pitch", Float.class)).floatValue();
            RotationCommand.mc.player.setPitch(pitch >= 0.0f ? Math.min(pitch, 90.0f) : Math.max(pitch, -90.0f));
            float yaw = RotationCommand.mc.player.getYaw() + ((Float)context.getArgument("yaw", Float.class)).floatValue();
            RotationCommand.mc.player.setYaw(MathHelper.wrapDegrees((float)yaw));
            return 1;
        }))));
    }
}

