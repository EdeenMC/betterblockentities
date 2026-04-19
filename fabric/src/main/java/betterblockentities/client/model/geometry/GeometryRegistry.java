package betterblockentities.client.model.geometry;

/* local */
import betterblockentities.client.chunk.util.QuadTransform;
import betterblockentities.client.model.MultiPartBlockModel;

/* minecraft */
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.resources.Identifier;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* java/misc */
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Registry which holds the base geometry for all our supported block entity models, which is then
 * later used when meshing happens in {@link betterblockentities.client.chunk.pipeline.BBEEmitter}
 * -We append geometry to the registry cache with {@link #cacheGeometry}
 * -Clear the whole registry cache with {@link #clearCache}
 * -And get an entry from the cache with {@link #getModel}
 */
public class GeometryRegistry {
    private static final ConcurrentHashMap<ModelLayerLocation, BlockStateModel> cache = new ConcurrentHashMap<>();

    public static void cacheGeometry(ModelLayerLocation key, ModelPart root, Identifier texture, PoseStack stack) {
        MultiPartBlockModel model = new MultiPartBlockModel(root, QuadTransform.getSprite(texture), stack);
        cache.put(key, model);
    }

    public static BlockStateModel getModel(ModelLayerLocation layer) {
        return cache.get(layer);
    }

    public static void clearCache() {
        cache.clear();
    }

    public static Map<ModelLayerLocation, BlockStateModel> getCache() {
        return cache;
    }

    /* supported vanilla model layers which are mapped inside the entityModelSet */
    public static class SupportedVanillaModelLayers {
        public static final ModelLayerLocation CHEST;
        public static final ModelLayerLocation LEFT_CHEST;
        public static final ModelLayerLocation RIGHT_CHEST;
        public static final ModelLayerLocation BELL_BODY;
        public static final ModelLayerLocation DECORATED_POT_BASE;
        public static final ModelLayerLocation DECORATED_POT_SIDES;
        public static final ModelLayerLocation SHULKER;
        public static final ModelLayerLocation BED_HEAD;
        public static final ModelLayerLocation BED_FOOT;
        public static final ModelLayerLocation STANDING_BANNER;
        public static final ModelLayerLocation WALL_BANNER;
        public static final ModelLayerLocation STANDING_BANNER_FLAG;
        public static final ModelLayerLocation WALL_BANNER_FLAG;
        public static final ModelLayerLocation SIGN_WALL;
        public static final ModelLayerLocation SIGN_STANDING;
        public static final ModelLayerLocation HANGING_SIGN_WALL;
        public static final ModelLayerLocation HANGING_SIGN_CEILING;
        public static final ModelLayerLocation HANGING_SIGN_CEILING_MIDDLE;
        public static final ModelLayerLocation COPPER_GOLEM;
        public static final ModelLayerLocation COPPER_GOLEM_RUNNING;
        public static final ModelLayerLocation COPPER_GOLEM_SITTING;
        public static final ModelLayerLocation COPPER_GOLEM_STAR;

        public static final ModelLayerLocation[] ALL;

        static {
            CHEST = ModelLayers.CHEST;
            LEFT_CHEST = ModelLayers.DOUBLE_CHEST_LEFT;
            RIGHT_CHEST = ModelLayers.DOUBLE_CHEST_RIGHT;
            BELL_BODY = ModelLayers.BELL;
            DECORATED_POT_BASE = ModelLayers.DECORATED_POT_BASE;
            DECORATED_POT_SIDES = ModelLayers.DECORATED_POT_SIDES;
            SHULKER = ModelLayers.SHULKER_BOX;
            BED_HEAD = ModelLayers.BED_HEAD;
            BED_FOOT = ModelLayers.BED_FOOT;
            STANDING_BANNER = ModelLayers.STANDING_BANNER;
            WALL_BANNER = ModelLayers.WALL_BANNER;
            STANDING_BANNER_FLAG = ModelLayers.STANDING_BANNER_FLAG;
            WALL_BANNER_FLAG = ModelLayers.WALL_BANNER_FLAG;
            SIGN_WALL = ModelLayers.createWallSignModelName(WoodType.OAK);
            SIGN_STANDING = ModelLayers.createStandingSignModelName(WoodType.OAK);
            HANGING_SIGN_WALL = ModelLayers.createHangingSignModelName(WoodType.OAK, HangingSignRenderer.AttachmentType.WALL);
            HANGING_SIGN_CEILING = ModelLayers.createHangingSignModelName(WoodType.OAK, HangingSignRenderer.AttachmentType.CEILING);
            HANGING_SIGN_CEILING_MIDDLE = ModelLayers.createHangingSignModelName(WoodType.OAK, HangingSignRenderer.AttachmentType.CEILING_MIDDLE);
            COPPER_GOLEM = ModelLayers.COPPER_GOLEM;
            COPPER_GOLEM_RUNNING = ModelLayers.COPPER_GOLEM_RUNNING;
            COPPER_GOLEM_SITTING = ModelLayers.COPPER_GOLEM_SITTING;
            COPPER_GOLEM_STAR = ModelLayers.COPPER_GOLEM_STAR;

            ALL = new ModelLayerLocation[]{CHEST, LEFT_CHEST, RIGHT_CHEST, BELL_BODY, DECORATED_POT_BASE, DECORATED_POT_SIDES,
                                           SHULKER, BED_HEAD, BED_FOOT, STANDING_BANNER, WALL_BANNER, STANDING_BANNER_FLAG, WALL_BANNER_FLAG,
                                           SIGN_WALL, SIGN_STANDING, HANGING_SIGN_WALL, HANGING_SIGN_CEILING, HANGING_SIGN_CEILING_MIDDLE,
                                           COPPER_GOLEM, COPPER_GOLEM_RUNNING, COPPER_GOLEM_SITTING, COPPER_GOLEM_STAR};
        }
    }

    /* placeholder sprite identifiers for the model part wrapper */
    public static class PlaceHolderSpriteIdentifiers {
        public static final Identifier CHEST;
        public static final Identifier BELL_BODY;
        public static final Identifier DECORATED_POT_BASE;
        public static final Identifier DECORATED_POT_SIDES;
        public static final Identifier SHULKER;
        public static final Identifier BED_HEAD;
        public static final Identifier BED_FOOT;
        public static final Identifier BANNER;
        public static final Identifier SIGN;
        public static final Identifier HANGING_SIGN;
        public static final Identifier COPPER_GOLEM_STATUE;

        public static final Identifier[] ALL;

        static {
            CHEST = Identifier.withDefaultNamespace("entity/chest/normal");
            BELL_BODY = Identifier.withDefaultNamespace("entity/bell/bell_body");
            DECORATED_POT_BASE = Identifier.withDefaultNamespace("entity/decorated_pot/decorated_pot_base");
            DECORATED_POT_SIDES = Identifier.withDefaultNamespace("entity/decorated_pot/decorated_pot_side");
            SHULKER = Identifier.withDefaultNamespace("entity/shulker/shulker");
            BED_HEAD = Identifier.withDefaultNamespace("entity/bed/bed_head");
            BED_FOOT = Identifier.withDefaultNamespace("entity/bed/bed_foot");
            BANNER = Identifier.withDefaultNamespace("entity/banner_base");
            SIGN = Sheets.getSignMaterial(WoodType.OAK).texture();
            HANGING_SIGN = Sheets.getHangingSignMaterial(WoodType.OAK).texture();
            COPPER_GOLEM_STATUE =  Identifier.withDefaultNamespace("entity/copper_golem/copper_golem");

            ALL = new Identifier[]{CHEST, BELL_BODY, DECORATED_POT_BASE, DECORATED_POT_SIDES, SHULKER, BED_HEAD,
                                   BED_FOOT, BANNER, SIGN, HANGING_SIGN, COPPER_GOLEM_STATUE};
        }
    }
}
