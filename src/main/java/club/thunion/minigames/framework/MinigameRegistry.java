package club.thunion.minigames.framework;

import club.thunion.minigames.THUnionSurvivalGames;
import club.thunion.minigames.sg.SurvivalGameLogic;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static club.thunion.minigames.THUnionSurvivalGames.SERVER;

public class MinigameRegistry {
    private static Logger LOGGER = LogManager.getLogger();
    private static MinigameLogic runningMinigame;
    public static boolean tickingMinigame = false;

    public static void tryTickMinigame() {
        if (runningMinigame != null && tickingMinigame) runningMinigame.tick();
    }

    public static void instantiateMinigameFromConfig(String configName) {
        if (SERVER == null) return;
        tickingMinigame = false;
        try {
            File configFile = new File(SERVER.getRunDirectory(), "config/" + configName + ".properties");
            Properties properties = new Properties();
            FileInputStream inputStream = new FileInputStream(configFile);
            properties.load(inputStream);
            inputStream.close();
            instantiateMinigameFromProperties(properties);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void instantiateMinigameFromProperties(Properties properties) throws Exception {
        Class<?> clazz = Class.forName(properties.getProperty("minigameClass"));
        runningMinigame = (MinigameLogic) clazz.getConstructor(Properties.class).newInstance(properties);
    }
}
