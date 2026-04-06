package betterblockentities.client.gui.option;

/**
 * unique enums types for building enum UI options, the consumer and supplier to said option
 * will most likely be a {@link betterblockentities.client.gui.option.IntegerOption}, so this
 * also includes functions to map IntegerOption value to type.
 */
public class EnumTypes {
    public enum UpdateSchedulerType {
        FAST, SMART;
        public static UpdateSchedulerType map(int value) {
            return (value == 0) ? UpdateSchedulerType.FAST : UpdateSchedulerType.SMART;
        }
        public static int map(UpdateSchedulerType type) {
            return (type == FAST) ? 0 : 1;
        }
    }

    public enum BannerGraphicsType {
        FAST, FANCY;
        public static BannerGraphicsType map(int value) {
            return (value == 0) ? BannerGraphicsType.FAST : BannerGraphicsType.FANCY;
        }
        public static int map(BannerGraphicsType type) {
            return (type == FAST) ? 0 : 1;
        }
    }
}
