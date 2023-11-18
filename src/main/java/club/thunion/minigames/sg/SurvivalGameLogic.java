package club.thunion.minigames.sg;

import club.thunion.minigames.framework.MinigameLogic;
import club.thunion.minigames.sg.tasks.RegisterCustomItemsTask;
import club.thunion.minigames.sg.tasks.RegisterParticipantsTask;
import club.thunion.minigames.sg.tasks.RepopulateLootTask;
import club.thunion.minigames.sg.tasks.ShrinkBorderTask;
import club.thunion.minigames.util.PropertyReader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Properties;

import static club.thunion.minigames.THUnionSurvivalGames.SERVER;

public class SurvivalGameLogic extends MinigameLogic {
    public final String SHRINK_BORDER_TASK_ID = "ShrinkBorder";
    public final String REGISTER_PLAYERS_TASK_ID = "RegisterPlayers";
    public final String REGISTER_ITEMS_TASK_ID = "RegisterCustomItems";
    public final String RESET_LOOT_TASK_ID = "ResetLoot";

    public SurvivalGameLogic(Properties properties) {
        super(properties);
        PropertyReader reader = new PropertyReader(properties);
        registerShrinkBorderTask(reader);
        registerInitializationTask(reader);
        registerCustomItemTask(reader);
        registerRepopulateLootTask(reader);
    }

    private void registerShrinkBorderTask(PropertyReader reader) {
        ServerWorld world = SERVER.getWorld(World.OVERWORLD);
        int startShrinkingTicks = reader.readInt("border.startTick");
        int totalShrinkingTicks = reader.readInt("border.shrinkDuration");
        int terminationTicks = reader.readInt("border.gameTerminationTick");
        Box initialBox = reader.readBox("border.initialBox");
        Box finalBox = reader.readBox("border.finalBox");
        ShrinkBorderTask shrinkBorderTask = new ShrinkBorderTask(
                world, initialBox, finalBox, startShrinkingTicks, totalShrinkingTicks, terminationTicks
        );
        registerTask(SHRINK_BORDER_TASK_ID, shrinkBorderTask);
    }

    private void registerInitializationTask(PropertyReader reader) {
        ServerWorld world = SERVER.getWorld(World.OVERWORLD);
        Vec3d centerPos = reader.readVec3d("init.spreadCenter");
        double spreadRadius = reader.readDouble("init.spreadRadius");
        Box spectatorBox = reader.readBox("init.specBox");
        int i = 0;
        String keyI;
        EnumMap<Formatting, Box> teamBoxes = new EnumMap<>(Formatting.class);
        while (reader.hasKey(keyI = "init.team" + i)) {
            Formatting formatting = Formatting.byName(reader.readString(keyI));
            if (formatting == null) throw new IllegalArgumentException();
            Box teamBox = reader.readBox(keyI + ".box");
            teamBoxes.put(formatting, teamBox);
        }
        RegisterParticipantsTask registerParticipantsTask = new RegisterParticipantsTask(
                world, teamBoxes, spectatorBox, centerPos, spreadRadius
        );
        registerTask(REGISTER_PLAYERS_TASK_ID, registerParticipantsTask);
    }

    private void registerCustomItemTask(PropertyReader reader) {
        int terminationTicks = reader.readInt("border.gameTerminationTick");
        registerTask(REGISTER_ITEMS_TASK_ID, new RegisterCustomItemsTask(terminationTicks));
    }

    private void registerRepopulateLootTask(PropertyReader reader) {
        BlockBox repopBox = reader.readBlockBox("loot.populationBox");
        int repopInterval = reader.readInt("loot.interval");
        int terminationTicks = reader.readInt("loot.terminationTicks");
        int randomizedChestAmount = reader.readInt("loot.count.unspecified");
        int[] fixedTierLootChestAmount = new int[RepopulateLootTask.COLOR_BY_LOOT_TIER.length];
        for (int i = 0; i <= RepopulateLootTask.COLOR_BY_LOOT_TIER.length; i ++) {
            fixedTierLootChestAmount[i] = reader.readInt("loot.count.tier" + (i + 1));
        }
        registerTask(RESET_LOOT_TASK_ID, new RepopulateLootTask(
                terminationTicks, repopInterval, repopBox, SERVER.getWorld(World.OVERWORLD),
                randomizedChestAmount, fixedTierLootChestAmount));
    }

    private List<ServerPlayerEntity> participants = new ArrayList<>();

    public List<ServerPlayerEntity> getParticipants() {
        return participants;
    }
}
