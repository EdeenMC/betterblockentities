package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.model.overrides.ChestModelOverride;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.SpecialDates;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;

/* java/misc */
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BBEChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T, ChestRenderState> {
    private final MaterialSet materials;
    private final boolean xmasTextures;

    private ChestModel singleModel;
    private ChestModel doubleLeftModel;
    private ChestModel doubleRightModel;

    private final ChestModel singleModelOrg;
    private final ChestModel doubleLeftModelOrg;
    private final ChestModel doubleRightModelOrg;

    private final ChestModel BBEsingleModel;
    private final ChestModel BBEdoubleLeftModel;
    private final ChestModel BBEdoubleRightModel;

    public BBEChestRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.xmasTextures = xmasTextures();


        this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
        this.doubleLeftModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleRightModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));

        this.singleModelOrg = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
        this.doubleLeftModelOrg = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleRightModelOrg = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));

        this.BBEsingleModel = new ChestModelOverride(context.bakeLayer(ModelLayers.CHEST));
        this.BBEdoubleLeftModel = new ChestModelOverride(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.BBEdoubleRightModel = new ChestModelOverride(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    public static boolean xmasTextures() {
        return SpecialDates.isExtendedChristmas();
    }

    public ChestRenderState createRenderState() {
        return new ChestRenderState();
    }

    public void extractRenderState(T blockEntity, ChestRenderState chestRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, chestRenderState, f, vec3, crumblingOverlay);
        boolean bl = blockEntity.getLevel() != null;
        BlockState blockState = bl ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        chestRenderState.type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        chestRenderState.angle = (blockState.getValue(ChestBlock.FACING)).toYRot();
        chestRenderState.material = this.getChestMaterial(blockEntity, this.xmasTextures);
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborCombineResult;
        if (bl && blockState.getBlock() instanceof ChestBlock chestBlock) {
            neighborCombineResult = chestBlock.combine(blockState, blockEntity.getLevel(), blockEntity.getBlockPos(), true);
        } else {
            neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;
        }

        chestRenderState.open = neighborCombineResult.apply(ChestBlock.opennessCombiner(blockEntity)).get(f);
        if (chestRenderState.type != ChestType.SINGLE) {
            chestRenderState.lightCoords = neighborCombineResult.apply(new BrightnessCombiner<>()).applyAsInt(chestRenderState.lightCoords);
        }

        ((BlockEntityRenderStateExt)chestRenderState).blockEntity(blockEntity);
    }

    public <S> void submitManagedModel(ChestRenderState state, SubmitNodeCollector collector, Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k, TextureAtlasSprite textureAtlasSprite, int l, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        this.singleModel = singleModelOrg;
        this.doubleLeftModel = doubleLeftModelOrg;
        this.doubleRightModel = this.doubleRightModelOrg;

        if (!ConfigCache.optimizeChests || !ConfigCache.masterOptimize) {
            collector.submitModel(model, object, poseStack, renderType, i, j, k, textureAtlasSprite, l, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)state;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, object, i, j, k, crumblingOverlay);
        if (!managed) {
            this.singleModel = this.BBEsingleModel;
            this.doubleLeftModel = this.BBEdoubleLeftModel;
            this.doubleRightModel = this.BBEdoubleRightModel;

            collector.submitModel(model, object, poseStack, renderType, i, j, k, textureAtlasSprite, l, crumblingOverlay);
        }
    }



    public void submit(ChestRenderState chestRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-chestRenderState.angle));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        float f = chestRenderState.open;
        f = 1.0F - f;
        f = 1.0F - f * f * f;
        Material material = Sheets.chooseMaterial(chestRenderState.material, chestRenderState.type);
        RenderType renderType = material.renderType(RenderTypes::entityCutout);
        TextureAtlasSprite textureAtlasSprite = this.materials.get(material);


        if (chestRenderState.type != ChestType.SINGLE) {
            if (chestRenderState.type == ChestType.LEFT) {
                submitManagedModel(
                        chestRenderState,
                        submitNodeCollector,
                        this.doubleLeftModel,
                        f,
                        poseStack,
                        renderType,
                        chestRenderState.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        -1,
                        textureAtlasSprite,
                        0,
                        chestRenderState.breakProgress
                );
            } else {
                submitManagedModel(
                        chestRenderState,
                        submitNodeCollector,
                        this.doubleRightModel,
                        f,
                        poseStack,
                        renderType,
                        chestRenderState.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        -1,
                        textureAtlasSprite,
                        0,
                        chestRenderState.breakProgress
                );
            }
        } else {
            submitManagedModel(
                    chestRenderState,
                    submitNodeCollector,
                    this.singleModel,
                    f,
                    poseStack,
                    renderType,
                    chestRenderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    -1,
                    textureAtlasSprite,
                    0,
                    chestRenderState.breakProgress
            );
        }

        poseStack.popPose();
    }

    private static ChestRenderState.ChestMaterialType getChestMaterial(final BlockEntity entity, final boolean xmasTextures) {
        Block blockState = entity.getBlockState().getBlock();
        if (blockState instanceof CopperChestBlock) {
            CopperChestBlock copperChestBlock = (CopperChestBlock)blockState;
            ChestRenderState.ChestMaterialType var10000;
            switch (copperChestBlock.getState()) {
                case UNAFFECTED -> var10000 = ChestRenderState.ChestMaterialType.COPPER_UNAFFECTED;
                case EXPOSED -> var10000 = ChestRenderState.ChestMaterialType.COPPER_EXPOSED;
                case WEATHERED -> var10000 = ChestRenderState.ChestMaterialType.COPPER_WEATHERED;
                case OXIDIZED -> var10000 = ChestRenderState.ChestMaterialType.COPPER_OXIDIZED;
                default -> throw new MatchException((String)null, (Throwable)null);
            }

            return var10000;
        } else if (entity instanceof EnderChestBlockEntity) {
            return ChestRenderState.ChestMaterialType.ENDER_CHEST;
        } else if (xmasTextures) {
            return ChestRenderState.ChestMaterialType.CHRISTMAS;
        } else {
            return entity instanceof TrappedChestBlockEntity ? ChestRenderState.ChestMaterialType.TRAPPED : ChestRenderState.ChestMaterialType.REGULAR;
        }
    }
}
