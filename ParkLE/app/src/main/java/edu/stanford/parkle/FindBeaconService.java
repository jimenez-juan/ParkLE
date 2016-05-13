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
import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class FindBeaconService extends IntentService implements BluetoothAdapter.LeScanCallback {

    // TODO: Probably should use UUIDs instead of MAC addresses to ensure uniqueness

//    private static final UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
//    private static final UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
//    private static final UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
//    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private Intent serviceIntent;

    private BluetoothAdapter mAdapter;
    // TODO: Maybe make this an hashmap of strings and forget the device to make for faster processing
    private ArrayList<BluetoothDevice> mDevices;

    private Firebase mFirebaseRef;

    public FindBeaconService() {
        super("FindBeaconService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        serviceIntent = intent;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new ArrayList<>();

        mFirebaseRef = new Firebase("https://park-le.firebaseio.com");

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

        Log.e("TESTING", "Previous State " + currentCarState);

        switch (currentCarState) {
            case ParkLE.CAR_NOT_IN_LOT:
                if ((currentCarModuleState == ParkLE.CONNECTED) && (currentBeaconState == ParkLE.CONNECTED)) {
                    currentCarState = ParkLE.CAR_IDLE_IN_LOT;
                }
                break;

            case ParkLE.CAR_IDLE_IN_LOT:
                if (currentCarModuleState == ParkLE.NOT_CONNECTED) {
                    currentCarState = ParkLE.CAR_PARKED_IN_LOT;
                    updateCloud(true, ParkLE.lotNames.get(currentBeaconAddress));
                } else if (currentBeaconState == ParkLE.NOT_CONNECTED) {
                    currentCarState = ParkLE.CAR_NOT_IN_LOT;
                }
                break;

            case ParkLE.CAR_PARKED_IN_LOT:
                if (currentCarModuleState == ParkLE.CONNECTED) {
                    currentCarState = ParkLE.CAR_IDLE_IN_LOT;
                    updateCloud(false, ParkLE.lotNames.get(currentBeaconAddress));
                }
                break;
        }

        Log.e("TESTING", "Current State: " + currentCarState);

        SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();
        editor.putInt(ParkLE.CAR_STATE_INFO, currentCarState);
        editor.putString(ParkLE.BEACON_ADDRESS_INFO, currentBeaconAddress);
        editor.commit();
    }

    private void updateCloud(final boolean isParked, final String lotName) {
        final Semaphore semaphore = new Semaphore(0);

//        mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("rides")
//                .setValue(json);
        mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
                .setValue(isParked, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError error, Firebase ref) {
                        if (error == null) {
                            semaphore.release();
                        } else {
                            mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
                                    .setValue(isParked, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError error, Firebase ref) {
                                            if (error == null) {
                                                semaphore.release();
                                            } else {
                                                Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
                                                semaphore.release();
                                            }
                                        }
                                    });
                        }
                    }
        });

        mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("lotName")
                .setValue(lotName, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError error, Firebase ref) {
                        if (error == null) {
                            semaphore.release();
                        } else {
                            mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("lotName")
                                    .setValue(lotName, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError error, Firebase ref) {
                                            if (error == null) {
                                                semaphore.release();
                                            } else {
                                                Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
                                                semaphore.release();
                                            }
                                        }
                                    });
                        }
                    }
        });


        // TODO: Decide if we want to do the difficult process of getting the value in the service or calculate fon the other end
        // TODO: Also change the hardcoded value of the liscense plate
        mFirebaseRef.child("lots").child(lotName).child("ab1234")
                .setValue(isParked, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError error, Firebase ref) {
                        if (error == null) {
                            semaphore.release();
                        } else {
                            mFirebaseRef.child("lots").child(lotName).child("ab1234")
                                    .setValue(isParked, new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError error, Firebase ref) {
                                            if (error == null) {
                                                semaphore.release();
                                            } else {
                                                Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
                                                semaphore.release();
                                            }
                                        }
                                    });
                        }
                    }
                });


//        mFirebaseRef.child("lots").child(lotName).child("numTakenSpots")
//                .setValue(lotName, new Firebase.CompletionListener() {
//                    @Override
//                    public void onComplete(FirebaseError error, Firebase ref) {
//                        if (error == null) {
//                            semaphore.release();
//                        } else {
//                            mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
//                                    .setValue(parked, new Firebase.CompletionListener() {
//                                        @Override
//                                        public void onComplete(FirebaseError error, Firebase ref) {
//                                            if (error == null) {
//                                                semaphore.release();
//                                            } else {
//                                                Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
//                                                semaphore.release();
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//        });

        try
        {
            semaphore.acquire(3);
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }

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
