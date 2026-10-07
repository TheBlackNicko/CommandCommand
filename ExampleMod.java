package com.example;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ExampleMod implements ModInitializer {

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			for (String name : new String[] { "cmd", "command" }) {
				dispatcher.register(
						Commands.literal(name)
								.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
								// /cmd -> обычный командный блок
								.executes(ctx -> give(ctx.getSource(), 1))
								// /cmd 1..3
								.then(Commands.argument("type", IntegerArgumentType.integer(1, 3))
										.executes(ctx -> give(
												ctx.getSource(),
												IntegerArgumentType.getInteger(ctx, "type"))))
				);
			}
		});
	}

	private static int give(CommandSourceStack source, int type) {
		ServerPlayer player = source.getPlayer();
		if (player == null) {
			source.sendFailure(Component.literal("Эту команду может использовать только игрок"));
			return 0;
		}

		Item item = switch (type) {
			case 2 -> Items.REPEATING_COMMAND_BLOCK;
			case 3 -> Items.CHAIN_COMMAND_BLOCK;
			default -> Items.COMMAND_BLOCK;
		};

		ItemStack stack = new ItemStack(item);
		if (!player.getInventory().add(stack)) {
			source.sendFailure(Component.literal("Инвентарь полон"));
			return 0;
		}

		source.sendSuccess(() -> Component.literal("Выдан: ").append(item.getName(stack)), false);
		return 1;
	}
}
