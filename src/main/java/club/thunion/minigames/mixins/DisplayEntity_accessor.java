package club.thunion.minigames.mixins;

import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.AffineTransformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DisplayEntity.class)
public interface DisplayEntity_accessor {
    @Invoker("setTransformation")
    void invokeSetTransformation(AffineTransformation transformation);
}
