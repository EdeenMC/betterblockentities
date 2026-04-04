package betterblockentities.mixin.render.immediate.blockentity;

/* local */
import betterblockentities.api.render.AltRenderers;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.blockentity.renderers.*;

/* minecraft */
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* java/misc */
import java.util.Map;

@Mixin(BlockEntityRenderers.class)
public class BlockEntityRenderersMixin {
    /**
     * replace vanilla renderers, we can't mixin into the static initializer as we need this function's
     * reload capabilities.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "createEntityRenderers", at = @At("HEAD"))
    private static void replaceVanillaRenderers(CallbackInfoReturnable<Map<BlockEntityType<?>, BlockEntityRenderer<?, ?>>> cir) {

        //take ConfigCache.masterOptimize into account

        if (AltRenderers.hasRendererOverride(BlockEntityType.SIGN)) {
            removeRegistration(BlockEntityType.SIGN);
        } else {
            BlockEntityRendererProvider r0 =
                    ConfigCache.optimizeSigns ? BBEStandingSignRenderer::new : StandingSignRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.SIGN, r0);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.HANGING_SIGN)) {
            removeRegistration(BlockEntityType.HANGING_SIGN);
        } else {
            BlockEntityRendererProvider r1 =
                    ConfigCache.optimizeSigns ? BBEHangingSignRenderer::new : HangingSignRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.HANGING_SIGN, r1);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.CHEST)) {
            removeRegistration(BlockEntityType.CHEST);
        } else {
            BlockEntityRendererProvider r2 =
                    ConfigCache.optimizeChests ? BBEChestRenderer::new : ChestRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.CHEST, r2);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.ENDER_CHEST)) {
            removeRegistration(BlockEntityType.ENDER_CHEST);
        } else {
            BlockEntityRendererProvider r3 =
                    ConfigCache.optimizeChests ? BBEChestRenderer::new : ChestRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.ENDER_CHEST, r3);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.TRAPPED_CHEST)) {
            removeRegistration(BlockEntityType.TRAPPED_CHEST);
        } else {
            BlockEntityRendererProvider r4 =
                    ConfigCache.optimizeChests ? BBEChestRenderer::new : ChestRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.TRAPPED_CHEST, r4);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.BANNER)) {
            removeRegistration(BlockEntityType.BANNER);
        } else {
            BlockEntityRendererProvider r5 =
                    ConfigCache.optimizeBanners ? BBEBannerRenderer::new : BannerRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.BANNER, r5);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.SHULKER_BOX)) {
            removeRegistration(BlockEntityType.SHULKER_BOX);
        } else {
            BlockEntityRendererProvider r6 =
                    ConfigCache.optimizeShulker ? BBEShulkerBoxRenderer::new : ShulkerBoxRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.SHULKER_BOX, r6);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.BED)) {
            removeRegistration(BlockEntityType.BED);
        } else {
            BlockEntityRendererProvider r7 =
                    ConfigCache.optimizeBeds ? BBEBedRenderer::new : BedRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.BED, r7);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.BELL)) {
            removeRegistration(BlockEntityType.BELL);
        } else {
            BlockEntityRendererProvider r8 =
                    ConfigCache.optimizeBells ? BBEBellRenderer::new : BellRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.BELL, r8);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.DECORATED_POT)) {
            removeRegistration(BlockEntityType.DECORATED_POT);
        } else {
            BlockEntityRendererProvider r9 =
                    ConfigCache.optimizeDecoratedPots ? BBEDecoratedPotRenderer::new : DecoratedPotRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.DECORATED_POT, r9);
        }

        if (AltRenderers.hasRendererOverride(BlockEntityType.COPPER_GOLEM_STATUE)) {
            removeRegistration(BlockEntityType.COPPER_GOLEM_STATUE);
        } else {
            BlockEntityRendererProvider r10 =
                    ConfigCache.optimizeCopperGolemStatue ? BBECopperGolemStatueBlockRenderer::new : CopperGolemStatueBlockRenderer::new;
            BlockEntityRenderersAccessor.invokeRegister(BlockEntityType.COPPER_GOLEM_STATUE, r10);
        }
    }

    /*
        this is super shit but for some reason the renderer seem to be tied to if the block-entity itself gets added to a render section :/
        i.e. we cant just remove it, and we cant pass a null value. performance wise it should be fine as the "dummy" renderer basically does nothing
    */
    @Unique
    private static void removeRegistration(BlockEntityType<?> blockEntityType) {
        //BlockEntityRenderersAccessor.getProviders().remove(blockEntityType);
        BlockEntityRenderersAccessor.invokeRegister(blockEntityType, ctx -> new BBEDummyRenderer());
    }
}
