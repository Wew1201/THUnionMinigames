package club.thunion.minigames.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosiveProjectileEntity.class)
public abstract class FireballEntity_selfDestruct extends ProjectileEntity {
    public FireballEntity_selfDestruct(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void selfDestruct(CallbackInfo ci) {
        ExplosiveProjectileEntity self = (ExplosiveProjectileEntity) (Object) this;
        if (self instanceof FireballEntity && self.getVelocity().lengthSquared() < 0.0001) {
            self.discard();
            ci.cancel();
        }
    }
}
