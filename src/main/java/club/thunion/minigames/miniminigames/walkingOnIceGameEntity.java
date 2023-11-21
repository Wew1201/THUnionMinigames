package club.thunion.minigames.miniminigames;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public interface walkingOnIceGameEntity {
    ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand);
}
