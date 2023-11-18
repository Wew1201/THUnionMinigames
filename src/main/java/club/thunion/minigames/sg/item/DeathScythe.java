package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class DeathScythe extends CustomItemBehavior {
    public static void userMakeSacrifice(PlayerEntity user) {
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0));
        if (user.getHealth() > 6) user.damage(user.getDamageSources().magic(), 6);
    }

    public static void attractVictims(PlayerEntity user) {
        List<PlayerEntity> victims = user.getWorld().getEntitiesByClass(PlayerEntity.class
                , user.getBoundingBox().expand(7), __ -> __ != user);
        for (PlayerEntity victim: victims) {
            Vec3d targetPos = victim.getPos().add(user.getPos().multiply(2)).multiply(1.0 / 3);
            BlockPos targetPosI = new BlockPos(
                    MathHelper.floor(targetPos.x), MathHelper.floor(targetPos.y), MathHelper.floor(targetPos.z)
            );
            if (user.getWorld().getBlockState(targetPosI).isAir() && user.getWorld().getBlockState(targetPosI.up()).isAir()) {
                victim.refreshPositionAfterTeleport(targetPos);
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
