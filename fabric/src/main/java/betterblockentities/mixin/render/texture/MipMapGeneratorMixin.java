package betterblockentities.mixin.render.texture;

/* minecraft */
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MipmapGenerator.class)
public abstract class MipMapGeneratorMixin {
    @Redirect(method = "generateMipLevels(Lnet/minecraft/resources/Identifier;[Lcom/mojang/blaze3d/platform/NativeImage;ILnet/minecraft/client/renderer/texture/MipmapStrategy;F)[Lcom/mojang/blaze3d/platform/NativeImage;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/MipmapGenerator;scaleAlphaToCoverage(Lcom/mojang/blaze3d/platform/NativeImage;FFF)V")
    )
    private static void skipEntityTextureAlphaCoverage(NativeImage image, float h, float g, float f, Identifier identifier) {
        if (shouldScale(identifier, image, h, g, f)) {
            MipMapGeneratorAccessor.scaleAlphaToCoverageInvoke(image, h, g, f);
        }
    }

    @Unique
    private static boolean shouldScale(Identifier identifier, NativeImage image, float h, float g, float f) {
        return !identifier.getPath().contains("entity");
    }
}