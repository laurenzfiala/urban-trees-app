package urbantrees.spaklingscience.at.urbantrees.util;

/**
 * Interface for callback objects to pass to methods.
 * @author Laurenz Fiala
 * @since 2018/10/27
 */
public interface Callback<T> {

    void call(T value);

    void error(Throwable t);

}
