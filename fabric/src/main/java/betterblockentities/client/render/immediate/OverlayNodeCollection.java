package betterblockentities.client.render.immediate;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* java/misc */
import java.util.ArrayList;
import java.util.List;

public class OverlayNodeCollection {
    private static final List<OverlaySubmit<?>> submits = new ArrayList<>();

    public static <S> void submitCrumblingOverlay(PoseStack poseStack, Model<? super S> model, S state, int light, int overlayCoords, int tint, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        OverlaySubmit<?> overlaySubmit = new OverlaySubmit<>(poseStack.last().copy(), model, state, light, overlayCoords, tint, crumblingOverlay);
        submits.add(overlaySubmit);
    }

    public static List<OverlaySubmit<?>> getSubmits() {
        return submits;
    }

    public static void clearSubmits() {
        submits.clear();
    }

    public record OverlaySubmit<S>(
            PoseStack.Pose poseStack,
            Model<? super S> model,
            S state,
            int lightCoords,
            int overlayCoords,
            int tintedColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) { }
}
