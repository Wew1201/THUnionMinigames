package club.thunion.minigames.mixins;

import club.thunion.minigames.miniminigames.walkingOnIceGameLogic;
import club.thunion.minigames.miniminigames.walkingOnIceGameEntities;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftSeverMixin {
    walkingOnIceGameLogic startCheck=new walkingOnIceGameLogic();
    @Inject(at = @At("HEAD"), method = "tick")
    private void init(CallbackInfo info) {
        if (walkingOnIceGameEntities.isEnabled == true) {
            startCheck.startCheck(walkingOnIceGameEntities.player);
        }
    }
}
