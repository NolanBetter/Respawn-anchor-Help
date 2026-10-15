package respawn.anchor.help.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;

/**
 * One place that knows which hotbar slot is <em>yours</em>.
 *
 * <p>Features that move the selection temporarily "borrow" it and give it back when their
 * reason is over. The slot to give back is the one the player last picked by hand, not
 * whatever happened to be selected when the feature fired — otherwise a totem interrupting
 * a food swap would hand back the food slot rather than the crystals you were holding.
 *
 * <p>Only one loan runs at a time. A second borrow takes the loan over without disturbing
 * the return slot, and touching the hotbar yourself cancels the loan outright, because at
 * that point you have clearly said where you want to be.
 */
public final class HotbarSelection {
	public enum Loan {
		NONE,
		TOTEM,
		FOOD,
		SWORD
	}

	/** The slot the player last chose for themselves. */
	private static int ownSlot = -1;
	/** The one before that — where "back to my last item" points after a manual switch. */
	private static int previousOwnSlot = -1;
	/** Last slot the mod set, so a change by the player can be told apart from ours. */
	private static int modSelected = -1;

	private static Loan loan = Loan.NONE;
	private static int loanReturn = -1;

	private HotbarSelection() {
	}

	/** Must run before anything that borrows, so this tick sees the player's own changes. */
	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;

		if (player == null) {
			ownSlot = -1;
			previousOwnSlot = -1;
			modSelected = -1;
			loan = Loan.NONE;
			loanReturn = -1;
			return;
		}

		int selected = player.getInventory().getSelectedSlot();
		if (selected == modSelected) return;

		// The player moved it themselves.
		if (selected != ownSlot) {
			previousOwnSlot = ownSlot;
			ownSlot = selected;
		}

		loan = Loan.NONE;
		loanReturn = -1;
		modSelected = -1;
	}

	/** Take the selection for a while, remembering where to hand it back. */
	public static void borrow(Inventory inventory, Loan reason, int slot) {
		if (slot < 0 || slot >= Inventory.SELECTION_SIZE) return;

		if (loan == Loan.NONE) {
			loanReturn = ownSlot;
		}

		loan = reason;
		select(inventory, slot);
	}

	/** Hand the selection back, if this feature is the one holding it. */
	public static void release(Inventory inventory, Loan reason) {
		if (loan != reason) return;

		int back = loanReturn;
		loan = Loan.NONE;
		loanReturn = -1;

		select(inventory, back);
	}

	public static Loan currentLoan() {
		return loan;
	}

	/** Go back to the item held before the current one. Ignored while a loan is running. */
	public static void returnToPrevious(Inventory inventory) {
		if (loan != Loan.NONE) return;
		if (previousOwnSlot < 0 || previousOwnSlot >= Inventory.SELECTION_SIZE) return;

		select(inventory, previousOwnSlot);
		ownSlot = previousOwnSlot;
	}

	/**
	 * Move the selection and treat the new slot as the player's own — for changes meant to
	 * stick, like the anchor cycle, rather than a temporary loan.
	 */
	public static void selectAndOwn(Inventory inventory, int slot) {
		if (slot < 0 || slot >= Inventory.SELECTION_SIZE) return;
		if (inventory.getSelectedSlot() == slot) return;

		if (slot != ownSlot) {
			previousOwnSlot = ownSlot;
			ownSlot = slot;
		}

		select(inventory, slot);
	}

	private static void select(Inventory inventory, int slot) {
		if (slot < 0 || slot >= Inventory.SELECTION_SIZE) return;
		if (inventory.getSelectedSlot() == slot) return;

		inventory.setSelectedSlot(slot);
		modSelected = slot;
	}
}
