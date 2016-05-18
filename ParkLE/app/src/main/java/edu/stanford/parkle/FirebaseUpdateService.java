package edu.stanford.parkle;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;

public class FirebaseUpdateService extends Service {

    private Firebase mFirebaseRef;

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseRef = new Firebase("https://park-le.firebaseio.com");
        Log.e("Testing", "Service got created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            final boolean isParked = intent.getBooleanExtra("isParked", false);
            final String lotName = intent.getStringExtra("lotName");
            final String passType = intent.getStringExtra("passType");

            Log.e("Testing", "Service got started");
            Log.e("Testing", "" + isParked);


//        mFirebaseRef.child("users").child(mFirebaseRef.getAuth().getUid()).child("rides")
//                .setValue(json);
            mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
                    .setValue(isParked, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError error, Firebase ref) {
                            if (error == null) {
                                // TODO: Something?
                            } else {
                                mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("isParked?")
                                        .setValue(isParked, new Firebase.CompletionListener() {
                                            @Override
                                            public void onComplete(FirebaseError error, Firebase ref) {
                                                if (error == null) {
                                                    // TODO: Something?
                                                } else {
                                                    Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
                                                    Log.e("FIREBASE", error.getMessage());
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
                                // TODO: Something?
                            } else {
                                mFirebaseRef.child("users").child("-KHb0FTdZ4D3J1OmijHp").child("lotName")
                                        .setValue(lotName, new Firebase.CompletionListener() {
                                            @Override
                                            public void onComplete(FirebaseError error, Firebase ref) {
                                                if (error == null) {
                                                    // TODO: Something?
                                                } else {
                                                    Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
                                                    Log.e("FIREBASE", error.getMessage());
                                                }
                                            }
                                        });
                            }
                        }
                    });

            mFirebaseRef.child("lots").child(lotName).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(final MutableData currentData) {
                    if (currentData.getValue() == null) {
                        // TODO: Handle this error
                        Log.e("FIREBASE", "current data is null");
                        Log.e("FIREBASE", "a " + currentData.toString());
                        currentData.child("numCPassesInLot").setValue(1);
                    } else {
                        long inc = isParked ? 1 : -1;
                        if (passType.equals("A")) {
                            currentData.child("numAPassesInLot").setValue((long) currentData.child("numAPassesInLot").getValue() + inc);
                        } else if (passType.equals("C")) {
                            currentData.child("numCPassesInLot").setValue((long) currentData.child("numCPassesInLot").getValue() + inc);
                        }
                    }
                    Log.e("FIREBASE", "b " + currentData.toString());
                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(FirebaseError firebaseError, boolean committed, DataSnapshot currentData) {
                    if (firebaseError != null) {
                        Log.e("FIREBASE", "Could not update the firebase. Data is now inaccurate.");
                        Log.e("FIREBASE", firebaseError.getMessage());
                    } else {
                        if (committed) {
                            Log.e("FIREBASE", "c " + currentData.toString());
                        }
                    }
                }
            }, false);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
