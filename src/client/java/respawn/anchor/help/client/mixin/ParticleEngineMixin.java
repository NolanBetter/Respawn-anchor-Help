package respawn.anchor.help.client.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import respawn.anchor.help.client.ParticleFilter;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	/**
	 * {@code createParticle} is already allowed to return null for a type with no
	 * registered provider, and {@code ClientLevel#doAddParticle} discards the result
	 * without dereferencing it, so returning null here is a supported no-op.
	 */
	@Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
	private void respawnAnchorHelp$filterParticle(ParticleOptions options, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
		if (ParticleFilter.isHidden(options)) {
			cir.setReturnValue(null);
		}
	}
}
