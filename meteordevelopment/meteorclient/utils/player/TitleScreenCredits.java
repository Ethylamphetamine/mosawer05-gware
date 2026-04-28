/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.toast.Toast
 *  net.minecraft.item.Items
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.StringVisitable
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 */
package meteordevelopment.meteorclient.utils.player;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.AddonManager;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.CommitsScreen;
import meteordevelopment.meteorclient.mixininterface.IText;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.render.MeteorToast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.Toast;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TitleScreenCredits {
    private static final List<Credit> credits = new ArrayList<Credit>();

    private TitleScreenCredits() {
    }

    private static void init() {
        for (MeteorAddon addon : AddonManager.ADDONS) {
            TitleScreenCredits.add(addon);
        }
        credits.sort(Comparator.comparingInt(value -> value.addon == MeteorClient.ADDON ? Integer.MIN_VALUE : -MeteorClient.mc.textRenderer.getWidth((StringVisitable)value.text)));
        MeteorExecutor.execute(() -> {
            block9: for (Credit credit : credits) {
                if (credit.addon.getRepo() == null || credit.addon.getCommit() == null) continue;
                GithubRepo repo = credit.addon.getRepo();
                Http.Request request = Http.get("https://api.github.com/repos/%s/branches/%s".formatted(repo.getOwnerName(), repo.branch()));
                request.exceptionHandler(e -> MeteorClient.LOG.error("Could not fetch repository information for addon '%s'.".formatted(credit.addon.name), (Throwable)e));
                repo.authenticate(request);
                HttpResponse res = request.sendJsonResponse((Type)((Object)Response.class));
                switch (res.statusCode()) {
                    case 401: {
                        String message = "Invalid authentication token for repository '%s'".formatted(repo.getOwnerName());
                        MeteorClient.mc.getToastManager().add((Toast)new MeteorToast(Items.BARRIER, "GitHub: Unauthorized", message));
                        MeteorClient.LOG.warn(message);
                        if (System.getenv("meteor.github.authorization") != null) continue block9;
                        MeteorClient.LOG.info("Consider setting an authorization token with the 'meteor.github.authorization' environment variable.");
                        MeteorClient.LOG.info("See: https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
                        break;
                    }
                    case 403: {
                        MeteorClient.LOG.warn("Could not fetch updates for addon '%s': Rate-limited by GitHub.".formatted(credit.addon.name));
                        break;
                    }
                    case 404: {
                        MeteorClient.LOG.warn("Could not fetch updates for addon '%s': GitHub repository '%s' not found.".formatted(credit.addon.name, repo.getOwnerName()));
                        break;
                    }
                    case 200: {
                        if (credit.addon.getCommit().equals(((Response)res.body()).commit.sha)) break;
                        MutableText mutableText = credit.text;
                        synchronized (mutableText) {
                            credit.text.append((Text)Text.literal((String)"*").formatted(Formatting.RED));
                            ((IText)credit.text).meteor$invalidateCache();
                            break;
                        }
                    }
                }
            }
        });
    }

    private static void add(MeteorAddon addon) {
        Credit credit = new Credit(addon);
        credit.text.append((Text)Text.literal((String)addon.name).styled(style -> style.withColor(addon.color.getPacked())));
        credit.text.append((Text)Text.literal((String)" by ").formatted(Formatting.GRAY));
        for (int i = 0; i < addon.authors.length; ++i) {
            if (i > 0) {
                credit.text.append((Text)Text.literal((String)(i == addon.authors.length - 1 ? " & " : ", ")).formatted(Formatting.GRAY));
            }
            credit.text.append((Text)Text.literal((String)addon.authors[i]).formatted(Formatting.WHITE));
        }
        credits.add(credit);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(DrawContext context) {
        if (credits.isEmpty()) {
            TitleScreenCredits.init();
        }
        int y = 3;
        for (Credit credit : credits) {
            MutableText mutableText = credit.text;
            synchronized (mutableText) {
                int x = MeteorClient.mc.currentScreen.width - 3 - MeteorClient.mc.textRenderer.getWidth((StringVisitable)credit.text);
                context.drawTextWithShadow(MeteorClient.mc.textRenderer, (Text)credit.text, x, y, -1);
            }
            Objects.requireNonNull(MeteorClient.mc.textRenderer);
            y += 9 + 2;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean onClicked(double mouseX, double mouseY) {
        int y = 3;
        for (Credit credit : credits) {
            int width;
            MutableText mutableText = credit.text;
            synchronized (mutableText) {
                width = MeteorClient.mc.textRenderer.getWidth((StringVisitable)credit.text);
            }
            int x = MeteorClient.mc.currentScreen.width - 3 - width;
            if (mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y) {
                Objects.requireNonNull(MeteorClient.mc.textRenderer);
                if (mouseY <= (double)(y + 9 + 2) && credit.addon.getRepo() != null && credit.addon.getCommit() != null) {
                    MeteorClient.mc.setScreen((Screen)new CommitsScreen(GuiThemes.get(), credit.addon));
                    return true;
                }
            }
            Objects.requireNonNull(MeteorClient.mc.textRenderer);
            y += 9 + 2;
        }
        return false;
    }

    private static class Credit {
        public final MeteorAddon addon;
        public final MutableText text = Text.empty();

        public Credit(MeteorAddon addon) {
            this.addon = addon;
        }
    }

    private static class Response {
        public Commit commit;

        private Response() {
        }
    }

    private static class Commit {
        public String sha;

        private Commit() {
        }
    }
}

