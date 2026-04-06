package betterblockentities.mixin.render.immediate.blockentity.chest;

import net.minecraft.world.level.block.entity.ChestLidController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChestLidController.class)
public interface ChestLidControllerAccessor {
    @Accessor("shouldBeOpen")
    boolean getOpen();

    @Accessor("shouldBeOpen")
    void setOpen(boolean value);

    @Accessor("openness")
    float getProgress();

    @Accessor("openness")
    void setProgress(float value);

    @Accessor("oOpenness")
    float getLastProgress();

    @Accessor("oOpenness")
    void setLastProgress(float value);
}
