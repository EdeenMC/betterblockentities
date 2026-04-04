package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
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
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
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

public class BBEHangingSignRenderer extends BBEAbstractSignRenderer<HangingSignRenderState> {
    private static final String PLANK = "plank";
    private static final String V_CHAINS = "vChains";
    private static final String NORMAL_CHAINS = "normalChains";
    private static final String CHAIN_L_1 = "chainL1";
    private static final String CHAIN_L_2 = "chainL2";
    private static final String CHAIN_R_1 = "chainR1";
    private static final String CHAIN_R_2 = "chainR2";
    private static final String BOARD = "board";
    private static final float MODEL_RENDER_SCALE = 1.0F;
    private static final float TEXT_RENDER_SCALE = 0.9F;
    private static final Vector3fc TEXT_OFFSET = new Vector3f(0.0F, -0.32F, 0.073F);
    public static final WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS = new WallAndGroundTransformations<>(
            BBEHangingSignRenderer::createWallTransformation, BBEHangingSignRenderer::createGroundTransformation, 16
    );
    private final Map<WoodType, BBEHangingSignRenderer.Models> signModels;

    public BBEHangingSignRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
        this.signModels = (Map<WoodType, BBEHangingSignRenderer.Models>)WoodType.values()
                .collect(ImmutableMap.toImmutableMap(type -> type, type -> BBEHangingSignRenderer.Models.create(context, type)));
    }

    public HangingSignRenderState createRenderState() {
        return new HangingSignRenderState();
    }

    public void extractRenderState(
            final SignBlockEntity blockEntity,
            final HangingSignRenderState state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.attachmentType = HangingSignBlock.getAttachmentPoint(blockState);
        if (blockState.getBlock() instanceof WallHangingSignBlock) {
            state.transformations = TRANSFORMATIONS.wallTransformation(blockState.getValue(WallHangingSignBlock.FACING));
        } else {
            state.transformations = TRANSFORMATIONS.freeTransformations((Integer)blockState.getValue(CeilingHangingSignBlock.ROTATION));
        }
    }

    public static Model.Simple createSignModel(final EntityModelSet entityModelSet, final WoodType woodType, final HangingSignBlock.Attachment attachmentType) {
        return new Model.Simple(entityModelSet.bakeLayer(ModelLayers.createHangingSignModelName(woodType, attachmentType)), RenderTypes::entityCutout);
    }

    private static Matrix4f baseTransformation(final float angle) {
        return new Matrix4f().translation(0.5F, 0.9375F, 0.5F).rotate(Axis.YP.rotationDegrees(-angle)).translate(0.0F, -0.3125F, 0.0F);
    }

    private static Transformation bodyTransformation(final float angle) {
        return new Transformation(baseTransformation(angle).scale(1.0F, -1.0F, -1.0F));
    }

    private static Transformation textTransformation(final float angle, final boolean isFrontText) {
        Matrix4f result = baseTransformation(angle);
        if (!isFrontText) {
            result.rotate(Axis.YP.rotationDegrees(180.0F));
        }

        float s = 0.0140625F;
        result.translate(TEXT_OFFSET);
        result.scale(0.0140625F, -0.0140625F, 0.0140625F);
        return new Transformation(result);
    }

    private static SignRenderState.SignTransformations createTransformations(final float angle) {
        return new SignRenderState.SignTransformations(bodyTransformation(angle), textTransformation(angle, true), textTransformation(angle, false));
    }

    private static SignRenderState.SignTransformations createGroundTransformation(final int segment) {
        return createTransformations(RotationSegment.convertToDegrees(segment));
    }

    private static SignRenderState.SignTransformations createWallTransformation(final Direction direction) {
        return createTransformations(direction.toYRot());
    }

    protected Model.Simple getSignModel(final HangingSignRenderState state) {
        return ((BBEHangingSignRenderer.Models)this.signModels.get(state.woodType)).get(state.attachmentType);
    }

    @Override
    protected SpriteId getSignSprite(final WoodType type) {
        return Sheets.getHangingSignSprite(type);
    }

    public static void submitSpecial(
            final SpriteGetter sprites,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final int lightCoords,
            final int overlayCoords,
            final Model.Simple model,
            final SpriteId sprite
    ) {
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, sprites, 0, null);
    }

    public static LayerDefinition createHangingSignLayer(final HangingSignBlock.Attachment type) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 12).addBox(-7.0F, 0.0F, -1.0F, 14.0F, 10.0F, 2.0F), PartPose.ZERO);
        if (type == HangingSignBlock.Attachment.WALL) {
            root.addOrReplaceChild("plank", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -2.0F, 16.0F, 2.0F, 4.0F), PartPose.ZERO);
        }

        if (type == HangingSignBlock.Attachment.WALL || type == HangingSignBlock.Attachment.CEILING) {
            PartDefinition normalChains = root.addOrReplaceChild("normalChains", CubeListBuilder.create(), PartPose.ZERO);
            normalChains.addOrReplaceChild(
                    "chainL1",
                    CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
                    PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
            );
            normalChains.addOrReplaceChild(
                    "chainL2",
                    CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
                    PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
            );
            normalChains.addOrReplaceChild(
                    "chainR1",
                    CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
                    PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (-Math.PI / 4), 0.0F)
            );
            normalChains.addOrReplaceChild(
                    "chainR2",
                    CubeListBuilder.create().texOffs(6, 6).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 6.0F, 0.0F),
                    PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, (float) (Math.PI / 4), 0.0F)
            );
        }

        if (type == HangingSignBlock.Attachment.CEILING_MIDDLE) {
            root.addOrReplaceChild("vChains", CubeListBuilder.create().texOffs(14, 6).addBox(-6.0F, -6.0F, 0.0F, 12.0F, 6.0F, 0.0F), PartPose.ZERO);
        }

        return LayerDefinition.create(mesh, 64, 32);
    }

    private record Models(Model.Simple ceiling, Model.Simple ceilingMiddle, Model.Simple wall) {
        public static BBEHangingSignRenderer.Models create(final BlockEntityRendererProvider.Context context, final WoodType type) {
            return new BBEHangingSignRenderer.Models(
                    BBEHangingSignRenderer.createSignModel(context.entityModelSet(), type, HangingSignBlock.Attachment.CEILING),
                    BBEHangingSignRenderer.createSignModel(context.entityModelSet(), type, HangingSignBlock.Attachment.CEILING_MIDDLE),
                    BBEHangingSignRenderer.createSignModel(context.entityModelSet(), type, HangingSignBlock.Attachment.WALL)
            );
        }

        public Model.Simple get(final HangingSignBlock.Attachment attachmentType) {
            return switch (attachmentType) {
                case CEILING -> this.ceiling;
                case CEILING_MIDDLE -> this.ceilingMiddle;
                case WALL -> this.wall;
            };
        }
    }
}
