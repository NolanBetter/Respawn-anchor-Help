package respawn.anchor.help.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds a config button to the icon row on the title and pause screens - the row holding
 * the Mod Menu, friends and similar square buttons.
 *
 * <p>That row is not a vanilla widget with an address we can ask for; it is however many
 * 20x20 buttons vanilla and other mods happened to line up at the same height. So rather
 * than hardcoding a position that breaks whenever the modlist changes, the row is found
 * by looking for the largest run of same-height square buttons, and the whole row is
 * nudged left by half a slot so it stays centred once ours joins the end.
 */
public final class ConfigButtons {
	private static final int SIZE = 20;
	private static final int DEFAULT_GAP = 4;
	private static final int CORNER_MARGIN = 6;

	private ConfigButtons() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
				add(screen, Screens.getWidgets(screen));
			}
		});
	}

	private static void add(Screen parent, List<AbstractWidget> widgets) {
		Button button = Button.builder(Component.literal("RA"), press -> open(parent))
				.bounds(CORNER_MARGIN, CORNER_MARGIN, SIZE, SIZE)
				.tooltip(Tooltip.create(Component.literal("Respawn Anchor Help")))
				.build();

		List<AbstractWidget> row = findIconRow(widgets);

		if (!row.isEmpty()) {
			joinRow(button, row);
		}

		widgets.add(button);
	}

	private static void joinRow(Button button, List<AbstractWidget> row) {
		AbstractWidget last = row.get(row.size() - 1);
		int step = SIZE + gapOf(row);

		button.setY(last.getY());
		button.setX(last.getX() + step);

		// Re-centre: the row just got one slot wider, so everything moves half a slot left.
		int shift = step / 2;

		for (AbstractWidget widget : row) {
			widget.setX(widget.getX() - shift);
		}

		button.setX(button.getX() - shift);
	}

	/**
	 * The largest set of visible square buttons sharing a Y coordinate. Ties go to the
	 * lowest row on screen, which on both screens is the one the mod buttons live in.
	 */
	private static List<AbstractWidget> findIconRow(List<AbstractWidget> widgets) {
		Map<Integer, List<AbstractWidget>> rows = new HashMap<>();

		for (AbstractWidget widget : widgets) {
			if (!widget.visible) continue;
			if (widget.getWidth() != SIZE || widget.getHeight() != SIZE) continue;

			rows.computeIfAbsent(widget.getY(), y -> new ArrayList<>()).add(widget);
		}

		List<AbstractWidget> best = List.of();

		for (Map.Entry<Integer, List<AbstractWidget>> entry : rows.entrySet()) {
			List<AbstractWidget> candidate = entry.getValue();

			// A lone square button is some other control, not the row.
			if (candidate.size() < 2) continue;

			if (candidate.size() > best.size()
					|| (candidate.size() == best.size() && entry.getKey() > best.get(0).getY())) {
				best = candidate;
			}
		}

		List<AbstractWidget> sorted = new ArrayList<>(best);
		sorted.sort(Comparator.comparingInt(AbstractWidget::getX));
		return sorted;
	}

	private static int gapOf(List<AbstractWidget> row) {
		AbstractWidget first = row.get(0);
		AbstractWidget second = row.get(1);
		int gap = second.getX() - (first.getX() + first.getWidth());

		return gap >= 0 && gap <= 16 ? gap : DEFAULT_GAP;
	}

	private static void open(Screen parent) {
		Minecraft.getInstance().setScreenAndShow(ConfigScreen.create(parent));
	}
}
