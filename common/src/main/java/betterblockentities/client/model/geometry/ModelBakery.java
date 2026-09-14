package betterblockentities.client.model.geometry;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.model.texture.PlaceHolderSpriteIdentifiers;
import betterblockentities.client.tasks.ResourceTasks;

/* minecraft */
import betterblockentities.platform.GlobalScope;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/**
 * runs our model baking / geometry setup code and appends each model to our registry
 * this should only ever be scheduled to be run on the main thread
 */
public class ModelBakery {
    public static int generateAppend() {
        PoseStack stack = new PoseStack();

        EntityModelSet entityModelSet = tryGetEntityModelSet();
        if (entityModelSet == null) {
            return ResourceTasks.FAILED;
        }

        /* iterate all supported layers, bake root, transform, build and append geometry to registry */
        for (ModelLayerLocation layer : SupportedModelLayers.ALL) {
            try {
                bakeLayerSetupAndAppend(entityModelSet, layer, stack);
            }
            catch (Exception e) {
                GlobalScope.LOGGER.error("Geometry setup for ModelLayer {} failed!", layer.layer(), e);
            }
        }
        return ResourceTasks.COMPLETE;
    }

    public static void bakeLayerSetupAndAppend(EntityModelSet entityModelSet, ModelLayerLocation layer, PoseStack stack) {
        ModelPart root = entityModelSet.bakeLayer(layer);
        if (root.getAllParts().isEmpty()) {
            return;
        }

        /* reset stack per layer iteration */
        stack.setIdentity();

        if (layer == ModelLayers.SHULKER_BOX) {
            setupShulker(layer, root, stack);
        }

        else if (layer == ModelLayers.DOUBLE_CHEST_RIGHT  ||
                 layer == ModelLayers.DOUBLE_CHEST_LEFT   ||
                 layer == ModelLayers.CHEST) {
            setupChest(layer, root, stack);
        }

        else if (layer == ModelLayers.BELL) {
            setupBell(layer, root, stack);
        }

        else if (layer == ModelLayers.DECORATED_POT_BASE   ||
                 layer == ModelLayers.DECORATED_POT_SIDES) {
            setupDecoratedPot(layer, root, stack);
        }

        else if (layer == ModelLayers.STANDING_BANNER      ||
                 layer == ModelLayers.WALL_BANNER          ||
                 layer == ModelLayers.STANDING_BANNER_FLAG ||
                 layer == ModelLayers.WALL_BANNER_FLAG) {
            setupBanners(layer, root, stack);
        }

        else if (layer == ModelLayers.COPPER_GOLEM         ||
                layer == ModelLayers.COPPER_GOLEM_RUNNING  ||
                layer == ModelLayers.COPPER_GOLEM_SITTING  ||
                layer == ModelLayers.COPPER_GOLEM_STAR) {
            setupCopperGolemStatue(layer, root, stack);
        }
    }

    private static void setupShulker(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        GeometryRegistry.cacheGeometry(layer, root, PlaceHolderSpriteIdentifiers.SHULKER, stack);
    }

    private static void setupChest(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        GeometryRegistry.cacheGeometry(layer, root, PlaceHolderSpriteIdentifiers.CHEST, stack);
    }

    private static void setupBell(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        GeometryRegistry.cacheGeometry(layer, root, PlaceHolderSpriteIdentifiers.BELL_BODY, stack);
    }

    private static void setupDecoratedPot(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        GeometryRegistry.cacheGeometry(layer, root, PlaceHolderSpriteIdentifiers.DECORATED_POT_BASE, stack);
    }

    private static void setupBanners(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        if (layer == ModelLayers.WALL_BANNER_FLAG || layer == ModelLayers.STANDING_BANNER_FLAG) {
            ModelPart flag = root.getChild("flag");

            float rotDegrees = Math.clamp(-0.45f * ConfigCache.bannerPose, -4.05f, -0.45f);
            float xRot = (float)Math.toRadians(rotDegrees);

            GlobalScope.bannerPhase = calculateBannerPhase(xRot);

            flag.xRot = xRot;
        }
        GeometryRegistry.cacheGeometry(layer, root, PlaceHolderSpriteIdentifiers.BANNER, stack);
    }

    private static void setupCopperGolemStatue(ModelLayerLocation layer, ModelPart root, PoseStack stack) {
        stack.pushPose();
        stack.rotateDegrees(Axis.XP, 180);
        stack.rotateDegrees(Axis.YP, 180);
        GeometryRegistry.cacheGeometry(layer, root, PlaceHolderSpriteIdentifiers.COPPER_GOLEM_STATUE, stack);
        stack.popPose();
    }

    private static EntityModelSet tryGetEntityModelSet() {
        try {
            return Minecraft.getInstance().getEntityModels();
        } catch (Exception e) {
            GlobalScope.LOGGER.error("Failed to get EntityModelSet while caching block entity geometry!", e);
            return null;
        }
    }

    //reverse mojang formula, we need this for overlay rendering
    private static float calculateBannerPhase(float xRot) {
        float cosValue = ((xRot / (float) Math.PI) + 0.0125f) / 0.01f;
        cosValue = Math.clamp(cosValue, -1.0f, 1.0f);

        return (float) (Math.acos(cosValue) / (Math.PI * 2.0));
    }
}
