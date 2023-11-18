package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WitherBomb extends CustomItemBehavior {
    @Override
    public Item getItem() {
        return Items.WITHER_SKELETON_SKULL;
    }

    @Override
    public TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        if (!isCustom(itemStack)) return null;
        world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            float pitch = user.getPitch();
            float yaw = user.getYaw();
            float roll = 0.0f;
            float f = -MathHelper.sin(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
            float g = -MathHelper.sin((pitch + roll) * 0.017453292F);
            float h = MathHelper.cos(yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F);
            Vec3d userVelocity = user.getVelocity();
            Vec3d velocity = new Vec3d(f, g, h).add(userVelocity.x, user.isOnGround() ? 0.0 : userVelocity.y, userVelocity.z);
            Vec3d velocityNormalized = velocity.normalize();
            WitherSkullEntity skullEntity = new WitherSkullEntity(world, user, velocityNormalized.x, velocityNormalized.y, velocityNormalized.z);
            skullEntity.setVelocity(velocity);
            skullEntity.refreshPositionAfterTeleport(skullEntity.getPos().add(velocityNormalized.multiply(0.6)));
            world.spawnEntity(skullEntity);
        }
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
