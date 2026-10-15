package respawn.anchor.help.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.function.Function;

/**
 * Keeps "one click, one action" true.
 *
 * <p>Vanilla repeats both the use and attack keys while they are held, so any feature that
 * reacts to a click would otherwise fire again every few ticks off a single held button.
 * Features ask {@link #canAct()} first and call {@link #consume()} after, so the key has to
 * come back up before the mod does anything else.
 */
public final class KeyGuard {
	public static final KeyGuard USE = new KeyGuard(options -> options.keyUse);
	public static final KeyGuard ATTACK = new KeyGuard(options -> options.keyAttack);

	private final Function<Options, KeyMapping> mapping;
	private boolean released = true;

	private KeyGuard(Function<Options, KeyMapping> mapping) {
		this.mapping = mapping;
	}

	public static void tickAll(Minecraft client) {
		USE.tick(client);
		ATTACK.tick(client);
	}

	public boolean canAct() {
		return released;
	}

	public void consume() {
		released = false;
	}

	private void tick(Minecraft client) {
		if (!mapping.apply(client.options).isDown()) {
			released = true;
		}
	}
}
