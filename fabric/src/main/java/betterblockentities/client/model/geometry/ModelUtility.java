package betterblockentities.client.model.geometry;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.mixin.model.modelpart.ModelPartAccessor;

/* minecraft */
import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* java/misc */
import org.joml.Vector3f;
import org.joml.Vector3fc;
import java.util.List;

/**
 * A utility class for converting an entire Model root tree or a child ModelPart into a single list of BakedQuads
 * Each implementation respects PoseStack transforms and these should be applied before calling.
 */
public class ModelUtility {
    public static void toBakedQuads(ModelPart part, List<BakedQuad> output, TextureAtlasSprite sprite, PoseStack stack) {
        part.visit(stack, (pose, name, idx, cube) -> {
            for (ModelPart.Polygon poly : cube.polygons) {
                /* skip non-quad polygons */
                if (poly.vertices().length != 4) {
                    BBE.getLogger().error("Non quad polygon detected when assembling block geometry! Skipping");
                    continue;
                }

                Vector3f[] positions = new Vector3f[4];
                long[] packedUvs = new long[4];

                /* convert polygon normal to face direction */
                Direction dir = normalToDirection(poly.normal());

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
                BakedQuad.MaterialInfo matInfo = BakedQuad.MaterialInfo.of(bakedMat, Transparency.NONE, -1, true, 0);

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

    /**
     * Takes in a normal and outputs a Direction by comparing the normal components
     * in each cardinal direction, else we fall back to the axis with the largest component
     */
    public static Direction normalToDirection(Vector3fc normal) {
        for (Direction dir : Direction.values()) {
            if (dir.getStepX() == normal.x() &&
                dir.getStepY() == normal.y() &&
                dir.getStepZ() == normal.z()) {
                return dir;
            }
        }

        /* fallback */
        float x = normal.x();
        float y = normal.y();
        float z = normal.z();

        float absX = Math.abs(x);
        float absY = Math.abs(y);
        float absZ = Math.abs(z);

        if (absX > absY && absX > absZ) {
            return x > 0 ? Direction.EAST : Direction.WEST;
        } else if (absY > absZ) {
            return y > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
