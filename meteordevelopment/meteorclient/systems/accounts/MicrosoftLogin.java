/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Util
 *  org.apache.http.NameValuePair
 *  org.apache.http.client.utils.URLEncodedUtils
 */
package meteordevelopment.meteorclient.systems.accounts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class MicrosoftLogin {
    private static final String CLIENT_ID = "4673b348-3efa-4f6a-bbb6-34e141cdc638";
    private static final int PORT = 9675;
    private static HttpServer server;
    private static Consumer<String> callback;

    private MicrosoftLogin() {
    }

    public static void getRefreshToken(Consumer<String> callback) {
        MicrosoftLogin.callback = callback;
        MicrosoftLogin.startServer();
        Util.getOperatingSystem().open("https://login.live.com/oauth20_authorize.srf?client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&response_type=code&redirect_uri=http://127.0.0.1:9675&scope=XboxLive.signin%20offline_access&prompt=select_account");
    }

    public static LoginData login(String refreshToken) {
        AuthTokenResponse res = (AuthTokenResponse)Http.post("https://login.live.com/oauth20_token.srf").bodyForm("client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=http://127.0.0.1:9675").sendJson((Type)((Object)AuthTokenResponse.class));
        if (res == null) {
            return new LoginData();
        }
        String accessToken = res.access_token;
        refreshToken = res.refresh_token;
        XblXstsResponse xblRes = (XblXstsResponse)Http.post("https://user.auth.xboxlive.com/user/authenticate").bodyJson("{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessToken + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}").sendJson((Type)((Object)XblXstsResponse.class));
        if (xblRes == null) {
            return new LoginData();
        }
        XblXstsResponse xstsRes = (XblXstsResponse)Http.post("https://xsts.auth.xboxlive.com/xsts/authorize").bodyJson("{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblRes.Token + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}").sendJson((Type)((Object)XblXstsResponse.class));
        if (xstsRes == null) {
            return new LoginData();
        }
        McResponse mcRes = (McResponse)Http.post("https://api.minecraftservices.com/authentication/login_with_xbox").bodyJson("{\"identityToken\":\"XBL3.0 x=" + xblRes.DisplayClaims.xui[0].uhs + ";" + xstsRes.Token + "\"}").sendJson((Type)((Object)McResponse.class));
        if (mcRes == null) {
            return new LoginData();
        }
        GameOwnershipResponse gameOwnershipRes = (GameOwnershipResponse)Http.get("https://api.minecraftservices.com/entitlements/mcstore").bearer(mcRes.access_token).sendJson((Type)((Object)GameOwnershipResponse.class));
        if (gameOwnershipRes == null || !gameOwnershipRes.hasGameOwnership()) {
            return new LoginData();
        }
        ProfileResponse profileRes = (ProfileResponse)Http.get("https://api.minecraftservices.com/minecraft/profile").bearer(mcRes.access_token).sendJson((Type)((Object)ProfileResponse.class));
        if (profileRes == null) {
            return new LoginData();
        }
        return new LoginData(mcRes.access_token, refreshToken, profileRes.id, profileRes.name);
    }

    private static void startServer() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9675), 0);
            server.createContext("/", new Handler());
            server.setExecutor(MeteorExecutor.executor);
            server.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        if (server == null) {
            return;
        }
        server.stop(0);
        server = null;
        callback = null;
    }

    private static class AuthTokenResponse {
        public String access_token;
        public String refresh_token;

        private AuthTokenResponse() {
        }
    }

    public static class LoginData {
        public String mcToken;
        public String newRefreshToken;
        public String uuid;
        public String username;

        public LoginData() {
        }

        public LoginData(String mcToken, String newRefreshToken, String uuid, String username) {
            this.mcToken = mcToken;
            this.newRefreshToken = newRefreshToken;
            this.uuid = uuid;
            this.username = username;
        }

        public boolean isGood() {
            return this.mcToken != null;
        }
    }

    private static class XblXstsResponse {
        public String Token;
        public DisplayClaims DisplayClaims;

        private XblXstsResponse() {
        }

        private static class DisplayClaims {
            private Claim[] xui;

            private DisplayClaims() {
            }

            private static class Claim {
                private String uhs;

                private Claim() {
                }
            }
        }
    }

    private static class McResponse {
        public String access_token;

        private McResponse() {
        }
    }

    private static class GameOwnershipResponse {
        private Item[] items;

        private GameOwnershipResponse() {
        }

        private boolean hasGameOwnership() {
            boolean hasProduct = false;
            boolean hasGame = false;
            for (Item item : this.items) {
                if (item.name.equals("product_minecraft")) {
                    hasProduct = true;
                    continue;
                }
                if (!item.name.equals("game_minecraft")) continue;
                hasGame = true;
            }
            return hasProduct && hasGame;
        }

        private static class Item {
            private String name;

            private Item() {
            }
        }
    }

    private static class ProfileResponse {
        public String id;
        public String name;

        private ProfileResponse() {
        }
    }

    private static class Handler
    implements HttpHandler {
        private Handler() {
        }

        @Override
        public void handle(HttpExchange req) throws IOException {
            if (req.getRequestMethod().equals("GET")) {
                List query = URLEncodedUtils.parse((URI)req.getRequestURI(), (Charset)StandardCharsets.UTF_8);
                boolean ok = false;
                for (NameValuePair pair : query) {
                    if (!pair.getName().equals("code")) continue;
                    this.handleCode(pair.getValue());
                    ok = true;
                    break;
                }
                if (!ok) {
                    this.writeText(req, "Cannot authenticate.");
                    callback.accept(null);
                } else {
                    this.writeText(req, "You may now close this page.");
                }
            }
            MicrosoftLogin.stopServer();
        }

        private void handleCode(String code) {
            AuthTokenResponse res = (AuthTokenResponse)Http.post("https://login.live.com/oauth20_token.srf").bodyForm("client_id=4673b348-3efa-4f6a-bbb6-34e141cdc638&code=" + code + "&grant_type=authorization_code&redirect_uri=http://127.0.0.1:9675").sendJson((Type)((Object)AuthTokenResponse.class));
            if (res == null) {
                callback.accept(null);
            } else {
                callback.accept(res.refresh_token);
            }
        }

        private void writeText(HttpExchange req, String text) throws IOException {
            OutputStream out = req.getResponseBody();
            req.sendResponseHeaders(200, text.length());
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }
    }
}

