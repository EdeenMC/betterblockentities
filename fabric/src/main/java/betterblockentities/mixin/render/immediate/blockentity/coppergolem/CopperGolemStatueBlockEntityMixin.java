package betterblockentities.mixin.render.immediate.blockentity.coppergolem;

import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CopperGolemStatueBlockEntity.class)
public class CopperGolemStatueBlockEntityMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt)(Object)this;

        ext.supportedBlockEntity(true);
        ext.renderingMode(RenderingMode.TERRAIN);
        ext.terrainMeshReady(true);
        ext.optKind(InstancedBlockEntityManager.OptKind.CGS);
    }
}
