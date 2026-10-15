package respawn.anchor.help.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

import respawn.anchor.help.client.mixin.AbstractContainerScreenAccessor;

/**
 * Right-click a totem to put it in your offhand — either on the slot in any inventory
 * screen, or in the world while holding one.
 *
 * <p>Both are one action per click of yours, and both go out as the same thing the vanilla
 * offhand-swap key sends: a slot click with the offhand button in a container, or a single
 * {@code SWAP_ITEM_WITH_OFFHAND} action in the world. Nothing fires without a click.
 */
public final class TotemEquip {
	/** Vanilla's button number for "the offhand" in a {@link ContainerInput#SWAP}. */
	private static final int OFFHAND_BUTTON = 40;
	/** GLFW's right mouse button. */
	private static final int RIGHT_BUTTON = 1;

	private TotemEquip() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof AbstractContainerScreen<?>) {
				ScreenMouseEvents.allowMouseClick(screen).register(TotemEquip::onMouseClick);
			}
		});
	}

	/** Right-click on a totem in any inventory screen. */
	private static boolean onMouseClick(Screen screen, MouseButtonEvent event) {
		if (!ModConfig.totemClickInventory) return true;
		if (event.button() != RIGHT_BUTTON) return true;
		if (!(screen instanceof AbstractContainerScreen<?>)) return true;

		AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
		Slot slot = accessor.respawnAnchorHelp$hoveredSlot();

		if (slot == null || !slot.getItem().is(Items.TOTEM_OF_UNDYING)) return true;

		accessor.respawnAnchorHelp$slotClicked(slot, slot.index, OFFHAND_BUTTON, ContainerInput.SWAP);

		// No selection handling here on purpose: the totem lands in the offhand, which ends
		// any totem loan on the next tick and hands your own slot back by itself.

		// Swallow the click so it does not also split the stack onto the cursor.
		return false;
	}

	/**
	 * Right-click in the world while holding a totem.
	 *
	 * <p>Hooked on {@code useItem} rather than the click itself, because {@code useItem} is
	 * only reached once a block or entity interaction has declined the click. Right-clicking
	 * a chest while holding a totem still opens the chest.
	 */
	public static void afterUseItem(Player player, InteractionHand hand, InteractionResult result) {
		if (!ModConfig.totemClickInHand) return;
		if (hand != InteractionHand.MAIN_HAND) return;
		if (result != null && result.consumesAction()) return;
		if (!player.getItemInHand(hand).is(Items.TOTEM_OF_UNDYING)) return;
		if (!KeyGuard.USE.canAct()) return;

		Minecraft client = Minecraft.getInstance();

		if (client.player == null || client.player.isSpectator()) return;
		if (client.getConnection() == null) return;

		client.getConnection().send(new ServerboundPlayerActionPacket(
				ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));

		// Go back to whatever was in hand before the totem was picked up. If the totem assist
		// had borrowed the selection, leave it alone - its own release does this properly.
		// The swap packet is already away, and the slot change syncs a tick later, so the
		// server still swaps the slot the totem was actually in.
		if (ModConfig.totemReturnAfterEquip) {
			HotbarSelection.returnToPrevious(player.getInventory());
		}

		KeyGuard.USE.consume();
	}
}
