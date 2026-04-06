package betterblockentities.client.model.geometry;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.tasks.ResourceTasks;

/* minecraft */
import betterblockentities.mixin.model.modelpart.ModelPartAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;

/**
 * runs our model baking / geometry setup code and appends each model to our registry
 * this should only ever be scheduled to be run on the main thread
 */
public class ModelGenerator {
    public static int generateAppend() {
        PoseStack stack = new PoseStack();

        EntityModelSet entityModelSet = tryGetEntityModelSet();
        if (entityModelSet == null) {
            return ResourceTasks.FAILED;
        }

        /* iterate all supported layers, bake root, transform, build and append geometry to registry */
        for (ModelLayerLocation layer : GeometryRegistry.SupportedVanillaModelLayers.ALL) {
            try {
                bakeLayerSetupAndAppend(entityModelSet, layer, stack);
            }
            catch (Exception e) {
                BBE.getLogger().error("Geometry setup for ModelLayer {} failed!", layer.layer(), e);
            }
        }
        return ResourceTasks.COMPLETE;
    }

    public static void bakeLayerSetupAndAppend(EntityModelSet entityModelSet, ModelLayerLocation layer, PoseStack stack) {
        ModelPart root = entityModelSet.bakeLayer(layer);
        if (root.getAllParts().isEmpty()) {
            BBE.getLogger().error("Root ModelPart for ModelLayer {} is empty! after bake! Skipping", layer.layer());
            return;
        }

        /* reset stack per layer iteration */
        stack.setIdentity();

        if (layer == ModelLayers.SHULKER_BOX) {
            setupShulker(layer, root, stack);
        } else if (layer == ModelLayers.DOUBLE_CHEST_RIGHT || layer == ModelLayers.DOUBLE_CHEST_LEFT || layer == ModelLayers.CHEST) {
            setupChest(layer, root, stack);
        } else if (layer == ModelLayers.BELL) {
            setupBell(layer, root, stack);
        } else if (layer == ModelLayers.BED_HEAD || layer == ModelLayers.BED_FOOT) {
            setupBed(layer, root, stack);
        } else if (layer == ModelLayers.DECORATED_POT_BASE || layer == ModelLayers.DECORATED_POT_SIDES) {
            setupDecoratedPot(layer, root, stack);
        } else if (layer == ModelLayers.STANDING_BANNER ||
                layer == ModelLayers.WALL_BANNER ||
                layer == ModelLayers.STANDING_BANNER_FLAG ||
                layer == ModelLayers.WALL_BANNER_FLAG) {
            setupBanners(layer, root, stack);
        } else if (layer == GeometryRegistry.SupportedVanillaModelLayers.SIGN_STANDING ||
                layer == GeometryRegistry.SupportedVanillaModelLayers.SIGN_WALL) {
            setupSigns(layer, root, stack);
        } else if (layer == GeometryRegistry.SupportedVanillaModelLayers.HANGING_SIGN_WALL ||
                layer == GeometryRegistry.SupportedVanillaModelLayers.HANGING_SIGN_CEILING ||
                layer == GeometryRegistry.SupportedVanillaModelLayers.HANGING_SIGN_CEILING_MIDDLE) {
            setupHangingSigns(layer, root, stack);
        } else if (layer == ModelLayers.COPPER_GOLEM      ||
                layer == ModelLayers.COPPER_GOLEM_RUNNING ||
                layer == ModelLayers.COPPER_GOLEM_SITTING ||
                layer == ModelLayers.COPPER_GOLEM_STAR) {
            setupCopperGolemStatue(layer, root, stack);
        }
    }

