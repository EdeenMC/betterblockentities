package betterblockentities.client.gui.option;

/**
 * simple data struct for saving integer like options
 */
public class IntegerOption implements OptionObject<Integer>{
    private int value;
    private final String key;

    public IntegerOption(String key, int value) {
        this.key = key;
        this.value = value;
    }

    @Override public Integer getValue() { return value; }
    @Override public void setValue(Integer value) { this.value = value; }
    @Override public String getKey() { return key; }
}
