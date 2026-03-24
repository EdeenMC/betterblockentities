package betterblockentities.mixin.render.immediate.blockentity.chest;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.model.overrides.ChestModelOverride;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.phys.Vec3;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin <T extends BlockEntity & LidBlockEntity> {
    @Shadow @Final public static MultiblockChestResources<ModelLayerLocation> LAYERS;
    @Final @Shadow @Mutable private MultiblockChestResources<ChestModel> models;

    @Unique private MultiblockChestResources<ChestModel> orgModels;
    @Unique private MultiblockChestResources<ChestModel> bbeModels;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cacheAndInitModels(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        orgModels = LAYERS.map(layer -> new ChestModel(context.bakeLayer(layer)));
        bbeModels = LAYERS.map(layer -> new ChestModelOverride(context.bakeLayer(layer)));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V", at = @At("TAIL"), cancellable = true)
    public void extractRenderState(T blockEntity, ChestRenderState chestRenderState, float f, Vec3 vec3, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)chestRenderState;
        stateExt.blockEntity(blockEntity);
    }

    @Redirect(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/ChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/SubmitNodeCollector.submitModel (Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;IIILnet/minecraft/client/resources/model/sprite/SpriteId;Lnet/minecraft/client/resources/model/sprite/SpriteGetter;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    public <S> void manageSubmit(SubmitNodeCollector collector, Model<S> model, S state, PoseStack poseStack, int light, int overlayCoords, int tint, SpriteId spriteId, SpriteGetter spriteGetter, int i4, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, @Local(index = 1)ChestRenderState chestRenderState) {
        this.models = orgModels;

        if (!ConfigCache.optimizeChests || !ConfigCache.masterOptimize) {
            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
            return;
        }

        BlockEntityRenderStateExt stateExt = (BlockEntityRenderStateExt)chestRenderState;

        boolean managed = OverlayRenderer.manageCrumblingOverlay(stateExt.blockEntity(), poseStack, model, state, light, overlayCoords, tint, crumblingOverlay);
        if (!managed) {
            this.models = bbeModels;

            collector.submitModel(model, state, poseStack, light, overlayCoords, tint, spriteId, spriteGetter, i4, crumblingOverlay);
        }
    }

    @Inject(method = "xmasTextures", at = @At("HEAD"), cancellable = true)
    private static void xmasTextures(CallbackInfoReturnable<Boolean> cir) {
        if (ConfigCache.christmasChests && ConfigCache.optimizeChests && ConfigCache.masterOptimize)
            cir.setReturnValue(true);
    }
}
