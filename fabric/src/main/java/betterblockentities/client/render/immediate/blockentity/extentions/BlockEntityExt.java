package betterblockentities.client.render.immediate.blockentity.extentions;

import betterblockentities.client.render.immediate.blockentity.misc.RenderingMode;

public interface BlockEntityExt {
    boolean supportedBlockEntity();
    void supportedBlockEntity(boolean bl);

    RenderingMode renderingMode();
    void renderingMode(RenderingMode mode);

    boolean terrainMeshReady();
    void terrainMeshReady(boolean b);

    boolean hasSpecialManager();
    void hasSpecialManager(boolean bl);

    byte optKind();
    void optKind(byte k);
}
