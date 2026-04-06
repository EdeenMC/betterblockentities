package betterblockentities.client.gui.option;

/**
 * simple data struct for saving boolean like options
 */
public class BooleanOption implements OptionObject<Boolean> {
    private boolean value;
    private final String key;

    public BooleanOption(String key, boolean value) {
        this.key = key;
        this.value = value;
    }

    @Override public Boolean getValue() { return value; }
    @Override public void setValue(Boolean value) { this.value = value; }
    @Override public String getKey() { return key; }
}
