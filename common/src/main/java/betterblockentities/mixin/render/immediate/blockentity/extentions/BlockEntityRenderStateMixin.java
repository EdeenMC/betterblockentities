package betterblockentities.mixin.render.immediate.blockentity.extentions;

/* local */
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;

/* minecraft */
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderState.class)
public class BlockEntityRenderStateMixin implements BlockEntityRenderStateExt {
    @Unique private RenderingMode renderingMode = RenderingMode.TERRAIN;
    @Unique private boolean terrainMeshReady = true;
    @Unique private boolean hasSpecialManager = false;
    @Unique private byte bbeKind = 0;
    @Unique private boolean supportedBlockEntity = false;

    @Inject(method = "extractBase", at = @At("TAIL"))
    private static void bbe$fillBaseState(BlockEntity blockEntity, BlockEntityRenderState state, ModelFeatureRenderer.CrumblingOverlay breakProgress, CallbackInfo ci) {
        BlockEntityRenderStateExt renderStateExt = (BlockEntityRenderStateExt)state;
        BlockEntityExt blockEntityExt = (BlockEntityExt)blockEntity;
        renderStateExt.bbe$setRenderingMode(blockEntityExt.bbe$getRenderingMode());
        renderStateExt.bbe$setTerrainMeshReady(blockEntityExt.bbe$isTerrainMeshReady());
        renderStateExt.bbe$setSpecialManager(blockEntityExt.bbe$hasSpecialManager());
        renderStateExt.bbe$setOptKind(blockEntityExt.bbe$getOptKind());
        renderStateExt.bbe$setSupportedBlockEntity(blockEntityExt.bbe$isSupportedBlockEntity());
    }

    @Override
    public boolean bbe$isSupportedBlockEntity() {
        return supportedBlockEntity;
    }

    @Override
    public void bbe$setSupportedBlockEntity(boolean bl) {
        supportedBlockEntity = bl;
    }

    @Override
    public RenderingMode bbe$getRenderingMode() {
        return renderingMode;
    }

    @Override
    public void bbe$setRenderingMode(RenderingMode mode) {
        renderingMode = mode;
    }

    @Override
    public boolean bbe$isTerrainMeshReady() {
        return terrainMeshReady;
    }

    @Override
    public void bbe$setTerrainMeshReady(boolean b) {
        terrainMeshReady = b;
    }

    @Override
    public boolean bbe$hasSpecialManager() {
        return hasSpecialManager;
    }

    @Override
    public void bbe$setSpecialManager(boolean bl) {
        hasSpecialManager = bl;
    }

    @Override
    public byte bbe$getOptKind() {
        return bbeKind;
    }

    @Override
    public void bbe$setOptKind(byte k) {
        bbeKind = k;
    }
}
