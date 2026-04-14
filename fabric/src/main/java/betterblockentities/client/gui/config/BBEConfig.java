package betterblockentities.client.gui.config;

/* local */
import betterblockentities.client.BBE;
import betterblockentities.client.gui.config.builder.ConfigBuilder;
import betterblockentities.client.gui.config.wrapper.GenericConfigWrapper;
import betterblockentities.client.gui.option.*;
import betterblockentities.client.gui.storage.ConfigStorageCollection;
import betterblockentities.client.gui.storage.ConfigStorageObject;
import betterblockentities.client.gui.storage.ConfigStorageIdentifiers;

/* minecraft */
import betterblockentities.client.render.immediate.blockentity.manager.InstancedBlockEntityManager;

/* gson */
import com.google.gson.*;

/* java/misc */
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class BBEConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config", "BBEConfig.json");

    private static final ConfigStorageCollection configStorageCollection = new ConfigStorageCollection();

    public ConfigStorageObject MAIN;
    public ConfigStorageObject EXPERIMENTAL;
    public ConfigStorageObject HIDDEN;

    public BBEConfig() { buildConfigStorages(); }

    /**
     * builds all of our config storages, loads options from config file if present, if not, writes default settings to file
     * each option is built in {@link ConfigBuilder}
     */
    private void buildConfigStorages() {
        /* build "dummy" storages (default settings) */
        ConfigStorageObject main = ConfigBuilder.buildMainStorage();
        ConfigStorageObject experimental = ConfigBuilder.buildExperimentalStorage();
        ConfigStorageObject hidden = ConfigBuilder.buildHiddenStorage();

        /* if a config file exists, update each ConfigStorageObject -> { OptionObject, .... } with saved value from the config file */
        if (CONFIG_FILE.exists()) {
            load(main);
            load(experimental);
            load(hidden);
        }

        /* no config file found, create and write defaults to file */
        else {
            save(main);
            save(experimental);
        }

        /* append each storage to the storage collection */
        configStorageCollection.addStorage(ConfigStorageIdentifiers.MAIN, main);
        configStorageCollection.addStorage(ConfigStorageIdentifiers.EXPERIMENTAL, experimental);
        configStorageCollection.addStorage(ConfigStorageIdentifiers.HIDDEN, hidden);

        /* static "getters" for each storage */
        MAIN = configStorageCollection.getStorage(ConfigStorageIdentifiers.MAIN);
        EXPERIMENTAL = configStorageCollection.getStorage(ConfigStorageIdentifiers.EXPERIMENTAL);
        HIDDEN = configStorageCollection.getStorage(ConfigStorageIdentifiers.HIDDEN);
    }

    /**
     * load config storage from root object inside config file
     */
    public static void load(ConfigStorageObject storage) {
        String storageId = storage.getStorageId();

        if (storage == null) {
            throw new IllegalArgumentException("storage must not be null");
        }
        if (storageId == null || storageId.isBlank()) {
            throw new IllegalArgumentException("storageId must not be blank");
        }
        if (!CONFIG_FILE.exists()) return;

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed == null || !parsed.isJsonObject()) return;

            JsonObject root = parsed.getAsJsonObject();
            JsonElement arrEl = root.get(storageId);
            if (arrEl == null || !arrEl.isJsonArray()) return;

            JsonArray array = arrEl.getAsJsonArray();

            for (JsonElement el : array) {
                if (el == null || !el.isJsonObject()) continue;

                JsonObject entry = el.getAsJsonObject();

                JsonElement keyEl = entry.get("option");
                JsonElement valEl = entry.get("value");
                if (keyEl == null || keyEl.isJsonNull() || valEl == null) continue;

                String key = keyEl.getAsString().toLowerCase(Locale.ROOT);

                /* existing option in default storage */
                OptionObject<?> opt = storage.getOption(key);
                if (opt == null) continue; //unknown option in file, ignore

                if (valEl.isJsonNull()) {
                    continue; //skip (keep defaults)
                }

                /* apply value based on option type */
                if (valEl.isJsonPrimitive()) {
                    if (opt instanceof BooleanOption boolOpt) {
                        boolOpt.setValue(valEl.getAsBoolean());
                    } else if (opt instanceof IntegerOption intOpt) {
                        intOpt.setValue(valEl.getAsInt());
                    } else if (opt instanceof FloatOption floatOpt) {
                        floatOpt.setValue(valEl.getAsFloat());
                    } else {
                        opt.setValue(null); //this is fine as this option is of an invalid type
                    }
                }
            }

        } catch (Exception ex) {
            //failed to read : handle this as well
            ex.printStackTrace();
        }
    }

    /**
     * write config storage to root object inside config file
     */
    public static void save(ConfigStorageObject storage) {
        try {
            File parent = CONFIG_FILE.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create config directory: " + parent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* read existing file (or start fresh) */
        JsonObject root = new JsonObject();
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed != null && parsed.isJsonObject()) {
                    root = parsed.getAsJsonObject();
                }
            } catch (Exception e) {
                //corrupted file, handle this by resetting to defaults?
                e.printStackTrace();
                root = new JsonObject();
            }
        }

        /* build JSON array for this storage */
        JsonArray array = new JsonArray();
        storage.getAllOptions().forEach((key, option) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("option", key);

            Object value = option.getValue();
            if (value instanceof Boolean b) entry.addProperty("value", b);
            else if (value instanceof Number n) entry.addProperty("value", n);
            else if (value instanceof String s) entry.addProperty("value", s);
            else if (value == null) entry.add("value", JsonNull.INSTANCE);
            else entry.addProperty("value", String.valueOf(value));

            array.add(entry);
        });

        /* put it into the root (this updates this storage only) */
        String storageId = storage.getStorageId();
        root.add(storageId, array);

        /* write the full root back (preserves other storages) */
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * cache the main config options to avoid repeated lookups in the config storage
     */
    public static void updateConfigCache() {
        ConfigCache.masterOptimize = GenericConfigWrapper.MainStorage.master();

        if (ConfigCache.masterOptimize) {
            ConfigCache.chestAnims = GenericConfigWrapper.MainStorage.animateChest();
            ConfigCache.shulkerAnims = GenericConfigWrapper.MainStorage.animateShulker();
            ConfigCache.bellAnims = GenericConfigWrapper.MainStorage.animateBell();
            ConfigCache.potAnims = GenericConfigWrapper.MainStorage.animateDecoratedpot();
            ConfigCache.signText = GenericConfigWrapper.MainStorage.signText();
            ConfigCache.signTextRenderDistance = GenericConfigWrapper.MainStorage.signTextDistance();
            ConfigCache.optimizeSigns = GenericConfigWrapper.MainStorage.optimizeSign();
            ConfigCache.christmasChests = GenericConfigWrapper.MainStorage.useChristmasChestTextures();
            ConfigCache.optimizeChests = GenericConfigWrapper.MainStorage.optimizeChests();
            ConfigCache.optimizeDecoratedPots = GenericConfigWrapper.MainStorage.optimizeDecoratedPot();
            ConfigCache.optimizeBeds = GenericConfigWrapper.MainStorage.optimizeBed();
            ConfigCache.optimizeShulker = GenericConfigWrapper.MainStorage.optimizeShulker();
            ConfigCache.bannerGraphics = GenericConfigWrapper.MainStorage.bannerGraphics();
            ConfigCache.optimizeBells = GenericConfigWrapper.MainStorage.optimizeBell();
            ConfigCache.optimizeBanners = GenericConfigWrapper.MainStorage.optimizeBanner();
            ConfigCache.bannerPose = GenericConfigWrapper.MainStorage.bannerPose();
            ConfigCache.optimizeCopperGolemStatue = GenericConfigWrapper.MainStorage.optimizeCopperGolemStatue();
            ConfigCache.updateType = BBE.ModCompact.isSchedulerOptionLimited() ? EnumTypes.UpdateSchedulerType.SMART.ordinal() : GenericConfigWrapper.MainStorage.updateScheduler();
            ConfigCache.signTextCulling = GenericConfigWrapper.MainStorage.signTextCulling();
            ConfigCache.optimizeShelves = GenericConfigWrapper.MainStorage.optimizeShelf();
            ConfigCache.optimizeCampfire = GenericConfigWrapper.MainStorage.optimizeCampfire();
        }
        else {
            ConfigCache.optimizeSigns = false;
            ConfigCache.optimizeChests = false;
            ConfigCache.optimizeDecoratedPots = false;
            ConfigCache.optimizeBeds = false;
            ConfigCache.optimizeShulker = false;
            ConfigCache.optimizeBells = false;
            ConfigCache.optimizeBanners = false;
            ConfigCache.optimizeCopperGolemStatue = false;
            ConfigCache.optimizeShelves = false;
            ConfigCache.optimizeCampfire = false;
        }

        OptEnabledTable.rebuildFromConfig();
    }

    /**
     * fast lookup table for main opts
     */
    public static class OptEnabledTable {
        private OptEnabledTable() {}

        public static final boolean[] ENABLED = new boolean[256];

        public static void rebuildFromConfig() {
            ENABLED[InstancedBlockEntityManager.OptKind.CHEST]   = ConfigCache.optimizeChests;
            ENABLED[InstancedBlockEntityManager.OptKind.SIGN]    = ConfigCache.optimizeSigns;
            ENABLED[InstancedBlockEntityManager.OptKind.BED]     = ConfigCache.optimizeBeds;
            ENABLED[InstancedBlockEntityManager.OptKind.SHULKER] = ConfigCache.optimizeShulker;
            ENABLED[InstancedBlockEntityManager.OptKind.POT]     = ConfigCache.optimizeDecoratedPots;
            ENABLED[InstancedBlockEntityManager.OptKind.BANNER]  = ConfigCache.optimizeBanners;
            ENABLED[InstancedBlockEntityManager.OptKind.BELL]    = ConfigCache.optimizeBells;
            ENABLED[InstancedBlockEntityManager.OptKind.CGS]     = ConfigCache.optimizeCopperGolemStatue;
            ENABLED[InstancedBlockEntityManager.OptKind.SHELF]     = ConfigCache.optimizeShelves;
            ENABLED[InstancedBlockEntityManager.OptKind.CAMPFIRE]     = ConfigCache.optimizeCampfire;

            ENABLED[InstancedBlockEntityManager.OptKind.NONE] = false;
        }
    }
}
