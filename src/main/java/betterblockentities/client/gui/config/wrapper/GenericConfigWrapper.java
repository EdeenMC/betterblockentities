package betterblockentities.client.gui.config.wrapper;

/* local */
import betterblockentities.client.BBE;

/**
 * getters for all of our generic options, casts each option to the correct data type
 */
public class GenericConfigWrapper {
    public GenericConfigWrapper() { }

    public static class MainStorage {
        private static boolean getGenericBooleanOption(String key) { return (boolean)BBE.GlobalScope.CONFIG.MAIN.getOption(key).getValue(); }
        private static int getGenericIntegerOption(String key) {return (int)BBE.GlobalScope.CONFIG.MAIN.getOption(key).getValue();}

        public static boolean master() { return getGenericBooleanOption("optimize.master"); }
        public static boolean optimizeChests() {return getGenericBooleanOption("optimize.chest");}
        public static boolean optimizeShulker() {return getGenericBooleanOption("optimize.shulker");}
        public static boolean optimizeSign() {return getGenericBooleanOption("optimize.sign");}
        public static boolean optimizeDecoratedPot() {return getGenericBooleanOption("optimize.decoratedpot");}
        public static boolean optimizeBanner() {return getGenericBooleanOption("optimize.banner");}
        public static boolean optimizeBell() { return getGenericBooleanOption("optimize.bell"); }
        public static boolean optimizeBed() {return getGenericBooleanOption("optimize.bed");}
        public static boolean optimizeCopperGolemStatue() {return getGenericBooleanOption("optimize.copper_golem_statue");}
        public static boolean optimizeShelf() {return getGenericBooleanOption("optimize.shelf");}
        public static boolean optimizeCampfire() {return getGenericBooleanOption("optimize.campfire");}
        public static boolean animateChest() {return getGenericBooleanOption("animation.chest");}
        public static boolean animateShulker() {return getGenericBooleanOption("animation.shulker");}
        public static boolean animateBell() {return getGenericBooleanOption("animation.bell");}
        public static boolean animateDecoratedpot() {return getGenericBooleanOption("animation.decoratedpot");}
        public static int bannerPose() {return getGenericIntegerOption("misc.banner_pose");}
        public static int bannerGraphics() {return getGenericIntegerOption("misc.banner_graphics");}
        public static boolean useChristmasChestTextures() {return getGenericBooleanOption("misc.christmas_chest");}
        public static int signTextDistance() {return getGenericIntegerOption("misc.sign_text_distance");}
        public static boolean signText() {return getGenericBooleanOption("misc.sign_text");}
        public static boolean signTextCulling() {return getGenericBooleanOption("misc.sign_text_culling");}
        public static int updateScheduler() {return getGenericIntegerOption("misc.update_scheduler");}
    }

    public static class HiddenStorage {
        private static boolean getGenericBooleanOption(String key) { return (boolean)BBE.GlobalScope.CONFIG.HIDDEN.getOption(key).getValue(); }
        private static int getGenericIntegerOption(String key) {return (int)BBE.GlobalScope.CONFIG.HIDDEN.getOption(key).getValue();}

        public static boolean debugOverlays() {return getGenericBooleanOption("debug.overlays");}
    }
}
