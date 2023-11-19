package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.sg.SurvivalGameLogic;
import com.mojang.logging.LogUtils;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegisterParticipantsTask extends MinigameTask<SurvivalGameLogic> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ServerWorld world;
    private final Map<Formatting, Box> teamJoinBoxMap;
    private final Box spectatorBox;
    private final Vec3d initialPlayerCenter;
    private final double playerSpreadRadius;

    public RegisterParticipantsTask(ServerWorld world, Map<Formatting, Box> teamJoinBoxMap, Box spectatorBox, Vec3d initialPlayerCenter, double playerSpreadRadius) {
        super(Integer.MIN_VALUE, SurvivalGameLogic.class);
        this.world = world;
        this.teamJoinBoxMap = teamJoinBoxMap;
        this.spectatorBox = spectatorBox;
        this.initialPlayerCenter = initialPlayerCenter;
        this.playerSpreadRadius = playerSpreadRadius;
    }

    private void assignSpectators() {
        Scoreboard scoreboard = world.getServer().getScoreboard();
        for (ServerPlayerEntity player: world.getEntitiesByClass(ServerPlayerEntity.class, spectatorBox, __ -> true)) {
            LOGGER.info("Assigned {} as spectator", player.getEntityName());
            scoreboard.clearPlayerTeam(player.getEntityName());
            player.changeGameMode(GameMode.SPECTATOR);
            player.sendMessage(Text.of("你已成为旁观者"));
        }
    }

    private void assignTeamsAndSpreadPlayers(SurvivalGameLogic logic) {
        world.getWorldBorder().setSize(100000);
        Scoreboard scoreboard = world.getServer().getScoreboard();
        List<ServerPlayerEntity> allPlayers = new ArrayList<>();
        for (Formatting teamColor: teamJoinBoxMap.keySet()) {
            Team team = scoreboard.getTeam(teamColor.getName());
            if (team == null) {
                team = scoreboard.addTeam(teamColor.getName());
                team.setColor(teamColor);
            }
            List<ServerPlayerEntity> players = world.getEntitiesByClass(ServerPlayerEntity.class, teamJoinBoxMap.get(teamColor), __ -> true);
            for (ServerPlayerEntity player: players) {
                LOGGER.info("Assigned {} as member of team {}", player.getEntityName(), teamColor.getName());
                scoreboard.clearPlayerTeam(player.getEntityName());
                scoreboard.addPlayerToTeam(player.getEntityName(), team);
                allPlayers.add(player);
                player.sendMessage(Text.of("你的队伍为 " + team.getColor().name()));
            }
        }
        double stepAngle = 2 * Math.PI / allPlayers.size();
        double angle = 0;
        LOGGER.info("Spreading {} players", allPlayers.size());
        allPlayers.forEach(p -> logic.getParticipants().add(p.getEntityName()));
        for (ServerPlayerEntity player: allPlayers) {
            Vec3d tpPos = initialPlayerCenter.add(
                    playerSpreadRadius * Math.cos(angle), 0, playerSpreadRadius * Math.sin(angle));
            player.teleport(tpPos.x, tpPos.y, tpPos.z);
            player.changeGameMode(GameMode.ADVENTURE);
            LOGGER.info("TPed player {} to position {}", player.getEntityName(), tpPos);
            angle += stepAngle;
        }
    }

    @Override
    public void execute(SurvivalGameLogic logic, int tickCounter) {
        assignSpectators();
        assignTeamsAndSpreadPlayers(logic);
        setRemoved(true);
    }
}
