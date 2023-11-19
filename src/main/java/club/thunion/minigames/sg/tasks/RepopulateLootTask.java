package club.thunion.minigames.sg.tasks;

import club.thunion.minigames.framework.MinigameTask;
import club.thunion.minigames.sg.SurvivalGameLogic;
import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.command.LootCommand;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class RepopulateLootTask extends MinigameTask<SurvivalGameLogic> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final int repopulateInterval;
    private final int runningTicks;
    private final BlockBox repopulateArea;
    private final World world;

    public static final DyeColor RANDOM_CHEST_COLOR = DyeColor.BLACK;
    public static final DyeColor[] COLOR_BY_LOOT_TIER = new DyeColor[] {
            DyeColor.WHITE, DyeColor.LIME, DyeColor.BLUE, DyeColor.PURPLE, DyeColor.ORANGE
    };
    private final Set<BlockPos> randomizedLootChestPositions = new HashSet<>();
    private final Set<BlockPos>[] fixedTierLootChestPositions;
    private final int randomizedChestAmount;
    private final int[] fixedTierLootChestAmount;
    private final double[] unspecifiedChestTierWeight;
    private final double cumulativeWeight;

    public RepopulateLootTask(int repopulateInterval, int runningTicks, BlockBox repopulateArea, World world,
                              int randomizedChestAmount, int[] fixedTierLootChestAmount, double[] unspecifiedChestTierWeight) {
        super(0, SurvivalGameLogic.class);
        this.repopulateInterval = repopulateInterval;
        this.runningTicks = runningTicks;
        this.repopulateArea = repopulateArea;
        this.world = world;
        this.randomizedChestAmount = randomizedChestAmount;
        this.fixedTierLootChestAmount = fixedTierLootChestAmount;
        this.unspecifiedChestTierWeight = unspecifiedChestTierWeight;
        this.cumulativeWeight = Arrays.stream(unspecifiedChestTierWeight).sum();
        fixedTierLootChestPositions = new Set[COLOR_BY_LOOT_TIER.length];
        for (int i = 0; i < COLOR_BY_LOOT_TIER.length; i ++) fixedTierLootChestPositions[i] = new HashSet<>();
    }

    public static <E> void trimSetToSize(Set<E> setToTrim, int size, Consumer<E> trimmedElementCallback) {
        if (setToTrim.size() <= size) return;
        List<E> list = new ArrayList<>(setToTrim);
        Collections.shuffle(list);
        for (E element: list) {
            if (setToTrim.size() <= size) return;
            setToTrim.remove(element);
            trimmedElementCallback.accept(element);
        }
    }

    public void detectLootChests() {
        int minChunkX = repopulateArea.getMinX() >> 4;
        int maxChunkX = repopulateArea.getMaxX() >> 4;
        int minChunkZ = repopulateArea.getMinZ() >> 4;
        int maxChunkZ = repopulateArea.getMaxZ() >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx ++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz ++) {
                WorldChunk chunk = world.getChunk(cx, cz);
                Map<BlockPos, BlockEntity> blockEntityMap = chunk.getBlockEntities();
                for (BlockPos pos: blockEntityMap.keySet()) {
                    if (!repopulateArea.contains(pos)) continue;
                    if (world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock) {
                        DyeColor boxColor = ShulkerBoxBlock.getColor(world.getBlockState(pos).getBlock());
                        if (boxColor == RANDOM_CHEST_COLOR) {
                            randomizedLootChestPositions.add(pos);
                        } else {
                            for (int i = 0; i < COLOR_BY_LOOT_TIER.length; i ++) {
                                if (boxColor == COLOR_BY_LOOT_TIER[i]) fixedTierLootChestPositions[i].add(pos);
                            }
                        }
                    }
                }
            }
        }
        LOGGER.info("{} black shulkers in total", randomizedLootChestPositions.size());
        trimSetToSize(randomizedLootChestPositions, randomizedChestAmount, pos -> {
            if (world.getBlockEntity(pos) instanceof Inventory inventory) inventory.clear();
            world.removeBlock(pos, false);
        });
        for (int i = 0; i < COLOR_BY_LOOT_TIER.length; i ++) {
            LOGGER.info("{} tier {} shulkers in total", fixedTierLootChestPositions[i].size(), i + 1);
            trimSetToSize(fixedTierLootChestPositions[i], fixedTierLootChestAmount[i], pos -> {
                if (world.getBlockEntity(pos) instanceof Inventory inventory) inventory.clear();
                world.removeBlock(pos, false);
            });
        }
    }

    public void repopulateLootChests() {
        Random random = new Random();
        for (BlockPos pos: randomizedLootChestPositions) {
            Block block = world.getBlockState(pos).getBlock();
            if (block instanceof ShulkerBoxBlock) {
                DyeColor color = ShulkerBoxBlock.getColor(block);
                if (color == RANDOM_CHEST_COLOR) {
                    ShulkerBoxBlockEntity boxBE = world.getBlockEntity(pos, BlockEntityType.SHULKER_BOX).orElse(null);
                    if (boxBE != null) {
                        int tier = 0;
                        double weight = random.nextDouble() * cumulativeWeight;
                        while ((weight -= unspecifiedChestTierWeight[tier]) > 0) {
                            tier ++;
                        }
                        tier = MathHelper.clamp(tier, 0, COLOR_BY_LOOT_TIER.length - 1);
                        boxBE.clear();
                        boxBE.setLootTable(Identifier.tryParse("sg:chest_loot_tier" + (tier + 1)), random.nextLong());
                    }
                }
            }
        }
        for (int i = 0; i < COLOR_BY_LOOT_TIER.length; i ++) {
            for (BlockPos pos: fixedTierLootChestPositions[i]) {
                DyeColor tierColor = COLOR_BY_LOOT_TIER[i];
                Block block = world.getBlockState(pos).getBlock();
                if (block instanceof ShulkerBoxBlock) {
                    DyeColor color = ShulkerBoxBlock.getColor(block);
                    if (color == tierColor) {
                        ShulkerBoxBlockEntity boxBE = world.getBlockEntity(pos, BlockEntityType.SHULKER_BOX).orElse(null);
                        if (boxBE != null) {
                            boxBE.setLootTable(Identifier.tryParse("sg:chest_loot_tier" + (i + 1)), random.nextLong());
                        }
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
