package betterblockentities.data;

public final class ValidationChecks {
    public static boolean registrationTypeValid(RegistrationInfo info) {
        if (info == null) {
            return false;
        }

        boolean valid = true;

        if (info.blockEntityType() == null) {
            valid = false;
        }

        if (info.rendererProvider() == null) {
            valid = false;
        }

        return valid;
    }
}
