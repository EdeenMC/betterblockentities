package betterblockentities.client.chunk.pipeline;

/* local */
import betterblockentities.render.AltRenderers;
import betterblockentities.client.BBE;
import betterblockentities.client.chunk.section.SectionUpdateDispatcher;
import betterblockentities.client.chunk.util.QuadTransform;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.gui.option.EnumTypes;
import betterblockentities.client.model.geometry.GeometryRegistry;
import betterblockentities.client.model.MaterialSelector;
import betterblockentities.client.model.MultiPartBlockModel;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;
import betterblockentities.client.tasks.TaskScheduler;
import betterblockentities.client.tasks.ResourceTasks;

/* minecraft */
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevels;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.services.PlatformModelEmitter;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;

/* java/misc */
import java.util.*;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

/**
 * A wrapper/redirect for {@link net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer#renderModel} ->
 * {@link net.caffeinemc.mods.sodium.client.services.DefaultModelEmitter#emitModel} which hands over mesh assembly to us
 */

public final class BBEEmitter {
    private static final ThreadLocal<ArrayList<BlockStateModelPart>> ALLOCATED_PARTS_LIST = ThreadLocal.withInitial(() -> new ArrayList<>(64));

    public static void emit(PlatformModelEmitter instance, BlockStateModel model, Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockAndTintGetter level, LevelSlice slice, BlockPos pos, BlockState state, PlatformModelEmitter.Bufferer bufferer, BlockRenderer blockRenderer) {
        final Block block = state.getBlock();

        if (!ConfigCache.masterOptimize) {
            instance.emitModel(model, isFaceCulled, emitter, random, level, pos, state, bufferer);
            return;
        }

        final BlockEntity blockEntity;
        final BlockRenderHelper helper;

        if (block instanceof ChestBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeChests)
                emitChest(isFaceCulled, emitter, random, state, helper, false, blockEntity);
        }

