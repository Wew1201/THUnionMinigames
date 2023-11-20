package club.thunion.minigames.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static club.thunion.minigames.THUnionSurvivalGames.SERVER;

@Mixin(WitherSkullEntity.class)
public class WitherSkullEntity_nerf extends ExplosiveProjectileEntity  {

    protected WitherSkullEntity_nerf(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "onEntityHit", constant = @Constant(intValue = 20))
    public int nerfWitherDuration(int value) {
        return 4;
    }

    @Inject(method = "onCollision", at = @At(value = "TAIL"))
    public void buffWitherRange(HitResult hitResult, CallbackInfo ci) {
        WitherSkullEntity self = (WitherSkullEntity) (Object) this;
        self.getWorld().getEntitiesByClass(PlayerEntity.class, self.getBoundingBox().expand(2),
                p -> self.getOwner() == null || (SERVER.getScoreboard().getPlayerTeam(self.getOwner().getEntityName())
                        != SERVER.getScoreboard().getPlayerTeam(p.getEntityName())))
                .forEach(p -> p.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER,
                        MathHelper.clamp(MathHelper.floor(25 * (3 - p.getPos().distanceTo(self.getPos()))), 0, 70), 1)));
    }
}
