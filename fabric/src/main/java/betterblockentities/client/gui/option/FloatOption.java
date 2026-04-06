package betterblockentities.client.gui.option;

/**
 * simple data struct for saving float like options
 */
public class FloatOption implements OptionObject<Float>{
    private float value;
    private final String key;

    public FloatOption(String key, float value) {
        this.key = key;
        this.value = value;
    }

    @Override public Float getValue() { return value; }
    @Override public void setValue(Float value) { this.value = value; }
    @Override public String getKey() { return key; }
}
