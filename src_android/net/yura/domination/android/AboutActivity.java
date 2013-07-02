package net.yura.domination.android;

import java.util.ResourceBundle;
import net.yura.domination.R;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.MiniUtil;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TabHost;
import android.app.Activity;

public class AboutActivity extends Activity implements TabHost.TabContentFactory {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ResourceBundle resb = TranslationBundle.getBundle();
        //setTitle( resb.getString("about.title") );

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabContentFactory factory = this;

        tabHost.addTab( tabHost.newTabSpec("about").setIndicator( resb.getString("about.title") ).setContent(factory) );
        tabHost.addTab( tabHost.newTabSpec("credits").setIndicator( resb.getString("about.tab.credits") ).setContent(factory) );
        tabHost.addTab( tabHost.newTabSpec("license").setIndicator( resb.getString("about.tab.license") ).setContent(factory) );
        tabHost.addTab( tabHost.newTabSpec("changelog").setIndicator( resb.getString("about.tab.changelog") ).setContent(factory) );

        tabHost.setCurrentTab(0);  
    }

    @Override
    public View createTabContent(String tag) {
        WebView webView = new WebView(this);
        String prefix = "file:///android_asset/";
        if ("about".equals(tag)) {
            WebSettings settings = webView.getSettings();
            settings.setDefaultTextEncodingName("utf-8");
            webView.loadData(MiniUtil.getAboutHtml(), "text/html", null);
        }
        else if ("credits".equals(tag)){
            webView.loadUrl(prefix+"help/game_credits.htm");
        }
        else if ("license".equals(tag)){
            webView.loadUrl(prefix+"gpl.txt");
        }
        else if ("changelog".equals(tag)){
            webView.loadUrl(prefix+"ChangeLog.txt");
        }
        else {
            throw new IllegalArgumentException("strange tag "+tag);
        }
        return webView;
    }

}
