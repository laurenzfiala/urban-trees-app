package urbantrees.spaklingscience.at.urbantrees.activities;

import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;

/**
 * Implementing classes act as a property holder.
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public interface ApplicationProperties {

    String getProperty(String propertyKey, Object ...replacements);
    String[] getArrayProperty(String propertyKey, Object ...replacements);
    boolean getBooleanProperty(String propertyKey, Object ...replacements);

}
