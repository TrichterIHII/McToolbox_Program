package net.mctoolbox;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class McToolboxClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		Keybinds.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			String json = DataCollector.collect(client);
			if (json != null && McToolbox.server != null) {
				McToolbox.server.broadcast(json);
			}
		});
	}
}
