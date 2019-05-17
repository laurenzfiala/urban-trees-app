package urbantrees.spaklingscience.at.urbantrees.http;

import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import urbantrees.spaklingscience.at.urbantrees.activities.MainActivityInterface;

/**
 * @author Laurenz Fiala
 * @since 2019/02/23
 */
public class CustomWebViewClient extends WebViewClient {

    private MainActivityInterface mainActivity;

    public CustomWebViewClient(MainActivityInterface mainActivity) {
        this.mainActivity = mainActivity;
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

}
