package betterblockentities.api.data;

/* local */
import betterblockentities.api.render.AltRendererProvider;

@SuppressWarnings("rawtypes")
public record RegistrationInfo(
        SupportedBlockEntityTypes blockEntityType,
        AltRendererProvider rendererProvider)
{ }
