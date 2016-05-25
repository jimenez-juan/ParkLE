package edu.stanford.parkle;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.HashMap;
import java.util.Map;

public class ParkLE extends Application {

    // All constants and global variables are purposely declared here without a privacy modifier
    // because they are intended to be visible to the entire package which is the default.

    static final long ALARM_INTERVAL_MS = 5*1000;
    static final long SCAN_PERIOD_MS = 2*1000;

    static final String INTENT_ACTION_CHECK_BEACON = "edu.parkle.CHECK_BEACON";

    static final String USER_INFO = "userInfo";
    static final String CAR_STATE_INFO = "CURRENT_STATE" ;
    static final String BEACON_ADDRESS_INFO = "BEACON_ADDRESS";
    //    protected static final String BEACON_STATE_INFO = "BEACON_STATE";
//    protected static final String CAR_MODULE_STATE_INFO = "carModuleState";
    static final String PASS_TYPE_KEY = "PASS_TYPE_KEY";
    static final String MAC_ADDRESS_KEY = "MAC_ADDRESS_KEY";
    static final String UID_KEY = "UID_KEY";
    static final String WAS_PARKED_KEY = "WAS_PARKED_KEY";

    static final String LOT_A_BEACON_ADDRESS = "FA:AD:C0:21:19:3C";
    static final String LOT_B_BEACON_ADDRESS = "C8:05:62:94:0C:10";
    static final String LOT_C_BEACON_ADDRESS = "D4:85:90:D6:9A:CD";

    static final int CAR_NOT_IN_LOT = 0;
    static final int CAR_IDLE_IN_LOT = 1;
    static final int CAR_PARKED_IN_LOT = 2;
    static final int NOT_CONNECTED = 0;
    static final int CONNECTED = 1;

    static SharedPreferences sharedPreferences;

    static final Map<String, String> lotNames = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        sharedPreferences = getSharedPreferences(USER_INFO, Context.MODE_PRIVATE);
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(CAR_STATE_INFO, CAR_IDLE_IN_LOT);
//        editor.putString(BEACON_ADDRESS_INFO, "FA:AD:C0:21:19:3C");
//        editor.commit();

        lotNames.put(LOT_A_BEACON_ADDRESS, getString(R.string.lot_a_name));
        lotNames.put(LOT_B_BEACON_ADDRESS, getString(R.string.lot_b_name));
        lotNames.put(LOT_C_BEACON_ADDRESS, getString(R.string.lot_c_name));

        if (sharedPreferences.contains(UID_KEY)) // User is already logged in...
        {
            Intent checkBeaconAlarm = new Intent(this, BeaconWakefulReceiver.class);
            checkBeaconAlarm.setAction(INTENT_ACTION_CHECK_BEACON);
            PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(this, 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_INTERVAL_MS, pendingCheckBeaconAlarm);

            Log.e("ME202", "Setting alarm");
        }


//        boolean userLoggedIn = false;
//        if (userLoggedIn) {
//            Intent checkBeaconAlarm = new Intent(this, BeaconWakefulReceiver.class);
//            checkBeaconAlarm.setAction(INTENT_ACTION_CHECK_BEACON);
//            PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(this, 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//            alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ALARM_INTERVAL_MS, pendingCheckBeaconAlarm);
//
//            Log.e("ME202", "Setting alarm");
//        }
    }

}
