package respawn.anchor.help.client.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import respawn.anchor.help.client.AnchorSwap;
import respawn.anchor.help.client.SwordSwap;
import respawn.anchor.help.client.TotemEquip;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@Inject(method = "useItemOn", at = @At("HEAD"))
	private void respawnAnchorHelp$armSwap(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> cir) {
		AnchorSwap.beforeUseItemOn(player, hand, hitResult);
	}

	@Inject(method = "useItemOn", at = @At("RETURN"))
	private void respawnAnchorHelp$applySwap(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult,
			CallbackInfoReturnable<InteractionResult> cir) {
		AnchorSwap.afterUseItemOn(player, cir.getReturnValue());
	}

	@Inject(method = "attack", at = @At("RETURN"))
	private void respawnAnchorHelp$swapSword(Player attacker, Entity target, CallbackInfo ci) {
		SwordSwap.afterAttack(attacker, target);
	}

	@Inject(method = "useItem", at = @At("RETURN"))
	private void respawnAnchorHelp$equipTotem(Player player, InteractionHand hand,
			CallbackInfoReturnable<InteractionResult> cir) {
		TotemEquip.afterUseItem(player, hand, cir.getReturnValue());
	}
}
