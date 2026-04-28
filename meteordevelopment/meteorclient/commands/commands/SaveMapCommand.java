/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  net.minecraft.client.render.MapRenderer
 *  net.minecraft.client.render.MapRenderer$MapTexture
 *  net.minecraft.command.CommandSource
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.MapIdComponent
 *  net.minecraft.item.FilledMapItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.map.MapState
 *  net.minecraft.text.Text
 *  net.minecraft.world.World
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 */
package meteordevelopment.meteorclient.commands.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.mixin.MapRendererAccessor;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class SaveMapCommand
extends Command {
    private static final SimpleCommandExceptionType MAP_NOT_FOUND = new SimpleCommandExceptionType((Message)Text.literal((String)"You must be holding a filled map."));
    private static final SimpleCommandExceptionType OOPS = new SimpleCommandExceptionType((Message)Text.literal((String)"Something went wrong."));
    private final PointerBuffer filters = BufferUtils.createPointerBuffer((int)1);

    public SaveMapCommand() {
        super("save-map", "Saves a map to an image.", "sm");
        ByteBuffer pngFilter = MemoryUtil.memASCII((CharSequence)"*.png");
        this.filters.put(pngFilter);
        this.filters.rewind();
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        ((LiteralArgumentBuilder)builder.executes(context -> {
            this.saveMap(128);
            return 1;
        })).then(SaveMapCommand.argument("scale", IntegerArgumentType.integer((int)1)).executes(context -> {
            this.saveMap(IntegerArgumentType.getInteger((CommandContext)context, (String)"scale"));
            return 1;
        }));
    }

    private void saveMap(int scale) throws CommandSyntaxException {
        ItemStack map = this.getMap();
        MapState state = this.getMapState();
        if (map == null || state == null) {
            throw MAP_NOT_FOUND.create();
        }
        File path = this.getPath();
        if (path == null) {
            throw OOPS.create();
        }
        MapRenderer mapRenderer = SaveMapCommand.mc.gameRenderer.getMapRenderer();
        MapRenderer.MapTexture texture = ((MapRendererAccessor)mapRenderer).invokeGetMapTexture((MapIdComponent)map.get(DataComponentTypes.MAP_ID), state);
        if (texture.texture.getImage() == null) {
            throw OOPS.create();
        }
        try {
            if (scale == 128) {
                texture.texture.getImage().writeTo(path);
            } else {
                int[] data = texture.texture.getImage().copyPixelsRgba();
                BufferedImage image = new BufferedImage(128, 128, 2);
                image.setRGB(0, 0, image.getWidth(), image.getHeight(), data, 0, 128);
                BufferedImage scaledImage = new BufferedImage(scale, scale, 2);
                scaledImage.createGraphics().drawImage(image, 0, 0, scale, scale, null);
                ImageIO.write((RenderedImage)scaledImage, "png", path);
            }
        }
        catch (IOException e) {
            this.error("Error writing map texture", new Object[0]);
            MeteorClient.LOG.error(e.toString());
        }
    }

    @Nullable
    private MapState getMapState() {
        ItemStack map = this.getMap();
        if (map == null) {
            return null;
        }
        return FilledMapItem.getMapState((MapIdComponent)((MapIdComponent)map.get(DataComponentTypes.MAP_ID)), (World)SaveMapCommand.mc.world);
    }

    @Nullable
    private File getPath() {
        Object path = TinyFileDialogs.tinyfd_saveFileDialog((CharSequence)"Save image", null, (PointerBuffer)this.filters, null);
        if (path == null) {
            return null;
        }
        if (!((String)path).endsWith(".png")) {
            path = (String)path + ".png";
        }
        return new File((String)path);
    }

    @Nullable
    private ItemStack getMap() {
        ItemStack itemStack = SaveMapCommand.mc.player.getMainHandStack();
        if (itemStack.getItem() == Items.FILLED_MAP) {
            return itemStack;
        }
        itemStack = SaveMapCommand.mc.player.getOffHandStack();
        if (itemStack.getItem() == Items.FILLED_MAP) {
            return itemStack;
        }
        return null;
    }
}

