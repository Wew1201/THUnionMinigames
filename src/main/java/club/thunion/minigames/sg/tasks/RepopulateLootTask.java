package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.sg.SurvivalGameLogic;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class RepopulateLootTask extends MinigameTask<SurvivalGameLogic> {
    private final int repopulateInterval;
    private final int runningTicks;
    private final BlockBox repopulateArea;
    private final Set<BlockPos> lootChestPositions = new HashSet<>();
    private final World world;

    public final Set<DyeColor> VALID_COLORS = Set.of(DyeColor.WHITE, DyeColor.LIME, DyeColor.BLUE, DyeColor.PURPLE);

    public RepopulateLootTask(int repopulateInterval, int runningTicks, BlockBox repopulateArea, World world) {
        super(0, SurvivalGameLogic.class);
        this.repopulateInterval = repopulateInterval;
        this.runningTicks = runningTicks;
        this.repopulateArea = repopulateArea;
        this.world = world;
    }

    public void detectLootChests() {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        for (int x = repopulateArea.getMinX(); x <= repopulateArea.getMaxX(); x ++) {
            for (int y = repopulateArea.getMinY(); y <= repopulateArea.getMaxY(); y ++) {
                for (int z = repopulateArea.getMinZ(); x <= repopulateArea.getMaxZ(); z ++) {
                    mutablePos.set(x, y, z);
                    if (world.getBlockState(mutablePos).getBlock() instanceof ShulkerBoxBlock) {
                        if (VALID_COLORS.contains(ShulkerBoxBlock.getColor(world.getBlockState(mutablePos).getBlock()))) {
                            lootChestPositions.add(mutablePos.toImmutable());
                        }
                    }
                }
            }
        }
    }

    public void repopulateLootChests() {
        for (BlockPos pos: lootChestPositions) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof ShulkerBoxBlock) {
                DyeColor color = ShulkerBoxBlock.getColor(block);
                if (VALID_COLORS.contains(color)) {
                    ShulkerBoxBlockEntity boxBE = world.getBlockEntity(pos, BlockEntityType.SHULKER_BOX).orElse(null);
                    if (boxBE != null) {
                        boxBE.setLootTable(Identifier.tryParse("sg_loot_" + color.getName()), System.nanoTime() - System.currentTimeMillis());
                    }
                }
            }
        }
    }

    @Override
    public void execute(SurvivalGameLogic logic, int tickCounter) {
        if (tickCounter == 0) {
            detectLootChests();
        }
        if (tickCounter % repopulateInterval == 0) {
            repopulateLootChests();
        }
        if (tickCounter >= runningTicks) setRemoved(true);
    }
}
