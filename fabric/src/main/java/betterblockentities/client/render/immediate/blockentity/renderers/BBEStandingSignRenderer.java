package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import betterblockentities.client.BBE;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.WallAndGroundTransformations;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;

/* java/misc */
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import java.util.Map;
import com.google.common.collect.ImmutableMap;

public class BBEStandingSignRenderer extends BBEAbstractSignRenderer<StandingSignRenderState> {
    private static final Vector3fc TEXT_OFFSET = new Vector3f(0.0F, 0.33333334F, 0.046666667F);
    public static final WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS = new WallAndGroundTransformations<>(
            BBEStandingSignRenderer::createWallTransformation, BBEStandingSignRenderer::createGroundTransformation, 16
    );
    private final Map<WoodType, BBEStandingSignRenderer.Models> signModels;

    public BBEStandingSignRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);

        /* filter out invalid sign types to prevent crashes, types in general are irrelevant in our render context */
        this.signModels = WoodType.values()
                .<Map.Entry<WoodType, BBEStandingSignRenderer.Models>>mapMulti((woodType, consumer) -> {
                    BBEStandingSignRenderer.Models models = BBEStandingSignRenderer.Models.create(context, woodType);

                    if (models.standing() != null && models.wall() != null) {
                        consumer.accept(Map.entry(woodType, models));
                    }
                })
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public StandingSignRenderState createRenderState() {
        return new StandingSignRenderState();
    }

    public void extractRenderState(final SignBlockEntity blockEntity, final StandingSignRenderState state, final float partialTicks, final Vec3 cameraPosition, final ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.attachmentType = PlainSignBlock.getAttachmentPoint(blockState);
        if (blockState.getBlock() instanceof WallSignBlock) {
            state.transformations = TRANSFORMATIONS.wallTransformation(blockState.getValue(WallSignBlock.FACING));
        } else {
            state.transformations = TRANSFORMATIONS.freeTransformations((Integer)blockState.getValue(StandingSignBlock.ROTATION));
        }
    }

    protected Model.Simple getSignModel(final StandingSignRenderState state) {
        return ((BBEStandingSignRenderer.Models)this.signModels.get(state.woodType)).get(state.attachmentType);
    }

    @Override
    protected SpriteId getSignSprite(final WoodType type) {
        return Sheets.getSignSprite(type);
    }

    private static Matrix4f baseTransformation(final float angle, final PlainSignBlock.Attachment attachmentType) {
        Matrix4f result = new Matrix4f().translate(0.5F, 0.5F, 0.5F).rotate(Axis.YP.rotationDegrees(-angle));
        if (attachmentType == PlainSignBlock.Attachment.WALL) {
            result.translate(0.0F, -0.3125F, -0.4375F);
        }

        return result;
    }

    private static Transformation bodyTransformation(final PlainSignBlock.Attachment attachmentType, final float angle) {
        return new Transformation(baseTransformation(angle, attachmentType).scale(0.6666667F, -0.6666667F, -0.6666667F));
    }

    private static Transformation textTransformation(final PlainSignBlock.Attachment attachmentType, final float angle, final boolean isFrontText) {
        Matrix4f result = baseTransformation(angle, attachmentType);
        if (!isFrontText) {
            result.rotate(Axis.YP.rotationDegrees(180.0F));
        }

        float s = 0.010416667F;
        return new Transformation(result.translate(TEXT_OFFSET).scale(0.010416667F, -0.010416667F, 0.010416667F));
    }

    private static SignRenderState.SignTransformations createTransformations(final PlainSignBlock.Attachment attachmentType, final float angle) {
        return new SignRenderState.SignTransformations(
                bodyTransformation(attachmentType, angle), textTransformation(attachmentType, angle, true), textTransformation(attachmentType, angle, false)
        );
    }

    private static SignRenderState.SignTransformations createGroundTransformation(final int segment) {
        return createTransformations(PlainSignBlock.Attachment.GROUND, RotationSegment.convertToDegrees(segment));
    }

    private static SignRenderState.SignTransformations createWallTransformation(final Direction direction) {
        return createTransformations(PlainSignBlock.Attachment.WALL, direction.toYRot());
    }

    public static Model.Simple createSignModel(final EntityModelSet entityModelSet, final WoodType woodType, final PlainSignBlock.Attachment attachment) {
        ModelLayerLocation layer = switch (attachment) {
            case GROUND -> createStandingSignModelName(woodType);
            case WALL -> createWallSignModelName(woodType);
        };

        if (layer != null) {
            return new Model.Simple(entityModelSet.bakeLayer(layer), RenderTypes::entityCutout);
        }
        return null;
    }

    public static ModelLayerLocation createStandingSignModelName(final WoodType type) {
        return createLocation("sign/standing/" + type.name(), "main");
    }

    public static ModelLayerLocation createWallSignModelName(final WoodType type) {
        return createLocation("sign/wall/" + type.name(), "main");
    }

    private static ModelLayerLocation createLocation(final String model, final String layerId) {
        ModelLayerLocation layer;
        try {
            layer = new ModelLayerLocation(Identifier.withDefaultNamespace(model), layerId);
            return layer;
        } catch (Exception e) {
            BBE.getLogger().error("Error creating model for {}", model);
            return null;
        }
    }

    private record Models(Model.Simple standing, Model.Simple wall) {
        public static BBEStandingSignRenderer.Models create(final BlockEntityRendererProvider.Context context, final WoodType type) {
            return new BBEStandingSignRenderer.Models(
                    BBEStandingSignRenderer.createSignModel(context.entityModelSet(), type, PlainSignBlock.Attachment.GROUND),
                    BBEStandingSignRenderer.createSignModel(context.entityModelSet(), type, PlainSignBlock.Attachment.WALL)
            );
        }

        public Model.Simple get(final PlainSignBlock.Attachment attachmentType) {
            return switch (attachmentType) {
                case GROUND -> this.standing;
                case WALL -> this.wall;
            };
        }
    }
}
