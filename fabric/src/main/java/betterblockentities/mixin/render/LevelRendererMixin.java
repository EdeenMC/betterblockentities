package betterblockentities.mixin.render;

/* local */
import betterblockentities.client.BBE;

/* minecraft */
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Inject(at = @At("HEAD"), method = "cullTerrain")
    private void captureFrustum(Camera camera, Frustum frustum, boolean bl, CallbackInfo ci) {
        BBE.GlobalScope.frustum = frustum;
    }

    @Inject(at = @At("HEAD"), method = "extractLevel")
    private void updateAltRenderDispatcher(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
        BBE.GlobalScope.altRenderDispatcher.prepare(camera.position());
    }

    @Inject(at = @At("HEAD"), method = "submitBlockEntities")
    private void updateSignRenderState(CallbackInfo ci) {
        BBE.GlobalScope.limitVanillaSignRendering = true;
    }

    /*
     *  give ourselves a lower priority so we can make sure this executes before any other mixins here
    */
    @Inject(method = "submitBlockEntities", at = @At("RETURN"), order = 900)
    private void submitAltRenderers(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage submitNodeStorage, CallbackInfo ci) {
        BBE.GlobalScope.limitVanillaSignRendering = false;

        Vec3 cameraPos = levelRenderState.cameraRenderState.pos;
        double camX = cameraPos.x();
        double camY = cameraPos.y();
        double camZ = cameraPos.z();

        for (BlockEntityRenderState renderState : BBE.GlobalScope.altBlockEntityRenderStates) {
            BlockPos blockPos = renderState.blockPos;
            poseStack.pushPose();
            poseStack.translate(blockPos.getX() - camX, blockPos.getY() - camY, blockPos.getZ() - camZ);
            BBE.GlobalScope.altRenderDispatcher.submit(
                    renderState, poseStack, submitNodeStorage, levelRenderState.cameraRenderState
            );
            poseStack.popPose();
        }
    }


    @Inject(at = @At("TAIL"), method = "renderLevel")
    private void clearRenderStates(CallbackInfo ci) {
        BBE.GlobalScope.altBlockEntityRenderStates.clear();
        BBE.GlobalScope.altRenderDispatcher.clearStateRendererPairs();
    }
}
