package betterblockentities.mixin.sodium.render;

/* local */
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.OverlayRenderer;
import betterblockentities.client.render.immediate.blockentity.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.RenderingMode;
import betterblockentities.client.render.immediate.blockentity.SpecialBlockEntityManager;

/* minecraft */
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.*;

/* mojang */
import com.mojang.blaze3d.vertex.PoseStack;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* java/misc */
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.SortedSet;

@Pseudo
@Mixin(SodiumWorldRenderer.class)
public abstract class SodiumWorldRendererMixin {
    @Inject(method = "extractBlockEntity", at = @At("HEAD"), cancellable = true)
    private void extractBlockEntity(BlockEntity blockEntity, PoseStack poseStack, Camera camera, float tickDelta, Long2ObjectMap<SortedSet<BlockDestructionProgress>> progression, LevelRenderState levelRenderState, CallbackInfo ci) {
        if (!ConfigCache.masterOptimize) return;

        BlockEntityExt ext = (BlockEntityExt) blockEntity;
        if (!ext.supportedBlockEntity()) return;
        if (!BBEConfig.OptEnabledTable.ENABLED[ext.optKind() & 0xFF]) return;

        /* never cancel while fence pending */
        if (ext.renderingMode() != RenderingMode.TERRAIN && !ext.terrainMeshReady()) return;
        if (ext.renderingMode() == RenderingMode.TERRAIN && !ext.terrainMeshReady()) return;

        /* allow the renderer if there is breaking progress (we manage the breaking overlay inside the renderer) */
        if (OverlayRenderer.isBreaking(blockEntity.getBlockPos().asLong(), progression))
            return;

        /* if a special manager is not specified go ahead and cancel this renderer */
        boolean cancel = !ext.hasSpecialManager() || !SpecialBlockEntityManager.shouldRender(blockEntity);
        if (cancel) {
            ci.cancel();
        }
    }
}