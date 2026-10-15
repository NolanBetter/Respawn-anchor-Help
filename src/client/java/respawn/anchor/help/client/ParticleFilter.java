package respawn.anchor.help.client;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;

/**
 * Decides which particles never get built.
 *
 * <p>Both the explosion burst ({@code ClientPacketListener#handleExplosion}) and the
 * totem pop ({@code ClientPacketListener}, on entity event 35) reach the client through
 * {@code ClientLevel#addParticle} -> {@code ParticleEngine#createParticle}, so filtering
 * the one factory method covers every source of them.
 */
public final class ParticleFilter {
	private ParticleFilter() {
	}

	public static boolean isHidden(ParticleOptions options) {
		ParticleType<?> type = options.getType();

		if (ModConfig.hideExplosionParticles
				&& (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER)) {
			return true;
		}

		return ModConfig.hideTotemParticles && type == ParticleTypes.TOTEM_OF_UNDYING;
	}
}
