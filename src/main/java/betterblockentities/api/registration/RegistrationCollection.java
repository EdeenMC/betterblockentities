package betterblockentities.api.registration;

/* local */
import betterblockentities.api.data.RegistrationInfo;
import betterblockentities.api.data.ValidationChecks;
import betterblockentities.api.render.AltRendererProvider;
import betterblockentities.client.BBE;

/* fabric */
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

/* java/misc */
import java.util.*;

@SuppressWarnings("rawtypes")
public class RegistrationCollection {
    public static final String ENTRYPOINT_KEY = "bbe:renderer_registration_api";
    private static final Map<AltRendererProvider, RegistrationInfo> REGISTRATIONS = new HashMap<>();

    public static void addRegistration(AltRendererProvider provider, RegistrationInfo regInfo) {
        if (!ValidationChecks.registrationTypeValid(regInfo)) {
            return;
        }

        REGISTRATIONS.put(provider, regInfo);
    }

    public static void collectEntryPoints() {
        List<EntrypointContainer<BBEApiEntryPoint>> containers =
                FabricLoader.getInstance().getEntrypointContainers(ENTRYPOINT_KEY, BBEApiEntryPoint.class);

        AltRendererRegistration context = new AltRendererRegistration();

        BBE.getLogger().info("Registering alt renderers via each collected entrypoint...");

        for (EntrypointContainer<BBEApiEntryPoint> container : containers) {
            BBEApiEntryPoint entrypoint = container.getEntrypoint();
            ModContainer provider = container.getProvider();

            String modId = provider.getMetadata().getId();
            String entryPointId = entrypoint.getClass().getName();

            try {
                entrypoint.registerRenderers(context);
            } catch (Exception e) {
                BBE.getLogger().error("Failed to register alt renderers for modId: {} with entrypoint: {} -> {}",
                        modId, entryPointId, e);
            }
        }
    }

    public static Map<AltRendererProvider, RegistrationInfo> getRegistrations() {
        return REGISTRATIONS;
    }
}
