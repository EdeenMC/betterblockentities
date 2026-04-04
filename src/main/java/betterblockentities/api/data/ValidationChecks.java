package betterblockentities.api.data;

/* local */
import betterblockentities.client.BBE;

public final class ValidationChecks {
    public static boolean registrationTypeValid(RegistrationInfo info) {
        if (info == null) {
            BBE.getLogger().error("Registration info was null! Skipping registration for this renderer!");
            return false;
        }

        boolean valid = true;

        if (info.blockEntityType() == null) {
            BBE.getLogger().error("Invalid BlockEntityType passed during alt renderer registration!");
            valid = false;
        }

        if (info.rendererProvider() == null) {
            BBE.getLogger().error("Invalid renderer passed during alt renderer registration!");
            valid = false;
        }

        if (!valid) {
            BBE.getLogger().error("Faulty registration information was detected upon registration of an AltRenderer. Skipping!");
        }

        return valid;
    }
}
