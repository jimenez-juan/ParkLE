package edu.stanford.parkle;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

public class FindBeaconService extends IntentService implements BluetoothAdapter.LeScanCallback {

    // TODO: Probably should use UUIDs instead of MAC addresses to ensure uniqueness

//    private static final UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
//    private static final UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
//    private static final UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
//    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private Intent serviceIntent;

    private BluetoothAdapter mAdapter;
//    private BluetoothGatt mConnectedGatt = null;
//    private BluetoothGattCharacteristic tx;
//    private BluetoothGattCharacteristic rx;
    private ArrayList<BluetoothDevice> mDevices;

    public FindBeaconService() {
        super("FindBeaconService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        serviceIntent = intent;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new ArrayList<>();

        mAdapter.startLeScan(this);

        try {
            Thread.sleep(ParkLE.SCAN_PERIOD);
        } catch (Exception e) {
            Log.e("BeaconScanning", "Error in attempt to sleep for scan.");
            restartAlarm();
            this.stopSelf();
        }
        if (mDevices.size() > 0) {
            Log.e("Service", "found these devices:");
            for (BluetoothDevice d : mDevices) {
                Log.e("Service", d.getAddress());
            }
        }
        stopScan();
        updateState();
        restartAlarm();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!mDevices.contains(device)) mDevices.add(device);
//        if (device.getAddress().equals("E9:40:B9:B9:C0:05")) {
//            mDevices.add(device);
//            stopScan();
//        }
    }

    private void stopScan() {
        mAdapter.stopLeScan(this);
    }

    private void updateState() {
        int currentCarState = ParkLE.sharedPreferences.getInt(ParkLE.CAR_STATE_INFO, ParkLE.CAR_NOT_IN_LOT);
        int currentBeaconState = ParkLE.NOT_CONNECTED;
        int currentCarModuleState = ParkLE.NOT_CONNECTED;
        String currentBeaconAddress = ParkLE.sharedPreferences.getString(ParkLE.BEACON_ADDRESS_INFO, "");

        // TODO: This might be inefficient and dumb and perhaps should be done in a better way...
        for (BluetoothDevice d : mDevices) {
            String mac = d.getAddress();
            if (mac.equals(ParkLE.CAR_MODULE_ADDRESS)) {
                currentCarModuleState = ParkLE.CONNECTED;
            } else if (ParkLE.lotNames.containsKey(mac)) {
                currentBeaconState = ParkLE.CONNECTED;
                currentBeaconAddress = mac;
            }
        }


        switch (currentCarState) {
            case ParkLE.CAR_NOT_IN_LOT:
                if ((currentCarModuleState == ParkLE.CONNECTED) && (currentBeaconState == ParkLE.CONNECTED)) {
                    currentCarState = ParkLE.CAR_IDLE_IN_LOT;
                }
                Log.e("TESTING", "CAR_NOT_IN_LOT");
                break;

            case ParkLE.CAR_IDLE_IN_LOT:
                if (currentCarModuleState == ParkLE.NOT_CONNECTED) {
                    currentCarState = ParkLE.CAR_PARKED_IN_LOT;
                    updateCloud(true, ParkLE.lotNames.get(currentBeaconAddress));
                } else if (currentBeaconState == ParkLE.NOT_CONNECTED) {
                    currentCarState = ParkLE.CAR_NOT_IN_LOT;
                }
                Log.e("TESTING", "CAR_IDLE_IN_LOT");
                break;

            case ParkLE.CAR_PARKED_IN_LOT:
                if (currentCarModuleState == ParkLE.CONNECTED) {
                    currentCarState = ParkLE.CAR_IDLE_IN_LOT;
                    updateCloud(false, ParkLE.lotNames.get(currentBeaconAddress));
                }
                Log.e("TESTING", "CAR_PARKED_IN_LOT");
                break;
        }

        SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();
        editor.putInt(ParkLE.CAR_STATE_INFO, currentCarState);
        editor.putString(ParkLE.BEACON_ADDRESS_INFO, currentBeaconAddress);
        editor.commit();
    }

    private void updateCloud(boolean parked, String lotName) {

    }

    private void restartAlarm() {
        Log.e("Restart Alarm", "Setting alarm");
        Intent checkBeaconAlarm = new Intent(this, BeaconWakefulReceiver.class);
        checkBeaconAlarm.setAction(ParkLE.INTENT_ACTION_CHECK_BEACON);
        PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(this, 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ParkLE.ALARM_INTERVAL, pendingCheckBeaconAlarm);
        BeaconWakefulReceiver.completeWakefulIntent(serviceIntent);
    }
}
