package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.sg.SurvivalGameLogic;
import club.thunion.minigames.util.ChatHelper;
import club.thunion.minigames.util.Milestone;
import com.mojang.logging.LogUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;

import static club.thunion.minigames.THUnionSurvivalGames.SERVER;

public class ShrinkBorderTask extends MinigameTask<SurvivalGameLogic> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ServerWorld world;
    private final Box initialBox;
    private final Box terminalBox;
    private Box currentBox;
    private final int shrinkingStartTick;
    private final int shrinkingTicks;
    private final int terminationTicks;

    private final Milestone milestone;

    public ShrinkBorderTask(ServerWorld world, Box initialBox, Box terminalBox, int shrinkingStartTick, int shrinkingTicks, int terminationTicks) {
        super(-20, SurvivalGameLogic.class);
        this.world = world;
        this.currentBox = this.initialBox = initialBox;
        this.terminalBox = terminalBox;
        this.shrinkingStartTick = shrinkingStartTick;
        this.shrinkingTicks = shrinkingTicks;
        this.terminationTicks = terminationTicks;
        this.milestone = new Milestone()
                .addKeyPoint(shrinkingStartTick, () -> ChatHelper.broadcast(SERVER, Text.of("开始缩圈!")))
                .addKeyPoint(shrinkingStartTick + shrinkingTicks, () -> ChatHelper.broadcast(SERVER, Text.of("缩圈结束")));

        for (int time = 1200; time < shrinkingStartTick; time += 1200) {
            int finalTime = time;
            this.milestone.addKeyPoint(
                    shrinkingStartTick - time,
                    () -> ChatHelper.broadcast(SERVER, Text.of("缩圈开始倒计时： " + finalTime / 1200 + "分钟")));
        }

        for (int time = 1200; time < terminationTicks; time += 1200) {
            int finalTime = time;
            if (finalTime / 1200 > 5) {
                break;
            }
            this.milestone.addKeyPoint(
                    terminationTicks - time,
                    () -> ChatHelper.broadcast(SERVER, Text.of("结束倒计时： " + finalTime / 1200 + "分钟")));
        }

        for (int time = 100; time < terminationTicks; time += 100) {
            int finalTime = time;
            if (finalTime / 100 > 12) {
                break;
            }
            this.milestone.addKeyPoint(
                    terminationTicks - time,
                    () -> ChatHelper.broadcast(SERVER, Text.of("结束倒计时： " + finalTime / 20 + "秒")));
        }
    }

    public void updateWorldBorder() {
        double sideLength = Math.min(currentBox.getXLength(), currentBox.getZLength());
        Vec3d center = currentBox.getCenter();
        world.getWorldBorder().setCenter(center.x, center.z);
        world.getWorldBorder().setSize(sideLength);
        world.getWorldBorder().setDamagePerBlock(1);
    }

    public void disqualifyPlayers(SurvivalGameLogic logic) {
        for (ServerPlayerEntity player: world.getPlayers(p -> logic.getParticipants().contains(p.getEntityName()))) {
            if (!currentBox.contains(player.getPos())) {
                world.getScoreboard().clearPlayerTeam(player.getEntityName());
                player.changeGameMode(GameMode.SPECTATOR);
                logic.getParticipants().remove(player.getEntityName());
            }
        }
    }

    @Override
    public void execute(SurvivalGameLogic logic, int tickCounter) {
        milestone.update(tickCounter);
        if (tickCounter > shrinkingStartTick) {
            double progress = (double) (tickCounter - shrinkingStartTick) / shrinkingTicks;
            if (progress <= 1) {
                currentBox = new Box(
                        MathHelper.lerp(progress, initialBox.minX, terminalBox.minX),
                        initialBox.minY, // No more vertical boundary
                        MathHelper.lerp(progress, initialBox.minZ, terminalBox.minZ),
                        MathHelper.lerp(progress, initialBox.maxX, terminalBox.maxX),
                        initialBox.maxY,
                        MathHelper.lerp(progress, initialBox.maxZ, terminalBox.maxZ)
                );
            }
        }
        updateWorldBorder();
        if (tickCounter > 0) {
            disqualifyPlayers(logic);
        }
        if (tickCounter >= terminationTicks) {
            this.setRemoved(true);
        }
    }

    public Box getCurrentBox() {
        return currentBox;
    }
}
