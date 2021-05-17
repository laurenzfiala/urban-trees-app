package urbantrees.spaklingscience.at.urbantrees.activities;

import android.content.Intent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;

/**
 * Interface for the {@link urbantrees.spaklingscience.at.urbantrees.activities.MainActivity}
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public interface MainActivityInterface {

    void loadInitialPage();
    void onWebviewError(WebResourceRequest request, WebResourceError error);

    /**
     * Attention: may be called multiple times for some reason.
     *            In the past we encountered a problem where some devices only called this method
     *            with the correct URL one of two times.
     * @param url URL that was loaded (or not, see "Attention" above)
     */
    void onWebviewPageFinished(final String url);
    void updateSearchControls();
    void onWebviewResouceLoaded();
    void startActivityForResult(Intent intent, int resultCode);

}
