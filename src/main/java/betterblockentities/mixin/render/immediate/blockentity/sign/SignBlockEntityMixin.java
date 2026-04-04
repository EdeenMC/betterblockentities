package betterblockentities.mixin.render.immediate.blockentity.sign;

/* local */
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;

/* minecraft */
import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt)(Object)this;
        ext.supportedBlockEntity(true);
        ext.terrainMeshReady(true);
        ext.hasSpecialManager(true);
        ext.optKind(InstancedBlockEntityManager.OptKind.SIGN);
    }
}
