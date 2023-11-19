package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class DeathScythe extends CustomItemBehavior {
    public static void userMakeSacrifice(PlayerEntity user) {
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0));
    }

    public static void attractVictims(PlayerEntity user) {
        List<PlayerEntity> victims = user.getWorld().getEntitiesByClass(PlayerEntity.class
                , user.getBoundingBox().expand(7), __ -> __ != user);
        World world = user.getWorld();
        for (PlayerEntity victim: victims) {
            Vec3d targetPos = victim.getPos().add(user.getPos().multiply(2)).multiply(1.0 / 3);
            BlockPos targetPosI = new BlockPos(
                    MathHelper.floor(targetPos.x), MathHelper.floor(targetPos.y), MathHelper.floor(targetPos.z)
            );
            if (!world.getBlockState(targetPosI).isSolidBlock(world, targetPosI) &&
                    !user.getWorld().getBlockState(targetPosI.up()).isSolidBlock(world, targetPosI)) {
                victim.teleport(targetPos.x, targetPos.y, targetPos.z);
            }
        }
    }

    @Override
    public Item getItem() {
        return Items.NETHERITE_HOE;
    }

    @Override
    public TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        if (!isCustom(itemStack)) return null;
        userMakeSacrifice(user);
        attractVictims(user);
        if (!user.getAbilities().creativeMode) itemStack.damage(1500, user, __ -> {});
        return TypedActionResult.success(itemStack);
    }
}
