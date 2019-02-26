package urbantrees.spaklingscience.at.urbantrees.activities;

import urbantrees.spaklingscience.at.urbantrees.http.HttpManager;

/**
 * TODO
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public interface ApplicationProperties {

    String getProperty(String propertyKey, Object ...replacements);
    HttpManager getHttpManager();

}
