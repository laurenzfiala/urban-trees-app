package urbantrees.spaklingscience.at.urbantrees.http;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;
import urbantrees.spaklingscience.at.urbantrees.activities.MainActivity;
import urbantrees.spaklingscience.at.urbantrees.activities.MainActivityInterface;

/**
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public class CustomWebViewClient extends WebViewClient {

    private static final String LOGGING_TAG = CustomWebViewClient.class.getName();

    private MainActivityInterface mainActivity;
    private Context context;

    /**
     * Urls allowed to be accessed inside the app.
     * All other URLs will be loaded in the default browser.
     */
    private String[] allowedSiteUrls;

    /**
     * Whether the webview is currently loading a page or not.
     * Used to avoid multiple calls of {@link #onPageFinished(WebView, String)}.
     */
    private boolean pageStarted = false;

    public CustomWebViewClient(MainActivityInterface mainActivity,
                               Context context,
                               ApplicationProperties props,
                               String ...allowedSiteUrls) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.allowedSiteUrls = allowedSiteUrls;

        this.allowedSiteUrls = props.getArrayProperty("webview.allowed.urls");
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        this.mainActivity.onWebviewResouceLoaded();
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame()) {
            Log.i(LOGGING_TAG, "onReceivedError(" + request.getUrl() + "): " + error);
            this.mainActivity.onWebviewError(request, error);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.i(LOGGING_TAG, "onPageStarted(" + url + ")");
        super.onPageStarted(view, url, favicon);
        this.pageStarted = true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(LOGGING_TAG, "onPageFinished(" + url + ")");
        super.onPageFinished(view, url);
        this.mainActivity.onWebviewPageFinished(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.i(LOGGING_TAG, "shouldOverrideUrlLoading(" + url + ")");
        for (String s : this.allowedSiteUrls) {
            if (url.startsWith(s)) {
                return false;
            }
        }
        this.context.startActivity(
            new Intent(Intent.ACTION_VIEW, Uri.parse(url))
        );
        return true;
    }

}
