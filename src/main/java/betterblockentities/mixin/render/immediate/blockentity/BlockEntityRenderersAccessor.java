package betterblockentities.mixin.render.immediate.blockentity;

/* minecraft */
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* java/misc */
import java.util.Map;

@Mixin(BlockEntityRenderers.class)
public interface BlockEntityRenderersAccessor {
    @Invoker("register")
    static <T extends BlockEntity, S extends BlockEntityRenderState> void invokeRegister(
            BlockEntityType<? extends T> type,
            BlockEntityRendererProvider<T, S> renderer
    ) {
        throw new AssertionError();
    }

    @Accessor("PROVIDERS")
    static Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> getProviders() {
        throw new AssertionError();
    }
}
