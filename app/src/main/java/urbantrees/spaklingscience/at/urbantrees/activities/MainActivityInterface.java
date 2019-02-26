package urbantrees.spaklingscience.at.urbantrees.activities;

import android.content.Intent;

/**
 * Interface for the {@link urbantrees.spaklingscience.at.urbantrees.activities.MainActivity}
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public interface MainActivityInterface {

    void loadInitialPage();
    void onWebviewError();
    void onWebviewPageFinished();
    void showSearchControls();
    void onWebviewResouceLoaded();
    void startActivityForResult(Intent intent, int resultCode);

}
