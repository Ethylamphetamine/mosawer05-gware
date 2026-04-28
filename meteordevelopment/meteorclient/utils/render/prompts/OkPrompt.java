/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package meteordevelopment.meteorclient.utils.render.prompts;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.render.prompts.Prompt;
import net.minecraft.client.gui.screen.Screen;

public class OkPrompt
extends Prompt<OkPrompt> {
    private Runnable onOk = () -> {};

    private OkPrompt(GuiTheme theme, Screen parent) {
        super(theme, parent);
    }

    public static OkPrompt create() {
        return new OkPrompt(GuiThemes.get(), MeteorClient.mc.currentScreen);
    }

    public static OkPrompt create(GuiTheme theme, Screen parent) {
        return new OkPrompt(theme, parent);
    }

    public OkPrompt onOk(Runnable action) {
        this.onOk = action;
        return this;
    }

    @Override
    protected void initialiseWidgets(Prompt.PromptScreen screen) {
        WButton okButton = screen.list.add(this.theme.button("Ok")).expandX().widget();
        okButton.action = () -> {
            if (screen.dontShowAgainCheckbox != null && screen.dontShowAgainCheckbox.checked) {
                Config.get().dontShowAgainPrompts.add(this.id);
            }
            this.onOk.run();
            screen.close();
        };
    }
}

