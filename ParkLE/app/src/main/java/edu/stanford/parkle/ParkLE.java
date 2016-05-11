package edu.stanford.parkle;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.client.Firebase;

/**
 * Created by juanj on 5/5/16.
 */
public class ParkLE extends Application {

    protected static final long ALARM_INTERVAL = 20*1000;
    protected static final long SCAN_PERIOD = 2*1000;

    protected static final String INTENT_ACTION_CHECK_BEACON = "com.parklive.parklive.CHECK_BEACON";

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        boolean userExists = true;
        if (userExists) {
            Intent checkBeaconAlarm = new Intent(this, BeaconWakefulReceiver.class);
            checkBeaconAlarm.setAction(INTENT_ACTION_CHECK_BEACON);
            PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(this, 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_INTERVAL, pendingCheckBeaconAlarm);

            Log.e("ME202", "Setting alarm");
        }
    }

}
