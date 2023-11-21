package club.thunion.minigames.miniminigames;

import club.thunion.minigames.util.PropertyReader;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.server.command.CommandManager.literal;

public class walkingOnIceGameLogic {
    PlayerEntity player = walkingOnIceGameEntities.player;
    World world = walkingOnIceGameEntities.world;
    BlockState block;
    static BlockPos pos, gameEndPos;
    static Vec3d startPos, endPos, gameEndPos1;

    //PropertyReader reader;
    BlockBox area;


    BlockPos prePos;

    public void startCheck(PlayerEntity player) {
        pos = player.getSteppingPos();
        if (!pos.equals(prePos)) {
            //player.sendMessage(Text.literal("walked"));
            prePos = pos;
            changeBlock(pos);
        }
        if (pos.equals(gameEndPos)) {
            getEnd();
        }
    }

    public void changeBlock(BlockPos pos) {
        this.area = new BlockBox(-17, 71, 10, 11, 71, 16);
        //gameEndPos1=reader.readVec3d("icegame.gameEndPos");
        //if(gameEndPos1==null){
        gameEndPos1 = new Vec3d(-11, 71, 10);
        //}
        gameEndPos = new BlockPos((int) gameEndPos1.x, (int) gameEndPos1.y, (int) gameEndPos1.z);
        world = walkingOnIceGameEntities.player.getWorld();
        block = world.getBlockState(pos);
        if (block.isOf(Blocks.ICE)) {
            // 如果脚下是冰块，则将其替换为蓝冰块
            world.setBlockState(pos, Blocks.BLUE_ICE.getDefaultState());
        }
        if (block.isOf(Blocks.BLUE_ICE)) {
            gameFail();//如果脚下是蓝冰，说明走到了重复的方块上，游戏失败
        }
        if (pos == gameEndPos) {
            getEnd();//到达终点开始判断是否完成游戏
        }
    }

    public void gameFail() {
        player = walkingOnIceGameEntities.player;
        player.sendMessage(Text.literal("看上去失败了呢，不要灰心，再来一次吧"));
        walkingOnIceGameEntities.isEnabled = false;
        gameRestart();
    }

    private void gameSuccess() {
        //endPos=reader.readVec3d("icegame.endPos");
        //if (endPos==null){
        endPos = new Vec3d(-10, 71, 9);
        //}
        player.sendMessage(Text.literal("恭喜你完成走冰小游戏，现在去取你的奖励吧"));
        player.teleport(endPos.getX(), endPos.getY(), endPos.getZ());
        walkingOnIceGameEntities.isEnabled = false;
    }

    public void gameRestart() {
        startPos = walkingOnIceGameEntities.startPos;
        //area= reader.readBlockBox("icegame.areaBox");
        //if(area==null){
        //}
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        world = walkingOnIceGameEntities.player.getWorld();
        player.teleport(-17, 72, 18);
        for (int x = area.getMinX(); x <= area.getMaxX(); x++) {
            for (int z = area.getMinZ(); z <= area.getMaxZ(); z++) {
                mutablePos.set(x, area.getMaxY(), z);
                if (world.getBlockState(mutablePos).isOf(Blocks.BLUE_ICE)) {
                    world.setBlockState(mutablePos, Blocks.ICE.getDefaultState());
                }
            }
        }

    }

    public void getEnd() {
        boolean gameFail = false;
        for (int x = area.getMinX(); x <= area.getMaxX(); x++) {
            for (int z = area.getMinZ(); z <= area.getMaxZ(); z++) {
                BlockPos.Mutable mutablePos = new BlockPos.Mutable();
                mutablePos.set(x, area.getMaxY(), z);
                if (world.getBlockState(mutablePos).isOf(Blocks.ICE) && !gameFail) {
                    gameFail = true;
                    gameFail();
                }
                if (world.getBlockState(mutablePos).isOf(Blocks.BLUE_ICE)) {
                    world.setBlockState(mutablePos, Blocks.ICE.getDefaultState());
                    //LOGGER.info("get");
                }
            }
        }
        if (!gameFail) {
            gameSuccess();
        }

    }


    private static int spawnInvisibleArmorStand(World world, Vec3d position, Vec2f rotation, String name) {
        // 创建一个盔甲架实体对象，传入世界和位置参数
        Text Name = Text.literal(name);
        walkingOnIceGameEntities armorStand = new walkingOnIceGameEntities(EntityType.ARMOR_STAND, world, Name);
        // 设置盔甲架的位置
        armorStand.setPosition(position);
        world.spawnEntity(armorStand);
        return 1;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ice").executes(c -> spawnInvisibleArmorStand(c.getSource().getWorld(), c.getSource().getPosition(), c.getSource().getRotation(), "ice")));
    }
}
