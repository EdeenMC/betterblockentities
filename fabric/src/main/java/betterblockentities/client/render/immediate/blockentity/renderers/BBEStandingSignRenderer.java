package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import betterblockentities.client.BBE;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/* java/misc */
import java.util.Map;
import com.google.common.collect.ImmutableMap;

public class BBEStandingSignRenderer extends BBEAbstractSignRenderer {
    public static final float RENDER_SCALE = 0.6666667F;
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 0.046666667F);
    private final Map<WoodType, BBEStandingSignRenderer.Models> signModels;

    /* filter out invalid sign types to prevent crashes, types in general are irrelevant in our render context */
    public BBEStandingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.signModels = WoodType.values()
                .<Map.Entry<WoodType, BBEStandingSignRenderer.Models>>mapMulti((woodType, consumer) -> {
                    Model.Simple standing = createSignModel(context.entityModelSet(), woodType, true);
                    Model.Simple wall = createSignModel(context.entityModelSet(), woodType, false);

                    if (standing != null && wall != null) {
                        consumer.accept(Map.entry(
                                woodType,
                                new BBEStandingSignRenderer.Models(standing, wall)
                        ));
                    }
                })
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    protected Model.Simple getSignModel(BlockState blockState, WoodType woodType) {
        BBEStandingSignRenderer.Models models = (BBEStandingSignRenderer.Models)this.signModels.get(woodType);
        return blockState.getBlock() instanceof StandingSignBlock ? models.standing() : models.wall();
    }

    @Override
    protected Material getSignMaterial(WoodType woodType) {
        return Sheets.getSignMaterial(woodType);
    }

    @Override
    protected float getSignModelRenderScale() {
        return 0.6666667F;
    }

    @Override
    protected float getSignTextRenderScale() {
        return 0.6666667F;
    }

    private static void translateBase(PoseStack poseStack, float f) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(f));
    }

    @Override
    protected void translateSign(PoseStack poseStack, float f, BlockState blockState) {
        translateBase(poseStack, f);
        if (!(blockState.getBlock() instanceof StandingSignBlock)) {
            poseStack.translate(0.0F, -0.3125F, -0.4375F);
        }
    }

    @Override
    protected Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    public static Model.Simple createSignModel(EntityModelSet entityModelSet, WoodType woodType, boolean bl) {
        ModelLayerLocation modelLayerLocation;
        try {
            modelLayerLocation = bl ? ModelLayers.createStandingSignModelName(woodType) : ModelLayers.createWallSignModelName(woodType);
            return new Model.Simple(entityModelSet.bakeLayer(modelLayerLocation), RenderTypes::entityCutoutNoCull);
        } catch(Exception e) {
            BBE.getLogger().error("Error creating standing sign model for wood type {}", woodType.name());
            return null;
        }
    }

    @Environment(EnvType.CLIENT)
    record Models(Model.Simple standing, Model.Simple wall) {
    }
}
