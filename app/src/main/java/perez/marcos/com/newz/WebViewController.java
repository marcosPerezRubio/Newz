package perez.marcos.com.newz;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by marcos on 19/06/2015.
 */
public class WebViewController extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
