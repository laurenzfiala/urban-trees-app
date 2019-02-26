package urbantrees.spaklingscience.at.urbantrees.http;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import urbantrees.spaklingscience.at.urbantrees.activities.MainActivityInterface;

import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE;
import static urbantrees.spaklingscience.at.urbantrees.activities.ActivityResultCode.FILECHOOSER_RESULT_CODE_ARRAY;

/**
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public class CustomWebChromeClient extends WebChromeClient {

    private MainActivityInterface mainActivity;

    private ValueCallback<Uri> uploadMessage;

    private ValueCallback<Uri[]> uploadMessageArray;

    public CustomWebChromeClient(MainActivityInterface mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        this.openFileChooserImpl(uploadMsg, null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        this.openFileChooserImpl(uploadMsg, null);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        this.openFileChooserImpl(uploadMsg, null);
    }

    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
        this.openFileChooserImpl(null, uploadMsg);
        return true;
    }

    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg, ValueCallback<Uri[]> uploadMsgArray) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");

        int resultCode;
        if (uploadMsg != null) {
            this.uploadMessage = uploadMsg;
            resultCode = FILECHOOSER_RESULT_CODE;
        } else {
            this.uploadMessageArray = uploadMsgArray;
            resultCode = FILECHOOSER_RESULT_CODE_ARRAY;
        }
        this.mainActivity.startActivityForResult(Intent.createChooser(i, "Choose a photo"), resultCode); // TODO strings.xml

    }

    public void onResult(int resultCode, Intent intent) {

        if (this.uploadMessage == null) {
            return;
        }

        Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
        this.uploadMessage.onReceiveValue(result);
        this.uploadMessage = null;

    }

    public void onResultArray(int resultCode, Intent intent) {

        if (this.uploadMessageArray == null) {
            return;
        }

        Uri result = null;
        if (intent != null && resultCode == Activity.RESULT_OK) {
            result = intent.getData();
        }

        if (result != null) {
            this.uploadMessageArray.onReceiveValue(new Uri[]{result});
        } else {
            this.uploadMessageArray.onReceiveValue(new Uri[]{});
        }
        this.uploadMessageArray = null;

    }

}
