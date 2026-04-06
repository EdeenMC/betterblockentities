package betterblockentities.client.gui.storage;

/**
 * struct that holds all of our storage ids
 */
public class ConfigStorageIdentifiers {
    public static final String MAIN;
    public static final String EXPERIMENTAL;
    public static final String HIDDEN;

    static {
        MAIN = "bbe.config.storage.main";
        EXPERIMENTAL = "bbe.config.storage.experimental";
        HIDDEN = "bbe.config.storage.hidden";
    }
}
