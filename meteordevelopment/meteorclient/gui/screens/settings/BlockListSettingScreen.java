/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.registry.Registries
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.screens.settings.RegistryListSettingScreen;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.mixin.IdentifierAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockListSettingScreen
extends RegistryListSettingScreen<Block> {
    private static final Identifier ID = Identifier.of((String)"minecraft", (String)"");

    public BlockListSettingScreen(GuiTheme theme, Setting<List<Block>> setting) {
        super(theme, "Select Blocks", setting, (Collection)setting.get(), Registries.BLOCK);
    }

    @Override
    protected boolean includeValue(Block value) {
        Predicate<Block> filter = ((BlockListSetting)this.setting).filter;
        if (filter == null) {
            return value != Blocks.AIR;
        }
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(Block value) {
        return this.theme.itemWithLabel(value.asItem().getDefaultStack(), this.getValueName(value));
    }

    @Override
    protected String getValueName(Block value) {
        return Names.get(value);
    }

    @Override
    protected boolean skipValue(Block value) {
        return Registries.BLOCK.getId((Object)value).getPath().endsWith("_wall_banner");
    }

    @Override
    protected Block getAdditionalValue(Block value) {
        String path = Registries.BLOCK.getId((Object)value).getPath();
        if (!path.endsWith("_banner")) {
            return null;
        }
        ((IdentifierAccessor)ID).setPath(path.substring(0, path.length() - 6) + "wall_banner");
        return (Block)Registries.BLOCK.get(ID);
    }
}

