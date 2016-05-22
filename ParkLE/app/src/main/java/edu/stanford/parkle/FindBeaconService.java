package edu.stanford.parkle;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Semaphore;

// TODO: Change hardcoded UID and Lisence Plate and Bluetooth module mac address and get from preferences.

public class FindBeaconService extends IntentService implements BluetoothAdapter.LeScanCallback {

    // TODO: Probably should use UUIDs instead of MAC addresses to ensure uniqueness

//    private static final UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
//    private static final UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
//    private static final UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
//    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter mAdapter;
    //private HashSet<BluetoothDevice> mDevices;
    private HashSet<String> mDevices;

    private Firebase mFirebaseRef;

    public FindBeaconService() {
        super("FindBeaconService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new HashSet<>();

        mFirebaseRef = new Firebase("https://park-le.firebaseio.com");

        mAdapter.startLeScan(this);

        try {
            Thread.sleep(ParkLE.SCAN_PERIOD_MS);
        } catch (Exception e) {
            Log.e("BeaconScanning", "Error in attempt to sleep for scan.");
            restartAlarm(intent);
            BeaconWakefulReceiver.completeWakefulIntent(intent);
            return;
        }
        if (mDevices.size() > 0) {
            Log.e("Service", "found these devices:");
            for (String s : mDevices) {
                Log.e("Service", s);
            }
        }
        stopScan();
        updateState();
        if (!ParkLE.sharedPreferences.contains(ParkLE.UID_KEY)) { // Don't restart alarm if the user logged out while this thread was running.
            BeaconWakefulReceiver.completeWakefulIntent(intent);
        } else {
            restartAlarm(intent);
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        String addr = device.getAddress();
        if (!mDevices.contains(addr)) mDevices.add(addr);
    }

    private void stopScan() {
        mAdapter.stopLeScan(this);
    }

    private void updateState() {
        int currentCarState = ParkLE.sharedPreferences.getInt(ParkLE.CAR_STATE_INFO, ParkLE.CAR_NOT_IN_LOT);
        int currentCarModuleState = ParkLE.NOT_CONNECTED;
        int currentBeaconState = ParkLE.NOT_CONNECTED;
        String uID = ParkLE.sharedPreferences.getString(ParkLE.UID_KEY,"");
        String currentBeaconAddress = ParkLE.sharedPreferences.getString(ParkLE.BEACON_ADDRESS_INFO, "");
        String userPassType = ParkLE.sharedPreferences.getString(ParkLE.PASS_TYPE_KEY, "");
        String userCarModuleMAC = ParkLE.sharedPreferences.getString(ParkLE.MAC_ADDRESS_KEY, "");

        // TODO: This might be inefficient and dumb and perhaps should be done in a better way...
        if (currentCarState == ParkLE.CAR_PARKED_IN_LOT) {
            for (String mac : mDevices) {
                if (mac.equals(userCarModuleMAC)) {
                    currentCarModuleState = ParkLE.CONNECTED;
                }
            }
        } else {
            for (String mac : mDevices) {
                if (mac.equals(userCarModuleMAC)) {
                    currentCarModuleState = ParkLE.CONNECTED;
                } else if (ParkLE.lotNames.containsKey(mac)) {
                    currentBeaconState = ParkLE.CONNECTED;
                    currentBeaconAddress = mac;
                }
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
                    updateCloud(uID, true, ParkLE.lotNames.get(currentBeaconAddress), userPassType);
                } else if (currentBeaconState == ParkLE.NOT_CONNECTED) {
                    currentCarState = ParkLE.CAR_NOT_IN_LOT;
                }
                break;

            case ParkLE.CAR_PARKED_IN_LOT:
                if (currentCarModuleState == ParkLE.CONNECTED) {
                    currentCarState = ParkLE.CAR_IDLE_IN_LOT;
                    updateCloud(uID, false, ParkLE.lotNames.get(currentBeaconAddress), userPassType);
                }
                break;
        }

        Log.e("TESTING", "Current State: " + currentCarState);

        SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();
        editor.putInt(ParkLE.CAR_STATE_INFO, currentCarState);
        editor.putString(ParkLE.BEACON_ADDRESS_INFO, currentBeaconAddress);
        editor.commit();
    }

    private void updateCloud(final String uID, final boolean isParked, final String lotName, final String passType) {
        Intent intent = new Intent( this, FirebaseUpdateService.class );
        intent.putExtra("uID", uID);
        intent.putExtra("isParked", isParked);
        intent.putExtra("lotName", lotName);
        intent.putExtra("passType", passType);

        startService(intent);

//        final Semaphore semaphore = new Semaphore(0);
//
//
////        mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("rides")
////                .setValue(json);
//        mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
//                .setValue(isParked, new Firebase.CompletionListener() {
//                    @Override
//                    public void onComplete(FirebaseError error, Firebase ref) {
//                        if (error == null) {
//                            semaphore.release();
//                        } else {
//                            mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
//                                    .setValue(isParked, new Firebase.CompletionListener() {
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
//                });
//
//        mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("lotName")
//                .setValue(lotName, new Firebase.CompletionListener() {
//                    @Override
//                    public void onComplete(FirebaseError error, Firebase ref) {
//                        if (error == null) {
//                            semaphore.release();
//                        } else {
//                            mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("lotName")
//                                    .setValue(lotName, new Firebase.CompletionListener() {
//                                        @Override
//                                        public void onComplete(FirebaseError error, Firebase ref) {
//                                            if (error == null) {
//                                                semaphore.release();
//                                            } else {
//                                                Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
//                                                Log.e("FIREBASE", error.getMessage());
//                                                semaphore.release();
//                                            }
//                                        }
//                                    });
//                        }
//                    }
//                });
//
//        mFirebaseRef.child("lots").child(lotName).runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(final MutableData currentData) {
//                if (currentData.getValue() == null) {
//                    // TODO: Handle this error
//                    Log.e("FIREBASE", "current data is null");
//                    Log.e("FIREBASE", "a " + currentData.toString());
//                    currentData.child("numCPassesInLot").setValue(1);
//                } else {
//                    long inc = isParked ? 1 : -1;
//                    if (passType.equals("A")) {
//                        currentData.child("numAPassesInLot").setValue((long) currentData.child("numAPassesInLot").getValue() + inc);
//                    } else if (passType.equals("C")) {
//                        currentData.child("numCPassesInLot").setValue((long) currentData.child("numCPassesInLot").getValue() + inc);
//                    }
//                }
//                Log.e("FIREBASE", "b " + currentData.toString());
//                return Transaction.success(currentData);
//            }
//
//            @Override
//            public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
//                if (firebaseError != null) {
//                    Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
//                    Log.e("FIREBASE", firebaseError.getMessage());
//                    semaphore.release();
//                } else {
//                    if (committed) {
//                        Log.e("FIREBASE", "c " + currentData.toString());
//                        semaphore.release();
//                    }
//                }
//            }
//        }, false);
//
//        try {
//            semaphore.acquire(3);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }

    }

    private void restartAlarm(Intent serviceIntent) {
        Log.e("Restart Alarm", "Setting alarm");
        Intent checkBeaconAlarm = new Intent(this, BeaconWakefulReceiver.class);
        checkBeaconAlarm.setAction(ParkLE.INTENT_ACTION_CHECK_BEACON);
        PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(this, 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ParkLE.ALARM_INTERVAL_MS, pendingCheckBeaconAlarm);
        BeaconWakefulReceiver.completeWakefulIntent(serviceIntent);
    }
}
