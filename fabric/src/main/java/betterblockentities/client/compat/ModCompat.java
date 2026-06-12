package betterblockentities.client.compat;

/* local */
import betterblockentities.client.compat.IrisCompat;

public class ModCompat{
    private static boolean isIrisLoaded = false;

    public static void setIrisLoaded(){
        isIrisLoaded = true;
    }

    public static boolean isIrisLoaded(){
        return isIrisLoaded();
    }

    public static boolean isShadowPass(){
        // Prevent loading Iris-dependent class if Iris is not loaded
        if(isIrisLoaded()) return IrisCompat.isShadowPass();
        else return false;
    }
}