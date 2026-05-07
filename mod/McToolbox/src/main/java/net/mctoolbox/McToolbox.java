package net.mctoolbox;

import net.fabricmc.api.ModInitializer;
import org.java_websocket.server.WebSocketServer;

public class McToolbox implements ModInitializer {

    public static final String MOD_ID = "mctoolbox";

    public static WebSocketServer server;

    @Override
    public void onInitialize() {
        server = new McToolboxWebSocket(8080);
        server.start();
    }
}