package betterblockentities.mixin.render.immediate.blockentity.extentions;

/* local */
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;

/* minecraft */
import net.minecraft.world.level.block.entity.BlockEntity;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityExt {
    @Unique private RenderingMode renderingMode = RenderingMode.TERRAIN;
    @Unique private boolean terrainMeshReady = true;
    @Unique private boolean terrainRendererAvailable = false;
    @Unique private boolean hasSpecialManager = false;
    @Unique private byte bbeKind = 0;
    @Unique private boolean supportedBlockEntity = false;
    @Unique private boolean terrainRenderingReady = false;

    @Override public boolean supportedBlockEntity() { return supportedBlockEntity; }
    @Override public void supportedBlockEntity(boolean bl) {
        supportedBlockEntity = bl;
        updateTerrainRenderingReady();
    }

    @Override public RenderingMode renderingMode() { return renderingMode; }
    @Override public void renderingMode(RenderingMode mode) {
        renderingMode = mode;
        updateTerrainRenderingReady();
    }

    @Override public boolean terrainMeshReady() { return terrainMeshReady; }
    @Override public void terrainMeshReady(boolean bl) {
        terrainMeshReady = bl;
        updateTerrainRenderingReady();
    }

    @Override public boolean terrainRendererAvailable() { return terrainRendererAvailable; }
    @Override public void terrainRendererAvailable(boolean available) {
        terrainRendererAvailable = available;
        updateTerrainRenderingReady();
    }

    @Override public boolean terrainRenderingReady() { return terrainRenderingReady; }

    @Override public boolean hasSpecialManager() { return hasSpecialManager; }
    @Override public void hasSpecialManager(boolean bl) { hasSpecialManager = bl; }

    @Override public byte optKind() { return bbeKind; }
    @Override public void optKind(byte k) { bbeKind = k; }

    @Unique private void updateTerrainRenderingReady() {
        terrainRenderingReady = supportedBlockEntity
                && terrainRendererAvailable
                && terrainMeshReady
                && renderingMode == RenderingMode.TERRAIN;
    }
}