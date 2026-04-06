package betterblockentities.client.gui.storage;

/* local */
import betterblockentities.client.gui.option.OptionObject;

/* java/misc */
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * holds generic options ( {Type}Option ), data type is lost here, and upon option value
 * retrieval we need to cast to the correct data type. each storage has a unique id
 * and gets saved as a separate JSON array inside a root object in the config file
 */
public class ConfigStorageObject {
    private final Map<String, OptionObject<?>> options = new LinkedHashMap<>();
    private final String storageId;

    public ConfigStorageObject(String storageId) {
        this.storageId = storageId;
    }

    public String getStorageId() {
        return this.storageId;
    }

    public void addOption(OptionObject<?> option) {
        String key = option.getKey();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Option key must not be blank or unset!");
        }
        options.put(key.toLowerCase(Locale.ROOT), option);
    }

    public OptionObject<?> getOption(String key) {
        if (key == null) return null;
        String builtKey = key.toLowerCase(Locale.ROOT);

        if (!options.containsKey(builtKey)) {
            throw new IllegalArgumentException("Unknown options key: " + builtKey + " not found in storage: " + this.getStorageId());
        }
        return options.get(builtKey);
    }

    public <T> void setOption(String key, T value) {
        OptionObject<?> optAny = getOption(key);
        if (optAny == null) return;

        @SuppressWarnings("unchecked")
        OptionObject<T> opt = (OptionObject<T>) optAny;

        opt.setValue(value);
    }

    public Map<String, OptionObject<?>> getAllOptions() {
        return options;
    }
}
