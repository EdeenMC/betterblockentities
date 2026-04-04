package betterblockentities.api.registration;

/* local */
import betterblockentities.api.render.AltRendererProvider;
import betterblockentities.api.data.RegistrationInfo;
import betterblockentities.api.data.SupportedBlockEntityTypes;

@SuppressWarnings("rawtypes")
public class AltRendererRegistration {
    public void registerRenderer(SupportedBlockEntityTypes blockEntityType, AltRendererProvider rendererProvider) {
        RegistrationInfo altInfo = new RegistrationInfo(blockEntityType, rendererProvider);
        RegistrationCollection.addRegistration(rendererProvider, altInfo);
    }
}
