package betterblockentities.mixin.sodium.render;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.render.AltRenderers;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.manager.SpecialBlockEntityManager;
import betterblockentities.client.render.immediate.blockentity.manager.SodiumBlockEntityCulling;

/* minecraft */
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.*;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* java/misc */
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;
import java.util.SortedSet;

@Pseudo
@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererMixin {
    @Unique private static final ThreadLocal<LongSet> bbe$extracted_breaking_states = ThreadLocal.withInitial(LongOpenHashSet::new);

    @Inject(method = "extractBlockEntities", at = @At("HEAD"))
    private void resetExtractedBreakingStates(Camera camera, float tickDelta, Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression, LevelRenderState levelRenderState, CallbackInfo ci) {
        bbe$extracted_breaking_states.get().clear();
    }

    @Inject(method = "extractBlockEntities", at = @At("TAIL"))
    private void extractTerrainBreakingStates(Camera camera, float tickDelta, Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (progression.isEmpty() || Minecraft.getInstance().level == null) return;

        LongSet extracted = bbe$extracted_breaking_states.get();
        try {
            for (long posAsLong : progression.keySet()) {
                BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(BlockPos.of(posAsLong));
                if (!extracted.contains(posAsLong) && SodiumBlockEntityCulling.needsSeparateBreakingState(blockEntity)) {
                    this.extractBlockEntity(blockEntity, new PoseStack(), camera, tickDelta, progression, levelRenderState);
                }
            }
        } finally {
            extracted.clear();
        }
    }

    /**
     * @author ceeden
     * @reason We overwrite this because we don't want other mods in here, this is a critical mixin that
     * can mess a lot of stuff up if other mods change execution flow. If additional renders needs to be ran or
     * something similar, our API is available for just that
     */
    @Overwrite
    private void extractBlockEntity(BlockEntity blockEntity, PoseStack poseStack, Camera camera, float tickDelta, Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression, LevelRenderState levelRenderState) {
        BlockEntityExt ext = (BlockEntityExt) blockEntity;
        boolean managed = bbe$shouldManage(ext);
        boolean hasAltRenderers = AltRenderers.renderersLoaded();

        if (managed && !ext.bbe$hasSpecialManager() && progression.isEmpty() && !hasAltRenderers) {
            return;
        }

        final BlockPos blockPos = blockEntity.getBlockPos();
        final SortedSet<BlockDestructionProgress> sortedSet = progression.get(blockPos.asLong());

        ModelFeatureRenderer.CrumblingOverlay crumblingOverlay;
        if (sortedSet != null && !sortedSet.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(
                    (double) blockPos.getX() - camera.position().x,
                    (double) blockPos.getY() - camera.position().y,
                    (double) blockPos.getZ() - camera.position().z
            );
            crumblingOverlay = new ModelFeatureRenderer.CrumblingOverlay(sortedSet.last().getProgress(), poseStack.last());
            poseStack.popPose();
        } else {
            crumblingOverlay = null;
        }

        if (crumblingOverlay != null) {
            bbe$extracted_breaking_states.get().add(blockPos.asLong());
        }

        /* extract our registered alt renderers for this block entity */
        if (hasAltRenderers) {
            List<BlockEntityRenderState> altBlockEntityRenderStates =
                    BBE.GlobalScope.altRenderDispatcher.tryExtractRenderStates(blockEntity, tickDelta, crumblingOverlay);
            for (BlockEntityRenderState altState : altBlockEntityRenderStates) {
                if (altState != null) {
                    BBE.GlobalScope.altBlockEntityRenderStates.add(altState);
                }
            }
        }

        /* manage this block entity if optimizations for it is turned on */
        if (managed && crumblingOverlay == null)  {
            boolean cancel = !ext.bbe$hasSpecialManager() || !SpecialBlockEntityManager.shouldRender(blockEntity);
            if (cancel) {
                return;
            }
        }

        /* extract the default registered render state */
        BlockEntityRenderState blockEntityRenderState =
                Minecraft.getInstance().getBlockEntityRenderDispatcher().tryExtractRenderState(blockEntity, tickDelta, crumblingOverlay);
        if (blockEntityRenderState != null) {
            levelRenderState.blockEntityRenderStates.add(blockEntityRenderState);
        }
    }

    @Unique
    private static boolean bbe$shouldManage(BlockEntityExt ext) {
        return ext.bbe$isSupportedBlockEntity()                               &&
                BBEConfig.OptEnabledTable.ENABLED[ext.bbe$getOptKind() & 0xFF] &&
                ext.bbe$isTerrainMeshReady();
    }
}