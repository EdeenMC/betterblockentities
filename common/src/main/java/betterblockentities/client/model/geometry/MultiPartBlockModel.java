package betterblockentities.client.model.geometry;

/* local */
import betterblockentities.mixin.accessors.ModelPartAccessor;

/* minecraft */
import betterblockentities.platform.GlobalScope;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.Transparency;

/* java/misc */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

public class MultiPartBlockModel implements BlockStateModel {
    private final List<BlockStateModel> models = new ArrayList<>();
    private final Map<String, BlockStateModel> pairs = new HashMap<>();

    public MultiPartBlockModel(ModelPart root, TextureAtlasSprite sprite, PoseStack stack) {
        generateMeshModel(root, sprite, stack);
    }

    public MultiPartBlockModel(List<BlockStateModelPart> parts) {
        constructSingleVariants(parts);
    }

    private void generateMeshModel(ModelPart root, TextureAtlasSprite sprite, PoseStack stack) {
        ModelPartAccessor modelAcc = (ModelPartAccessor)(Object)root;
        if (modelAcc == null) {
            GlobalScope.LOGGER.error("Failed to invoke accessor on root model part with sprite {}", sprite.contents().name());
            return;
        }

        Map<String, ModelPart> children = modelAcc.bbe$getChildren();
        if (children.isEmpty()) {
            GlobalScope.LOGGER.error("Root model part with sprite {} has no children, skipping!", sprite.contents().name());
            return;
        }

        children.forEach((key, part) -> {
            List<BakedQuad> quads = new ArrayList<>();
            this.toBakedQuads(part, quads, sprite, stack);

            QuadCollection collection = toUnculledCollection(quads);
            SimpleModelWrapper wrapper = new SimpleModelWrapper(collection, true, null);

            constructSingleVariants(List.of(wrapper));
            createModelPairs(key);
        });
    }

    private void toBakedQuads(ModelPart part, List<BakedQuad> output, TextureAtlasSprite sprite, PoseStack stack) {
        part.visit(stack, (pose, name, idx, cube) -> {
            for (ModelPart.Polygon poly : cube.polygons) {
                /* skip non-quad polygons */
                if (poly.vertices().length != 4) {
                    continue;
                }

                Vector3f[] positions = new Vector3f[4];
                long[] packedUvs = new long[4];

                /* convert polygon normal to face direction */
                Direction dir = this.normalToDirection(poly.normal());

                /* pack and transform UVS and vertex positions */
                for (int i = 0; i < 4; i++) {
                    ModelPart.Vertex vertex = poly.vertices()[i];
                    Vector3f vec = pose.pose().transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
                    positions[i] = vec;

                    float u = sprite.getU(vertex.u());
                    float v = sprite.getV(vertex.v());
                    packedUvs[i] = UVPair.pack(u, v);
                }

                Material.Baked bakedMat = new Material.Baked(sprite, false);
                BakedQuad.MaterialInfo matInfo = BakedQuad.MaterialInfo.of(bakedMat, Transparency.NONE, -1, null, 0);

                /* assemble quad */
                BakedQuad baked = new BakedQuad(
                        positions[0], positions[1], positions[2], positions[3],
                        packedUvs[0], packedUvs[1], packedUvs[2], packedUvs[3],
                        dir,
                        matInfo
                );
                output.add(baked);
            }
        });
    }


    private Direction normalToDirection(Vector3fc normal) {
        float epsilon = 1e-3f;

        float x = normal.x();
        float y = normal.y();
        float z = normal.z();

        float absX = Math.abs(x);
        float absY = Math.abs(y);
        float absZ = Math.abs(z);

        for (Direction dir : Direction.values()) {
            if (dir.getUnitVec3f().equals(normal, epsilon)) {
                return dir;
            }
        }

        if (absX > absY && absX >= absZ) {
            return x >= 0.0f ? Direction.EAST : Direction.WEST;
        } else if (absY >= absZ) {
            return y >= 0.0f ? Direction.UP : Direction.DOWN;
        } else {
            return z >= 0.0f ? Direction.SOUTH : Direction.NORTH;
        }
    }

    private QuadCollection toUnculledCollection(List<BakedQuad> quads) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
        return builder.build();
    }

    private void constructSingleVariants(List<BlockStateModelPart> parts) {
        for (BlockStateModelPart variant : parts) {
            models.add(new SingleVariant(variant));
        }
    }

    private void createModelPairs(String key) {
        pairs.put(key, models.getLast());
    }

    public Map<String, BlockStateModel> getPairs() {
        return pairs;
    }

    @Override
    public void collectParts(@NonNull RandomSource randomSource, @NonNull List<BlockStateModelPart> list) {
        if (models.isEmpty()) return;

        long seed = randomSource.nextLong();

        for (BlockStateModel model : this.models) {
            randomSource.setSeed(seed);
            model.collectParts(randomSource, list);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return null;
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return 0;
    }
}
