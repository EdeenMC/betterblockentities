package betterblockentities.data;

/* local */
import betterblockentities.render.AltRendererProvider;

@SuppressWarnings("rawtypes")
public record RegistrationInfo(
        SupportedBlockEntityTypes blockEntityType,
        AltRendererProvider rendererProvider)
{ }
