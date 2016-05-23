package edu.stanford.parkle;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private Button loginButton, registerButton;
    private ProgressDialog mAuthProgressDialog;
    private Firebase myRef;

    public static final String Email = "emailKey";
    public static final String Password = "passwordKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.loginEmail);
        password = (EditText) findViewById(R.id.loginPassword);

        loginButton = (Button) findViewById(R.id.loginButton);
        registerButton = (Button) findViewById(R.id.registerButton);

        myRef = new Firebase("https://park-le.firebaseio.com");

        // if user logged in, then take straight to next activity
        if (ParkLE.sharedPreferences.contains(Email) && ParkLE.sharedPreferences.contains(Password)) {
            String uid = ParkLE.sharedPreferences.getString(ParkLE.UID_KEY, null);
            Intent nextIntent = new Intent(getApplicationContext(),MapActivity.class);
            nextIntent.putExtra("uid", uid);
            startActivity(nextIntent);
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Setup the progress dialog that is displayed later when authenticating with Firebase */
                mAuthProgressDialog = new ProgressDialog(LoginActivity.this);
                mAuthProgressDialog.setTitle("Logging in.");
                mAuthProgressDialog.setMessage("Authenticating...");
                mAuthProgressDialog.setCancelable(false);
                mAuthProgressDialog.show();

                myRef.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        final String uid = authData.getUid();

                        myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String passType = dataSnapshot.child("passType").getValue(String.class);
                                String macAddress = dataSnapshot.child("moduleMacAddress").getValue(String.class);
                                boolean wasParked = dataSnapshot.child("isParked?").getValue(String.class).equals("true") ;

                                int carStatus = wasParked ? ParkLE.CAR_PARKED_IN_LOT : ParkLE.CAR_NOT_IN_LOT;
                                Log.e("PASS_T_V: ",passType);
                                Log.e("MAC_A_V: ", macAddress);

                                SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();

                                editor.putString(Email, email.getText().toString());
                                editor.putString(Password, password.getText().toString());

                                editor.putString(ParkLE.UID_KEY, uid);
                                editor.putInt(ParkLE.CAR_STATE_INFO, carStatus);
                                editor.putString(ParkLE.PASS_TYPE_KEY, passType);
                                editor.putString(ParkLE.MAC_ADDRESS_KEY, macAddress);
                                editor.putBoolean(ParkLE.WAS_PARKED_KEY, wasParked);
                                editor.commit();
                                email.setText("");
                                password.setText("");

                                // Setting Alarm
                                Intent checkBeaconAlarm = new Intent(getApplicationContext(), BeaconWakefulReceiver.class);
                                checkBeaconAlarm.setAction(ParkLE.INTENT_ACTION_CHECK_BEACON);
                                PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
                                AlarmManager alarms = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                                alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ParkLE.ALARM_INTERVAL_MS, pendingCheckBeaconAlarm);

                                Log.e("ME202", "Setting alarm");
                                // Done Setting Alarm

                                // Take user to next activity
                                Intent nextIntent = new Intent(getApplicationContext(),MapActivity.class);
                                startActivity(nextIntent);
                                mAuthProgressDialog.dismiss();
                                finish();
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                email.setText("");
                                password.setText("");
                                mAuthProgressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "There was an unknown error logging in. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });


                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        String t;
                        switch (firebaseError.getCode()) {
                            case FirebaseError.USER_DOES_NOT_EXIST:
                            case FirebaseError.INVALID_PASSWORD:
                            case FirebaseError.INVALID_EMAIL:
                            case FirebaseError.INVALID_AUTH_ARGUMENTS:
                                t = "Incorrect username and/or password";
                                break;
                            default:
                                t = "There was an error in authentication. Please try again.";
                                break;
                        }
                        mAuthProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, t, Toast.LENGTH_LONG).show();
                        email.setText("");
                    }
                });


            }

        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextIntent = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(nextIntent);
            }
        });


    }
}
