package betterblockentities.client.render.immediate.overlay;

/* local */
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;
import betterblockentities.platform.GlobalScope;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

public final class OverlayRenderer {
    private OverlayRenderer() { }

    public static void submitCrumblingOverlay(BlockEntityRenderDispatcher dispatcher, BlockEntityRenderState state, PoseStack poseStack, CameraRenderState camera) {
        BlockEntityRenderStateExt renderStateExt = (BlockEntityRenderStateExt) state;

        OverlayNodeStorage.SubmitParameters parameters;

        if (renderStateExt.bbe$getOptKind() == InstancedBlockEntityManager.OptKind.BANNER) {
            parameters = new OverlayNodeStorage.SubmitParameters(
                    call -> call.model() instanceof BannerFlagModel ?
                    new OverlayNodeStorage.SubmitResolution(
                        OverlayDrawPhase.AFTER_TRANSLUCENT_TERRAIN, GlobalScope.bannerPhase
                    ) :
                    new OverlayNodeStorage.SubmitResolution(
                        OverlayDrawPhase.BEFORE_TRANSLUCENT_TERRAIN, null
                    )
            );
        } else {
            parameters = new OverlayNodeStorage.SubmitParameters(
                    call -> new OverlayNodeStorage.SubmitResolution(
                            OverlayDrawPhase.BEFORE_TRANSLUCENT_TERRAIN, null
                    )
            );
        }

        try (OverlayNodeStorage.Scope ignored = GlobalScope.overlayNodeStorage.stack.push(parameters)) {
            dispatcher.submit(state, poseStack, GlobalScope.overlayNodeStorage, camera);
        }
    }

    @SuppressWarnings("unchecked")
    public static <S> void submitCrumblingOverlay(
            OverlayNodeCollection collection,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            PoseStack.Pose pose,
            Model<S> model,
            S state,
            int lightCoords,
            OverlayDrawPhase phase,
            Object modelStateOverride
    ) {
        S resolvedState = modelStateOverride == null ? state : (S) modelStateOverride;

        SimpleFeatureRenderPhase renderPhase = switch (phase) {
            case AFTER_TRANSLUCENT_TERRAIN -> collection.afterTerrain;
            case BEFORE_TRANSLUCENT_TERRAIN -> collection.breakingOverlay;
        };

        RenderType crumblingRenderType = ModelBakery.DESTROY_TYPES.get(crumblingOverlay.progress());

        ModelFeatureRenderer.Submit<?> submit = new ModelFeatureRenderer.Submit<>(
                crumblingRenderType,
                pose,
                model,
                resolvedState,
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                crumblingOverlay.cameraPose()
        );

        renderPhase.submit(submit);
    }
}
