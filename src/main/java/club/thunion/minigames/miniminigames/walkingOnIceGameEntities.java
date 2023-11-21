package club.thunion.minigames.miniminigames;

import club.thunion.minigames.util.PropertyReader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static com.mojang.text2speech.Narrator.LOGGER;

public class walkingOnIceGameEntities extends ArmorStandEntity {
    public static PlayerEntity player;
    public static World world;
    static Vec3d startPos;
    //PropertyReader reader;
    Vec3d gameStartPos;//=reader.readVec3d("icegame.gameStartPos");
    public static boolean isEnabled = false;

    // 构造方法，调用父类的构造方法，并设置一些属性
    public walkingOnIceGameEntities(EntityType<? extends ArmorStandEntity> entityType, World world, Text name) {
        super(entityType, world);
        this.setInvisible(true); // 设置为隐形
        this.setInvulnerable(true); // 设置为无敌
        this.setNoGravity(true); // 设置为无重力
        this.setCustomNameVisible(true);//显示文本
        this.setCustomName(Text.literal("ice"));
    }

    // 重写交互方法，当玩家右键点击该盔甲架时触发
    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        //if(this.getCustomName() == Text.literal("ice")) {
        gameStartPos = new Vec3d(-17, 72, 16);
        if (!player.isSneaking()) {
            player.sendMessage(Text.literal("oops")); // 记录点击的玩家
            //LOGGER.info("punch");
            this.player = player;
            this.world = player.getWorld();
            isEnabled = true;//开始统计
            startPos = hitPos;
            player.teleport(gameStartPos.x + 0.5, gameStartPos.y, gameStartPos.z + 0.5);
            player.sendMessage(Text.literal("§b走冰挑战开始，不重复地走过每一个冰块吧"));
            //}
            return ActionResult.SUCCESS; // 返回成功的结果
        }
        return ActionResult.FAIL;
    }

}
