package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.statue.CopperGolemStatueModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.CopperGolemStatueRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevels;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* java/misc */
import java.util.HashMap;
import java.util.Map;

public class BBECopperGolemStatueBlockRenderer implements BlockEntityRenderer<CopperGolemStatueBlockEntity, CopperGolemStatueRenderState> {
    private final Map<CopperGolemStatueBlock.Pose, CopperGolemStatueModel> models = new HashMap();

    public BBECopperGolemStatueBlockRenderer(BlockEntityRendererProvider.Context context) {
        EntityModelSet entityModelSet = context.entityModelSet();
        this.models.put(CopperGolemStatueBlock.Pose.STANDING, new CopperGolemStatueModel(entityModelSet.bakeLayer(ModelLayers.COPPER_GOLEM)));
        this.models.put(CopperGolemStatueBlock.Pose.RUNNING, new CopperGolemStatueModel(entityModelSet.bakeLayer(ModelLayers.COPPER_GOLEM_RUNNING)));
        this.models.put(CopperGolemStatueBlock.Pose.SITTING, new CopperGolemStatueModel(entityModelSet.bakeLayer(ModelLayers.COPPER_GOLEM_SITTING)));
        this.models.put(CopperGolemStatueBlock.Pose.STAR, new CopperGolemStatueModel(entityModelSet.bakeLayer(ModelLayers.COPPER_GOLEM_STAR)));
    }

    public CopperGolemStatueRenderState createRenderState() {
        return new CopperGolemStatueRenderState();
    }

    public void extractRenderState(CopperGolemStatueBlockEntity copperGolemStatueBlockEntity, CopperGolemStatueRenderState copperGolemStatueRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(copperGolemStatueBlockEntity, copperGolemStatueRenderState, f, vec3, crumblingOverlay);
        copperGolemStatueRenderState.direction = copperGolemStatueBlockEntity.getBlockState().getValue(CopperGolemStatueBlock.FACING);
        copperGolemStatueRenderState.pose = copperGolemStatueBlockEntity.getBlockState().getValue(BlockStateProperties.COPPER_GOLEM_POSE);

        ((BlockEntityRenderStateExt)copperGolemStatueRenderState).blockEntity(copperGolemStatueBlockEntity);
    }

    public void submit(final CopperGolemStatueRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        if (state.blockState.getBlock() instanceof CopperGolemStatueBlock block) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 0.0F, 0.5F);
            CopperGolemStatueModel model = this.models.get(state.pose);
            Direction direction = state.direction;
            RenderType renderType = RenderTypes.entityCutoutNoCull(CopperGolemOxidationLevels.getOxidationLevel(block.getWeatheringState()).texture());

            BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)state;

            boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, state.direction, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
            if (!managed) {
                submitNodeCollector.submitModel(
                        model,
                        direction,
                        poseStack,
                        renderType,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0,
                        state.breakProgress
                );
            }

            poseStack.popPose();
        }
    }
}
