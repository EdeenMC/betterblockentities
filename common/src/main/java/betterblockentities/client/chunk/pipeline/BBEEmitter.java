package betterblockentities.client.chunk.pipeline;

/* local */
import betterblockentities.client.chunk.util.QuadTransform;
import betterblockentities.client.model.texture.SpriteSelector;
import betterblockentities.mixin.accessors.AbstractBlockRenderContextAccessor;

/* minecraft */
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.helper.ModelHelper;
import net.caffeinemc.mods.sodium.client.render.model.*;
import net.caffeinemc.mods.sodium.client.services.PlatformBlockAccess;
import net.caffeinemc.mods.sodium.client.services.PlatformModelEmitter;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

/* java/misc */
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BBEEmitter {
    public static final int NO_QUAD_SPLITTING = 1 << 0; //0b0001 = 1
    public static final int IMMEDIATE_SHADING = 1 << 1; //0b0010 = 2
    public static final int SUPPORTED_FLAGS = NO_QUAD_SPLITTING | IMMEDIATE_SHADING;

    public static final int MAX_FACE_INDEX = 6;
    public static final int QUAD_VERTICES = 4;
    private final Vector3f scratchVector = new Vector3f();

    /* allocate PoseStack once and reuse over this emitter's lifecycle */
    private final PoseStack poseStack = new PoseStack();

    /* sodium block render context */
    private final AbstractBlockRenderContextAccessor sodiumContext;

    /* quad render data */
    private ChunkSectionLayer renderType;
    private TextureAtlasSprite sprite;
    private Quaternionf rotation;
    private Transformation b3dtransformation;
    private AmbientOcclusionMode aoMode;
    private SodiumShadeMode shadeMode;
    private float xRot = 0;
    private float yRot = 0;
    private float zRot = 0;
    private int color = -1;
    private int flags;

    public BBEEmitter(BlockRenderer sodiumBlockRenderer) {
        this.sodiumContext = (AbstractBlockRenderContextAccessor)sodiumBlockRenderer;
    }

    public void emit(ArrayList<BlockStateModelPart> partsLocal, Predicate<@Nullable Direction> cullTest, PlatformModelEmitter.Bufferer bufferer) {
        for(int i = 0; i < partsLocal.size(); ++i) {
            BlockStateModelPart part = partsLocal.get(i);
            bufferer.emit(part, cullTest, MutableQuadViewImpl::emitDirectly);
        }
    }

    public void buffer(BlockStateModelPart part, Predicate<Direction> cullTest, Consumer<MutableQuadViewImpl> sodiumEmitterConsumer) {
        final MutableQuadViewImpl sodiumEmitter = this.sodiumContext.bbe$getEmitter();

        this.sodiumContext.bbe$prepareAoInfo(part.useAmbientOcclusion());

        for (int i = 0; i <= MAX_FACE_INDEX; ++i) {
            Direction cullFace = ModelHelper.faceFromIndex(i);
            if (cullTest.test(cullFace)) {
                continue;
            }

            List<BakedQuad> quads = part.getQuads(cullFace);

            AmbientOcclusionMode sodiumAO = PlatformBlockAccess.getInstance().usesAmbientOcclusion(
                    part, sodiumContext.bbe$getState(), this.renderType, sodiumContext.bbe$getSlice(), sodiumContext.bbe$getPos()
            );

            for (int j = 0, count = quads.size(); j < count; ++j) {
                BakedQuad quad = quads.get(j);

                sodiumEmitter.fromBakedQuad(quad);
                sodiumEmitter.setCullFace(cullFace);

                /* modify sodium emitter data */
                this.applyShadeMode(sodiumEmitter);
                this.applyAmbientOcclusionMode(sodiumEmitter, sodiumAO);
                this.applyRenderType(sodiumEmitter);
                this.applySprite(sodiumEmitter);
                this.applyTransformation(sodiumEmitter);
                this.applyColor(sodiumEmitter);
                this.applyFlags(sodiumEmitter);

                sodiumEmitterConsumer.accept(sodiumEmitter);
            }
        }
        sodiumEmitter.clear();
    }

    private void applyRenderType(MutableQuadViewImpl sodiumEmitter) {
        if (this.renderType != null) {
            sodiumEmitter.setRenderType(this.renderType);
        }
    }

    private void applySprite(MutableQuadViewImpl sodiumEmitter) {
        if (this.sprite != null) {
            QuadTransform.remapSprite(this.sprite, sodiumEmitter);
        }
    }

    private void applyTransformation(MutableQuadViewImpl sodiumEmitter) {
        /* blaze3d path */
        if (this.b3dtransformation != null) {
            poseStack.pushPose();
            poseStack.mulPose(this.b3dtransformation);

            for (int v = 0; v < QUAD_VERTICES; ++v) {
                Matrix4f matrix = poseStack.last().pose();

                float vX = sodiumEmitter.sodium$getX(v);
                float vY = sodiumEmitter.sodium$getY(v);
                float vZ = sodiumEmitter.sodium$getZ(v);

                Vector3f pos = matrix.transformPosition(vX, vY, vZ, this.scratchVector);

                sodiumEmitter.setPos(v, pos.x, pos.y, pos.z);
            }
            poseStack.popPose();
        }

        /* quaternion rotation */
        if (this.rotation != null) {
            QuadTransform.rotate(sodiumEmitter, this.rotation, scratchVector);
        }

        /* ModelPart style Euler rotation */
        if (this.xRot != 0 || this.yRot != 0 || this.zRot != 0) {
            QuadTransform.rotateXYZ(sodiumEmitter, this.xRot, this.yRot, this.zRot);
        }
    }

    private void applyColor(MutableQuadViewImpl sodiumEmitter) {
        if (this.color != -1) {
            for (int v = 0; v < QUAD_VERTICES; ++v) {
                sodiumEmitter.setColor(v, this.color);
            }
        }
    }

    private void applyAmbientOcclusionMode(MutableQuadViewImpl sodiumEmitter, AmbientOcclusionMode sodiumAO) {
        if (this.aoMode == null) {
            sodiumEmitter.setAmbientOcclusion(sodiumAO.toTriState());
        }
        else {
            sodiumEmitter.setAmbientOcclusion(this.aoMode.toTriState());
        }
    }

    private void applyShadeMode(MutableQuadViewImpl sodiumEmitter) {
        if (this.shadeMode == null) {
            sodiumEmitter.setShadeMode(SodiumShadeMode.ENHANCED);
        }
        else {
            sodiumEmitter.setShadeMode(this.shadeMode);
        }
    }

    public void applyFlags(MutableQuadViewImpl sodiumEmitter) {
        sodiumEmitter.setTag(this.flags);
    }

    public void setRenderType(ChunkSectionLayer layer) {
        this.renderType = layer;
    }

    public void setTransformation(Transformation transformation) {
        this.b3dtransformation = transformation;
    }

    public void setRotation(float x, float y, float z) {
        this.xRot = x;
        this.yRot = y;
        this.zRot = z;
    }

    public void setRotation(Quaternionf quaternion) {
        this.rotation = quaternion;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        if (sprite != null) {
            this.sprite = sprite;
        }
    }

    public void setSprite(SpriteId identifier) {
        if (identifier != null) {
            this.sprite = SpriteSelector.getBlockSprite(identifier.texture());
        }
    }

    public void setAmbientOcclusionMode(AmbientOcclusionMode mode) {
        this.aoMode = mode;
    }

    public void setShadeMode(SodiumShadeMode mode) {
        this.shadeMode = mode;
    }

    public void setFlag(int flag) {
        if ((flag & ~SUPPORTED_FLAGS) != 0) {
            throw new IllegalArgumentException("Unsupported emitter flag: " + flag);
        }

        this.flags |= flag;
    }

    public void clear() {
        this.renderType = null;
        this.sprite = null;
        this.rotation = null;
        this.b3dtransformation = null;
        this.aoMode = null;
        this.shadeMode = null;
        this.xRot = 0;
        this.yRot = 0;
        this.zRot = 0;
        this.color = -1;
        this.flags = 0;
    }
}
