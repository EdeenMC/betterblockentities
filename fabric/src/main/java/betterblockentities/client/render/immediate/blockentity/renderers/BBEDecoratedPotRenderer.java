package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.DecoratedPotRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/* java/misc */
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class BBEDecoratedPotRenderer implements BlockEntityRenderer<DecoratedPotBlockEntity, DecoratedPotRenderState> {
    private final MaterialSet materials;
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float WOBBLE_AMPLITUDE = 0.125F;

    public BBEDecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet(), context.materials());
    }

    public BBEDecoratedPotRenderer(SpecialModelRenderer.BakingContext bakingContext) {
        this(bakingContext.entityModelSet(), bakingContext.materials());
    }

    public BBEDecoratedPotRenderer(EntityModelSet entityModelSet, MaterialSet materialSet) {
        this.materials = materialSet;
        ModelPart modelPart = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild("neck");
        this.top = modelPart.getChild("top");
        this.bottom = modelPart.getChild("bottom");
        ModelPart modelPart2 = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = modelPart2.getChild("front");
        this.backSide = modelPart2.getChild("back");
        this.leftSide = modelPart2.getChild("left");
        this.rightSide = modelPart2.getChild("right");
    }

    private static Material getSideMaterial(Optional<Item> optional) {
        if (optional.isPresent()) {
            Material material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem((Item)optional.get()));
            if (material != null) {
                return material;
            }
        }
        return Sheets.DECORATED_POT_SIDE;
    }

    public DecoratedPotRenderState createRenderState() {
        return new DecoratedPotRenderState();
    }

    public void extractRenderState(DecoratedPotBlockEntity decoratedPotBlockEntity, DecoratedPotRenderState decoratedPotRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(decoratedPotBlockEntity, decoratedPotRenderState, f, vec3, crumblingOverlay);
        decoratedPotRenderState.decorations = decoratedPotBlockEntity.getDecorations();
        decoratedPotRenderState.direction = decoratedPotBlockEntity.getDirection();
        DecoratedPotBlockEntity.WobbleStyle wobbleStyle = decoratedPotBlockEntity.lastWobbleStyle;
        if (wobbleStyle != null && decoratedPotBlockEntity.getLevel() != null) {
            decoratedPotRenderState.wobbleProgress = ((float)(decoratedPotBlockEntity.getLevel().getGameTime() - decoratedPotBlockEntity.wobbleStartedAtTick) + f)
                    / wobbleStyle.duration;
        } else {
            decoratedPotRenderState.wobbleProgress = 0.0F;
        }
    }


    public void submit(DecoratedPotRenderState decoratedPotRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        Direction direction = decoratedPotRenderState.direction;
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - direction.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        if (decoratedPotRenderState.wobbleProgress >= 0.0F && decoratedPotRenderState.wobbleProgress <= 1.0F) {
            if (decoratedPotRenderState.wobbleStyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                float f = 0.015625F;
                float g = decoratedPotRenderState.wobbleProgress * (float) (Math.PI * 2);
                float h = -1.5F * (Mth.cos(g) + 0.5F) * Mth.sin(g / 2.0F);
                poseStack.rotateAround(Axis.XP.rotation(h * 0.015625F), 0.5F, 0.0F, 0.5F);
                float i = Mth.sin(g);
                poseStack.rotateAround(Axis.ZP.rotation(i * 0.015625F), 0.5F, 0.0F, 0.5F);
            } else {
                float f = Mth.sin(-decoratedPotRenderState.wobbleProgress * 3.0F * (float) Math.PI) * 0.125F;
                float g = 1.0F - decoratedPotRenderState.wobbleProgress;
                poseStack.rotateAround(Axis.YP.rotation(f * g), 0.5F, 0.0F, 0.5F);
            }
        }

        this.submit(poseStack, submitNodeCollector, decoratedPotRenderState.lightCoords, OverlayTexture.NO_OVERLAY, decoratedPotRenderState.decorations, 0);
        poseStack.popPose();
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, PotDecorations potDecorations, int k) {
        RenderType renderType = Sheets.DECORATED_POT_BASE.renderType(RenderTypes::entitySolid);
        TextureAtlasSprite textureAtlasSprite = this.materials.get(Sheets.DECORATED_POT_BASE);
        submitNodeCollector.submitModelPart(this.neck, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, k);
        submitNodeCollector.submitModelPart(this.top, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, k);
        submitNodeCollector.submitModelPart(this.bottom, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, k);
        Material material = getSideMaterial(potDecorations.front());
        submitNodeCollector.submitModelPart(
                this.frontSide, poseStack, material.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material), false, false, -1, null, k
        );
        Material material2 = getSideMaterial(potDecorations.back());
        submitNodeCollector.submitModelPart(
                this.backSide, poseStack, material2.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material2), false, false, -1, null, k
        );
        Material material3 = getSideMaterial(potDecorations.left());
        submitNodeCollector.submitModelPart(
                this.leftSide, poseStack, material3.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material3), false, false, -1, null, k
        );
        Material material4 = getSideMaterial(potDecorations.right());
        submitNodeCollector.submitModelPart(
                this.rightSide, poseStack, material4.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material4), false, false, -1, null, k
        );
    }

    public void getExtents(Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        this.neck.getExtentsForGui(poseStack, consumer);
        this.top.getExtentsForGui(poseStack, consumer);
        this.bottom.getExtentsForGui(poseStack, consumer);
    }
}