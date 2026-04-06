package betterblockentities.mixin.render.texture;

/* minecraft */
import net.minecraft.client.renderer.texture.MipmapGenerator;

/* mojang */
import com.mojang.blaze3d.platform.NativeImage;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MipmapGenerator.class)
public interface MipMapGeneratorAccessor {
    @Invoker("scaleAlphaToCoverage")
    static void scaleAlphaToCoverageInvoke(NativeImage image, float h, float g, float f) {
        throw new AssertionError();
    }
}
