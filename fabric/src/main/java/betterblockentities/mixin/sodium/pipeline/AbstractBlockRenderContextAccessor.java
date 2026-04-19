package betterblockentities.mixin.sodium.pipeline;

/* minecraft */
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* java/misc */
import org.jetbrains.annotations.Nullable;

@Pseudo
@Mixin(AbstractBlockRenderContext.class)
public interface AbstractBlockRenderContextAccessor {
    @Accessor("level")
    BlockAndTintGetter getLevel();

    @Accessor("random")
    RandomSource getRandom();

    @Accessor("defaultRenderType")
    ChunkSectionLayer getDefaultRenderType();

    @Accessor("defaultRenderType")
    void setDefaultRenderType(@Nullable ChunkSectionLayer layer);

    @Accessor("state")
    BlockState getState();
    @Accessor("state")
    void setState(BlockState state);

    @Accessor("pos")
    BlockPos getPos();
    @Accessor("pos")
    void setPos(BlockPos pos);

    @Accessor("allowDowngrade")
    boolean getAllowDowngrade();
    @Accessor("allowDowngrade")
    void setAllowDowngrade(boolean allow);

    @Accessor("slice")
    LevelSlice getSlice();
    @Accessor("slice")
    void setSlice(LevelSlice slice);

    @Invoker("getForEmitting")
    MutableQuadViewImpl getEmitterInvoke();

    @Invoker("prepareAoInfo")
    void prepareAoInfoInvoke(boolean modelAo);

    @Invoker("prepareCulling")
    void prepareCullingInvoke(boolean enableCulling);

    @Invoker("isFaceCulled")
    boolean isFaceCulledInvoke(@Nullable Direction face);
}
