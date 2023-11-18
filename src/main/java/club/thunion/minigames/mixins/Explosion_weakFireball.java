package club.thunion.minigames.mixins;

import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public abstract class Explosion_weakFireball {
    private static final double KNOCKBACK_AMPLIFICATION = 4.0;
    private static final double DAMAGE_REDUCTION = 1.0 / 7;

    @ModifyConstant(method = "collectBlocksAndDamageEntities", constant = @Constant(doubleValue = 7.0))
    public double weakenFireballs(double constant) {
        Explosion self = (Explosion) (Object) this;
        return self.getEntity() instanceof FireballEntity ? (constant * DAMAGE_REDUCTION) : constant;
    }

    @Redirect(method = "collectBlocksAndDamageEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/ProtectionEnchantment;transformExplosionKnockback(Lnet/minecraft/entity/LivingEntity;D)D"))
    public double amplifyKnockback(LivingEntity entity, double velocity) {
        Explosion self = (Explosion) (Object) this;
        double transformedKB = ProtectionEnchantment.transformExplosionKnockback(entity, velocity);
        if (self.getEntity() instanceof FireballEntity) return transformedKB * KNOCKBACK_AMPLIFICATION;
        else return transformedKB;
    }
}
