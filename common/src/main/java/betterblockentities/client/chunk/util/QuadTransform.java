package betterblockentities.client.chunk.util;

/* minecraft */
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;

/* java/misc */
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Utility class for transforming Sodium's {@link net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl } emitter / render data
 */
public class QuadTransform {
    public static void rotateXYZ(MutableQuadViewImpl quad, float degreesX, float degreesY, float degreesZ) {
        float centerX = 0.5f;
        float centerY = 0.5f;
        float centerZ = 0.5f;

        float radX = (float) Math.toRadians(degreesX);
        float radY = (float) Math.toRadians(degreesY);
        float radZ = (float) Math.toRadians(degreesZ);

        float cosX = (float) Math.cos(radX);
        float sinX = (float) Math.sin(radX);

        float cosY = (float) Math.cos(radY);
        float sinY = (float) Math.sin(radY);

        float cosZ = (float) Math.cos(radZ);
        float sinZ = (float) Math.sin(radZ);

        for (int i = 0; i < 4; i++) {
            float x = quad.sodium$getX(i) - centerX;
            float y = quad.sodium$getY(i) - centerY;
            float z = quad.sodium$getZ(i) - centerZ;

            if (degreesX != 0f) {
                float newY = y * cosX - z * sinX;
                float newZ = y * sinX + z * cosX;
                y = newY;
                z = newZ;
            }

            if (degreesY != 0f) {
                float newX = x * cosY - z * sinY;
                float newZ = x * sinY + z * cosY;
                x = newX;
                z = newZ;
            }

            if (degreesZ != 0f) {
                float newX = x * cosZ - y * sinZ;
                float newY = x * sinZ + y * cosZ;
                x = newX;
                y = newY;
            }

            quad.setPos(i, x + centerX, y + centerY, z + centerZ);
        }
    }

    public static void rotate(MutableQuadViewImpl quad, Quaternionf rotation, Vector3f scratchVector) {
        if (rotation == null) {
            return;
        }

        float centerX = 0.5f;
        float centerY = 0.5f;
        float centerZ = 0.5f;

        for (int i = 0; i < 4; i++) {
            scratchVector.set(quad.sodium$getX(i) - centerX, quad.sodium$getY(i) - centerY, quad.sodium$getZ(i) - centerZ);
            scratchVector.rotate(rotation);
            quad.setPos(i, scratchVector.x + centerX, scratchVector.y + centerY, scratchVector.z + centerZ);
        }
    }

    public static void expand(float amount, MutableQuadViewImpl quad) {
        /* compute quad center */
        float cx = 0, cy = 0, cz = 0;
        for (int i = 0; i < 4; i++) {
            cx += quad.sodium$getX(i);
            cy += quad.sodium$getY(i);
            cz += quad.sodium$getZ(i);
        }
        cx *= 0.25f;
        cy *= 0.25f;
        cz *= 0.25f;

        /* face normal */
        Vector3f normal = quad.faceNormal();
        normal.normalize();

        /* compute tangent */
        Vector3f tangent = new Vector3f(
                quad.sodium$getX(1) - quad.sodium$getX(0),
                quad.sodium$getY(1) - quad.sodium$getY(0),
                quad.sodium$getZ(1) - quad.sodium$getZ(0)
        );
        tangent.normalize();

        /* bitangent = normal x tangent */
        Vector3f bitangent = new Vector3f(
                normal.y() * tangent.z() - normal.z() * tangent.y(),
                normal.z() * tangent.x() - normal.x() * tangent.z(),
                normal.x() * tangent.y() - normal.y() * tangent.x()
        );
        bitangent.normalize();

        /* scalar for diagonal expansion */
        float expand = amount;

        /* move each vertex */
        for (int i = 0; i < 4; i++) {
            float x = quad.sodium$getX(i);
            float y = quad.sodium$getY(i);
            float z = quad.sodium$getZ(i);

            /* local coordinates relative to center */
            float dx = x - cx;
            float dy = y - cy;
            float dz = z - cz;

            /* project vertex into tangent space */
            float tx = dx * tangent.x() + dy * tangent.y() + dz * tangent.z();
            float ty = dx * bitangent.x() + dy * bitangent.y() + dz * bitangent.z();

            /* move outward in 2D plane (diagonally) */
            x += tangent.x() * Math.signum(tx) * expand;
            y += tangent.y() * Math.signum(tx) * expand;
            z += tangent.z() * Math.signum(tx) * expand;

            x += bitangent.x() * Math.signum(ty) * expand;
            y += bitangent.y() * Math.signum(ty) * expand;
            z += bitangent.z() * Math.signum(ty) * expand;

            /* push outward along face normal */
            x += normal.x() * amount;
            y += normal.y() * amount;
            z += normal.z() * amount;

            quad.setPos(i, x, y, z);
        }
    }

    public static void remapSprite(TextureAtlasSprite newSprite, MutableQuadViewImpl quad) {
        final float uNewMin = newSprite.getU0();
        final float uNewMax = newSprite.getU1();
        final float vNewMin = newSprite.getV0();
        final float vNewMax = newSprite.getV1();

        if (!(quad instanceof MutableQuadViewImpl mQuad)) return;
        TextureAtlasSprite old = mQuad.cachedSprite();
        if (old == null) return;

        float uOldMin = old.getU0();
        float uOldMax = old.getU1();
        float vOldMin = old.getV0();
        float vOldMax = old.getV1();

        float uOldRange = uOldMax - uOldMin;
        float vOldRange = vOldMax - vOldMin;

        float uNewRange = uNewMax - uNewMin;
        float vNewRange = vNewMax - vNewMin;

        for (int i = 0; i < 4; i++) {
            float uNorm = (mQuad.sodium$getTexU(i) - uOldMin) / uOldRange;
            float vNorm = (mQuad.sodium$getTexV(i) - vOldMin) / vOldRange;

            mQuad.setUV(i,
                    uNewMin + uNorm * uNewRange,
                    vNewMin + vNorm * vNewRange
            );
        }
        mQuad.cachedSprite(newSprite);
    }

    private QuadTransform() {
        throw new IllegalStateException("Instancing of this class is not allowed!");
    }
}
