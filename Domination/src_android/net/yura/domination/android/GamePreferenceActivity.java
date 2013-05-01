package net.yura.domination.android;

import java.util.ResourceBundle;
import net.yura.domination.engine.translation.TranslationBundle;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class GamePreferenceActivity extends PreferenceActivity {

    private static ResourceBundle resb = TranslationBundle.getBundle();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setPreferenceScreen( makePreferenceScreen(getPreferenceManager(),this) );
        }
        else {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new GamePreferenceFragment()).commit();
        }
    }

    private static PreferenceScreen makePreferenceScreen(PreferenceManager man,Context context) {
        PreferenceScreen root = man.createPreferenceScreen(context);

        PreferenceCategory inlinePrefCat = new PreferenceCategory(context);
        inlinePrefCat.setTitle( resb.getString("swing.menu.options") );
        root.addPreference(inlinePrefCat);

        CheckBoxPreference show_toasts = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        show_toasts.setTitle( resb.getString("game.menu.showtoasts") );
        show_toasts.setKey("show_toasts");
        inlinePrefCat.addPreference(show_toasts);

        CheckBoxPreference color_blind = new CheckBoxPreference(context); // TwoStatePreference = new SwitchPreference(this);
        color_blind.setTitle( resb.getString("game.menu.colorblind") );
        color_blind.setKey("color_blind");
        inlinePrefCat.addPreference(color_blind);

        return root;
    }

    /*
    // Called only on Honeycomb and later
    @Override
    public void onBuildHeaders(List<Header> target) {
        Header header = new Header();
        header.title = "header title";
        header.fragment = GamePreferenceFragment.class.getName();
        target.add(header);
    }
    */

    public static class GamePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setPreferenceScreen( makePreferenceScreen(getPreferenceManager(),getActivity()) );
        }
    }

}
