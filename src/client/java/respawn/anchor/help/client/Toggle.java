package respawn.anchor.help.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * The mod's switchable features, so the keybinds and the {@code /anchorhelp} command
 * drive exactly the same set of options and print them the same way.
 */
public enum Toggle {
	SWAP("swap", "Anchor/glowstone swap",
			() -> ModConfig.anchorSwapBack, value -> ModConfig.anchorSwapBack = value),
	EXPLOSIONS("explosions", "Hide explosion particles",
			() -> ModConfig.hideExplosionParticles, value -> ModConfig.hideExplosionParticles = value),
	TOTEMS("totems", "Hide totem particles",
			() -> ModConfig.hideTotemParticles, value -> ModConfig.hideTotemParticles = value),
	TOTEM_ASSIST("totemswap", "Totem swap assist",
			() -> ModConfig.totemAssist, value -> ModConfig.totemAssist = value),
	SHIELD_FALLBACK("shield", "Shield fallback",
			() -> ModConfig.totemShieldFallback, value -> ModConfig.totemShieldFallback = value),
	TOTEM_CLICK_GUI("totemclick", "Right-click totem in inventory",
			() -> ModConfig.totemClickInventory, value -> ModConfig.totemClickInventory = value),
	TOTEM_CLICK_HAND("totemhand", "Right-click totem in hand",
			() -> ModConfig.totemClickInHand, value -> ModConfig.totemClickInHand = value),
	TOTEM_RETURN("totemreturn", "Return to previous item",
			() -> ModConfig.totemReturnAfterEquip, value -> ModConfig.totemReturnAfterEquip = value),
	SWORD_SWAP("sword", "Sword swap on hit",
			() -> ModConfig.swordSwap, value -> ModConfig.swordSwap = value),
	FOOD_ASSIST("foodswap", "Food swap assist",
			() -> ModConfig.foodAssist, value -> ModConfig.foodAssist = value);

	private final String commandName;
	private final String label;
	private final BooleanSupplier getter;
	private final Consumer<Boolean> setter;

	Toggle(String commandName, String label, BooleanSupplier getter, Consumer<Boolean> setter) {
		this.commandName = commandName;
		this.label = label;
		this.getter = getter;
		this.setter = setter;
	}

	public String commandName() {
		return commandName;
	}

	public String label() {
		return label;
	}

	public String translationKey() {
		return "key.respawn-anchor-help." + commandName;
	}

	public boolean get() {
		return getter.getAsBoolean();
	}

	public void set(boolean value) {
		if (get() == value) return;

		setter.accept(value);
		ModConfig.save();
	}

	public boolean toggle() {
		boolean value = !get();
		set(value);
		return value;
	}

	public MutableComponent status() {
		boolean on = get();

		return Component.literal(label + ": ")
				.withStyle(ChatFormatting.GRAY)
				.append(Component.literal(on ? "ON" : "OFF")
						.withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
	}
}
