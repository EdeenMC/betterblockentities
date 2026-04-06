package betterblockentities.mixin.model.modelpart;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
    @Accessor("cubes")
    List<ModelPart.Cube> getCubes();

    @Accessor("children")
    Map<String, ModelPart> getChildren();

    @Invoker("visit")
    void visitInvoke(PoseStack poseStack, ModelPart.Visitor visitor);

    @Invoker("translateAndRotate")
    void translateAndRotateInvoke(PoseStack poseStack);

    @Accessor("x")
    float getX();

    @Accessor("y")
    float getY();

    @Accessor("z")
    float getZ();

    @Accessor("xRot")
    float getXRot();

    @Accessor("yRot")
    float getYRot();

    @Accessor("zRot")
    float getZRot();

    @Accessor("xScale")
    float getXScale();

    @Accessor("yScale")
    float getYScale();

    @Accessor("zScale")
    float getZScale();
}
