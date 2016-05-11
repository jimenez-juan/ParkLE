package edu.stanford.parkle;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;


public class BeaconWakefulReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // This is the Intent to deliver to our service.
        Intent service = new Intent(context, FindBeaconService.class);

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
    }
}