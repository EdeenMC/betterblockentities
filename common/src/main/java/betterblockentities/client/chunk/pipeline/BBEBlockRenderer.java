package betterblockentities.client.chunk.pipeline;

/* local */
import betterblockentities.client.chunk.util.ModelResourceUtil;
import betterblockentities.client.compat.SableCompat;
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.gui.option.EnumTypes;
import betterblockentities.client.model.geometry.GeometryRegistry;
import betterblockentities.client.model.geometry.MultiPartBlockModel;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;
import betterblockentities.render.AltRenderers;

/* fabric */
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;

/* minecraft */
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;

/* java/misc */
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Supplier;

public final class BBEBlockRenderer {
    private final PoseStack poseStack;
    private final BBEEmitter emitter;

    public BBEBlockRenderer() {
        this.emitter = new BBEEmitter();
        this.poseStack = new PoseStack();
    }

    public void emitBlockModel(MutableQuadViewImpl quadEmitter, LevelSlice slice, BlockPos pos, BlockState state, Supplier<RandomSource> randomSupplier) {
        if (!ConfigCache.masterOptimize || !state.hasBlockEntity()) {
            return;
        }

        final BlockEntity blockEntity = tryGetBlockEntity(slice, pos);
        if (!(blockEntity instanceof BlockEntityExt ext) || !ext.supportedBlockEntity()) {
            return;
        }

        if (!BBEConfig.OptEnabledTable.ENABLED[ext.optKind() & 0xFF]) {
            return;
        }

        if (AltRenderers.hasRendererOverride(blockEntity.getType())) {
            return;
        }

        // Sable compat: never bake sublevel (contraption) block entities into main-world
        // Sodium terrain. Sable renders its plots with its own dispatcher + vanilla BERs.
        if (SableCompat.isOnSubLevel(blockEntity)) {
            return;
        }

        this.emitter.bind(quadEmitter);

        if (ConfigCache.shadeMode == EnumTypes.ShadeMode.VANILLA.ordinal()) {
            this.emitter.setFlag(BBEEmitter.IMMEDIATE_SHADING);
        }

        final BlockState blockState = blockEntity.getBlockState();
        final var block = blockState.getBlock();

        if (block instanceof ChestBlock || block instanceof EnderChestBlock) {
            if (ConfigCache.optimizeChests) {
                emitChest(blockEntity, ext, blockState, randomSupplier);
            }
        }
        else if (block instanceof ShulkerBoxBlock shulkerBoxBlock) {
            if (ConfigCache.optimizeShulker) {
                emitShulker(ext, shulkerBoxBlock, blockState, randomSupplier);
            }
        } else if (block instanceof BedBlock bedBlock) {
            if (ConfigCache.optimizeBeds) {
                emitBed(bedBlock, blockState, randomSupplier);
            }
        } else if (block instanceof BellBlock) {
            if (ConfigCache.optimizeBells) {
                emitBell(ext, randomSupplier);
            }
        } else if (block instanceof DecoratedPotBlock) {
            if (ConfigCache.optimizeDecoratedPots && blockEntity instanceof DecoratedPotBlockEntity decoratedPot) {
                emitDecoratedPot(ext, decoratedPot, randomSupplier);
            }
        } else if (block instanceof BannerBlock || block instanceof WallBannerBlock) {
            if (ConfigCache.optimizeBanners && blockEntity instanceof BannerBlockEntity banner) {
                emitBanner(banner, blockState, randomSupplier);
            }
        } else if (block instanceof StandingSignBlock || block instanceof WallSignBlock) {
            if (ConfigCache.optimizeSigns) {
                SignBlock signBlock = (SignBlock) block;
                emitSign(blockState, signBlock.type(), randomSupplier);
            }
        } else if (block instanceof CeilingHangingSignBlock || block instanceof WallHangingSignBlock) {
            if (ConfigCache.optimizeSigns) {
                SignBlock signBlock = (SignBlock) block;
                emitHangingSign(blockState, signBlock.type(), randomSupplier);
            }
        }

        this.emitter.clearState();
    }

    private void emitChest(BlockEntity blockEntity, BlockEntityExt ext, BlockState state, Supplier<RandomSource> randomSupplier) {
        if (!shouldRender(ext)) {
            return;
        }

        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getChestLayer(state));
        if (template == null) {
            return;
        }

        final ChestType type = state.hasProperty(ChestBlock.TYPE) ? state.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        final Material material = ModelResourceUtil.getChestMaterial(blockEntity, type, ConfigCache.christmasChests);
        final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(material);
        if (sprite == null) {
            return;
        }

