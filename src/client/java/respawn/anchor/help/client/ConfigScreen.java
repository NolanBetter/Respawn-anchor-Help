package respawn.anchor.help.client;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The YetAnotherConfigLib screen. Reached from Mod Menu, from the button
 * {@link ConfigButtons} puts on the title and pause screens, or from
 * {@code /anchorhelp config}.
 */
public final class ConfigScreen {
	private ConfigScreen() {
	}

	public static Screen create(Screen parent) {
		return YetAnotherConfigLib.createBuilder()
				.title(Component.literal("Respawn Anchor Help"))
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("Anchor"))
						.tooltip(Component.literal("Hotbar handling around the respawn anchor cycle."))
						.option(toggle(Toggle.SWAP,
								"Place a respawn anchor and the hotbar moves to your glowstone; charge that "
										+ "anchor with the glowstone and it moves back to your anchors.",
								"Both items have to already be on your hotbar.",
								"Detonating an anchor is not a placement and never moves the hotbar, and a held "
										+ "right-click cannot walk the cycle by itself - each swap needs its own "
										+ "click."))
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("Assist"))
						.tooltip(Component.literal("Selection-only helpers. These pick the slot; you still "
								+ "press the button."))
						.option(toggle(Toggle.TOTEM_ASSIST,
								"When your offhand totem goes, selects a replacement totem from your hotbar.",
								"It only changes which slot is highlighted - press your offhand-swap key (F) to "
										+ "put it across. The mod never moves items for you."))
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Totem swap delay"))
								.description(OptionDescription.of(
										Component.literal("How long to wait before selecting the replacement."),
										Component.literal("0 selects it on the same tick the totem goes.")))
								.binding(20, () -> ModConfig.totemDelayTicks, value -> ModConfig.totemDelayTicks = value)
								.controller(option -> IntegerSliderControllerBuilder.create(option)
										.range(ModConfig.TOTEM_DELAY_MIN, ModConfig.TOTEM_DELAY_MAX)
										.step(1)
										.formatValue(ConfigScreen::formatTicks))
								.build())
						.option(toggle(Toggle.SHIELD_FALLBACK,
								"With no totem left on the hotbar, select a shield instead."))
						.option(toggle(Toggle.TOTEM_CLICK_GUI,
								"Right-click a totem in any inventory screen to send it straight to your offhand.",
								"Uses the same slot click as the vanilla offhand-swap key, so it works anywhere "
										+ "that key does."))
						.option(toggle(Toggle.TOTEM_CLICK_HAND,
								"Right-click while holding a totem to move it to your offhand.",
								"Only when the click had nothing else to do - right-clicking a chest while "
										+ "holding a totem still opens the chest."))
						.option(toggle(Toggle.TOTEM_RETURN,
								"After equipping a totem from your hand, go back to the item you were holding "
										+ "before you switched to it.",
								"The assists always hand your own slot back on their own; this covers the case "
										+ "where you picked the totem up yourself."))
						.option(toggle(Toggle.SWORD_SWAP,
								"Hit a player and the hotbar moves to your sword; hit again and it moves back to "
										+ "what you were holding.",
								"Each hit lands with the item that was already in your hand, so the first costs "
										+ "you a weak hit and the second is the sword one. The mod never changes "
										+ "your item partway through an attack.",
								"Only attacks on players count - detonating a crystal leaves the hotbar alone."))
						.option(toggle(Toggle.FOOD_ASSIST,
								"When you drop below the health threshold, selects the best food on your hotbar, "
										+ "and hands your previous slot back once you are healed.",
								"You still hold right-click to eat. Food has to be on the hotbar.",
								"Rotten flesh, spider eyes, pufferfish, chorus fruit, suspicious stew, poisonous "
										+ "potatoes and raw chicken are skipped."))
						.option(Option.<Integer>createBuilder()
								.name(Component.literal("Food health threshold"))
								.description(OptionDescription.of(
										Component.literal("Select food at or below this much health.")))
								.binding(14, () -> ModConfig.foodHealthThreshold,
										value -> ModConfig.foodHealthThreshold = value)
								.controller(option -> IntegerSliderControllerBuilder.create(option)
										.range(ModConfig.FOOD_THRESHOLD_MIN, ModConfig.FOOD_THRESHOLD_MAX)
										.step(1)
										.formatValue(ConfigScreen::formatHealth))
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.literal("Particles"))
						.tooltip(Component.literal("Visual clutter during fights."))
						.option(toggle(Toggle.EXPLOSIONS,
								"Hides the explosion and explosion_emitter particles from crystals, anchors and "
										+ "TNT.",
								"Sound, damage and knockback are untouched."))
						.option(toggle(Toggle.TOTEMS,
								"Hides the particle burst when a totem pops.",
								"The on-screen totem animation and the sound are left alone."))
						.build())
				.save(ModConfig::save)
				.build()
				.generateScreen(parent);
	}

	private static Component formatTicks(int ticks) {
		if (ticks == 0) return Component.literal("Instant");
		return Component.literal(String.format("%d ticks (%.2fs)", ticks, ticks / 20.0));
	}

	private static Component formatHealth(int halfHearts) {
		return Component.literal(String.format("%.1f hearts", halfHearts / 2.0));
	}

	private static Option<Boolean> toggle(Toggle toggle, String... description) {
		Component[] lines = new Component[description.length];

		for (int i = 0; i < description.length; i++) {
			lines[i] = Component.literal(description[i]);
		}

		return Option.<Boolean>createBuilder()
				.name(Component.literal(toggle.label()))
				.description(OptionDescription.of(lines))
				.binding(true, toggle::get, toggle::set)
				.controller(option -> BooleanControllerBuilder.create(option).coloured(true).onOffFormatter())
				.build();
	}
}
