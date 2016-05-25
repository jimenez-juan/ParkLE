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
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import org.w3c.dom.Text;

public class MapActivity extends AppCompatActivity implements ProfileDialogFragment.OnCompleteListener {

    Firebase myRef;
    TextView pref1Name, pref2Name, pref3Name, pref1Spaces, pref2Spaces, pref3Spaces;
    Button changePass, changePref;
    String userName;

    FragmentManager FM = getFragmentManager();
    final FragmentTransaction FT = FM.beginTransaction();

    final MapFragmentClass MF = new MapFragmentClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        userName = null;

        FT.add(R.id.mapLayout, MF);
        FT.commit();


        myRef = new Firebase("https://park-le.firebaseio.com");

        pref1Spaces = (TextView) findViewById(R.id.parkingLotSpaces1);
        pref2Spaces = (TextView) findViewById(R.id.parkingLotSpaces2);
        pref3Spaces = (TextView) findViewById(R.id.parkingLotSpaces3);

        pref1Name = (TextView) findViewById(R.id.parkingLotName1);
        pref2Name = (TextView) findViewById(R.id.parkingLotName2);
        pref3Name = (TextView) findViewById(R.id.parkingLotName3);


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
                    pref1Spaces.setText(String.valueOf(numAvailable));
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
//                long occupancy = ((numSpots - numAvailable)*100/numSpots);

                if (occupancy >= 100) {
                    pref2Spaces.setText("FULL");
                } else {
                    pref2Spaces.setText(String.valueOf(numAvailable));
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        myRef.child("lots").child("Lot C").addValueEventListener(new ValueEventListener() {
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
                    pref3Spaces.setText("FULL");
                } else {
                    pref3Spaces.setText(String.valueOf(numAvailable));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        myRef.child("users").child(ParkLE.sharedPreferences.getString(ParkLE.UID_KEY,null)).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userName = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.profile:
                profileMenu();
                break;
            case R.id.logout_settings:
                logoutMenu();
                break;
        }

        return true;
    }

    private void profileMenu () {
        String passType = ParkLE.sharedPreferences.getString(ParkLE.PASS_TYPE_KEY, "C");

        FragmentManager fm = getFragmentManager();
        ProfileDialogFragment profileDialog =  ProfileDialogFragment.newInstance(passType,"Lot A",userName);
        profileDialog.show(fm, "profile_fragment");
    }

    public void onComplete(String passType) {
        // update the shared preferences
        ParkLE.sharedPreferences.edit().putString(ParkLE.PASS_TYPE_KEY, passType);

        // update firebase
        myRef.child("users").child(ParkLE.sharedPreferences.getString(ParkLE.UID_KEY,null)).child("passType").setValue(passType);
    }

    public void onCompletePref(String pref1, String pref2, String pref3) {
        // update preferences shown
        pref1Name.setText(pref1);
        pref2Name.setText(pref2);
        pref3Name.setText(pref3);
    }


    private void logoutMenu () {
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

}
