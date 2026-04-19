package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

public class BBEBellRenderer implements BlockEntityRenderer<BellBlockEntity, BellRenderState> {
    public static final Material BELL_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("bell/bell_body");
    private final MaterialSet materials;
    private final BellModel model;

    public BBEBellRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.model = new BellModel(context.bakeLayer(ModelLayers.BELL));
    }

    public BellRenderState createRenderState() {
        return new BellRenderState();
    }

    public void extractRenderState(BellBlockEntity bellBlockEntity, BellRenderState bellRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(bellBlockEntity, bellRenderState, f, vec3, crumblingOverlay);
        bellRenderState.ticks = bellBlockEntity.ticks + f;
        bellRenderState.shakeDirection = bellBlockEntity.shaking ? bellBlockEntity.clickDirection : null;

        ((BlockEntityRenderStateExt)bellRenderState).blockEntity(bellBlockEntity);
    }

    public void submit(final BellRenderState bellRenderState, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        BellModel.State modelState = new BellModel.State(bellRenderState.ticks, bellRenderState.shakeDirection);
        this.model.setupAnim(modelState);

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)bellRenderState;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, modelState, bellRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, bellRenderState.breakProgress);
        if (!managed) {
            RenderType renderType = BELL_TEXTURE.renderType(RenderTypes::entitySolid);
            submitNodeCollector.submitModel(
                    this.model,
                    modelState,
                    poseStack,
                    renderType,
                    bellRenderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    this.materials.get(BELL_TEXTURE),
                    0,
                    bellRenderState.breakProgress
            );
        }
    }
}
