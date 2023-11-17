package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class DeathScythe extends CustomItemBehavior {
    public static void userMakeSacrifice(PlayerEntity user) {
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 40, 0));
        if (user.getHealth() > 6) user.damage(user.getDamageSources().magic(), 6);
    }

    public static void attractVictims(PlayerEntity user) {
        List<PlayerEntity> victims = user.getWorld().getEntitiesByClass(PlayerEntity.class
                , user.getBoundingBox().expand(8), __ -> __ != user);
        for (PlayerEntity victim: victims) {
            victim.addVelocity(user.getPos().subtract(victim.getPos()).multiply(0.2));
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
        if (!user.getAbilities().creativeMode) itemStack.damage(300, user, __ -> {});
        return TypedActionResult.success(itemStack);
    }
}
