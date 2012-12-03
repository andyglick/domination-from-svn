package net.yura.domination.android;

import java.util.logging.Logger;
import com.google.android.gcm.GCMRegistrar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class GCMActivity extends Activity {

    public static final String SENDER_ID = "783159960229";
    
    static final Logger logger = Logger.getLogger(GCMActivity.class.getName());
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
        setup();
	//unregister();
        finish();
    }
    
    public static void displayMessage(Context context,String text) {
        logger.info(text);
    }
    
    public static void setup() {
        Context context = net.yura.android.AndroidMeApp.getContext();
        
        GCMRegistrar.checkDevice(context);
        GCMRegistrar.checkManifest(context);
        final String regId = GCMRegistrar.getRegistrationId(context);
        if (regId.equals("")) {
          GCMRegistrar.register(context, SENDER_ID);
        }
        else {
            if (GCMRegistrar.isRegisteredOnServer(context)) {
                displayMessage(context,"Already registered");
            }
            else {
                GCMServerUtilities.register(context, regId);
                
                // TODO if we FAIL at registering on our server then call
                // GCMRegistrar.unregister(context);
                // currently can not tell
            }
        }
    }
    
    public static void unregister() {
        Context context = net.yura.android.AndroidMeApp.getContext();
        GCMRegistrar.unregister(context);
    }
    
}
