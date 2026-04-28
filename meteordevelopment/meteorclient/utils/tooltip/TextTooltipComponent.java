/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent
 *  net.minecraft.client.gui.tooltip.TooltipComponent
 *  net.minecraft.text.OrderedText
 *  net.minecraft.text.Text
 */
package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class TextTooltipComponent
extends OrderedTextTooltipComponent
implements MeteorTooltipData {
    public TextTooltipComponent(OrderedText text) {
        super(text);
    }

    public TextTooltipComponent(Text text) {
        this(text.asOrderedText());
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }
}

