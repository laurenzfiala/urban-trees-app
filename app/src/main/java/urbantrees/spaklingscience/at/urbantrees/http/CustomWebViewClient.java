package urbantrees.spaklingscience.at.urbantrees.http;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import urbantrees.spaklingscience.at.urbantrees.activities.ApplicationProperties;
import urbantrees.spaklingscience.at.urbantrees.activities.MainActivityInterface;

/**
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public class CustomWebViewClient extends WebViewClient {

    private MainActivityInterface mainActivity;
    private Context context;

    /**
     * Urls allowed to be accessed inside the app.
     * All other URLs will be loaded in the default browser.
     */
    private String[] allowedSiteUrls;

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
        this.mainActivity.onWebviewError(request, error);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        this.mainActivity.onWebviewPageFinished();
        this.mainActivity.showSearchControls();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        for (String s : this.allowedSiteUrls) {
            if (url.startsWith(s)) {
                view.loadUrl(url);
                return true;
            }
        }
        this.context.startActivity(
            new Intent(Intent.ACTION_VIEW, Uri.parse(url))
        );
        return true;
    }

}
