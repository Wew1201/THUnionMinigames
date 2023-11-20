package club.thunion.minigames.framework.item;

import club.thunion.minigames.sg.item.SignalScreener;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class CustomItemRegistry {
    public static final Map<CustomItemBehavior, Object> BEHAVIOR_TO_GROUP = new HashMap<>();
    public static final Map<Item, CustomItemBehavior> BEHAVIOR_REGISTRY = new HashMap<>();

    public static TypedActionResult<ItemStack> handleCustomItem(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        if (BEHAVIOR_REGISTRY.containsKey(itemStack.getItem())) {
            CustomItemBehavior behavior = BEHAVIOR_REGISTRY.get(itemStack.getItem());
            if (behavior.respectSignalScreening()) {
                for (Iterator<Long2ObjectMap.Entry<Box>> iterator = SignalScreener.screenedFields.long2ObjectEntrySet().iterator();
                     iterator.hasNext(); ) {
                    Map.Entry<Long, Box> entry = iterator.next();
                    if (entry.getKey() < (world).getTime()) iterator.remove();
                    else if (entry.getValue().contains(user.getPos())) return null;
                }
            }
            return behavior.tryHandle(itemStack, world, user, hand);
        }
        return null;
    }

    public static void registerCustomItem(Object group, CustomItemBehavior behavior) {
        BEHAVIOR_REGISTRY.put(behavior.getItem(), behavior);
        BEHAVIOR_TO_GROUP.put(behavior, group);
    }

    public static void removeCustomItemsInGroup(Object group) {
        for (Iterator<Item> it = BEHAVIOR_REGISTRY.keySet().iterator(); it.hasNext();) {
            Item item = it.next();
            if (BEHAVIOR_TO_GROUP.get(BEHAVIOR_REGISTRY.get(item)) == group) {
                BEHAVIOR_TO_GROUP.remove(BEHAVIOR_REGISTRY.get(item));
                it.remove();
            }
        }
    }
}