        else if (block instanceof EnderChestBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeChests)
                emitChest(isFaceCulled, emitter, random, state, helper, true, blockEntity);
        }

        else if (block instanceof ShulkerBoxBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeShulker)
                emitShulker(isFaceCulled, emitter, random, state, helper, blockEntity);
        }

        else if (block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeSigns)
                emitHangingSign(isFaceCulled, emitter, random, state, helper);
        }

        else if (block instanceof WallSignBlock || block instanceof StandingSignBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeSigns)
                emitSign(isFaceCulled, emitter, random, state, helper);
        }

        else if (block instanceof BellBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeBells)
                emitBell(isFaceCulled, emitter, random, state, helper, blockEntity);
        }

        else if (block instanceof DecoratedPotBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeDecoratedPots)
                emitDecoratedPot(isFaceCulled, emitter, random, state, helper, blockEntity);
        }

        else if (block instanceof BedBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeBeds)
                emitBed(isFaceCulled, emitter, random, state, helper);
        }

        else if (block instanceof BannerBlock || block instanceof WallBannerBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeBanners)
                emitBanner(isFaceCulled, emitter, random, state, helper, blockEntity);
        }

        else if (block instanceof CopperGolemStatueBlock) {
            blockEntity = tryGetBlockEntity(pos, level, slice);
            if (blockEntity == null) return;

            helper = new BlockRenderHelper(blockRenderer);

            if (ConfigCache.optimizeCopperGolemStatue)
                emitCopperGolemStatue(isFaceCulled, emitter, random, state, helper);
        }

        /* emit any accessory parts if there are any, catch unsupported blocks or regular terrain  */
        instance.emitModel(model, isFaceCulled, emitter, random, level, pos, state, bufferer);
    }

    private static void emitChest(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper, boolean emissive, BlockEntity blockEntity) {
        ModelLayerLocation layer;
        if (emissive) {
            layer = ModelLayers.CHEST;
        } else if (state.hasProperty(ChestBlock.TYPE)) {
            ChestType t = state.getValue(ChestBlock.TYPE);
            layer = (t == ChestType.LEFT) ? ModelLayers.DOUBLE_CHEST_LEFT
                    : (t == ChestType.RIGHT) ? ModelLayers.DOUBLE_CHEST_RIGHT
                    : ModelLayers.CHEST;
        } else {
            layer = ModelLayers.CHEST;
        }

        Map<String, BlockStateModel> pairs = getPairs(layer);
        if (pairs.isEmpty()) return;

        final boolean drawLid = shouldRender((BlockEntityExt) blockEntity);
        final boolean addBase = (ConfigCache.updateType == EnumTypes.UpdateSchedulerType.FAST.ordinal())
                || (drawLid && ConfigCache.updateType == EnumTypes.UpdateSchedulerType.SMART.ordinal());

        ArrayList<BlockStateModelPart> merged = partsBuf();
        if (addBase) addParts(merged, pairs.get("bottom"), random);
        if (drawLid) {
            addParts(merged, pairs.get("lid"), random);
            addParts(merged, pairs.get("lock"), random);
        }
        if (merged.isEmpty()) return;

        final boolean christmas = ConfigCache.christmasChests;
        ChestRenderState.ChestMaterialType chestMat = MaterialSelector.getChestMaterial(blockEntity, christmas);
        final ChestType type = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        final SpriteId material = Sheets.chooseSprite(chestMat, type);

        helper.setMaterial(material);
        helper.setRendertype(ChunkSectionLayer.SOLID);
        BlockRenderHelper.emitModelPart(merged, emitter, state, isFaceCulled, helper::emitGE);
    }

    /* avoid new allocations : these never change */
    private static final float[] ROT_UP    = {180f, 180f};
    private static final float[] ROT_DOWN  = {  0f, 180f};
    private static final float[] ROT_NORTH = { 90f,   0f};
    private static final float[] ROT_SOUTH = { 90f, 180f};
    private static final float[] ROT_WEST  = { 90f, 270f};
    private static final float[] ROT_EAST  = { 90f,  90f};

    private static void emitShulker(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper, BlockEntity blockEntity) {
        Map<String, BlockStateModel> pairs = getPairs(ModelLayers.SHULKER_BOX);
        if (pairs.isEmpty()) return;

        final boolean drawLid = shouldRender((BlockEntityExt) blockEntity);
        final boolean addBase = (ConfigCache.updateType == EnumTypes.UpdateSchedulerType.FAST.ordinal())
                || (drawLid && ConfigCache.updateType == EnumTypes.UpdateSchedulerType.SMART.ordinal());

        ArrayList<BlockStateModelPart> merged = partsBuf();
        if (addBase) addParts(merged, pairs.get("base"), random);
        if (drawLid) addParts(merged, pairs.get("lid"), random);
        if (merged.isEmpty()) return;

        final Direction facing = state.hasProperty(ShulkerBoxBlock.FACING) ? state.getValue(ShulkerBoxBlock.FACING) : Direction.UP;
        final float[] rotation = switch (facing) {
            case UP    -> ROT_UP;
            case DOWN  -> ROT_DOWN;
            case NORTH -> ROT_NORTH;
            case SOUTH -> ROT_SOUTH;
            case WEST  -> ROT_WEST;
            case EAST  -> ROT_EAST;
        };

        DyeColor color = ((ShulkerBoxBlock) state.getBlock()).getColor();
        SpriteId shulkerMaterial = (color == null) ? Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION : Sheets.getShulkerBoxSprite(color);

        helper.setMaterial(shulkerMaterial);
        helper.setRendertype(ChunkSectionLayer.CUTOUT);
        helper.setRotation(rotation);
        BlockRenderHelper.emitModelPart(merged, emitter, state, isFaceCulled, helper::emitGE);
        helper.setRotation(null);
    }

    private static void emitSign(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper) {
        final boolean isWallSign = !state.hasProperty(BlockStateProperties.ROTATION_16);
        final ModelLayerLocation layerLocation = isWallSign
                ? GeometryRegistry.SupportedVanillaModelLayers.SIGN_WALL
                : GeometryRegistry.SupportedVanillaModelLayers.SIGN_STANDING;

        Map<String, BlockStateModel> pairs = getPairs(layerLocation);
        if (pairs.isEmpty()) return;

        ArrayList<BlockStateModelPart> merged = partsBuf();
        addAllParts(merged, pairs.values(), random);
        if (merged.isEmpty()) return;

        WoodType woodType = ((SignBlock) state.getBlock()).type();
        SpriteId signMaterial = Sheets.getSignSprite(woodType);

        helper.setMaterial(signMaterial);
        helper.setRendertype(ChunkSectionLayer.SOLID);
        BlockRenderHelper.emitModelPart(merged, emitter, state, isFaceCulled, helper::emitGE);
    }

    private static void emitHangingSign(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper) {
        final boolean isWall = !state.hasProperty(CeilingHangingSignBlock.ATTACHED);
        final boolean attached = !isWall && state.getValue(CeilingHangingSignBlock.ATTACHED);

        final ModelLayerLocation layerLocation = isWall ? GeometryRegistry.SupportedVanillaModelLayers.HANGING_SIGN_WALL
                        : attached ? GeometryRegistry.SupportedVanillaModelLayers.HANGING_SIGN_CEILING_MIDDLE
                        : GeometryRegistry.SupportedVanillaModelLayers.HANGING_SIGN_CEILING;

        Map<String, BlockStateModel> pairs = getPairs(layerLocation);
        if (pairs.isEmpty()) return;

        WoodType woodType = ((SignBlock) state.getBlock()).type();
        SpriteId signMaterial = Sheets.getHangingSignSprite(woodType);

        ArrayList<BlockStateModelPart> merged = partsBuf();
        addAllParts(merged, pairs.values(), random);

        BlockStateModel chains = pairs.get(attached ? "vChains" : "normalChains");
        if (chains != null) {
            List<BlockStateModelPart> chainParts = new ArrayList<>();
            chains.collectParts(random, chainParts);
            if (!chainParts.isEmpty()) {
                float[] rotation = {0f, (BlockRenderHelper.getRotationFromBlockState(state) + 180f) % 360f};
                helper.setRotation(rotation);
                helper.setMaterial(signMaterial);
                helper.setRendertype(ChunkSectionLayer.CUTOUT);
                BlockRenderHelper.emitModelPart(chainParts, emitter, state, isFaceCulled, helper::emitGE);
                helper.setRotation(null);
            }
        }

        if (merged.isEmpty()) return;

        helper.setMaterial(signMaterial);
        helper.setRendertype(ChunkSectionLayer.CUTOUT);
        BlockRenderHelper.emitModelPart(merged, emitter, state, isFaceCulled, helper::emitGE);
    }

    private static void emitBell(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper, BlockEntity blockEntity) {
        if (!shouldRender((BlockEntityExt) blockEntity)) return;

        Map<String, BlockStateModel> pairs = getPairs(ModelLayers.BELL);
        if (pairs.isEmpty()) return;

        BlockStateModel bellBody = pairs.get("bell_body");
        if (bellBody == null) return;

        List<BlockStateModelPart> bellBodyParts = new ArrayList<>();
        bellBody.collectParts(random, bellBodyParts);

        if (bellBodyParts.isEmpty()) return;

        SpriteId bellBodyMaterial = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("bell/bell_body");

        helper.setMaterial(bellBodyMaterial);
        helper.setRendertype(ChunkSectionLayer.SOLID);
        BlockRenderHelper.emitModelPart(bellBodyParts, emitter, state, isFaceCulled, helper::emitGE);
    }

    private static void emitBed(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper) {
        ModelLayerLocation layer = (state.getValue(BedBlock.PART) == BedPart.HEAD) ? ModelLayers.BED_HEAD : ModelLayers.BED_FOOT;

        Map<String, BlockStateModel> pairs = getPairs(layer);
        if (pairs.isEmpty()) return;

        ArrayList<BlockStateModelPart> merged = partsBuf();
        addAllParts(merged, pairs.values(), random);
        if (merged.isEmpty()) return;

        DyeColor color = ((BedBlock) state.getBlock()).getColor();
        SpriteId bedMaterial = Sheets.getBedSprite(color);

        helper.setMaterial(bedMaterial);
        helper.setRendertype(ChunkSectionLayer.SOLID);
        BlockRenderHelper.emitModelPart(merged, emitter, state, isFaceCulled, helper::emitGE);
    }

    private static void emitDecoratedPot(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper, BlockEntity blockEntity) {
        if (!shouldRender((BlockEntityExt) blockEntity)) return;
        if (!(blockEntity instanceof DecoratedPotBlockEntity potBE)) return;

        Map<String, BlockStateModel> basePairs = getPairs(ModelLayers.DECORATED_POT_BASE);
        Map<String, BlockStateModel> sidePairs = getPairs(ModelLayers.DECORATED_POT_SIDES);
        if (basePairs.isEmpty() || sidePairs.isEmpty()) return;

        ArrayList<BlockStateModelPart> baseParts = partsBuf();
        addAllParts(baseParts, basePairs.values(), random);
        if (!baseParts.isEmpty()) {
            helper.setMaterial(Sheets.DECORATED_POT_BASE);
            helper.setRendertype(ChunkSectionLayer.SOLID);
            BlockRenderHelper.emitModelPart(baseParts, emitter, state, isFaceCulled, helper::emitGE);
        }

        PotDecorations decorations = potBE.getDecorations();
        for (Map.Entry<String, BlockStateModel> e : sidePairs.entrySet()) {
            String key = e.getKey();
            BlockStateModel m = e.getValue();
            if (m == null) continue;

            List<BlockStateModelPart> sideParts = new ArrayList<>();
            m.collectParts(random, sideParts);

            if (sideParts.isEmpty()) continue;

            SpriteId sideMaterial = switch (key) {
                case "back"  -> MaterialSelector.getDPSideMaterial(decorations.back());
                case "front" -> MaterialSelector.getDPSideMaterial(decorations.front());
                case "left"  -> MaterialSelector.getDPSideMaterial(decorations.left());
                case "right" -> MaterialSelector.getDPSideMaterial(decorations.right());
                default      -> MaterialSelector.getDPSideMaterial(Optional.empty());
            };

            helper.setMaterial(sideMaterial);
            helper.setRendertype(ChunkSectionLayer.SOLID);
            BlockRenderHelper.emitModelPart(sideParts, emitter, state, isFaceCulled, helper::emitGE);
        }
    }

    private static void emitBanner(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper, BlockEntity blockEntity) {
        if (!(blockEntity instanceof BannerBlockEntity bannerBE)) return;

        final boolean isWallBanner = !state.hasProperty(BlockStateProperties.ROTATION_16);
        final ModelLayerLocation baseLayer = isWallBanner ? ModelLayers.WALL_BANNER : ModelLayers.STANDING_BANNER;
        final ModelLayerLocation flagLayer = isWallBanner ? ModelLayers.WALL_BANNER_FLAG : ModelLayers.STANDING_BANNER_FLAG;

        Map<String, BlockStateModel> basePairs = getPairs(baseLayer);
        Map<String, BlockStateModel> canvasPairs = getPairs(flagLayer);
        if (basePairs.isEmpty() || canvasPairs.isEmpty()) return;

        ArrayList<BlockStateModelPart> baseParts = partsBuf();
        addAllParts(baseParts, basePairs.values(), random);

        ArrayList<BlockStateModelPart> canvasParts = new ArrayList<>(32);
        for (BlockStateModel m : canvasPairs.values()) {
            List<BlockStateModelPart> parts = new ArrayList<>();
            m.collectParts(random, parts);


            if (!parts.isEmpty()) canvasParts.addAll(parts);
        }

        if (!baseParts.isEmpty()) {
            helper.setMaterial(Sheets.BANNER_BASE);
            helper.setRendertype(ChunkSectionLayer.SOLID);
            BlockRenderHelper.emitModelPart(baseParts, emitter, state, isFaceCulled, helper::emitGE);
        }

        if (canvasParts.isEmpty()) return;

        helper.setColor(bannerBE.getBaseColor().getTextureDiffuseColor());
        BlockRenderHelper.emitModelPart(canvasParts, emitter, state, isFaceCulled, helper::emitGE);

        final int fancy = EnumTypes.BannerGraphicsType.FANCY.ordinal();
        final ChunkSectionLayer rt = (ConfigCache.bannerGraphics == fancy) ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.CUTOUT;

        for (BannerPatternLayers.Layer layer : bannerBE.getPatterns().layers()) {
            SpriteId layerMaterial = MaterialSelector.getBannerMaterial(layer.pattern());
            DyeColor layerColor = layer.color();

            helper.setMaterial(layerMaterial);
            helper.setRendertype(rt);
            helper.setColor(layerColor.getTextureDiffuseColor());
            BlockRenderHelper.emitModelPart(canvasParts, emitter, state, isFaceCulled, helper::emitGE);
        }
    }

    private static void emitCopperGolemStatue(Predicate<Direction> isFaceCulled, MutableQuadViewImpl emitter, RandomSource random, BlockState state, BlockRenderHelper helper) {
        ModelLayerLocation layerLocation = ModelLayers.COPPER_GOLEM;
        CopperGolemStatueBlock.Pose pose = state.getValue(BlockStateProperties.COPPER_GOLEM_POSE);
        if (pose == CopperGolemStatueBlock.Pose.SITTING) layerLocation = ModelLayers.COPPER_GOLEM_SITTING;
        else if (pose == CopperGolemStatueBlock.Pose.RUNNING) layerLocation = ModelLayers.COPPER_GOLEM_RUNNING;
        else if (pose == CopperGolemStatueBlock.Pose.STAR) layerLocation = ModelLayers.COPPER_GOLEM_STAR;

        Map<String, BlockStateModel> pairs = getPairs(layerLocation);
        if (pairs.isEmpty()) return;

        ArrayList<BlockStateModelPart> merged = partsBuf();
        addAllParts(merged, pairs.values(), random);
        if (merged.isEmpty()) return;

        Identifier texture = CopperGolemOxidationLevels.getOxidationLevel(((CopperGolemStatueBlock) state.getBlock()).getWeatheringState()).texture();

        String path = texture.getPath();
        if (path.endsWith(".png")) path = path.substring(0, path.length() - 4);
        if (path.startsWith("textures/")) path = path.substring("textures/".length());

        Identifier strippedTexture = Identifier.withDefaultNamespace(path);
        TextureAtlasSprite sprite = QuadTransform.getSprite(strippedTexture);

        helper.setSprite(sprite);
        helper.setRendertype(ChunkSectionLayer.SOLID);
        BlockRenderHelper.emitModelPart(merged, emitter, state, isFaceCulled, helper::emitGE);
        helper.setSprite(null);
    }

    private static @Nullable BlockEntity tryGetBlockEntity(BlockPos pos, BlockAndTintGetter level, LevelSlice slice) {
        try {
            return AltRenderers.hasRendererOverride(
                    level.getBlockEntity(pos).getType()) ?
                    null : level.getBlockEntity(pos);
        } catch (Exception e) {
            BBE.getLogger().error("Failed to get block entity at {}. Attempting fallback.", pos, e);
            try {
                return AltRenderers.hasRendererOverride(
                        slice.getBlockEntity(pos).getType()) ?
                        null : slice.getBlockEntity(pos);
            } catch (Throwable t) {
                BBE.getLogger().error("Fallback failed! This block entity will be skipped and not added to this mesh!", t);
                return null;
            }
        }
    }

    private static Map<String, BlockStateModel> tryGetPairs(ModelLayerLocation location) {
        try {
            MultiPartBlockModel model = (MultiPartBlockModel) GeometryRegistry.getModel(location);
            return model.getPairs();
        } catch (Exception e) {
            TaskScheduler.schedule(() -> {
                if (ResourceTasks.populateGeometryRegistry() == ResourceTasks.FAILED) {
                    throw new RuntimeException("Failed to repopulate geometry registry after failed location lookup!");
                }
                SectionUpdateDispatcher.queueUpdateAllSections();
            });
            return Map.of();
        }
    }

    private static Map<String, BlockStateModel> getPairs(ModelLayerLocation location) {
        return tryGetPairs(location);
    }

    private static ArrayList<BlockStateModelPart> partsBuf() {
        ArrayList<BlockStateModelPart> buf = ALLOCATED_PARTS_LIST.get();
        buf.clear();
        return buf;
    }

    private static void addParts(ArrayList<BlockStateModelPart> out, BlockStateModel model, RandomSource random) {
        if (model == null) return;
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);

        if (!parts.isEmpty()) out.addAll(parts);
    }

    private static void addAllParts(ArrayList<BlockStateModelPart> out, Iterable<BlockStateModel> models, RandomSource random) {
        for (BlockStateModel m : models) {
            addParts(out, m, random);
        }
    }

    private static boolean shouldRender(BlockEntityExt ext) {
        return ext == null || ext.renderingMode() == RenderingMode.TERRAIN;
    }
}
