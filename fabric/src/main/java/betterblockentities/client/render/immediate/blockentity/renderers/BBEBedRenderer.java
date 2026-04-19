package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;

/* java/misc */
import java.util.Map;
import java.util.function.Consumer;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class BBEBedRenderer implements BlockEntityRenderer<BedBlockEntity, BedRenderState> {
    private final MaterialSet materials;
    private final Model.Simple headModel;
    private final Model.Simple footModel;

    public BBEBedRenderer(BlockEntityRendererProvider.Context context) {
        this(context.materials(), context.entityModelSet());
    }

    public BBEBedRenderer(SpecialModelRenderer.BakingContext bakingContext) {
        this(bakingContext.materials(), bakingContext.entityModelSet());
    }

    public BBEBedRenderer(MaterialSet materialSet, EntityModelSet entityModelSet) {
        this.materials = materialSet;
        this.headModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_HEAD), RenderTypes::entitySolid);
        this.footModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_FOOT), RenderTypes::entitySolid);
    }

    public BedRenderState createRenderState() {
        return new BedRenderState();
    }

    public void extractRenderState(final BedBlockEntity bedBlockEntity, final BedRenderState bedRenderState, final float partialTicks, final Vec3 cameraPosition, final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(bedBlockEntity, bedRenderState, partialTicks, cameraPosition, crumblingOverlay);
        bedRenderState.color = bedBlockEntity.getColor();
        bedRenderState.facing = bedBlockEntity.getBlockState().getValue(BedBlock.FACING);
        bedRenderState.isHead = bedBlockEntity.getBlockState().getValue(BedBlock.PART) == BedPart.HEAD;
        if (bedBlockEntity.getLevel() != null) {
            DoubleBlockCombiner.NeighborCombineResult<? extends BedBlockEntity> neighborCombineResult = DoubleBlockCombiner.combineWithNeigbour(
                    BlockEntityType.BED,
                    BedBlock::getBlockType,
                    BedBlock::getConnectedDirection,
                    ChestBlock.FACING,
                    bedBlockEntity.getBlockState(),
                    bedBlockEntity.getLevel(),
                    bedBlockEntity.getBlockPos(),
                    (levelAccessor, blockPos) -> false
            );
            bedRenderState.lightCoords = neighborCombineResult.apply(new BrightnessCombiner<>()).get(bedRenderState.lightCoords);
        }

        ((BlockEntityRenderStateExt)bedRenderState).blockEntity(bedBlockEntity);
    }

    public void submit(BedRenderState bedRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Material material = Sheets.getBedMaterial(bedRenderState.color);
        this.submitPiece(
                bedRenderState,
                poseStack,
                submitNodeCollector,
                bedRenderState.isHead ? this.headModel : this.footModel,
                bedRenderState.facing,
                material,
                bedRenderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                false,
                bedRenderState.breakProgress,
                0
        );
    }

    public void submitPiece(final BedRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Model.Simple simple, Direction direction, Material material, int i, int j, boolean bl, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int k) {
        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)state;

        poseStack.pushPose();
        preparePose(poseStack, bl, direction);

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, simple, Unit.INSTANCE, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        if (!managed) {
            submitNodeCollector.submitModel(
                    simple, Unit.INSTANCE, poseStack, material.renderType(RenderTypes::entitySolid), i, j, -1, this.materials.get(material), k, crumblingOverlay
            );
        }

        poseStack.popPose();
    }

    private static void preparePose(PoseStack poseStack, boolean bl, Direction direction) {
        poseStack.translate(0.0F, 0.5625F, bl ? -1.0F : 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F + direction.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
    }

    private Model.Simple getPieceModel(final BedPart part) {
        return switch (part) {
            case HEAD -> this.headModel;
            case FOOT -> this.footModel;
        };
    }

    public void getExtents(final BedPart part, final Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.getPieceModel(part).root().getExtentsForGui(poseStack, output);
    }
}
