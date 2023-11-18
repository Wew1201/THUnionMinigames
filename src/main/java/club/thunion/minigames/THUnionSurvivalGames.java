package club.thunion.minigames;

import club.thunion.minigames.framework.MinigameRegistry;
import club.thunion.minigames.framework.item.CustomItemBehavior;
import club.thunion.minigames.framework.item.CustomItemRegistry;
import club.thunion.minigames.sg.item.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.TypedActionResult;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class THUnionSurvivalGames implements ModInitializer {
    public static MinecraftServer SERVER;

    public void registerCustomItem(CustomItemBehavior... behaviors) {
        for (CustomItemBehavior behavior: behaviors) {
            CustomItemRegistry.registerCustomItem(this, behavior);
        }
    }

    public void registerMinigameCommand() {
        CommandRegistrationCallback.EVENT.register(new CommandRegistrationCallback() {
            @Override
            public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
                dispatcher.register(literal("minigames").then(literal("start").executes(__ -> {
                    MinigameRegistry.tickingMinigame = true;
                    return 1;
                })).then(literal("stop").executes(__ -> {
                    MinigameRegistry.tickingMinigame = false;
                    return 1;
                })).then(literal("instantiate").then(argument("configName", StringArgumentType.string()).executes(cmd -> {
                    String configName = cmd.getArgument("configName", String.class);
                    MinigameRegistry.instantiateMinigameFromConfig(configName);
                    return 1;
                }))));
            }
        });
        CommandRegistrationCallback.EVENT.register(new CommandRegistrationCallback() {
            @Override
            public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
                dispatcher.register(literal("customItems").then(literal("on").executes(__ -> {
                    registerCustomItem(new DeathScythe(), new Fireball(), new IceBomb(), new SignalScreener(), new ThunderTrident(), new WitherBomb());
                    return 1;
                })).then(literal("off")).executes(__ -> {
                    CustomItemRegistry.removeCustomItemsInGroup(THUnionSurvivalGames.this);
                    return 1;
                }));
            }
        });
    }

    @Override
    public void onInitialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            TypedActionResult<ItemStack> result = CustomItemRegistry.handleCustomItem(stack, world, player, hand);
            if (result != null) return result;
            return TypedActionResult.pass(stack);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SERVER = server;
            MinigameRegistry.tryTickMinigame();
        });
        registerMinigameCommand();
    }
}
