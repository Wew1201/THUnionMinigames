package club.thunion.minigames.mixins;

import club.thunion.minigames.sg.item.SignalScreener;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntity_interfere extends Entity implements Ownable {
    public ProjectileEntity_interfere(EntityType<?> type, World world) {
        super(type, world);
    }

    public Long2ObjectMap<Box> getInterferenceBoxes(ProjectileEntity self) {
        if (self instanceof EnderPearlEntity) return SignalScreener.pearlInterferenceFields;
        return SignalScreener.interferenceFields;
    }

    public boolean checkInterference(ProjectileEntity self, Long2ObjectMap<Box> interferenceBoxes) {
        Vec3d pos = self.getPos();
        World world = self.getWorld();
        long time = world.getTime();
        for (Iterator<Long2ObjectMap.Entry<Box>> entryIterator = interferenceBoxes.long2ObjectEntrySet().iterator();
             entryIterator.hasNext(); ) {
            Long2ObjectMap.Entry<Box> entry = entryIterator.next();
            if (entry.getLongKey() < time) {
                entryIterator.remove();
            }
            else if (entry.getValue().contains(pos)) return true;
        }
        return false;
    }

    public void eliminateVelocity(ProjectileEntity self) {
        if (self instanceof EnderPearlEntity) {
            self.setVelocity(0, 0, 0);
        } else {
            self.setVelocity(0, self.getVelocity().y, 0);
        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void interfere(CallbackInfo ci) {
        ProjectileEntity self = (ProjectileEntity) (Object) this;
        Long2ObjectMap<Box> interferenceBoxes = getInterferenceBoxes(self);
        if (checkInterference(self, interferenceBoxes)) {
            eliminateVelocity(self);
        }
    }
}
