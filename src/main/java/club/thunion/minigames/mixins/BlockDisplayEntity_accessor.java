package club.thunion.minigames.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DisplayEntity.BlockDisplayEntity.class)
public interface BlockDisplayEntity_accessor {
    @Invoker("setBlockState")
    void invokeSetBlockState(BlockState state);
}
