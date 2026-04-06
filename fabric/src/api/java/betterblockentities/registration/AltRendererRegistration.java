package betterblockentities.registration;

/* local */
import betterblockentities.render.AltRendererProvider;
import betterblockentities.data.RegistrationInfo;
import betterblockentities.data.SupportedBlockEntityTypes;

@SuppressWarnings("rawtypes")
public class AltRendererRegistration {
    public void registerRenderer(SupportedBlockEntityTypes blockEntityType, AltRendererProvider rendererProvider) {
        RegistrationInfo altInfo = new RegistrationInfo(blockEntityType, rendererProvider);
        RegistrationCollection.addRegistration(rendererProvider, altInfo);
    }
}
