package edu.stanford.parkle;

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

    EditText email, password;
    Button loginButton, registerButton;
    ProgressDialog mAuthProgressDialog;
    Firebase myRef;

    String uid;

    String passType, macAddress;

    public static final String Email = "emailKey";
    public static final String Password = "passwordKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText)findViewById(R.id.loginEmail);
        password = (EditText)findViewById(R.id.loginPassword);

        loginButton = (Button)findViewById(R.id.loginButton);
        registerButton = (Button)findViewById(R.id.registerButton);

        myRef = new Firebase("https://park-le.firebaseio.com");

        // if user logged in, then take straight to next activity
        if (ParkLE.sharedPreferences.contains(Email) && ParkLE.sharedPreferences.contains(Password)) {
            uid = ParkLE.sharedPreferences.getString(ParkLE.UID_KEY, null);
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
                mAuthProgressDialog.setTitle("Loading");
                mAuthProgressDialog.setMessage("Authenticating...");
                mAuthProgressDialog.setCancelable(false);
                mAuthProgressDialog.show();

                myRef.authWithPassword(email.getText().toString(), password.getText().toString(), new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        uid = authData.getUid();
                        String key = myRef.child(uid).getKey();
                        Log.e("TEST: ",key);

                        myRef.child(uid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                passType = dataSnapshot.child("passType").getValue().toString();
                                macAddress = dataSnapshot.child("moduleMacAddress").getValue().toString();

                                Log.e("PASS_T_V: ",passType);
                                Log.e("MAC_A_V: ",macAddress);
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });

//                        myRef.child(uid).addChildEventListener(new ChildEventListener() {
//                            @Override
//                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                passType = dataSnapshot.child("passType").getValue().toString();
//                                macAddress = dataSnapshot.child("moduleMacAddress").getValue().toString();
//
//                                Log.e("PASS_T: ",passType);
//                                Log.e("MAC_A: ",macAddress);
//                            }
//
//                            @Override
//                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                            }
//
//                            @Override
//                            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                            }
//
//                            @Override
//                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                            }
//
//                            @Override
//                            public void onCancelled(FirebaseError firebaseError) {
//                            }
//                        });

                        SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();

                        editor.putString(Email, email.getText().toString());
                        editor.putString(Password, password.getText().toString());
                        editor.putString(ParkLE.UID_KEY, uid);
                        editor.putString(ParkLE.PASS_TYPE_KEY, passType);
                        editor.putString(ParkLE.MAC_ADDRESS_KEY, macAddress);
                        editor.commit();

                        mAuthProgressDialog.dismiss();
                        email.setText("");
                        password.setText("");


                        //                        passType = (String) myRef.child(uid).child("passType").getValue().toString();

//                        myRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Map<String, String> userInfo = new HashMap<String, String>();
//                                userInfo = (Map) dataSnapshot.getValue();
//                                passType = userInfo.get("passType");
//                                macAddress = userInfo.get("MacAddress");
//
//                                Log.e("PASS_T: ", passType);
//                                Log.e("MAC_A: ", macAddress);
//                            }
//
//                            @Override
//                            public void onCancelled(FirebaseError firebaseError) {
//
//                            }
//                        });


                        // Take user to next activity
                        Intent nextIntent = new Intent(getApplicationContext(),MapActivity.class);
                        startActivity(nextIntent);
                        finish();
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        mAuthProgressDialog.dismiss();
                        // Authentication failed
                        Toast.makeText(getApplicationContext(), "Invalid username and password", Toast.LENGTH_SHORT).show();
                        email.setText("");
                        password.setText("");
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