        this.poseStack.setIdentity();
        final float angle = state.getValue(ChestBlock.FACING).toYRot();
        this.poseStack.translate(0.5F, 0.5F, 0.5F);
        this.poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        this.poseStack.translate(-0.5F, -0.5F, -0.5F);

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
        this.emitter.setSprite(sprite);
        this.emitter.setTransform(new Matrix4f(this.poseStack.last().pose()));


        Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        List<BakedModel> merged = new ArrayList<>(pairs.values());

        this.emitter.emit(merged, randomSupplier);
    }


    private void emitShulker(BlockEntityExt ext, ShulkerBoxBlock block, BlockState state, Supplier<RandomSource> randomSupplier) {
        if (!shouldRender(ext)) {
            return;
        }

        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getShulkerBoxLayer());
        if (template == null) {
            return;
        }

        Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        List<BakedModel> merged = new ArrayList<>(pairs.values());

        final DyeColor color = block.getColor();
        final Material material = ModelResourceUtil.getShulkerMaterial(state, color);
        final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(material);
        if (sprite == null) {
            return;
        }

        this.poseStack.setIdentity();
        final Direction facing = state.getValue(ShulkerBoxBlock.FACING);
        this.poseStack.translate(0.5F, 0.5F, 0.5F);
        this.poseStack.scale(0.9995F, 0.9995F, 0.9995F);
        this.poseStack.mulPose(facing.getRotation());
        this.poseStack.scale(1.0F, -1.0F, -1.0F);
        this.poseStack.translate(0.0F, -1.0F, 0.0F);

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.CUTOUT));
        this.emitter.setSprite(sprite);
        this.emitter.setTransform(new Matrix4f(this.poseStack.last().pose()));

        this.emitter.emit(merged, randomSupplier);
    }

    private void emitBed(BedBlock bedBlock, BlockState state, Supplier<RandomSource> randomSupplier) {
        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getBedLayer(state));
        if (template == null) {
            return;
        }

        Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        List<BakedModel> merged = new ArrayList<>(pairs.values());

        final Material material = ModelResourceUtil.getBedMaterial(state, bedBlock.getColor());
        final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(material);
        if (sprite == null) {
            return;
        }

        final Direction facing = state.getValue(BedBlock.FACING);

        this.poseStack.setIdentity();
        this.poseStack.translate(0.0F, 0.5625F, 0.0F);
        this.poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        this.poseStack.translate(0.5F, 0.5F, 0.5F);
        this.poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F + facing.toYRot()));
        this.poseStack.translate(-0.5F, -0.5F, -0.5F);

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
        this.emitter.setSprite(sprite);
        this.emitter.setTransform(new Matrix4f(this.poseStack.last().pose()));
        this.emitter.emit(merged, randomSupplier);
    }

    private void emitBell(BlockEntityExt ext, Supplier<RandomSource> randomSupplier) {
        if (!shouldRender(ext)) {
            return;
        }

        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getBellLayer());
        if (template == null) {
            return;
        }

        Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        List<BakedModel> merged = new ArrayList<>(pairs.values());

        final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(BellRenderer.BELL_RESOURCE_LOCATION);
        if (sprite == null) {
            return;
        }

        this.poseStack.setIdentity();
        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
        this.emitter.setSprite(sprite);
        this.emitter.setTransform(new Matrix4f(this.poseStack.last().pose()));
        this.emitter.emit(merged, randomSupplier);
    }

    private void emitDecoratedPot(BlockEntityExt ext, DecoratedPotBlockEntity pot, Supplier<RandomSource> randomSupplier) {
        if (!shouldRender(ext)) {
            return;
        }

        final MultiPartBlockModel baseTemplate = GeometryRegistry.getModel(ModelResourceUtil.getDecoratedPotBaseLayer());
        final MultiPartBlockModel sideTemplate = GeometryRegistry.getModel(ModelResourceUtil.getDecoratedPotSideLayer());
        if (baseTemplate == null || sideTemplate == null) {
            return;
        }

        Map<String, BakedModel> basePairs = baseTemplate.getPairs();
        if (basePairs.isEmpty()) {
            return;
        }

        Map<String, BakedModel> sidePairs = sideTemplate.getPairs();
        if (sidePairs.isEmpty()) {
            return;
        }

        this.poseStack.setIdentity();
        final Direction facing = pot.getDirection();
        this.poseStack.translate(0.5D, 0.0D, 0.5D);
        this.poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        this.poseStack.translate(-0.5D, 0.0D, -0.5D);

        final Matrix4f transform = new Matrix4f(this.poseStack.last().pose());
        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
        this.emitter.setTransform(transform);

        final TextureAtlasSprite baseSprite = ModelResourceUtil.spriteForMaterial(ModelResourceUtil.getDecoratedPotBaseMaterial());

        if (baseSprite != null) {
            this.emitter.setSprite(baseSprite);

            final List<BakedModel> merged = new ArrayList<>(basePairs.values());
            this.emitter.emit(merged, randomSupplier);
        }

        for (Map.Entry<String, BakedModel> e : sidePairs.entrySet()) {
            final PotDecorations decorations = pot.getDecorations();

            String modelName = e.getKey();
            BakedModel model = e.getValue();

            Material sideMaterial = switch (modelName) {
                case "back"  -> ModelResourceUtil.getPotSideMaterial(decorations.back());
                case "front" -> ModelResourceUtil.getPotSideMaterial(decorations.front());
                case "left"  -> ModelResourceUtil.getPotSideMaterial(decorations.left());
                case "right" -> ModelResourceUtil.getPotSideMaterial(decorations.right());
                default      -> ModelResourceUtil.getPotSideMaterial(Optional.empty());
            };

            final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(sideMaterial);

            final List<BakedModel> merged = new ArrayList<>();
            merged.add(model);

            this.emitter.setSprite(sprite);
            this.emitter.emit(merged, randomSupplier);
        }
    }

    private void emitBanner(BannerBlockEntity banner, BlockState state, Supplier<RandomSource> randomSupplier) {
        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getBannerLayer());
        if (template == null) {
            return;
        }

        Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        this.poseStack.setIdentity();
        final boolean wall = state.getBlock() instanceof WallBannerBlock;
        if (!wall) {
            this.poseStack.translate(0.5F, 0.5F, 0.5F);
            this.poseStack.mulPose(Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(state.getValue(BannerBlock.ROTATION))));
        } else {
            this.poseStack.translate(0.5F, -0.16666667F, 0.5F);
            this.poseStack.mulPose(Axis.YP.rotationDegrees(-state.getValue(WallBannerBlock.FACING).toYRot()));
            this.poseStack.translate(0.0F, -0.3125F, -0.4375F);
        }
        this.poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);

        this.emitter.setTransform(new Matrix4f(this.poseStack.last().pose()));

        final List<BakedModel> merged = new ArrayList<>();

        final TextureAtlasSprite poleSprite = ModelResourceUtil.spriteForMaterial(ModelBakery.BANNER_BASE);
        if (poleSprite != null) {
            this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
            this.emitter.setSprite(poleSprite);
            this.emitter.setColor(0xFFFFFFFF);
            if (!wall) {
                merged.add(pairs.get("pole"));
            }
            merged.add((pairs.get("bar")));
            this.emitter.emit(merged, randomSupplier);
        }

        merged.clear();

        final BakedModel flagQuads = pairs.get("flag");

        final BlendMode flagBlend = ConfigCache.bannerGraphics == EnumTypes.BannerGraphicsType.FAST.ordinal()
                ? BlendMode.CUTOUT
                : BlendMode.TRANSLUCENT;
        final TextureAtlasSprite solidFlagSprite = ModelResourceUtil.spriteForMaterial(ModelBakery.BANNER_BASE);
        final TextureAtlasSprite basePatternSprite = ModelResourceUtil.spriteForMaterial(ModelResourceUtil.getBannerBaseMaterial(true));
        if (solidFlagSprite == null || basePatternSprite == null) {
            return;
        }

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
        this.emitter.clearFlag(BBEEmitter.NO_QUAD_SPLITTING);
        this.emitter.setSprite(solidFlagSprite);
        this.emitter.setColor(0xFFFFFFFF);
        merged.add(flagQuads);
        this.emitter.emit(merged, randomSupplier);

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(flagBlend));
        this.emitter.setFlag(BBEEmitter.NO_QUAD_SPLITTING);
        this.emitter.setSprite(basePatternSprite);
        this.emitter.setColor(banner.getBaseColor().getTextureDiffuseColor() | 0xFF000000);
        this.emitter.emit(merged, randomSupplier);

        final List<BannerPatternLayers.Layer> layers = banner.getPatterns().layers();
        for (int i = 0; i < 16 && i < layers.size(); i++) {
            final BannerPatternLayers.Layer layer = layers.get(i);
            final TextureAtlasSprite sprite = ModelResourceUtil.spriteForBannerPattern(layer.pattern());
            if (sprite == null) {
                continue;
            }
            this.emitter.setSprite(sprite);
            this.emitter.setColor(layer.color().getTextureDiffuseColor() | 0xFF000000);
            this.emitter.emit(merged, randomSupplier);
        }

        this.emitter.clearFlag(BBEEmitter.NO_QUAD_SPLITTING);
        this.emitter.setColor(0xFFFFFFFF);
    }

    private void emitSign(BlockState state, WoodType woodType, Supplier<RandomSource> randomSupplier) {
        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getSignLayer());
        if (template == null) {
            return;
        }

        Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        boolean standing = state.getBlock() instanceof StandingSignBlock;

        this.poseStack.setIdentity();
        if (standing) {
            final float angle = RotationSegment.convertToDegrees(state.getValue(BlockStateProperties.ROTATION_16));
            this.poseStack.translate(0.5F, 0.5F, 0.5F);
            this.poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        } else {
            final Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            this.poseStack.translate(0.5F, 0.5F, 0.5F);
            this.poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            this.poseStack.translate(0.0F, -0.3125F, -0.4375F);
        }
        this.poseStack.scale(0.6666667F, -0.6666667F, -0.6666667F);

        final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(ModelResourceUtil.getSignMaterial(woodType));
        if (sprite == null) {
            return;
        }

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.DEFAULT));
        this.emitter.setSprite(sprite);
        this.emitter.setTransform(new Matrix4f(this.poseStack.last().pose()));

        List<BakedModel> merged = new ArrayList<>();

        BakedModel sign = pairs.get("sign");
        if (sign != null) {
            merged.add(sign);
        }

        if (standing) {
            BakedModel stick = pairs.get("stick");
            if (stick != null) {
                merged.add(stick);
            }
        }

        if (!merged.isEmpty()) {
            this.emitter.emit(merged, randomSupplier);
        }
    }

    private void emitHangingSign(final BlockState state, final WoodType woodType, final Supplier<RandomSource> randomSupplier) {
        final MultiPartBlockModel template = GeometryRegistry.getModel(ModelResourceUtil.getHangingSignLayer());
        if (template == null) {
            return;
        }

        final Map<String, BakedModel> pairs = template.getPairs();
        if (pairs.isEmpty()) {
            return;
        }

        final TextureAtlasSprite sprite = ModelResourceUtil.spriteForMaterial(ModelResourceUtil.getHangingSignMaterial(woodType));
        if (sprite == null) {
            return;
        }

        this.poseStack.setIdentity();
        this.poseStack.translate(0.5F, 0.9375F, 0.5F);

        if (state.hasProperty(BlockStateProperties.ROTATION_16)) {
            final float angle = RotationSegment.convertToDegrees(state.getValue(BlockStateProperties.ROTATION_16));
            this.poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
        } else {
            final Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            this.poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        this.poseStack.translate(0.0F, -0.3125F, 0.0F);
        this.poseStack.scale(1.0F, -1.0F, -1.0F);

        final Matrix4f baseTransform = new Matrix4f(this.poseStack.last().pose());
        final Matrix4f flippedChainTransform = new Matrix4f(baseTransform).rotateY((float) Math.PI);

        this.emitter.setMaterial(ModelResourceUtil.toMaterial(BlendMode.CUTOUT));
        this.emitter.setSprite(sprite);

        final boolean wall = !(state.getBlock() instanceof CeilingHangingSignBlock);
        final boolean attached = !wall && state.getValue(CeilingHangingSignBlock.ATTACHED);

        final BakedModel board = pairs.get("board");
        final BakedModel plank = pairs.get("plank");
        final BakedModel normalChains = pairs.get("normalChains");
        final BakedModel verticalChains = pairs.get("vChains");
        final BakedModel chainModel = wall || !attached ? normalChains : verticalChains;

        final List<BakedModel> solidParts = new ArrayList<>();
        if (board != null) {
            solidParts.add(board);
        }
        if (wall && plank != null) {
            solidParts.add(plank);
        }

        if (!solidParts.isEmpty()) {
            this.emitter.setTransform(baseTransform);
            this.emitter.emit(solidParts, randomSupplier);
        }

        if (chainModel != null) {
            final List<BakedModel> chains = List.of(chainModel);

            this.emitter.setTransform(baseTransform);
            this.emitter.emit(chains, randomSupplier);

            this.emitter.setTransform(flippedChainTransform);
            this.emitter.emit(chains, randomSupplier);
        }
    }

    public static BlockEntity tryGetBlockEntity(final LevelSlice slice, final BlockPos pos) {
        try {
            return slice.getBlockEntity(pos);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static boolean shouldRender(final BlockEntityExt ext) {
        return ext.renderingMode() == RenderingMode.TERRAIN;
    }
}
