package respawn.anchor.help.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import respawn.anchor.help.RespawnAnchorHelp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Optional keybinds for the three toggles. All unbound by default so they cannot collide
 * with anything the player already uses — bind them in Options -> Controls if wanted.
 */
public final class ModKeys {
	public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(RespawnAnchorHelp.id("main"));

	private static final Map<KeyMapping, Toggle> BINDINGS = new LinkedHashMap<>();

	private ModKeys() {
	}

	public static void register() {
		for (Toggle toggle : Toggle.values()) {
			KeyMapping mapping = new KeyMapping(toggle.translationKey(), InputConstants.UNKNOWN.getValue(), CATEGORY);
			BINDINGS.put(KeyMappingHelper.registerKeyMapping(mapping), toggle);
		}
	}

	public static void tick(Minecraft client) {
		for (Map.Entry<KeyMapping, Toggle> entry : BINDINGS.entrySet()) {
			boolean pressed = false;

			while (entry.getKey().consumeClick()) {
				pressed = true;
			}

			if (pressed) {
				Toggle toggle = entry.getValue();
				toggle.toggle();
				client.gui.hud.setOverlayMessage(toggle.status(), false);
			}
		}
	}
}
