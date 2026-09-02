package betterblockentities.mixin.render;

/* local */
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;
import betterblockentities.client.render.immediate.overlay.OverlayRenderer;

/* minecraft */
import betterblockentities.platform.GlobalScope;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* java/misc */
import org.joml.Matrix4fc;
import org.joml.Vector4f;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void bbe$captureFrustum(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, CameraRenderState cameraRenderState, Matrix4fc frustumMatrix, GpuBufferSlice fog, Vector4f clearColor, boolean drawSky, CallbackInfo ci) {
        GlobalScope.frustum = cameraRenderState.cullFrustum;
        GlobalScope.altRenderDispatcher.prepare(cameraRenderState.pos);
    }

    @WrapOperation(method = "submitBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
            )
    )
    private void bbe$submitBreakingOverlays(BlockEntityRenderDispatcher instance, BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, Operation<Void> original) {
        BlockEntityRenderStateExt renderStateExt = (BlockEntityRenderStateExt)state;

        if (renderStateExt.bbe$isSupportedBlockEntity() &&
            BBEConfig.OptEnabledTable.ENABLED[renderStateExt.bbe$getOptKind() & 0xFF] &&
            renderStateExt.bbe$getRenderingMode() == RenderingMode.TERRAIN &&
            renderStateExt.bbe$isTerrainMeshReady() &&
            state.breakProgress != null)
        {
            OverlayRenderer.submitCrumblingOverlay(instance, state, poseStack, camera);
            return;
        }

        original.call(instance, state, poseStack, submitNodeCollector, camera);
    }


    @Inject(method = "submitBlockEntities", at = @At("RETURN"), order = 900)
    private void bbe$submitAltRenderers(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        for (BlockEntityRenderState renderState : GlobalScope.altBlockEntityRenderStates) {
            BlockPos blockPos = renderState.blockPos;
            poseStack.pushPose();
            poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
            GlobalScope.altRenderDispatcher.submit(
                    renderState, poseStack, submitNodeCollector, levelRenderState.cameraRenderState
            );
            poseStack.popPose();
        }
    }


    @Inject(at = @At("TAIL"), method = "render")
    private void bbe$clearRenderStates(GraphicsResourceAllocator graphicsResourceAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, CameraRenderState cameraRenderState, Matrix4fc frustumMatrix, GpuBufferSlice fog, Vector4f clearColor, boolean drawSky, CallbackInfo ci) {
        GlobalScope.altBlockEntityRenderStates.clear();
        GlobalScope.altRenderDispatcher.clearStateRendererPairs();
    }
}
