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
    void onWebviewPageFinished();
    void showSearchControls();
    void onWebviewResouceLoaded();
    void startActivityForResult(Intent intent, int resultCode);

}
