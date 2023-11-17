package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class ThunderTrident extends CustomItemBehavior {
    public static Set<TridentEntity> thunderTridents = new HashSet<>();

    @Override
    public Item getItem() {
        return Items.TRIDENT;
    }

    @Override
    public TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity playerEntity, Hand hand) {
        if (!isCustom(itemStack)) return null;
        TridentEntity tridentEntity = new TridentEntity(world, playerEntity, itemStack);
        tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, 2.5F, 1.0F);
        if (playerEntity.getAbilities().creativeMode) {
            tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        }
        thunderTridents.add(tridentEntity);
        world.spawnEntity(tridentEntity);
        world.playSoundFromEntity((PlayerEntity)null, tridentEntity, SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (!playerEntity.getAbilities().creativeMode) {
            playerEntity.getInventory().removeOne(itemStack);
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
