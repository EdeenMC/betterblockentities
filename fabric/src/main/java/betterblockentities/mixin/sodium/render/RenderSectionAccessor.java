package betterblockentities.mixin.sodium.render;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSection.class)
public interface RenderSectionAccessor {

    @Accessor("chunkX")
    int getChunkX();

    @Accessor("chunkY")
    int getChunkY();

    @Accessor("chunkZ")
    int getChunkZ();
}
