package club.thunion.minigames.mixins;

import club.thunion.minigames.sg.item.ThunderTrident;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TridentEntity.class)
public abstract class TridentEntity_thunder {
    public void thunder(TridentEntity self, Entity hitEntity) {
        if (hitEntity == null) {
            List<PlayerEntity> playersNearby = self.getWorld().getEntitiesByClass(PlayerEntity.class,
                    self.getBoundingBox().expand(2), p -> p != self.getOwner());
            if (!playersNearby.isEmpty()) hitEntity = playersNearby.get(self.getWorld().random.nextInt(playersNearby.size()));
            else hitEntity = self;
        }
        Entity entity2 = self.getOwner();
        BlockPos blockPos = hitEntity.getBlockPos();
        LightningEntity lightningEntity = (LightningEntity) EntityType.LIGHTNING_BOLT.create(self.getWorld());
        if (lightningEntity != null) {
            lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(blockPos));
            lightningEntity.setChanneler(entity2 instanceof ServerPlayerEntity ? (ServerPlayerEntity) entity2 : null);
            self.getWorld().spawnEntity(lightningEntity);
            SoundEvent soundEvent = SoundEvents.ITEM_TRIDENT_THUNDER;
            self.playSound(soundEvent, 5.0F, 1.0F);
        }
        self.discard();
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    public void channelEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        TridentEntity self = (TridentEntity) (Object) this;
        if (ThunderTrident.thunderTridents.remove(self)) {
            thunder(self, entityHitResult.getEntity());
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void channelTick(CallbackInfo ci) {
        TridentEntity self = (TridentEntity) (Object) this;
        if (((PersistentProjectileEntity_accessor) self).getInGroundTicks() == 3 && ThunderTrident.thunderTridents.remove(self)) {
            thunder(self, null);
            ci.cancel();
        }
    }
}
