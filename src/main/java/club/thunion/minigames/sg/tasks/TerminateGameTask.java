package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.sg.SurvivalGameLogic;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static club.thunion.minigames.THUnionSurvivalGames.SERVER;

public class TerminateGameTask extends MinigameTask<SurvivalGameLogic> {
    public TerminateGameTask() {
        super(-10, SurvivalGameLogic.class);
    }

    private void setAllPlayersToSpectators(SurvivalGameLogic logic) {
        List<String> participantNames = logic.getParticipants();
        SERVER.getOverworld().getPlayers(p -> participantNames.contains(p.getEntityName())).forEach(p -> {
            p.changeGameMode(GameMode.SPECTATOR);
            SERVER.getScoreboard().clearPlayerTeam(p.getEntityName());
        });
    }

    @Override
    public void execute(SurvivalGameLogic logic, int tickCounter) {
        if (logic.getTask(SurvivalGameLogic.SHRINK_BORDER_TASK_ID) == null) {
            logic.running = false;
            SERVER.getOverworld().getPlayers().forEach(player -> {
                try {
                    player.networkHandler.sendPacket(
                            new TitleS2CPacket(Texts.parse(player.getCommandSource(), Text.of("游戏超时结束"), player, 0)));
                } catch (CommandSyntaxException ignore) {}
            });
        }
        List<String> participantNames = logic.getParticipants();
        if (participantNames.stream().map(SERVER.getScoreboard()::getPlayerTeam).filter(Objects::nonNull).collect(Collectors.toSet()).size() == 1) {
            Team team = SERVER.getScoreboard().getPlayerTeam(participantNames.get(0));
            assert team != null;
            Text text = Text.of("队伍 §" + team.getColor().getCode() + team.getName() + " §r 获得了胜利");
            logic.running = false;
            SERVER.getOverworld().getPlayers().forEach(player -> {
                try {
                    player.networkHandler.sendPacket(
                            new TitleS2CPacket(Texts.parse(player.getCommandSource(), text, player, 0)));
                } catch (CommandSyntaxException ignore) {}
            });
        }
    }
}
