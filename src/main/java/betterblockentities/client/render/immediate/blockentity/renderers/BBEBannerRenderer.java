package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.WallAndGroundTransformations;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;

/* java/misc */
import java.util.function.Consumer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class BBEBannerRenderer implements BlockEntityRenderer<BannerBlockEntity, BannerRenderState> {
    private static final int MAX_PATTERNS = 16;
    private static final float SIZE = 0.6666667F;
    private static final Vector3fc MODEL_SCALE = new Vector3f(0.6666667F, -0.6666667F, -0.6666667F);
    private static final Vector3fc MODEL_TRANSLATION = new Vector3f(0.5F, 0.0F, 0.5F);
    public static final WallAndGroundTransformations<Transformation> TRANSFORMATIONS = new WallAndGroundTransformations<>(
            BBEBannerRenderer::createWallTransformation, BBEBannerRenderer::createGroundTransformation, 16
    );
    private final SpriteGetter sprites;
    private final BannerModel standingModel;
    private final BannerModel wallModel;
    private final BannerFlagModel standingFlagModel;
    private final BannerFlagModel wallFlagModel;

    public BBEBannerRenderer(final BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet(), context.sprites());
    }

    public BBEBannerRenderer(final SpecialModelRenderer.BakingContext context) {
        this(context.entityModelSet(), context.sprites());
    }

    public BBEBannerRenderer(final EntityModelSet modelSet, final SpriteGetter sprites) {
        this.sprites = sprites;
        this.standingModel = new BannerModel(modelSet.bakeLayer(ModelLayers.STANDING_BANNER));
        this.wallModel = new BannerModel(modelSet.bakeLayer(ModelLayers.WALL_BANNER));
        this.standingFlagModel = new BannerFlagModel(modelSet.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.wallFlagModel = new BannerFlagModel(modelSet.bakeLayer(ModelLayers.WALL_BANNER_FLAG));
    }

    public BannerRenderState createRenderState() {
        return new BannerRenderState();
    }

    public void extractRenderState(
            final BannerBlockEntity blockEntity,
            final BannerRenderState state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.baseColor = blockEntity.getBaseColor();
        state.patterns = blockEntity.getPatterns();
        BlockState blockState = blockEntity.getBlockState();
        if (blockState.getBlock() instanceof BannerBlock) {
            state.transformation = TRANSFORMATIONS.freeTransformations((Integer)blockState.getValue(BannerBlock.ROTATION));
            state.attachmentType = BannerBlock.AttachmentType.GROUND;
        } else {
            state.transformation = TRANSFORMATIONS.wallTransformation(blockState.getValue(WallBannerBlock.FACING));
            state.attachmentType = BannerBlock.AttachmentType.WALL;
        }

        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;
        BlockPos blockPos = blockEntity.getBlockPos();
        state.phase = ((float)Math.floorMod(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13 + gameTime, 100L) + partialTicks) / 100.0F;


        ((BlockEntityRenderStateExt)state).blockEntity(blockEntity);
    }

    private BannerModel bannerModel(final BannerBlock.AttachmentType type) {
        return switch (type) {
            case WALL -> this.wallModel;
            case GROUND -> this.standingModel;
        };
    }

    private BannerFlagModel flagModel(final BannerBlock.AttachmentType type) {
        return switch (type) {
            case WALL -> this.wallFlagModel;
            case GROUND -> this.standingFlagModel;
        };
    }

    public void submit(final BannerRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(state.transformation);

        submitBanner(
                state,
                this.sprites,
                poseStack,
                submitNodeCollector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                this.bannerModel(state.attachmentType),
                this.flagModel(state.attachmentType),
                state.phase,
                state.baseColor,
                state.patterns,
                state.breakProgress,
                0
        );
        poseStack.popPose();
    }

    private static void submitBanner(
            final BannerRenderState state,
            final SpriteGetter sprites,
            final PoseStack poseStack,
            final SubmitNodeCollector collector,
            final int lightCoords,
            final int overlayCoords,
            final BannerModel model,
            final BannerFlagModel flagModel,
            final float phase,
            final DyeColor baseColor,
            final BannerPatternLayers patterns,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress,
            final int outlineColor
    ) {
        SpriteId sprite = Sheets.BANNER_BASE;

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)state;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, Unit.INSTANCE, lightCoords, overlayCoords, outlineColor, breakProgress);
        if (!managed) {
            collector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, sprites, outlineColor, breakProgress);
        }

        float step = -0.45f;
        float rot = step * ConfigCache.bannerPose;
        float rotClamped = Math.clamp(rot, -4.05f, -0.45f);
        flagModel.root().getChild("flag").xRot = (float)Math.toRadians(rotClamped);

        boolean managed2 = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, flagModel, null, lightCoords, overlayCoords, outlineColor, breakProgress);
        if (!managed2) {
            collector.submitModel(flagModel, phase, poseStack, lightCoords, overlayCoords, -1, sprite, sprites, outlineColor, breakProgress);
        }

        if (!managed && !managed2) {
            submitPatterns(sprites, poseStack, collector, lightCoords, overlayCoords, flagModel, phase, true, baseColor, patterns, breakProgress);
        }
    }

    public static <S> void submitPatterns(
            final SpriteGetter sprites,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final int lightCoords,
            final int overlayCoords,
            final Model<S> model,
            final S state,
            final boolean banner,
            final DyeColor baseColor,
            final BannerPatternLayers patterns,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        submitPatternLayer(
                sprites,
                poseStack,
                submitNodeCollector,
                lightCoords,
                overlayCoords,
                model,
                state,
                banner ? Sheets.BANNER_PATTERN_BASE : Sheets.SHIELD_PATTERN_BASE,
                baseColor,
                breakProgress
        );

        for (int maskIndex = 0; maskIndex < 16 && maskIndex < patterns.layers().size(); maskIndex++) {
            BannerPatternLayers.Layer layer = (BannerPatternLayers.Layer)patterns.layers().get(maskIndex);
            SpriteId sprite = banner ? Sheets.getBannerSprite(layer.pattern()) : Sheets.getShieldSprite(layer.pattern());
            submitPatternLayer(sprites, poseStack, submitNodeCollector, lightCoords, overlayCoords, model, state, sprite, layer.color(), null);
        }
    }

    private static <S> void submitPatternLayer(
            final SpriteGetter sprites,
            final PoseStack poseStack,
            final SubmitNodeCollector submitNodeCollector,
            final int lightCoords,
            final int overlayCoords,
            final Model<S> model,
            final S state,
            final SpriteId sprite,
            final DyeColor color,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        int diffuseColor = color.getTextureDiffuseColor();
        submitNodeCollector.submitModel(
                model, state, poseStack, sprite.renderType(RenderTypes::bannerPattern), lightCoords, overlayCoords, diffuseColor, sprites.get(sprite), 0, breakProgress
        );
    }

    public void getExtents(final Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.standingModel.root().getExtentsForGui(poseStack, output);
        this.standingFlagModel.setupAnim(0.0F);
        this.standingFlagModel.root().getExtentsForGui(poseStack, output);
    }

    private static Transformation modelTransformation(final float angle) {
        return new Transformation(MODEL_TRANSLATION, Axis.YP.rotationDegrees(-angle), MODEL_SCALE, null);
    }

    private static Transformation createGroundTransformation(final int segment) {
        return modelTransformation(RotationSegment.convertToDegrees(segment));
    }

    private static Transformation createWallTransformation(final Direction direction) {
        return modelTransformation(direction.toYRot());
    }
}
