package betterblockentities.client.gui.storage;

import betterblockentities.client.BBE;

import java.util.HashMap;
import java.util.Map;

/**
 * a wrapper struct that holds all of our ConfigStorageObjects
 */
public class ConfigStorageCollection {
    private final Map<String, ConfigStorageObject> configStorageObjects = new HashMap<>();

    public ConfigStorageCollection() { }

    public ConfigStorageObject getStorage(String storageId) {
        if (!configStorageObjects.containsKey(storageId)) {
            BBE.getLogger().error("Could not get storage with storageId: {}. No match found!", storageId);
            return null;
        }
        return configStorageObjects.get(storageId);
    }

    public void addStorage(String storageId, ConfigStorageObject storageObject) {
        if (configStorageObjects.containsKey(storageId)) {
            BBE.getLogger().error("Could not add storage with storageId: {}. Storage already exists!", storageId);
            return;
        }
        configStorageObjects.put(storageId, storageObject);
    }
}
