package respawn.anchor.help.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Puts the config screen behind the Mod Menu entry for this mod.
 *
 * <p>Mod Menu is optional: Fabric only loads this class when Mod Menu itself is present
 * and asks for its entrypoints, so nothing here runs without it installed.
 */
public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::create;
	}
}
