package betterblockentities.mixin.core;

/* local */
import betterblockentities.client.render.immediate.blockentity.misc.LidControllerSync;

/* minecraft */
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "setServerVerifiedBlockState", at = @At("TAIL"))
    public void syncLidControllers(BlockPos blockPos, BlockState blockState, int i, CallbackInfo ci) {
        LidControllerSync.sync((ClientLevel)(Object)this, blockPos, blockState);
    }
}
