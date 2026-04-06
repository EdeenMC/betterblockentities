package betterblockentities.client.gui.option;

/**
 * generic option, each {Type}Option implements this
 */
public interface OptionObject<T> {
    String getKey();
    T getValue();
    void setValue(T value);
}
