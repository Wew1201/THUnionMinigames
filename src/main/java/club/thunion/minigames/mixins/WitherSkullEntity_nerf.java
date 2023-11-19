package club.thunion.minigames.mixins;

import net.minecraft.entity.projectile.WitherSkullEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(WitherSkullEntity.class)
public class WitherSkullEntity_nerf {

    @ModifyConstant(method = "onEntityHit", constant = @Constant(intValue = 20))
    public int nerf(int value) {
        return 2;
    }
}
