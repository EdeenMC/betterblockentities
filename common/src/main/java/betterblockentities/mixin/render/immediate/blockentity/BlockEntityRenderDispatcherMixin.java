package betterblockentities.mixin.render.immediate.blockentity;

/* local */
import betterblockentities.client.compat.SableCompat;
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;
import betterblockentities.client.render.immediate.blockentity.manager.SpecialBlockEntityManager;
import betterblockentities.client.render.immediate.blockentity.misc.CrumblingOverlayConsumer;
import betterblockentities.client.render.immediate.blockentity.misc.CrumblingRenderContext;
import betterblockentities.platform.GlobalScope;
import betterblockentities.render.AltRenderers;

/* minecraft */
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow public Camera camera;
    @Shadow public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E blockEntity);

    @Shadow
    private static <T extends BlockEntity> void setupAndRender(
            BlockEntityRenderer<T> renderer,
            T blockEntity,
            float tickDelta,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        throw new AssertionError();
    }

    @Shadow
    private static void tryRender(BlockEntity blockEntity, Runnable runnable) {
        throw new AssertionError();
    }

    @Inject(method = "prepare", at = @At("TAIL"))
    private void prepareAltRenderDispatcher(Level level, Camera camera, HitResult hitResult, CallbackInfo ci) {
        if (GlobalScope.altRenderDispatcher != null) {
            GlobalScope.altRenderDispatcher.prepare(level, camera, hitResult);
        }
    }

    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void reloadAltRenderDispatcher(ResourceManager resourceManager, CallbackInfo ci) {
        GlobalScope.altRenderDispatcher.onResourceManagerReload(resourceManager);
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(BlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource consumer, CallbackInfo ci) {
        if (AltRenderers.renderersLoaded()) {
            GlobalScope.altRenderDispatcher.render(entity, tickDelta, matrices, consumer);
        }

        if (AltRenderers.hasRendererOverride(entity.getType())) {
            ci.cancel();
            return;
        }

        BlockEntityExt ext = (BlockEntityExt)entity;
        if (shouldManage(entity)) {
            boolean cancel = !ext.hasSpecialManager() || !SpecialBlockEntityManager.shouldRender(entity);
            if (CrumblingRenderContext.isActive()) {
                renderCrumblingOnly(entity, tickDelta, matrices, consumer);
            }
            if (cancel) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void renderAltItem(
            BlockEntity blockEntity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            int overlay,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (AltRenderers.renderersLoaded()
                && !GlobalScope.altRenderDispatcher.renderItem(blockEntity, poseStack, bufferSource, light, overlay)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean shouldManage(BlockEntity blockEntity) {
        // Sable compat: sublevel (contraption) block entities are rendered by Sable's own
        // dispatcher and never baked into Sodium terrain, so never cull their BER path.
        if (SableCompat.isOnSubLevel(blockEntity)) {
            return false;
        }
        BlockEntityExt ext = (BlockEntityExt) blockEntity;
        return ext.terrainRenderingReady()
                && BBEConfig.OptEnabledTable.ENABLED[ext.optKind() & 0xFF];
    }

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher.tryRender (Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/lang/Runnable;)V"
            )
    )
    private void suppressTerrainSignModel(
            BlockEntity blockEntity,
            Runnable runnable,
            Operation<Void> original
    ) {
        BlockEntityExt ext = (BlockEntityExt) blockEntity;
        if (!shouldManage(blockEntity)
                || ext.optKind() != InstancedBlockEntityManager.OptKind.SIGN
                || AltRenderers.hasRendererOverride(blockEntity.getType())) {
            original.call(blockEntity, runnable);
            return;
        }

        boolean previous = GlobalScope.limitVanillaSignRendering;
        GlobalScope.limitVanillaSignRendering = true;
        try {
            original.call(blockEntity, runnable);
        } finally {
            GlobalScope.limitVanillaSignRendering = previous;
        }
    }

    @Unique
    private <E extends BlockEntity> void renderCrumblingOnly(
            E blockEntity,
            float tickDelta,
            PoseStack poseStack,
            MultiBufferSource bufferSource
    ) {
        BlockEntityRenderer<E> renderer = this.getRenderer(blockEntity);
        if (renderer == null
                || !blockEntity.hasLevel()
                || !blockEntity.getType().isValid(blockEntity.getBlockState())
                || this.camera == null
                || !renderer.shouldRender(blockEntity, this.camera.getPosition())) {
            return;
        }

        MultiBufferSource crumblingOnly = new CrumblingOverlayConsumer.CrumblingOnlyBufferSource(bufferSource);
        tryRender(blockEntity, () -> setupAndRender(renderer, blockEntity, tickDelta, poseStack, crumblingOnly));
    }

    @WrapOperation(
            method = "renderItem",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher.tryRender (Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/lang/Runnable;)V",
                    ordinal = 0
            )
    )
    public <E extends BlockEntity> void detectItemInvokedRenderers(BlockEntity blockEntity, Runnable runnable, Operation<Void> original) {
        GlobalScope.isItemInvoked = true;
        original.call(blockEntity, runnable);
        GlobalScope.isItemInvoked = false;
    }
}
