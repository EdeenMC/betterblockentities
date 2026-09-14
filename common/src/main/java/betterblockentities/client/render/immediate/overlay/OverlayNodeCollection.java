package betterblockentities.client.render.immediate.overlay;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

public class OverlayNodeCollection extends SubmitNodeCollection {
    private final int order;
    private final OverlayNodeStorage.SubmitStack stack;

    public OverlayNodeCollection(int order, OverlayNodeStorage.SubmitStack stack) {
        super(false, new TranslucentFeatureRenderPhase());
        this.order = order;
        this.stack = stack;
    }

    @Override
    public <S> void submitModel(
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int tintedColor,
            UvMapping uvMapping,
            int outlineColor
    ) {
        // This collection only renders crumbling overlays.
    }

    @Override
    public <S> void submitCrumblingOverlay(
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int tintedColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        if (crumblingOverlay != null && renderType.affectsCrumbling()) {
            PoseStack.Pose pose = poseStack.last().copy();
            OverlayNodeStorage.SubmitParameters parameters = this.stack.last();

            OverlayNodeStorage.SubmitResolution resolution = parameters.resolver().resolve(
                    new OverlayNodeStorage.SubmitCall(this.order, model, state)
            );

            OverlayRenderer.submitCrumblingOverlay(
                    this,
                    crumblingOverlay,
                    pose,
                    model,
                    state,
                    lightCoords,
                    resolution.phase(),
                    resolution.modelStateOverride()
            );
        }
    }
}
