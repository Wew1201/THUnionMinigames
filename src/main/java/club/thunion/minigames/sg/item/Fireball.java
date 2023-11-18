package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class Fireball extends CustomItemBehavior {
    @Override
    public Item getItem() {
        return Items.FIRE_CHARGE;
    }

    @Override
    public TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        if (!isCustom(itemStack)) return null;
        world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            FireballEntity fireballEntity = new FireballEntity(world, user, 0D, 0D, 0D, 2);
            fireballEntity.setItem(itemStack);
            fireballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 4.0F, 1.0F);
            fireballEntity.refreshPositionAfterTeleport(fireballEntity.getPos().add(fireballEntity.getVelocity().multiply(0.4)));
            world.spawnEntity(fireballEntity);
        }
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
