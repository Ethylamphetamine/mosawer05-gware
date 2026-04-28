/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffectUtil
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.gui.widgets;

import java.util.Iterator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.widgets.WItem;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class WItemWithLabel
extends WHorizontalList {
    private ItemStack itemStack;
    private String name;
    private WItem item;
    private WLabel label;

    public WItemWithLabel(ItemStack itemStack, String name) {
        this.itemStack = itemStack;
        this.name = name;
    }

    @Override
    public void init() {
        this.item = this.add(this.theme.item(this.itemStack)).widget();
        this.label = this.add(this.theme.label(this.name + this.getStringToAppend())).widget();
    }

    private String getStringToAppend() {
        Object str = "";
        if (this.itemStack.getItem() == Items.POTION) {
            Iterator effects = ((PotionContentsComponent)this.itemStack.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS)).getEffects().iterator();
            if (!effects.hasNext()) {
                return str;
            }
            str = (String)str + " ";
            StatusEffectInstance effect = (StatusEffectInstance)effects.next();
            if (effect.getAmplifier() > 0) {
                str = (String)str + "%d ".formatted(effect.getAmplifier() + 1);
            }
            str = (String)str + "(%s)".formatted(StatusEffectUtil.getDurationText((StatusEffectInstance)effect, (float)1.0f, (float)(MeteorClient.mc.world != null ? MeteorClient.mc.world.getTickManager().getTickRate() : 20.0f)).getString());
        }
        return str;
    }

    public void set(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.item.itemStack = itemStack;
        this.name = Names.get(itemStack);
        this.label.set(this.name + this.getStringToAppend());
    }

    public String getLabelText() {
        return this.label == null ? this.name : this.label.get();
    }
}

