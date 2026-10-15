package respawn.anchor.help.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class RespawnAnchorHelpClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModConfig.load();
		ModKeys.register();
		ModCommands.register();
		ConfigButtons.register();
		TotemEquip.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			KeyGuard.tickAll(client);
			HotbarSelection.tick(client);
			ModKeys.tick(client);
			AnchorSwap.tick(client);
			CombatAssist.tick(client);
			SwordSwap.tick();
		});
	}
}
