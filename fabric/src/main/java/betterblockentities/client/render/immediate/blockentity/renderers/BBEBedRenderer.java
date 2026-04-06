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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
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

public class BBEBedRenderer implements BlockEntityRenderer<BedBlockEntity, BedRenderState> {
    private static final Map<Direction, Transformation> TRANSFORMATIONS = Util.makeEnumMap(Direction.class, BBEBedRenderer::createModelTransform);
    private final SpriteGetter sprites;
    private final Model.Simple headModel;
    private final Model.Simple footModel;

    public BBEBedRenderer(final BlockEntityRendererProvider.Context context) {
        this(context.sprites(), context.entityModelSet());
    }

    public BBEBedRenderer(final SpecialModelRenderer.BakingContext context) {
        this(context.sprites(), context.entityModelSet());
    }

    public BBEBedRenderer(final SpriteGetter sprites, final EntityModelSet entityModelSet) {
        this.sprites = sprites;
        this.headModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_HEAD), RenderTypes::entitySolid);
        this.footModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_FOOT), RenderTypes::entitySolid);
    }

    public BedRenderState createRenderState() {
        return new BedRenderState();
    }

    public void extractRenderState(
            final BedBlockEntity blockEntity,
            final BedRenderState state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.color = blockEntity.getColor();
        state.facing = blockEntity.getBlockState().getValue(BedBlock.FACING);
        state.part = blockEntity.getBlockState().getValue(BedBlock.PART);
        if (blockEntity.getLevel() != null) {
            DoubleBlockCombiner.NeighborCombineResult<? extends BedBlockEntity> combineResult = DoubleBlockCombiner.combineWithNeigbour(
                    BlockEntityType.BED,
                    BedBlock::getBlockType,
                    BedBlock::getConnectedDirection,
                    ChestBlock.FACING,
                    blockEntity.getBlockState(),
                    blockEntity.getLevel(),
                    blockEntity.getBlockPos(),
                    (levelAccessor, blockPos) -> false
            );
            state.lightCoords = combineResult.apply(new BrightnessCombiner<>()).get(state.lightCoords);
        }

        ((BlockEntityRenderStateExt)state).blockEntity(blockEntity);
    }

    public void submit(final BedRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        SpriteId sprite = Sheets.getBedSprite(state.color);
        poseStack.pushPose();
        poseStack.mulPose(modelTransform(state.facing));

        this.submitPiece(state, state.part, sprite, poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.breakProgress, 0);

        poseStack.popPose();
    }

    public void submitPiece(
            final BedRenderState state,
            final BedPart part,
            final SpriteId sprite,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final int lightCoords,
            final int overlayCoords,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress,
            final int outlineColor
    ) {
        Model.Simple model = this.getPieceModel(part);

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)state;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, Unit.INSTANCE, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        if (!managed) {
            submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, this.sprites, outlineColor, breakProgress);
        }
    }

    private Model.Simple getPieceModel(final BedPart part) {
        return switch (part) {
            case HEAD -> this.headModel;
            case FOOT -> this.footModel;
        };
    }

    private static Transformation createModelTransform(final Direction direction) {
        return new Transformation(
                new Matrix4f()
                        .translation(0.0F, 0.5625F, 0.0F)
                        .rotate(Axis.XP.rotationDegrees(90.0F))
                        .rotateAround(Axis.ZP.rotationDegrees(180.0F + direction.toYRot()), 0.5F, 0.5F, 0.5F)
        );
    }

    public static Transformation modelTransform(final Direction direction) {
        return (Transformation)TRANSFORMATIONS.get(direction);
    }

    public void getExtents(final BedPart part, final Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.getPieceModel(part).root().getExtentsForGui(poseStack, output);
    }
}
