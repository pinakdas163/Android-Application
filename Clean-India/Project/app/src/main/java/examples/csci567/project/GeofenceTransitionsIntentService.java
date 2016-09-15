package examples.csci567.project;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pinak on 08-05-2016.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService()
    {
        super("Geofence");

    }
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMsg= String.valueOf(geofencingEvent.getErrorCode());
            Log.e("Geofence: ", "geofencing event error"+errorMsg);
            return;
        }

        int geofenceTransition;
        geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
               // || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT)
            {

            List triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            sendNotification(this, geofenceTransitionDetails);
            Log.i("GeoFence: ", geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e("GeoFence: ", String.valueOf(R.string.geofence_transition_invalid_type +
                    geofenceTransition));
        }
    }

    private void sendNotification(Context context, String notificationText) {

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        /*PackageManager manager = getPackageManager();
        Intent launchIntent = manager.getLaunchIntentForPackage("examples.csci567.project");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);*/


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.drawable.ic_delete_forever_black_24dp)
                .setContentTitle(getString(R.string.notificationTitle))
                .setContentText(notificationText)
               //.setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(00, notificationBuilder.build());

        wakeLock.release();
    }

    private String getGeofenceTransitionDetails(Context context, int geofenceTransition,
            List<Geofence> triggeringGeofences) {
        String geofenceTransitionString;
        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.v("Geofence: ", "Geofence Entered");
                geofenceTransitionString = getString(R.string.transition_msg);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                geofenceTransitionString = getString(R.string.transitionExitmsg);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.v("Geofence: ", "Dwelling in Geofence");
                geofenceTransitionString = getString(R.string.transition_msg);
                break;
            default:
                geofenceTransitionString = "unknown transition";
                break;
        }
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;

    }
}
