package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import club.thunion.minigames.framework.item.CustomItemRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SignalScreener extends CustomItemBehavior {
    @Override
    public Item getItem() {
        return Items.CALIBRATED_SCULK_SENSOR;
    }

    @Override
    public TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        if (!isCustom(itemStack)) return null;
        Box screenedBox = user.getBoundingBox().expand(6);
        CustomItemRegistry.screenBox(screenedBox, world.getTime() + 300);
        if (!user.getAbilities().creativeMode) itemStack.decrement(1);
        return TypedActionResult.success(itemStack);
    }
}
