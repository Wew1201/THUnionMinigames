package club.thunion.minigames.mixins;

import club.thunion.minigames.sg.item.IceBomb;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SnowballEntity.class)
public abstract class SnowballEntity_iceBomb extends ThrownItemEntity {
    public SnowballEntity_iceBomb(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onCollision", at = @At("TAIL"))
    public void handleIceBomb(HitResult hitResult, CallbackInfo ci) {
        SnowballEntity self = (SnowballEntity) (Object) this;
        if (IceBomb.iceBombs.remove(self)) {
            List<PlayerEntity> playersAffected = self.getWorld().getEntitiesByClass(PlayerEntity.class, self.getBoundingBox().expand(2), __ -> true);
            for (PlayerEntity player: playersAffected) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));
            }
        }
    }
}
