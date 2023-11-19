package club.thunion.minigames;

import club.thunion.minigames.framework.MinigameRegistry;
import club.thunion.minigames.framework.item.CustomItemBehavior;
import club.thunion.minigames.framework.item.CustomItemRegistry;
import club.thunion.minigames.sg.item.*;
import club.thunion.minigames.util.ChatHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class THUnionSurvivalGames implements ModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();
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
                dispatcher.register(literal("minigames").then(literal("start").executes(command -> {
                    MinigameRegistry.tickingMinigame = true;
                    ChatHelper.broadcast(SERVER, Text.of("小游戏开始"));
                    return 1;
                })).then(literal("stop").executes(command -> {
                    MinigameRegistry.tickingMinigame = false;
                    ChatHelper.broadcast(SERVER, Text.of("小游戏停止"));
                    return 1;
                })).then(literal("instantiate").then(argument("configName", StringArgumentType.string()).executes(command -> {
                    String configName = command.getArgument("configName", String.class);
                    LOGGER.info("Start loading mini game: " + configName);
                    MinigameRegistry.instantiateMinigameFromConfig(configName);
                    LOGGER.info("Mini game loaded");
                    ChatHelper.broadcast(SERVER, Text.of("小游戏加载完成"));
                    return 1;
                }))));
            }
        });
        CommandRegistrationCallback.EVENT.register(new CommandRegistrationCallback() {
            @Override
            public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
                dispatcher.register(literal("customItems").then(literal("on").executes(command -> {
                    registerCustomItem(new DeathScythe(), new Fireball(), new IceBomb(), new SignalScreener(), new ThunderTrident(), new WitherBomb());
                    ChatHelper.broadcast(SERVER, Text.of("自定义物品开启"));
                    return 1;
                })).then(literal("off")).executes(command -> {
                    CustomItemRegistry.removeCustomItemsInGroup(THUnionSurvivalGames.this);
                    ChatHelper.broadcast(SERVER, Text.of("自定义物品关闭"));
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
