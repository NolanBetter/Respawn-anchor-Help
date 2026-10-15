package respawn.anchor.help.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/**
 * Selection-only combat assists: the mod moves which hotbar slot is highlighted, and you
 * still press the button.
 *
 * <p>Both assists borrow the selection through {@link HotbarSelection} and give it back
 * when they are done, so whichever one fires you end up back on the item you were actually
 * using. The totem outranks the food: if a totem goes while you are eating, the totem takes
 * the selection over, and handing it back afterwards returns you to your own item rather
 * than to the food.
 */
public final class CombatAssist {
	/**
	 * Food with a downside worth avoiding mid-fight. Merely weak food (raw beef, cod) is
	 * not listed - it already loses on score to anything better on the bar.
	 */
	private static final Set<Item> AVOID = Set.of(
			Items.ROTTEN_FLESH,
			Items.SPIDER_EYE,
			Items.POISONOUS_POTATO,
			Items.PUFFERFISH,
			Items.CHORUS_FRUIT,
			Items.SUSPICIOUS_STEW,
			Items.CHICKEN);

	/** Ticks left before borrowing the selection for a totem, or -1 when idle. */
	private static int totemCountdown = -1;
	/** Whether the offhand held a totem last tick, to catch the moment it goes. */
	private static boolean hadTotem = false;
	/** Whether we were below the health threshold last tick, so food fires on the edge. */
	private static boolean wasHurt = false;

	private CombatAssist() {
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;

		if (player == null) {
			totemCountdown = -1;
			hadTotem = false;
			wasHurt = false;
			return;
		}

		tickTotem(player);
		tickFood(player);
	}

	/**
	 * Watches the offhand rather than the totem-pop packet: the moment a totem stops being
	 * there is exactly the moment a replacement is wanted, and it needs no extra mixin.
	 * It also means the loan ends the moment a totem is back, however it got there — off the
	 * hotbar, out of the inventory, or picked up.
	 */
	private static void tickTotem(LocalPlayer player) {
		Inventory inventory = player.getInventory();
		boolean holdingTotem = player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);

		if (holdingTotem) {
			hadTotem = true;
			totemCountdown = -1;
			HotbarSelection.release(inventory, HotbarSelection.Loan.TOTEM);
			return;
		}

		if (hadTotem) {
			hadTotem = false;
			totemCountdown = ModConfig.totemAssist ? ModConfig.totemDelayTicks : -1;
		}

		if (totemCountdown < 0) return;

		if (totemCountdown > 0) {
			totemCountdown--;
			return;
		}

		totemCountdown = -1;

		if (!ModConfig.totemAssist) return;

		int slot = findSlot(inventory, Items.TOTEM_OF_UNDYING);

		if (slot < 0 && ModConfig.totemShieldFallback) {
			slot = findSlot(inventory, Items.SHIELD);
		}

		if (slot < 0) return;

		HotbarSelection.borrow(inventory, HotbarSelection.Loan.TOTEM, slot);
	}

	/**
	 * Edge-triggered on dropping below the threshold. Firing once rather than every tick is
	 * what stops it dragging you back to the food after something more urgent — a totem —
	 * has taken the selection.
	 */
	private static void tickFood(LocalPlayer player) {
		Inventory inventory = player.getInventory();

		if (!ModConfig.foodAssist) {
			wasHurt = false;
			HotbarSelection.release(inventory, HotbarSelection.Loan.FOOD);
			return;
		}

		float health = player.getHealth();
		boolean hurt = health > 0.0F && health <= ModConfig.foodHealthThreshold;

		if (!hurt) {
			if (wasHurt) {
				wasHurt = false;
				HotbarSelection.release(inventory, HotbarSelection.Loan.FOOD);
			}

			return;
		}

		if (wasHurt) return;
		wasHurt = true;

		// The totem is the more urgent of the two; do not take the selection off it.
		if (HotbarSelection.currentLoan() == HotbarSelection.Loan.TOTEM) return;

		int foodSlot = findBestFood(inventory);
		if (foodSlot < 0) return;

		HotbarSelection.borrow(inventory, HotbarSelection.Loan.FOOD, foodSlot);
	}

	private static int findBestFood(Inventory inventory) {
		int best = -1;
		float bestScore = 0.0F;

		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			ItemStack stack = inventory.getItem(slot);

			if (stack.isEmpty() || AVOID.contains(stack.getItem())) continue;

			FoodProperties food = stack.get(DataComponents.FOOD);
			if (food == null) continue;

			float score = food.nutrition() + food.saturation();

			if (score > bestScore) {
				bestScore = score;
				best = slot;
			}
		}

		return best;
	}

	private static int findSlot(Inventory inventory, Item item) {
		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			if (inventory.getItem(slot).is(item)) {
				return slot;
			}
		}

		return -1;
	}
}
