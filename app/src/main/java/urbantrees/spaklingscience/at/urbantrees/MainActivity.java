package urbantrees.spaklingscience.at.urbantrees;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link WebView} to show the webpage in.
     */
    private WebView webView;

    private static final int FILECHOOSER_RESULT_CODE = 1;

    private static final int FILECHOOSER_RESULT_CODE_ARRAY = 2;

    private ValueCallback<Uri> uploadMessage;

    private ValueCallback<Uri[]> uploadMessageArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.webView = (WebView) findViewById(R.id.web_view);
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.setWebChromeClient(this.getWebChromeClient());

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        String cookieString = "x-api-key=5ed7ff7c-c4d1-4474-869a-af6347f61240; Domain=141.201.106.39";
        cookieManager.setCookie("141.201.106.39", cookieString);

        this.webView.loadUrl("http://141.201.106.39/phenology");

    }

    /**
     * {@link WebChromeClient} with inplemented filechooser event handlers which
     * are needed for working input type=file in the website.
     * @return Newly created {@link WebChromeClient}
     */
    private WebChromeClient getWebChromeClient() {
        return new WebChromeClient() {

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                MainActivity.this.openFileChooserImpl(uploadMsg, null);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                MainActivity.this.openFileChooserImpl(uploadMsg, null);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                MainActivity.this.openFileChooserImpl(uploadMsg, null);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {
                MainActivity.this.openFileChooserImpl(null, uploadMsg);
                return true;
            }

        };
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
        MainActivity.this.startActivityForResult(Intent.createChooser(i,"Choose a photo"), resultCode);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULT_CODE) {
            if (this.uploadMessage == null) {
                return;
            }

            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            this.uploadMessage.onReceiveValue(result);
            this.uploadMessage = null;

        } else if (requestCode == FILECHOOSER_RESULT_CODE_ARRAY) {
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

}
