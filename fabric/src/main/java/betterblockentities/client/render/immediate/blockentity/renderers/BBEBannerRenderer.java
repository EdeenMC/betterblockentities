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
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.ModelBakery;
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
import org.jspecify.annotations.Nullable;

public class BBEBannerRenderer implements BlockEntityRenderer<BannerBlockEntity, BannerRenderState> {
    private static final int MAX_PATTERNS = 16;
    private static final float SIZE = 0.6666667F;
    private final MaterialSet materials;
    private final BannerModel standingModel;
    private final BannerModel wallModel;
    private final BannerFlagModel standingFlagModel;
    private final BannerFlagModel wallFlagModel;

    public BBEBannerRenderer(BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet(), context.materials());
    }

    public BBEBannerRenderer(SpecialModelRenderer.BakingContext bakingContext) {
        this(bakingContext.entityModelSet(), bakingContext.materials());
    }

    public BBEBannerRenderer(EntityModelSet entityModelSet, MaterialSet materialSet) {
        this.materials = materialSet;
        this.standingModel = new BannerModel(entityModelSet.bakeLayer(ModelLayers.STANDING_BANNER));
        this.wallModel = new BannerModel(entityModelSet.bakeLayer(ModelLayers.WALL_BANNER));
        this.standingFlagModel = new BannerFlagModel(entityModelSet.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
        this.wallFlagModel = new BannerFlagModel(entityModelSet.bakeLayer(ModelLayers.WALL_BANNER_FLAG));
    }

    public BannerRenderState createRenderState() {
        return new BannerRenderState();
    }

    public void extractRenderState(BannerBlockEntity bannerBlockEntity, BannerRenderState bannerRenderState, float f, Vec3 vec3, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(bannerBlockEntity, bannerRenderState, f, vec3, crumblingOverlay);
        bannerRenderState.baseColor = bannerBlockEntity.getBaseColor();
        bannerRenderState.patterns = bannerBlockEntity.getPatterns();
        BlockState blockState = bannerBlockEntity.getBlockState();
        if (blockState.getBlock() instanceof BannerBlock) {
            bannerRenderState.angle = -RotationSegment.convertToDegrees((Integer)blockState.getValue(BannerBlock.ROTATION));
            bannerRenderState.standing = true;
        } else {
            bannerRenderState.angle = -((Direction)blockState.getValue(WallBannerBlock.FACING)).toYRot();
            bannerRenderState.standing = false;
        }

        long l = bannerBlockEntity.getLevel() != null ? bannerBlockEntity.getLevel().getGameTime() : 0L;
        BlockPos blockPos = bannerBlockEntity.getBlockPos();
        bannerRenderState.phase = ((float)Math.floorMod((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l, 100L) + f) / 100.0F;

        ((BlockEntityRenderStateExt)bannerRenderState).blockEntity(bannerBlockEntity);
    }

    public void submit(BannerRenderState bannerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        BannerModel bannerModel;
        BannerFlagModel bannerFlagModel;
        if (bannerRenderState.standing) {
            bannerModel = this.standingModel;
            bannerFlagModel = this.standingFlagModel;
        } else {
            bannerModel = this.wallModel;
            bannerFlagModel = this.wallFlagModel;
        }

        submitBanner(bannerRenderState, this.materials, poseStack, submitNodeCollector, bannerRenderState.lightCoords, OverlayTexture.NO_OVERLAY, bannerRenderState.angle, bannerModel, bannerFlagModel, bannerRenderState.phase, bannerRenderState.baseColor, bannerRenderState.patterns, bannerRenderState.breakProgress, 0);
    }

    private static void submitBanner(BannerRenderState bannerRenderState, MaterialSet materialSet, PoseStack poseStack, SubmitNodeCollector collector, int i, int j, float f, BannerModel bannerModel, BannerFlagModel bannerFlagModel, float g, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int k) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(f));
        poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);

        Material material = ModelBakery.BANNER_BASE;

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)bannerRenderState;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, bannerModel, Unit.INSTANCE, i, j, k, crumblingOverlay);
        if (!managed) {
            collector.submitModel(bannerModel, Unit.INSTANCE, poseStack, material.renderType(RenderTypes::entitySolid), i, j, -1, materialSet.get(material), k, crumblingOverlay);
        }

        float step = -0.45f;
        float rot = step * ConfigCache.bannerPose;
        float rotClamped = Math.clamp(rot, -4.05f, -0.45f);
        bannerFlagModel.root().getChild("flag").xRot = (float)Math.toRadians(rotClamped);

        boolean managed2 = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, bannerFlagModel, null, i, j, k, crumblingOverlay);
        if (!managed2) {
            collector.submitModel(bannerFlagModel, g, poseStack, material.renderType(RenderTypes::entitySolid), i, j, -1, materialSet.get(material), k, crumblingOverlay);
        }

        if (!managed && !managed2) {
            submitPatterns(materialSet, poseStack, collector, i, j, bannerFlagModel, g, material, true, dyeColor, bannerPatternLayers, false, crumblingOverlay, k);
        }

        poseStack.popPose();
    }

    public static <S> void submitPatterns(MaterialSet materialSet, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, Model<S> model, S object, Material material, boolean bl, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, boolean bl2, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int k) {
        submitNodeCollector.submitModel(model, object, poseStack, material.renderType(RenderTypes::entitySolid), i, j, -1, materialSet.get(material), k, crumblingOverlay);
        if (bl2) {
            submitNodeCollector.submitModel(model, object, poseStack, RenderTypes.entityGlint(), i, j, -1, materialSet.get(material), 0, crumblingOverlay);
        }

        submitPatternLayer(materialSet, poseStack, submitNodeCollector, i, j, model, object, bl ? Sheets.BANNER_BASE : Sheets.SHIELD_BASE, dyeColor, crumblingOverlay);

        for(int l = 0; l < 16 && l < bannerPatternLayers.layers().size(); ++l) {
            BannerPatternLayers.Layer layer = (BannerPatternLayers.Layer)bannerPatternLayers.layers().get(l);
            Material material2 = bl ? Sheets.getBannerMaterial(layer.pattern()) : Sheets.getShieldMaterial(layer.pattern());
            submitPatternLayer(materialSet, poseStack, submitNodeCollector, i, j, model, object, material2, layer.color(), (ModelFeatureRenderer.CrumblingOverlay)null);
        }
    }

    private static <S> void submitPatternLayer(MaterialSet materialSet, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, Model<S> model, S object, Material material, DyeColor dyeColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        int k = dyeColor.getTextureDiffuseColor();
        submitNodeCollector.submitModel(model, object, poseStack, material.renderType(RenderTypes::entityNoOutline), i, j, k, materialSet.get(material), 0, crumblingOverlay);
    }
}
