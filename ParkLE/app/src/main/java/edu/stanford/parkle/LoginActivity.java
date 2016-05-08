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
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginButton, registerButton;
    ProgressDialog mAuthProgressDialog;
    Firebase myRef;

    SharedPreferences sharedPreferences;

    String uid;

    public static final String MyUSER = "MyUser" ;
    public static final String Email = "emailKey";
    public static final String Password = "passwordKey";
    public static final String Uid = "uidKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(MyUSER, Context.MODE_PRIVATE);

        email = (EditText)findViewById(R.id.loginEmail);
        password = (EditText)findViewById(R.id.loginPassword);

        loginButton = (Button)findViewById(R.id.loginButton);
        registerButton = (Button)findViewById(R.id.registerButton);

        myRef = new Firebase("https://park-le.firebaseio.com");

        // if user logged in, then take straight to next activity
        if (sharedPreferences.contains(Email) && sharedPreferences.contains(Password)) {
            uid = sharedPreferences.getString(Uid, null);
//            Intent nextIntent = new Intent(getApplicationContext(),ControlActivity.class);
//            nextIntent.putExtra("uid", uid);
//            startActivity(nextIntent);
//            finish();
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

                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        editor.putString(Email, email.getText().toString());
                        editor.putString(Password, password.getText().toString());
                        editor.putString(Uid, uid);
                        editor.commit();

                        mAuthProgressDialog.dismiss();
                        email.setText("");
                        password.setText("");

                        // Take user to next activity
//                        Intent nextIntent = new Intent(getApplicationContext(),ControlActivity.class);
//                        nextIntent.putExtra("uid", uid);
//                        startActivity(nextIntent);
//                        finish();
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
