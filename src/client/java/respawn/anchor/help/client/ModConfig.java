package respawn.anchor.help.client;

import net.fabricmc.loader.api.FabricLoader;

import respawn.anchor.help.RespawnAnchorHelp;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The three toggles, persisted to {@code config/respawn-anchor-help.properties}.
 *
 * <p>Everything is read live off these fields, so flipping one takes effect on the
 * very next interaction / particle without any further plumbing.
 */
public final class ModConfig {
	private static final Path FILE =
			FabricLoader.getInstance().getConfigDir().resolve(RespawnAnchorHelp.MOD_ID + ".properties");

	private static final String KEY_SWAP = "anchor-swap-back";
	private static final String KEY_EXPLOSIONS = "hide-explosion-particles";
	private static final String KEY_TOTEMS = "hide-totem-particles";
	private static final String KEY_TOTEM_ASSIST = "totem-swap-assist";
	private static final String KEY_TOTEM_DELAY = "totem-swap-delay-ticks";
	private static final String KEY_SHIELD_FALLBACK = "totem-shield-fallback";
	private static final String KEY_TOTEM_CLICK_GUI = "totem-right-click-inventory";
	private static final String KEY_TOTEM_CLICK_HAND = "totem-right-click-in-hand";
	private static final String KEY_TOTEM_RETURN = "totem-return-after-equip";
	private static final String KEY_SWORD_SWAP = "sword-swap-on-hit";
	private static final String KEY_FOOD_ASSIST = "food-swap-assist";
	private static final String KEY_FOOD_THRESHOLD = "food-health-threshold";

	public static final int TOTEM_DELAY_MIN = 0;
	public static final int TOTEM_DELAY_MAX = 100;
	public static final int FOOD_THRESHOLD_MIN = 2;
	public static final int FOOD_THRESHOLD_MAX = 19;

	/**
	 * Move the hotbar to the glowstone after placing a respawn anchor, and back to the
	 * anchors after charging one with glowstone.
	 */
	public static boolean anchorSwapBack = true;
	/** Suppress {@code explosion} and {@code explosion_emitter} particles. */
	public static boolean hideExplosionParticles = true;
	/** Suppress {@code totem_of_undying} particles. */
	public static boolean hideTotemParticles = true;

	/** After the offhand totem goes, select a replacement totem on the hotbar. */
	public static boolean totemAssist = true;
	/** Ticks to wait before doing so. 0 is the same tick. */
	public static int totemDelayTicks = 20;
	/** With no totem left, select a shield instead. */
	public static boolean totemShieldFallback = true;

	/** Right-clicking a totem in an inventory screen puts it in the offhand. */
	public static boolean totemClickInventory = true;
	/** Right-clicking in the world while holding a totem puts it in the offhand. */
	public static boolean totemClickInHand = true;
	/** After equipping a totem from the hand, go back to the item held before it. */
	public static boolean totemReturnAfterEquip = true;

	/** Hitting a player selects your sword; the next hit selects your previous item back. */
	public static boolean swordSwap = true;

	/** Below the health threshold, select the best food on the hotbar. */
	public static boolean foodAssist = true;
	/** Health, in half-hearts, at or below which the food is selected. */
	public static int foodHealthThreshold = 14;

	private ModConfig() {
	}

	public static void load() {
		if (!Files.isRegularFile(FILE)) {
			save();
			return;
		}

		Properties props = new Properties();

		try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
			props.load(reader);
		} catch (IOException e) {
			RespawnAnchorHelp.LOGGER.warn("Could not read {}, using defaults", FILE, e);
			return;
		}

		anchorSwapBack = bool(props, KEY_SWAP, anchorSwapBack);
		hideExplosionParticles = bool(props, KEY_EXPLOSIONS, hideExplosionParticles);
		hideTotemParticles = bool(props, KEY_TOTEMS, hideTotemParticles);
		totemAssist = bool(props, KEY_TOTEM_ASSIST, totemAssist);
		totemDelayTicks = integer(props, KEY_TOTEM_DELAY, totemDelayTicks, TOTEM_DELAY_MIN, TOTEM_DELAY_MAX);
		totemShieldFallback = bool(props, KEY_SHIELD_FALLBACK, totemShieldFallback);
		totemClickInventory = bool(props, KEY_TOTEM_CLICK_GUI, totemClickInventory);
		totemClickInHand = bool(props, KEY_TOTEM_CLICK_HAND, totemClickInHand);
		totemReturnAfterEquip = bool(props, KEY_TOTEM_RETURN, totemReturnAfterEquip);
		swordSwap = bool(props, KEY_SWORD_SWAP, swordSwap);
		foodAssist = bool(props, KEY_FOOD_ASSIST, foodAssist);
		foodHealthThreshold =
				integer(props, KEY_FOOD_THRESHOLD, foodHealthThreshold, FOOD_THRESHOLD_MIN, FOOD_THRESHOLD_MAX);
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty(KEY_SWAP, Boolean.toString(anchorSwapBack));
		props.setProperty(KEY_EXPLOSIONS, Boolean.toString(hideExplosionParticles));
		props.setProperty(KEY_TOTEMS, Boolean.toString(hideTotemParticles));
		props.setProperty(KEY_TOTEM_ASSIST, Boolean.toString(totemAssist));
		props.setProperty(KEY_TOTEM_DELAY, Integer.toString(totemDelayTicks));
		props.setProperty(KEY_SHIELD_FALLBACK, Boolean.toString(totemShieldFallback));
		props.setProperty(KEY_TOTEM_CLICK_GUI, Boolean.toString(totemClickInventory));
		props.setProperty(KEY_TOTEM_CLICK_HAND, Boolean.toString(totemClickInHand));
		props.setProperty(KEY_TOTEM_RETURN, Boolean.toString(totemReturnAfterEquip));
		props.setProperty(KEY_SWORD_SWAP, Boolean.toString(swordSwap));
		props.setProperty(KEY_FOOD_ASSIST, Boolean.toString(foodAssist));
		props.setProperty(KEY_FOOD_THRESHOLD, Integer.toString(foodHealthThreshold));

		try {
			Files.createDirectories(FILE.getParent());

			try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
				props.store(writer, "Respawn Anchor Help");
			}
		} catch (IOException e) {
			RespawnAnchorHelp.LOGGER.warn("Could not write {}", FILE, e);
		}
	}

	private static boolean bool(Properties props, String key, boolean fallback) {
		String value = props.getProperty(key);
		if (value == null) return fallback;
		return Boolean.parseBoolean(value.trim());
	}

	private static int integer(Properties props, String key, int fallback, int min, int max) {
		String value = props.getProperty(key);
		if (value == null) return fallback;

		try {
			return Math.clamp(Integer.parseInt(value.trim()), min, max);
		} catch (NumberFormatException e) {
			RespawnAnchorHelp.LOGGER.warn("Bad value '{}' for {}, using {}", value, key, fallback);
			return fallback;
		}
	}
}