    private static void setupShulker(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.translate(0.5F, -0.5F, 0.5F);
        stack.mulPose(Axis.YP.rotationDegrees(180.0F));
        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.SHULKER, stack);
        stack.popPose();
    }

    private static void setupChest(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.CHEST, stack);
        stack.popPose();
    }

    private static void setupBell(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.translate(0.5F, 0.0F, 0.5F);
        stack.mulPose(Axis.YP.rotationDegrees(90.0F));
        stack.translate(-0.5F, 0.0F, -0.5F);
        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.BELL_BODY, stack);
        stack.popPose();
    }

    private static void setupBed(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.translate(0.0F, 0.5625F, 0.0F);
        stack.mulPose(Axis.XP.rotationDegrees(90.0F));
        stack.translate(0.5F, 0.5F, 0.5F);
        stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        stack.translate(-0.5F, -0.5F, -0.5F);
        GeometryRegistry.cacheGeometry(layer, root,
                layer == ModelLayers.BED_HEAD ?
                        GeometryRegistry.PlaceHolderSpriteIdentifiers.BED_HEAD :
                        GeometryRegistry.PlaceHolderSpriteIdentifiers.BED_FOOT,
                stack);
        stack.popPose();
    }

    private static void setupDecoratedPot(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.translate(0.5F, 0.0F, 0.5F);
        stack.mulPose(Axis.YP.rotationDegrees(180.0F));
        stack.translate(-0.5F, 0.0F, -0.5F);
        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.DECORATED_POT_BASE, stack);
        stack.popPose();
    }

    private static void setupBanners(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();

        stack.translate(0.5F, 0.0F, 0.5F);
        stack.scale(0.6666667F, -0.6666667F, -0.6666667F);

        if (layer == ModelLayers.WALL_BANNER_FLAG || layer == ModelLayers.STANDING_BANNER_FLAG) {
            ModelPart flag = root.getChild("flag");

            float step = -0.45f;
            float rot = step * ConfigCache.bannerPose;
            float rotClamped = Math.clamp(rot, -4.05f, -0.45f);
            flag.xRot = (float)Math.toRadians(rotClamped);
        }

        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.BANNER, stack);
        stack.popPose();
    }

    private static void setupSigns(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        if (layer == GeometryRegistry.SupportedVanillaModelLayers.SIGN_STANDING) {
            stack.pushPose();
            stack.translate(0.5F, 0.5F, 0.5F);
            stack.scale(0.6666667F, -0.6666667F, -0.6666667F);
            GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.SIGN, stack);
            stack.popPose();
        } else if (layer == GeometryRegistry.SupportedVanillaModelLayers.SIGN_WALL) {
            stack.pushPose();
            stack.translate(0.5F, 0.5F, 0.5F);
            stack.translate(0.0F, -0.3125F, -0.4375F);
            stack.scale(0.6666667F, -0.6666667F, -0.6666667F);
            GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.SIGN, stack);
            stack.popPose();
        }
    }

    private static void setupHangingSigns(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.translate(0.5, 0.9375, 0.5);
        stack.mulPose(Axis.XP.rotationDegrees(180.0F));
        stack.translate(0.0F, 0.3125F, 0.0F);
        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.HANGING_SIGN, stack);
        stack.popPose();
    }

    private static void setupCopperGolemStatue(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.translate(0.5f, 0.5f, 0.5f);
        stack.mulPose(Axis.XP.rotationDegrees(180));
        stack.translate(-0.5f, -0.5f, -0.5f);
        stack.translate(0.5F, 1.0F, 0.5F);
        GeometryRegistry.cacheGeometry(layer, root, GeometryRegistry.PlaceHolderSpriteIdentifiers.COPPER_GOLEM_STATUE, stack);
        stack.popPose();
    }

    private static EntityModelSet tryGetEntityModelSet() {
        try {
            EntityModelSet set = Minecraft.getInstance().getEntityModels();
            if (set == null) {
                BBE.getLogger().error("EntityModelSet was null (unexpected)");
                return null;
            }
            return set;
        } catch (Exception e) {
            BBE.getLogger().error("Failed to get EntityModelSet", e);
            return null;
        }
    }
}
