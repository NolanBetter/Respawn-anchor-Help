package respawn.anchor.help.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * {@code /anchorhelp} (alias {@code /rah}) — a client command, never sent to the server.
 */
public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {
			LiteralCommandNode<FabricClientCommandSource> root = dispatcher.register(build("anchorhelp"));
			dispatcher.register(ClientCommands.literal("rah").executes(root.getCommand()).redirect(root));
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> build(String name) {
		LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommands.literal(name)
				.executes(context -> printAll(context.getSource()))
				.then(ClientCommands.literal("config")
						.executes(context -> openConfig(context.getSource())));

		for (Toggle toggle : Toggle.values()) {
			root.then(ClientCommands.literal(toggle.commandName())
					.executes(context -> print(context.getSource(), toggle))
					.then(ClientCommands.literal("on")
							.executes(context -> set(context.getSource(), toggle, true)))
					.then(ClientCommands.literal("off")
							.executes(context -> set(context.getSource(), toggle, false)))
					.then(ClientCommands.literal("toggle")
							.executes(context -> {
								toggle.toggle();
								return print(context.getSource(), toggle);
							})));
		}

		return root;
	}

	/**
	 * Deferred to the next tick: the chat screen is still closing while the command runs,
	 * and swapping the screen out from under it does not end well.
	 */
	private static int openConfig(FabricClientCommandSource source) {
		Minecraft client = source.getClient();
		client.execute(() -> client.setScreenAndShow(ConfigScreen.create(null)));
		return 1;
	}

	private static int set(FabricClientCommandSource source, Toggle toggle, boolean value) {
		toggle.set(value);
		return print(source, toggle);
	}

	private static int print(FabricClientCommandSource source, Toggle toggle) {
		source.sendFeedback(toggle.status());
		return 1;
	}

	private static int printAll(FabricClientCommandSource source) {
		source.sendFeedback(Component.literal("Respawn Anchor Help").withStyle(ChatFormatting.GOLD));

		for (Toggle toggle : Toggle.values()) {
			source.sendFeedback(Component.literal("  ").append(toggle.status()));
		}

		source.sendFeedback(Component.literal("  Totem swap delay: ")
				.withStyle(ChatFormatting.GRAY)
				.append(Component.literal(ModConfig.totemDelayTicks + " ticks").withStyle(ChatFormatting.YELLOW)));
		source.sendFeedback(Component.literal("  Food health threshold: ")
				.withStyle(ChatFormatting.GRAY)
				.append(Component.literal(ModConfig.foodHealthThreshold + " hp").withStyle(ChatFormatting.YELLOW)));
		source.sendFeedback(Component.literal("  /anchorhelp config  -  sliders and everything else")
				.withStyle(ChatFormatting.DARK_GRAY));
		return 1;
	}
}
