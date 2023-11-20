package club.thunion.minigames.mixins;

import club.thunion.minigames.sg.item.IceBomb;
import club.thunion.minigames.sg.item.SignalScreener;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;
import java.util.List;

@Mixin(SnowballEntity.class)
public abstract class SnowballEntity_iceBombAndScreener extends ThrownItemEntity {
    public SnowballEntity_iceBombAndScreener(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onCollision", at = @At("TAIL"))
    public void handleIceBombAndScreener(HitResult hitResult, CallbackInfo ci) {
        SnowballEntity self = (SnowballEntity) (Object) this;
        if (IceBomb.iceBombs.remove(self)) {
            List<PlayerEntity> playersAffected = self.getWorld().getEntitiesByClass(PlayerEntity.class, self.getBoundingBox().expand(2), __ -> true);
            for (PlayerEntity player: playersAffected) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));
            }
        } else if (SignalScreener.thrownScreeners.remove(self)) {
            Long2ObjectMap<Box> screenedFields = SignalScreener.screenedFields;
            long expiryTime = self.getWorld().getTime() + SignalScreener.EFFECTIVE_DURATION;
            while (screenedFields.containsKey(expiryTime)) expiryTime ++;
            screenedFields.put(expiryTime, self.getBoundingBox().expand(7));
            LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(self.getWorld());
            if (lightningEntity != null) {
                lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(self.getBlockPos()));
                lightningEntity.setCosmetic(true);
                self.getWorld().spawnEntity(lightningEntity);
                SoundEvent soundEvent = SoundEvents.ITEM_TRIDENT_THUNDER;
                self.playSound(soundEvent, 5.0F, 1.0F);
            }
        }
    }
}
