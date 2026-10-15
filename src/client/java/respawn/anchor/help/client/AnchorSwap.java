package respawn.anchor.help.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Runs the hotbar around the anchor cycle: place an anchor and the hotbar moves to your
 * glowstone, charge that anchor with the glowstone and it moves back to your anchors.
 *
 * <p>Purely a hotbar selection change — the mod never clicks, places or uses anything on
 * the player's behalf, and it adds no packets beyond the ordinary "held slot changed"
 * one that pressing a number key sends. Every interaction is still the player's own
 * click; one click stays one interaction.
 */
public final class AnchorSwap {
	/** Hotbar slots the player was last seen holding each item in, or -1. */
	private static int preferredAnchorSlot = -1;
	private static int preferredGlowstoneSlot = -1;

	/** Slot to select once the in-flight interaction reports success, or -1. */
	private static int armedSlot = -1;
	/** What that slot has to still contain for the swap to happen. */
	private static Item armedItem = null;
	/** Set only for a possible placement, which needs confirming against the world. */
	private static PendingPlace pendingPlace = null;


	private AnchorSwap() {
	}

	/**
	 * Remembers which hotbar slots the player keeps anchors and glowstone in, so that with
	 * several stacks of either on the bar we return to the ones they were actually using
	 * rather than always snapping to the leftmost.
	 */
	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;

		if (player == null) {
			preferredAnchorSlot = -1;
			preferredGlowstoneSlot = -1;
			disarm();
			return;
		}

		Inventory inventory = player.getInventory();
		rememberPreferred(inventory, inventory.getSelectedSlot());
	}

	/**
	 * Called at the head of {@code MultiPlayerGameMode#useItemOn}, while the world still
	 * shows the pre-interaction state.
	 */
	public static void beforeUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
		disarm();

		if (!ModConfig.anchorSwapBack) return;

		ItemStack held = player.getItemInHand(hand);

		if (held.is(Items.GLOWSTONE)) {
			armCharge(player, hitResult);
		} else if (held.is(Items.RESPAWN_ANCHOR)) {
			armPlacement(player, hitResult);
		}
	}

	/**
	 * Called at the return of {@code MultiPlayerGameMode#useItemOn}, by which point the
	 * interaction packet has already been sent and the client has predicted its result.
	 * The slot change is synced afterwards by {@code MultiPlayerGameMode#tick}, so the
	 * server sees the interaction first and the slot change second — the same order the
	 * player would have produced by hand.
	 */
	public static void afterUseItemOn(LocalPlayer player, InteractionResult result) {
		int slot = armedSlot;
		Item item = armedItem;
		PendingPlace place = pendingPlace;
		disarm();

		if (slot < 0 || item == null) return;
		if (result == null || !result.consumesAction()) return;

		// One click, one swap: a held right-click must not walk the cycle by itself.
		if (!KeyGuard.USE.canAct()) return;

		// Holding an anchor and clicking a charged anchor detonates it rather than placing
		// anything, and that must not move the hotbar. Only a respawn anchor that actually
		// appeared in the world counts as a placement.
		if (place != null && !place.anchorAppeared(player.level())) return;

		Inventory inventory = player.getInventory();

		if (inventory.getSelectedSlot() == slot) return;
		if (!holds(inventory, slot, item)) return;

		// Owned rather than borrowed: the anchor cycle is meant to leave you on the new item.
		HotbarSelection.selectAndOwn(inventory, slot);
		rememberPreferred(inventory, slot);
		KeyGuard.USE.consume();
	}

	/** Glowstone onto a chargeable anchor: line up a move back to the anchors. */
	private static void armCharge(LocalPlayer player, BlockHitResult hitResult) {
		// Sneaking makes the block interaction get skipped and places the glowstone instead.
		if (player.isSecondaryUseActive()) return;

		BlockState state = player.level().getBlockState(hitResult.getBlockPos());

		if (!state.is(Blocks.RESPAWN_ANCHOR)) return;
		if (state.getValue(RespawnAnchorBlock.CHARGE) >= RespawnAnchorBlock.MAX_CHARGES) return;

		arm(findSlot(player.getInventory(), Items.RESPAWN_ANCHOR, preferredAnchorSlot),
				Items.RESPAWN_ANCHOR, null);
	}

	/**
	 * Anchor in hand: line up a move to the glowstone, but only tentatively. Whether this
	 * click places an anchor or detonates one is decided by the interaction itself, so we
	 * record the two blocks a placement could land in and check them again afterwards.
	 */
	private static void armPlacement(LocalPlayer player, BlockHitResult hitResult) {
		int slot = findSlot(player.getInventory(), Items.GLOWSTONE, preferredGlowstoneSlot);
		if (slot < 0) return;

		Level level = player.level();
		BlockPos clicked = hitResult.getBlockPos();
		BlockPos offset = clicked.relative(hitResult.getDirection());

		arm(slot, Items.GLOWSTONE,
				new PendingPlace(clicked, isAnchor(level, clicked), offset, isAnchor(level, offset)));
	}

	private static void arm(int slot, Item item, PendingPlace place) {
		if (slot < 0) return;

		armedSlot = slot;
		armedItem = item;
		pendingPlace = place;
	}

	private static void disarm() {
		armedSlot = -1;
		armedItem = null;
		pendingPlace = null;
	}

	private static void rememberPreferred(Inventory inventory, int slot) {
		if (holds(inventory, slot, Items.RESPAWN_ANCHOR)) {
			preferredAnchorSlot = slot;
		} else if (holds(inventory, slot, Items.GLOWSTONE)) {
			preferredGlowstoneSlot = slot;
		}
	}

	private static int findSlot(Inventory inventory, Item item, int preferred) {
		if (holds(inventory, preferred, item)) {
			return preferred;
		}

		for (int slot = 0; slot < Inventory.SELECTION_SIZE; slot++) {
			if (holds(inventory, slot, item)) {
				return slot;
			}
		}

		return -1;
	}

	private static boolean holds(Inventory inventory, int slot, Item item) {
		if (slot < 0 || slot >= Inventory.SELECTION_SIZE) return false;
		return inventory.getItem(slot).is(item);
	}

	private static boolean isAnchor(Level level, BlockPos pos) {
		return level.getBlockState(pos).is(Blocks.RESPAWN_ANCHOR);
	}

	/**
	 * The two positions a block placement from this click could land in, and whether each
	 * already held a respawn anchor beforehand. The clicked block is included because a
	 * replaceable block (grass, water) is built into rather than against.
	 */
	private record PendingPlace(BlockPos clicked, boolean clickedWasAnchor,
			BlockPos offset, boolean offsetWasAnchor) {
		boolean anchorAppeared(Level level) {
			return (!clickedWasAnchor && isAnchor(level, clicked))
					|| (!offsetWasAnchor && isAnchor(level, offset));
		}
	}
}
