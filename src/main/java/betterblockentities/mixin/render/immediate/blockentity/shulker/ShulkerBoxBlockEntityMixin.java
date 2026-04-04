package betterblockentities.mixin.render.immediate.blockentity.shulker;

/* local */
import betterblockentities.client.gui.config.ConfigCache;
import betterblockentities.client.render.immediate.blockentity.extentions.BlockEntityExt;
import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;

/* minecraft */
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {
    @Unique private InstancedBlockEntityManager manager = new InstancedBlockEntityManager((BlockEntity)(Object)this);

    @Inject(method = "<init>(Lnet/minecraft/world/item/DyeColor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        BlockEntityExt ext = (BlockEntityExt)(Object)this;
        ext.supportedBlockEntity(true);
        ext.optKind(InstancedBlockEntityManager.OptKind.SHULKER);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private static void onTick(Level level, BlockPos blockPos, BlockState blockState, ShulkerBoxBlockEntity shulkerBoxBlockEntity, CallbackInfo ci) {
        ShulkerBoxBlockEntityMixin self = (ShulkerBoxBlockEntityMixin)(Object)shulkerBoxBlockEntity;

        self.manager.tick(shulkerBoxBlockEntity.getProgress(0.5f) > 0.01f, ConfigCache.shulkerAnims);
    }
}
