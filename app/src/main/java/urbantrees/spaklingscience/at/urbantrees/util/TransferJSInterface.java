package urbantrees.spaklingscience.at.urbantrees.util;

import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

/**
 * Javascript interface so the webapp can call the app
 * during a beacon data transfer.
 * @author Laurenz Fiala
 * @since 2021/05/10
 */
public class TransferJSInterface {

    private TransferJSListener listener;

    public TransferJSInterface(@NonNull TransferJSListener listener) {
        this.listener = listener;
    }

    /**
     * Name to use for the JS interface.
     */
    public static String name() {
        return "transferInterface";
    }

    @JavascriptInterface
    public void cancelTransfer() {
        this.listener.onUserCancel();
    }

}
