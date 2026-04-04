package betterblockentities.client.render.immediate.blockentity.renderers;

/* local */
import betterblockentities.client.model.overrides.ChestModelOverride;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.SpecialDates;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;

/* java/misc */
import org.joml.Matrix4f;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BBEChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T, ChestRenderState> {
    public static final MultiblockChestResources<ModelLayerLocation> LAYERS;
    private static final Map<Direction, Transformation> TRANSFORMATIONS;
    private final SpriteGetter sprites;
    private final boolean xmasTextures;

    private final MultiblockChestResources<ChestModel> models;
    private final MultiblockChestResources<ChestModel> bbeModels;

    public BBEChestRenderer(final BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.xmasTextures = xmasTextures();
        this.models = LAYERS.map((layer) -> new ChestModel(context.bakeLayer(layer)));
        this.bbeModels = LAYERS.map((layer) -> new ChestModelOverride(context.bakeLayer(layer)));
    }

    public static boolean xmasTextures() {
        return SpecialDates.isExtendedChristmas();
    }

    public ChestRenderState createRenderState() {
        return new ChestRenderState();
    }

    public void extractRenderState(
            final T blockEntity,
            final ChestRenderState state,
            final float partialTicks,
            final Vec3 cameraPosition,
            final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        boolean hasLevel = blockEntity.getLevel() != null;
        BlockState blockState = hasLevel ? blockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        state.type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        state.facing = blockState.getValue(ChestBlock.FACING);
        state.material = getChestMaterial(blockEntity, this.xmasTextures);
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combineResult;
        if (hasLevel && blockState.getBlock() instanceof ChestBlock chestBlock) {
            combineResult = chestBlock.combine(blockState, blockEntity.getLevel(), blockEntity.getBlockPos(), true);
        } else {
            combineResult = DoubleBlockCombiner.Combiner::acceptNone;
        }

        state.open = combineResult.apply(ChestBlock.opennessCombiner(blockEntity)).get(partialTicks);
        if (state.type != ChestType.SINGLE) {
            state.lightCoords = combineResult.apply(new BrightnessCombiner<>()).applyAsInt(state.lightCoords);
        }

        ((BlockEntityRenderStateExt)state).blockEntity(blockEntity);
    }

    public void submit(final ChestRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(modelTransformation(state.facing));
        float open = state.open;
        open = 1.0F - open;
        open = 1.0F - open * open * open;
        SpriteId spriteId = Sheets.chooseSprite(state.material, state.type);

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)state;

        ChestModel model = (ChestModel)this.models.select(state.type);
        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, open, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
        if (!managed) {
            model = (ChestModel)this.bbeModels.select(state.type);
            submitNodeCollector.submitModel(model, open, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, spriteId, this.sprites, 0, state.breakProgress);
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

    public static Transformation modelTransformation(final Direction facing) {
        return (Transformation)TRANSFORMATIONS.get(facing);
    }

    private static Transformation createModelTransformation(final Direction facing) {
        return new Transformation((new Matrix4f()).rotationAround(Axis.YP.rotationDegrees(-facing.toYRot()), 0.5F, 0.0F, 0.5F));
    }

    static {
        LAYERS = new MultiblockChestResources(ModelLayers.CHEST, ModelLayers.DOUBLE_CHEST_LEFT, ModelLayers.DOUBLE_CHEST_RIGHT);
        TRANSFORMATIONS = Util.makeEnumMap(Direction.class, BBEChestRenderer::createModelTransformation);
    }
}
