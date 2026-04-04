package betterblockentities.mixin.render.immediate.blockentity.extentions;

/* local */
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityRenderStateExt;

/* minecraft */
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockEntityRenderState.class)
public class BlockEntityRenderStateMixin implements BlockEntityRenderStateExt {
    @Unique private BlockEntity blockEntity;

    @Override public void blockEntity(BlockEntity blockEntity) { this.blockEntity = blockEntity; }
    @Override public BlockEntity blockEntity() { return this.blockEntity; }
}
