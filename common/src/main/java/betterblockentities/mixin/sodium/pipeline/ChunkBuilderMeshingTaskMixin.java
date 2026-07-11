package betterblockentities.mixin.sodium.pipeline;

/* local */
import betterblockentities.client.render.immediate.blockentity.manager.SodiumBlockEntityCulling;

/* sodium */
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.data.BuiltSectionInfo;

/* minecraft */
import net.minecraft.world.level.block.entity.BlockEntity;

/* mixin */
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(ChunkBuilderMeshingTask.class)
public abstract class ChunkBuilderMeshingTaskMixin {
    @WrapOperation(
            method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/data/BuiltSectionInfo$Builder;addBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Z)V"
            )
    )
    private void omitTerrainBlockEntitiesFromImmediateList(
            BuiltSectionInfo.Builder builder,
            BlockEntity entity,
            boolean cull,
            Operation<Void> original
    ) {
        if (!SodiumBlockEntityCulling.canRemoveFromImmediateList(entity)) {
            original.call(builder, entity, cull);
        }
    }
}
