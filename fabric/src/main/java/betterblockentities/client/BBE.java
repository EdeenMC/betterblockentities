package betterblockentities.client;

/* local */
import betterblockentities.registration.RegistrationCollection;
import betterblockentities.render.AltRenderDispatcher;
import betterblockentities.client.gui.DebugScreen;
import betterblockentities.client.gui.config.BBEConfig;
import betterblockentities.client.gui.config.wrapper.GenericConfigWrapper;
import betterblockentities.mixin.gui.DebugScreenEntriesAccessor;

/* fabric */
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/* minecraft */
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/* java/misc */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BBE implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        getLogger().info("Checking for loaded mods for compact...");
        LoadedModList.checkForLoadedMods();

        getLogger().info("Building config cache from user defined config file...");
        BBEConfig.updateConfigCache();

        DebugScreenEntriesAccessor.invokeRegister(GlobalScope.DEBUG_ID, new DebugScreen());

        getLogger().info("Collecting API Entrypoint's...");
        RegistrationCollection.collectEntryPoints();
    }

    public static class GlobalScope {
        /* debug */
        private static final Logger LOGGER = LoggerFactory.getLogger("BBE-Logger");
        public static DebugEntryCategory DEBUG_CATEGORY = new DebugEntryCategory(Component.literal("BBE"), 10F);
        public static Identifier DEBUG_ID = Identifier.fromNamespaceAndPath("bbe", "debug");

        /* config */
        public static final BBEConfig CONFIG = new BBEConfig();
        public static final GenericConfigWrapper OPTIONS = new GenericConfigWrapper();

        /* runtime render data */
        public static Frustum frustum;
        public static AltRenderDispatcher altRenderDispatcher;
        public static List<BlockEntityRenderState> altBlockEntityRenderStates = new ArrayList<>();
    }

    public static class LoadedModList {
        public static boolean EMF = false;
        public static boolean NVIDIUM = false;

        public static void checkForLoadedMods() {
            if (FabricLoader.getInstance().isModLoaded("entity_model_features") &&
                    !((boolean)GlobalScope.CONFIG.HIDDEN.getOption("override.forced_updatescheduler").getValue())) {
                EMF = true;
            }
            if (FabricLoader.getInstance().isModLoaded("nvidium")) {
                NVIDIUM = true;
            }
        }
    }

    /* global logger, used for info logging, error handling, etc... */
    public static Logger getLogger() {
        return GlobalScope.LOGGER;
    }
 }
