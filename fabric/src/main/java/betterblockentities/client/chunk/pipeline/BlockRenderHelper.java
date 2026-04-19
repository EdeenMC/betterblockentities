package betterblockentities.client.chunk.pipeline;

/* local */
import betterblockentities.client.chunk.util.QuadTransform;
import betterblockentities.mixin.sodium.pipeline.AbstractBlockRenderContextAccessor;

/* minecraft */
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.helper.ModelHelper;
import net.caffeinemc.mods.sodium.client.render.model.*;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelEmitter;

/* java/misc */
import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * this is a "subclass" / helper class of {@link betterblockentities.client.chunk.pipeline.BBEEmitter}
 * which actually pulls all the render data into Sodium's MutableQuadViewImpl (emitter)
 */
public class BlockRenderHelper {
    private final BlockRenderer ctx;
    private Material material;
    private ChunkSectionLayer rendertype;
    private float[] rotation;
    private int color = -1;
    private TextureAtlasSprite sprite;

    public BlockRenderHelper(BlockRenderer ctx) {
        this.ctx = ctx;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setRendertype(ChunkSectionLayer layer) {
        this.rendertype = layer;
    }

    public void setRotation(float rotation[]) {
        this.rotation = rotation;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }


    /**
     * Rebuild of DefaultModelEmitter->emitModel
     * see {@link net.caffeinemc.mods.sodium.client.services.DefaultModelEmitter#emitModel}
     */
    public static void emitModelPart(List<BlockModelPart> parts, MutableQuadViewImpl quad, BlockState state, Predicate<@Nullable Direction> cullTest, PlatformModelEmitter.Bufferer defaultBuffer) {
        for(int i = 0; i < parts.size(); ++i) {
            BlockModelPart part = (BlockModelPart)parts.get(i);
            defaultBuffer.emit(part, cullTest, MutableQuadViewImpl::emitDirectly);
        }
    }

    /**
     * Generic emitter, applies local render data (material/sprite, rotations, vertex color, rendertype) if set,
     * else we fall back on "arbitrary" methods to handle this for us. The actual logic for attaining this
     * render data should be done in {@link betterblockentities.client.chunk.pipeline.BBEEmitter} and passed
     * through the setters in this class
     */
    public void emitGE(BlockModelPart part, Predicate<Direction> cullTest, Consumer<MutableQuadViewImpl> emitter) {
        AbstractBlockRenderContextAccessor acc = (AbstractBlockRenderContextAccessor) ctx;

        TextureAtlasSprite sprite = (this.sprite != null) ?
                this.sprite :
                (this.material != null ? QuadTransform.getSprite(this.material.texture()) : null);

        float stateRotation = getRotationFromBlockState(acc.getState());

        MutableQuadViewImpl editorQuad = acc.getEmitterInvoke();
        acc.prepareAoInfoInvoke(part.useAmbientOcclusion());

        for (int i = 0; i <= 6; ++i) {
            Direction cullFace = ModelHelper.faceFromIndex(i);
            if (cullTest.test(cullFace)) continue;

            AmbientOcclusionMode ao = PlatformBlockAccess.getInstance().usesAmbientOcclusion(
                    part, acc.getState(), this.rendertype, acc.getSlice(), acc.getPos()
            );

            List<BakedQuad> quads = PlatformModelAccess.getInstance().getQuads(
                    acc.getLevel(), acc.getPos(), part, acc.getState(), cullFace, acc.getRandom(), this.rendertype
            );

            for (int j = 0, count = quads.size(); j < count; ++j) {
                BakedQuad q = quads.get(j);

                editorQuad.fromBakedQuad(q);
                editorQuad.setCullFace(cullFace);
                editorQuad.setRenderType(this.rendertype);
                editorQuad.setAmbientOcclusion(ao.toTriState());

                if (sprite != null) {
                    QuadTransform.swapSprite(sprite, editorQuad);
                }

                if (this.rotation != null) {
                    if (this.rotation[0] != 0) QuadTransform.rotateX(editorQuad, this.rotation[0]);
                    if (this.rotation[1] != 0) QuadTransform.rotateY(editorQuad, this.rotation[1]);
                } else {
                    QuadTransform.rotateY(editorQuad, stateRotation);
                }

                if (this.color != -1) {
                    int diffuseColor = this.color;
                    for (int vertex = 0; vertex < 4; ++vertex) {
                        editorQuad.setColor(vertex, diffuseColor);
                    }
                }
                emitter.accept(editorQuad);
            }
        }
        editorQuad.clear();
    }

    /**
     * select what rotation compute function to call based on block state, for X axis rotations
     * this function should not be used, instead you should pass a rotation array with {@link #setRotation(float[])}
     * where element [0] is the X axis rotation, and element [1] is the Y axis rotation
     */
    public static float getRotationFromBlockState(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            return getRotationFromFacing(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        if (state.hasProperty(BlockStateProperties.ROTATION_16))
            return compute16StepRotation(state);
        return 0f;
    }

    public static float getRotationFromFacing(Direction facing) {
        return switch (facing) {
            case NORTH, DOWN -> 180f;
            case EAST  -> 270f;
            case SOUTH, UP -> 0f;
            case WEST  -> 90f;
        };
    }

    public static float compute16StepRotation(BlockState state) {
        int rot = state.getValue(BlockStateProperties.ROTATION_16);
        return rot * 22.5f;
    }
}
