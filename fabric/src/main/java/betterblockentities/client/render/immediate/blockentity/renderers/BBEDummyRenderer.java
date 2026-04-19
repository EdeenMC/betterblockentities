package betterblockentities.client.render.immediate.blockentity.renderers;

/* minecraft */
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.*;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

public class BBEDummyRenderer implements BlockEntityRenderer<BlockEntity, BlockEntityRenderState> {
    public BlockEntityRenderState createRenderState() {return new BlockEntityRenderState();}

    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) { }
    public void extractRenderState(final BlockEntity blockEntity, final BlockEntityRenderState state, final float partialTicks, final Vec3 cameraPosition, final ModelFeatureRenderer.CrumblingOverlay breakProgress) {}
    @Override public boolean shouldRenderOffScreen() { return false;}
    @Override public int getViewDistance() {return 0;}
    public boolean shouldRender(final Object blockEntity, final Vec3 cameraPosition) {return false;}
}
