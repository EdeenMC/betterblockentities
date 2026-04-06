package betterblockentities.mixin.render.immediate.blockentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockEntityRenderState.class)
public interface BlockEntityRenderStateAccessor {
    @Accessor("blockState")
    BlockState getBlockState();
}
