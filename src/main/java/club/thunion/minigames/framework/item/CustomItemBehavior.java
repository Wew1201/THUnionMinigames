package club.thunion.minigames.framework.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class CustomItemBehavior {
    public boolean isCustom(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains("Custom");
    }

    public abstract Item getItem();

    public abstract TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity user, Hand hand);

    public boolean respectSignalScreening() {
        return true;
    }
}
