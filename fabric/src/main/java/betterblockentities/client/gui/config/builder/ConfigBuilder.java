package betterblockentities.client.gui.config.builder;

/* local */
import betterblockentities.client.gui.option.BooleanOption;
import betterblockentities.client.gui.option.EnumTypes;
import betterblockentities.client.gui.option.IntegerOption;
import betterblockentities.client.gui.option.OptionObject;
import betterblockentities.client.gui.storage.ConfigStorageObject;
import betterblockentities.client.gui.storage.ConfigStorageIdentifiers;

/* java/misc */
import java.util.ArrayList;
import java.util.List;

public class ConfigBuilder {
    public static ConfigStorageObject buildMainStorage() {
        ConfigStorageObject mainStorage = new ConfigStorageObject(ConfigStorageIdentifiers.MAIN);

        int defaultBannerGraphics = EnumTypes.BannerGraphicsType.FANCY.ordinal();
        int defaultUpdateScheduler = EnumTypes.UpdateSchedulerType.SMART.ordinal();

        List<OptionObject<?>> options = new ArrayList<>(List.of(
                new BooleanOption("optimize.master", true),

                new BooleanOption("optimize.chest", true),
                new BooleanOption("optimize.shulker", true),
                new BooleanOption("optimize.sign", true),
                new BooleanOption("optimize.decoratedpot", true),
                new BooleanOption("optimize.banner", true),
                new BooleanOption("optimize.bell", true),
                new BooleanOption("optimize.bed", true),
                new BooleanOption("optimize.copper_golem_statue", true),
                new BooleanOption("optimize.shelf", true),
                new BooleanOption("optimize.campfire", true),

                new BooleanOption("animation.chest", true),
                new BooleanOption("animation.shulker", true),
                new BooleanOption("animation.bell", true),
                new BooleanOption("animation.decoratedpot", true),

                new IntegerOption("misc.banner_pose", 1),
                new IntegerOption("misc.banner_graphics", defaultBannerGraphics),
                new BooleanOption("misc.christmas_chest", false),
                new IntegerOption("misc.sign_text_distance", 16),
                new BooleanOption("misc.sign_text", true),
                new BooleanOption("misc.sign_text_culling", true),
                new IntegerOption("misc.update_scheduler", defaultUpdateScheduler)
        ));

        for (OptionObject<?> option : options) {
            mainStorage.addOption(option);
        }
        return mainStorage;
    }

    public static ConfigStorageObject buildExperimentalStorage() {
        ConfigStorageObject experimentalStorage = new ConfigStorageObject(ConfigStorageIdentifiers.EXPERIMENTAL);
        return experimentalStorage;
    }

    public static ConfigStorageObject buildHiddenStorage() {
        ConfigStorageObject hiddenStorage = new ConfigStorageObject(ConfigStorageIdentifiers.HIDDEN);

        List<OptionObject<?>> options = new ArrayList<>(List.of(
                new BooleanOption("override.forced_updatescheduler", false),
                new BooleanOption("debug.overlays", false)
        ));

        for (OptionObject<?> option : options) {
            hiddenStorage.addOption(option);
        }
        return hiddenStorage;
    }
}
