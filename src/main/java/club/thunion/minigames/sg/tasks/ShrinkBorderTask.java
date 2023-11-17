package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.THUnionSurvivalGames;
import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.mixins.BlockDisplayEntity_accessor;
import club.thunion.minigames.mixins.DisplayEntity_accessor;
import club.thunion.minigames.sg.SurvivalGameLogic;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.List;

public class ShrinkBorderTask extends MinigameTask<SurvivalGameLogic> {
    private boolean initialized = false;
    private final EnumMap<Direction, DisplayEntity.BlockDisplayEntity> borderDisplays = new EnumMap<>(Direction.class);
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

    public static void adaptDisplayToWallOfBox(DisplayEntity.BlockDisplayEntity display, Box box, Direction side) {
        float x = 1f, y = 1f, z = 1f;
        double xSize = box.getXLength() + 2;
        double ySize = box.getYLength() + 2;
        double zSize = box.getZLength() + 2;
        ((BlockDisplayEntity_accessor) display).invokeSetBlockState(side.getAxis() == Direction.Axis.Y ?
                Blocks.WHITE_STAINED_GLASS.getDefaultState() : Blocks.WHITE_CONCRETE.getDefaultState());
        if (side.getAxis() != Direction.Axis.X) x = (float) xSize;
        if (side.getAxis() != Direction.Axis.Y) y = (float) ySize;
        if (side.getAxis() != Direction.Axis.Z) z = (float) zSize;
        ((DisplayEntity_accessor) (DisplayEntity) display).invokeSetTransformation(new AffineTransformation(
                null, null, new Vector3f(x, y, z), null
        ));
        double px = box.minX - 1, py = box.minY - 1, pz = box.minZ - 1;
        if (side == Direction.UP) py = box.maxY;
        if (side == Direction.EAST) px = box.maxX;
        if (side == Direction.SOUTH) pz = box.maxZ;
        display.setPosition(px, py, pz);
    }

    public void killPlayersOutsideBorder(List<ServerPlayerEntity> participants) {
        for (int i = participants.size() - 1; i >= 0; i --) {
            ServerPlayerEntity player = participants.get(i);
            if (!this.currentBox.contains(player.getPos())) {
                participants.remove(i);
                player.kill();
            }
        }
    }

    private void initializeDisplayEntities() {
        double maxSide = Math.max(initialBox.getXLength(), Math.max(initialBox.getYLength(), initialBox.getZLength()));
        for (Direction dir: Direction.values()) {
            DisplayEntity.BlockDisplayEntity display = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            adaptDisplayToWallOfBox(display, initialBox, dir);
            world.spawnEntity(display);
            borderDisplays.put(dir, display);
        }
    }

    @Override
    public void execute(SurvivalGameLogic logic, int tickCounter) {
        if (!this.initialized) {
            this.initializeDisplayEntities();
        }
        this.killPlayersOutsideBorder(logic.getParticipants());
        if (tickCounter > shrinkingStartTick) {
            double progress = (double) (tickCounter - shrinkingStartTick) / shrinkingTicks;
            if (progress <= 1) {
                Box currentBox = new Box(
                        MathHelper.lerp(progress, initialBox.minX, terminalBox.minX),
                        MathHelper.lerp(progress, initialBox.minY, terminalBox.minY),
                        MathHelper.lerp(progress, initialBox.minZ, terminalBox.minZ),
                        MathHelper.lerp(progress, initialBox.maxX, terminalBox.maxX),
                        MathHelper.lerp(progress, initialBox.maxY, terminalBox.maxY),
                        MathHelper.lerp(progress, initialBox.maxZ, terminalBox.maxZ)
                );
                for (Direction dir : Direction.values())
                    adaptDisplayToWallOfBox(borderDisplays.get(dir), currentBox, dir);
            }
        }
        if (tickCounter >= terminationTicks) {
            borderDisplays.values().forEach(Entity::discard);
            this.setRemoved(true);
        }
    }

    public Box getCurrentBox() {
        return currentBox;
    }
}
