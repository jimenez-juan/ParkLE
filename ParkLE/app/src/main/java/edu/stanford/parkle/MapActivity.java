package edu.stanford.parkle;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MapActivity extends Activity {

    Button logoutButton;
    GoogleMap googleMap;
    Firebase myRef;
    TextView pref1Spaces, pref2Spaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        logoutButton = (Button)findViewById(R.id.logoutButton);

        myRef = new Firebase("https://park-le.firebaseio.com");

        pref1Spaces = (TextView) findViewById(R.id.parkingLotSpaces1);
        pref2Spaces = (TextView) findViewById(R.id.parkingLotSpaces2);

        FragmentManager FM = getFragmentManager();
        final FragmentTransaction FT = FM.beginTransaction();

        final MapFragmentClass MF = new MapFragmentClass();
        FT.add(R.id.mapLayout, MF);
        FT.commit();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog popUpDialog = new AlertDialog.Builder(MapActivity.this).create();
                popUpDialog.setTitle("Logout?");
                popUpDialog.setMessage("Are you sure you want to logout?");
                popUpDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Logout",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        // destroy the stored PW and login in the shared preferences
                        SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();
                        editor.clear();
                        editor.commit();

                        FT.remove(MF);

                        // Removing the alarm
                        Intent checkBeaconAlarm = new Intent(getApplicationContext(), BeaconWakefulReceiver.class);
                        checkBeaconAlarm.setAction(ParkLE.INTENT_ACTION_CHECK_BEACON);
                        PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager alarms = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                        alarms.cancel(pendingCheckBeaconAlarm);
                        // Done Removing the alarm

                        // take the user back to the login screen
                        finish();
                        Intent nextIntent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(nextIntent);

                        Toast.makeText(getApplicationContext(),"Logged out successfully",Toast.LENGTH_SHORT).show();
                    }

                });

                popUpDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        // close the dialog
                    }
                });

                popUpDialog.show();
            }
        });

        myRef.child("lots").child("Lot A").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numSpots = (long) dataSnapshot.child("numSpots").getValue();
                long numASpacesInLot = (long) dataSnapshot.child("numAPassesInLot").getValue();
                long numCSpacesInLot = (long) dataSnapshot.child("numCPassesInLot").getValue();

                Log.e("FIREBASE",String.valueOf(numSpots));
                Log.e("FIREBASE A",String.valueOf(numASpacesInLot));
                Log.e("FIREBASE C",String.valueOf(numCSpacesInLot));

                long numAvailable = numSpots - numASpacesInLot - numCSpacesInLot;
                long occupancy = ((numSpots - numAvailable)*100/numSpots);
                if (occupancy >= 100) {
                    pref1Spaces.setText("FULL");
                } else {
                    pref1Spaces.setText(String.valueOf(occupancy)+"%");
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


        myRef.child("lots").child("Lot B").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long numSpots = (long) dataSnapshot.child("numSpots").getValue();
                long numASpacesInLot = (long) dataSnapshot.child("numAPassesInLot").getValue();
                long numCSpacesInLot = (long) dataSnapshot.child("numCPassesInLot").getValue();

                Log.e("FIREBASE",String.valueOf(numSpots));
                Log.e("FIREBASE A",String.valueOf(numASpacesInLot));
                Log.e("FIREBASE C",String.valueOf(numCSpacesInLot));

                long numAvailable = numSpots - numASpacesInLot - numCSpacesInLot;
                long occupancy = ((numSpots - numAvailable)*100/numSpots);

                if (occupancy >= 100) {
                    pref2Spaces.setText("FULL");
                } else {
                    pref2Spaces.setText(String.valueOf(occupancy)+"%");
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }
}
