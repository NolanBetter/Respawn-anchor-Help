package respawn.anchor.help.client;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

/**
 * Hit a player and the hotbar moves to your sword; hit again and it moves back.
 *
 * <p>The selection only ever changes <em>after</em> an attack, so every hit lands with the
 * item that was already in your hand when you clicked — the first with your crystals, the
 * second with the sword. One click is still one attack, and the mod never changes what you
 * are holding partway through one. That also means it costs you a weak first hit rather
 * than handing you sword damage for free.
 *
 * <p>Only attacks on players count. Detonating a crystal or hitting anything else leaves
 * the hotbar alone.
 */
public final class SwordSwap {
	/** Whether the sword is currently out because of us, waiting for the follow-up hit. */
	private static boolean swordOut = false;

	private SwordSwap() {
	}

	public static void reset() {
		swordOut = false;
	}

	/**
	 * Called after {@code MultiPlayerGameMode#attack}, by which point the attack packet has
	 * gone. The slot change syncs a tick later, so the server sees the hit first and the
	 * switch second — the order a player pressing a number key would produce.
	 */
	public static void afterAttack(Player attacker, Entity target) {
		if (!ModConfig.swordSwap) {
			swordOut = false;
			return;
		}

		if (!(target instanceof Player)) return;

		// A held attack key auto-repeats; each swap has to be its own click.
		if (!KeyGuard.ATTACK.canAct()) return;

		Inventory inventory = attacker.getInventory();

		if (swordOut) {
			swordOut = false;
			HotbarSelection.release(inventory, HotbarSelection.Loan.SWORD);
			KeyGuard.ATTACK.consume();
			return;
		}

		int slot = findSword(inventory);
		if (slot < 0 || slot == inventory.getSelectedSlot()) return;

		swordOut = true;
		HotbarSelection.borrow(inventory, HotbarSelection.Loan.SWORD, slot);
		KeyGuard.ATTACK.consume();
	}

	/** Called each tick so a loan lost to something more urgent does not leave us confused. */
	public static void tick() {
		if (swordOut && HotbarSelection.currentLoan() != HotbarSelection.Loan.SWORD) {
			swordOut = false;
		}
	}

	private static int findSword(Inventory inventory) {
		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			if (inventory.getItem(slot).is(ItemTags.SWORDS)) {
				return slot;
			}
		}

		return -1;
	}
}
