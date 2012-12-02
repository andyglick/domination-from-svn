package net.yura.domination.android;

import java.util.logging.Logger;
import net.yura.mobile.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

    static final Logger logger = Logger.getLogger(GCMIntentService.class.getName());

    static final String SENDER_ID = "783159960229";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        displayMessage(context,"Device registered: regId = "+registrationId);
        GCMServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        displayMessage(context, "Device unregistered");
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            GCMServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            displayMessage(context, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
	String msg = intent.getExtras().getString("message");
        String message = msg==null?"Received message":msg;
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        String message = "Received deleted messages notification "+total;
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        displayMessage(context, "Received error: "+errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        displayMessage(context, "Received recoverable error: "+errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, net.yura.android.AndroidMeActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }
    
    
    
    
    static void displayMessage(Context context,String text) {
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
