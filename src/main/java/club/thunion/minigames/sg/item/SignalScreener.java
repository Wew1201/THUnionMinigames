package club.thunion.minigames.sg.item;

import club.thunion.minigames.framework.item.CustomItemBehavior;
import club.thunion.minigames.framework.item.CustomItemRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class SignalScreener extends CustomItemBehavior {
    public static final Long2ObjectMap<Box> interferenceFields = new Long2ObjectRBTreeMap<>();
    public static final Long2ObjectMap<Box> pearlInterferenceFields = new Long2ObjectRBTreeMap<>();
    public static final Long2ObjectMap<Box> screenedFields = new Long2ObjectRBTreeMap<>();
    public static final Set<SnowballEntity> thrownScreeners = new HashSet<>();

    public static final long EFFECTIVE_DURATION = 300L;
    public static final double INTERFERENCE_RANGE = 7.0;
    public static final double PEARL_INTERFERENCE_RANGE = 21.0;
    public static final double SCREENING_RANGE = 7.0;


    @Override
    public Item getItem() {
        return Items.CALIBRATED_SCULK_SENSOR;
    }

    @Override
    public TypedActionResult<ItemStack> tryHandle(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        if (!isCustom(itemStack)) return null;
        if (user.isSneaking()) {
            SnowballEntity thrownScreener = new SnowballEntity(world, user);
            thrownScreener.setItem(new ItemStack(Items.CALIBRATED_SCULK_SENSOR, 1));
            thrownScreener.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2F, 0F);
            world.spawnEntity(thrownScreener);
            thrownScreeners.add(thrownScreener);
        } else {
            long expiryTime = world.getTime() + EFFECTIVE_DURATION;
            while (interferenceFields.containsKey(expiryTime)) expiryTime ++;
            interferenceFields.put(expiryTime, user.getBoundingBox().expand(INTERFERENCE_RANGE));
            expiryTime = world.getTime() + EFFECTIVE_DURATION;
            while (pearlInterferenceFields.containsKey(expiryTime)) expiryTime ++;
            pearlInterferenceFields.put(expiryTime, user.getBoundingBox().expand(PEARL_INTERFERENCE_RANGE));
        }
        if (!user.getAbilities().creativeMode) itemStack.decrement(1);
        return TypedActionResult.success(itemStack);
    }
}
