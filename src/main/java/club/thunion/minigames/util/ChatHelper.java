package club.thunion.minigames.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class ChatHelper {

    public static void broadcast(MinecraftServer server, Text text) {
        server.getWorlds().forEach(
                serverWorld -> serverWorld.getPlayers().forEach(
                        serverPlayerEntity -> serverPlayerEntity.sendMessage(text)));
    }

}
