package betterblockentities.mixin.model.item;

/* local */
import betterblockentities.client.gui.config.ConfigCache;

/* minecraft */
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.SpecialDates;

/* mixin */
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/* java/misc */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ClientItemInfoLoader.class)
public class ClientItemInfoLoaderMixin {
    @Unique private static final Identifier CHEST_ITEM_ID = Identifier.withDefaultNamespace("chest");
    @Unique private static final Identifier TRAPPED_CHEST_ITEM_ID = Identifier.withDefaultNamespace("trapped_chest");

    @Inject(method = "scheduleLoad", at = @At("RETURN"), cancellable = true)
    private static void rewriteChestItemAssets(ResourceManager resourceManager, Executor executor,
                                               CallbackInfoReturnable<CompletableFuture<ClientItemInfoLoader.LoadedClientInfos>> cir
    ) {
        if (!ShouldOverrideVanilla()) {
            return;
        }
        CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> original = cir.getReturnValue();
        cir.setReturnValue(original.thenApply(ClientItemInfoLoaderMixin::rewriteChestItems));
    }

    @Unique
    private static boolean ShouldOverrideVanilla() {
        return ConfigCache.optimizeChests
                && !ConfigCache.christmasChests
                && SpecialDates.isExtendedChristmas();
    }

    @Unique
    private static ClientItemInfoLoader.LoadedClientInfos rewriteChestItems(ClientItemInfoLoader.LoadedClientInfos loaded) {
        Map<Identifier, ClientItem> map = new HashMap<>(loaded.contents());

        patchOne(map, CHEST_ITEM_ID, ChestSpecialRenderer.NORMAL_CHEST_TEXTURE);
        patchOne(map, TRAPPED_CHEST_ITEM_ID, ChestSpecialRenderer.TRAPPED_CHEST_TEXTURE);

        return new ClientItemInfoLoader.LoadedClientInfos(Map.copyOf(map));
    }

    @Unique
    private static void patchOne(Map<Identifier, ClientItem> map, Identifier id, Identifier fallbackTexture) {
        ClientItem existing = map.get(id);
        if (existing == null) return;

        ItemModel.Unbaked patchedModel = patchModel(existing.model(), fallbackTexture);
        if (patchedModel != existing.model()) {
            map.put(id, new ClientItem(patchedModel, existing.properties(), existing.registrySwapper()));
        }
    }

    @Unique
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ItemModel.Unbaked patchModel(ItemModel.Unbaked model, Identifier fallbackTexture) {

        if (model instanceof SpecialModelWrapper.Unbaked(Identifier base, SpecialModelRenderer.Unbaked special)) {
            if (special instanceof ChestSpecialRenderer.Unbaked(Identifier tex, float openness)) {
                if (tex.equals(ChestSpecialRenderer.GIFT_CHEST_TEXTURE) || tex.getPath().contains("Christmas")) {
                    ChestSpecialRenderer.Unbaked replaced = new ChestSpecialRenderer.Unbaked(fallbackTexture, openness);
                    return new SpecialModelWrapper.Unbaked(base, replaced);
                }
            }
            return model;
        }

        if (model instanceof SelectItemModel.Unbaked(SelectItemModel.UnbakedSwitch<?, ?> sw, Optional<ItemModel.Unbaked> oldFallback)) {
            List<SelectItemModel.SwitchCase> cases = (List) sw.cases();

            boolean changed = false;
            List<SelectItemModel.SwitchCase> newCases = new ArrayList<>(cases.size());
            for (SelectItemModel.SwitchCase sc : cases) {
                ItemModel.Unbaked child = sc.model();
                ItemModel.Unbaked patchedChild = patchModel(child, fallbackTexture);
                if (patchedChild != child) changed = true;
                newCases.add(patchedChild == child ? sc : new SelectItemModel.SwitchCase(sc.values(), patchedChild));
            }

            Optional<ItemModel.Unbaked> newFallback = oldFallback.map(fb -> patchModel(fb, fallbackTexture));
            if (!oldFallback.equals(newFallback)) changed = true;

            if (!changed) return model;

            SelectItemModel.UnbakedSwitch newSw = new SelectItemModel.UnbakedSwitch(sw.property(), newCases);
            return new SelectItemModel.Unbaked(newSw, newFallback);
        }
        return model;
    }
}
