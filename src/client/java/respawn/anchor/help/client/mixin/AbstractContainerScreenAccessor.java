package respawn.anchor.help.client.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Reaches the hovered slot and vanilla's own slot-click path, so a totem is equipped by
 * exactly the route the offhand-swap key already uses rather than a hand-rolled packet.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("hoveredSlot")
	Slot respawnAnchorHelp$hoveredSlot();

	@Invoker("slotClicked")
	void respawnAnchorHelp$slotClicked(Slot slot, int slotIndex, int button, ContainerInput input);
}
