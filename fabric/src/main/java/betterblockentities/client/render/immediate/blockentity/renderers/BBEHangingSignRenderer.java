package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import betterblockentities.client.BBE;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/* java/misc */
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;

@Environment(EnvType.CLIENT)
public class BBEHangingSignRenderer extends BBEAbstractSignRenderer {
    public static final float MODEL_RENDER_SCALE = 1.0F;
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, -0.32F, 0.073F);
    private final Map<BBEHangingSignRenderer.ModelKey, Model.Simple> hangingSignModels;

    public BBEHangingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);

        /* filter out invalid sign types to prevent crashes, types in general are irrelevant in our render context */
        this.hangingSignModels = WoodType.values()
                .flatMap(woodType ->
                        Arrays.stream(BBEHangingSignRenderer.AttachmentType.values())
                                .map(attachmentType -> {
                                    Model.Simple model = createSignModel(
                                            context.entityModelSet(),
                                            woodType,
                                            attachmentType
                                    );
                                    return model == null
                                            ? null
                                            : Map.entry(new BBEHangingSignRenderer.ModelKey(woodType, attachmentType), model);
                                })
                )
                .filter(Objects::nonNull)
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static ModelLayerLocation createLocation(String string, String string2) {
        ModelLayerLocation layer;
        try {
            layer = new ModelLayerLocation(Identifier.withDefaultNamespace(string), string2);
            return layer;
        } catch (Exception e) {
            BBE.getLogger().error("Error creating model for {}", string);
            return null;
        }
    }

    public static ModelLayerLocation createHangingSignModelName(WoodType woodType, BBEHangingSignRenderer.AttachmentType attachmentType) {
        ModelLayerLocation layer = createLocation("hanging_sign/" + woodType.name() + "/" + attachmentType.getSerializedName(), "main");
        return layer;
    }

    public static Model.Simple createSignModel(EntityModelSet entityModelSet, WoodType woodType, BBEHangingSignRenderer.AttachmentType attachmentType) {
        ModelLayerLocation layer = createHangingSignModelName(woodType, attachmentType);

        if (layer != null) {
            return new Model.Simple(entityModelSet.bakeLayer(layer), RenderTypes::entityCutoutNoCull);
        }
        return null;
    }

    @Override
    protected float getSignModelRenderScale() {
        return 1.0F;
    }

    @Override
    protected float getSignTextRenderScale() {
        return 0.9F;
    }

    public static void translateBase(PoseStack poseStack, float f) {
        poseStack.translate(0.5, 0.9375, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(f));
        poseStack.translate(0.0F, -0.3125F, 0.0F);
    }

    @Override
    protected void translateSign(PoseStack poseStack, float f, BlockState blockState) {
        translateBase(poseStack, f);
    }

    @Override
    protected Model.Simple getSignModel(BlockState blockState, WoodType woodType) {
        BBEHangingSignRenderer.AttachmentType attachmentType = BBEHangingSignRenderer.AttachmentType.byBlockState(blockState);
        return (Model.Simple)this.hangingSignModels.get(new BBEHangingSignRenderer.ModelKey(woodType, attachmentType));
    }

    @Override
    protected Material getSignMaterial(WoodType woodType) {
        return Sheets.getHangingSignMaterial(woodType);
    }

    @Override
    protected Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    @Environment(EnvType.CLIENT)
    public static enum AttachmentType implements StringRepresentable {
        WALL("wall"),
        CEILING("ceiling"),
        CEILING_MIDDLE("ceiling_middle");

        private final String name;

        private AttachmentType(final String string2) {
            this.name = string2;
        }

        public static BBEHangingSignRenderer.AttachmentType byBlockState(BlockState blockState) {
            if (blockState.getBlock() instanceof CeilingHangingSignBlock) {
                return blockState.getValue(BlockStateProperties.ATTACHED) ? CEILING_MIDDLE : CEILING;
            } else {
                return WALL;
            }
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Environment(EnvType.CLIENT)
    public record ModelKey(WoodType woodType, BBEHangingSignRenderer.AttachmentType attachmentType) {
    }
}
