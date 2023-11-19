package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.sg.SurvivalGameLogic;
import com.mojang.logging.LogUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;

public class ShrinkBorderTask extends MinigameTask<SurvivalGameLogic> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ServerWorld world;
    private final Box initialBox;
    private final Box terminalBox;
    private Box currentBox;
    private final int shrinkingStartTick;
    private final int shrinkingTicks;
    private final int terminationTicks;

    public ShrinkBorderTask(ServerWorld world, Box initialBox, Box terminalBox, int shrinkingStartTick, int shrinkingTicks, int terminationTicks) {
        super(-20, SurvivalGameLogic.class);
        this.world = world;
        this.currentBox = this.initialBox = initialBox;
        this.terminalBox = terminalBox;
        this.shrinkingStartTick = shrinkingStartTick;
        this.shrinkingTicks = shrinkingTicks;
        this.terminationTicks = terminationTicks;
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
