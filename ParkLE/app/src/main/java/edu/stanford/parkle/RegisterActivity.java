package edu.stanford.parkle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText name, email, licensePlate, password, confirmpassword;
    Button register;
    RadioGroup passTypeGroup;
    RadioButton passA, passC, passRadioButton;
    String passType;

    Calendar c = Calendar.getInstance();

    Firebase myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myRef = new Firebase("https://park-le.firebaseio.com");

        name = (EditText)findViewById(R.id.registerNameInput);
        email = (EditText)findViewById(R.id.registerEmail);
        licensePlate = (EditText)findViewById(R.id.licensePlate);
        password = (EditText)findViewById(R.id.passwordInput);
        confirmpassword = (EditText)findViewById(R.id.confirmPasswordInput);

        passTypeGroup = (RadioGroup)findViewById(R.id.passTypeRadioGroup);
        passA = (RadioButton)findViewById(R.id.radioButtonA);
        passC = (RadioButton)findViewById(R.id.radioButtonC);

        register = (Button)findViewById(R.id.registerFirebase);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String regName = name.getText().toString();
                final String regEmail = email.getText().toString();
                final String regLicenseNum = licensePlate.getText().toString();
                final String regPW = password.getText().toString();
                final String regConfirmPW = confirmpassword.getText().toString();

                if(regName.isEmpty() || regEmail.isEmpty() || regLicenseNum.isEmpty() || regPW.isEmpty() || regConfirmPW.isEmpty() || !(passA.isChecked() || passC.isChecked())) {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    myRef.createUser(regEmail, regPW, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> stringObjectMap) {
                            // on success, log the user in and store needed information
                            Toast.makeText(getApplicationContext(), regName + ", welcome to ParkLE", Toast.LENGTH_SHORT).show();

                            // store pass type
                            String passType = null;
                            if (passA.isChecked()) {
                                passType = "A";
                                Log.e("TYPE:","A");
                            } else {
                                passType = "C";
                                Log.e("TYPE:","C");
                            }

                            // store register time
                            c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
                            String time = df.format(c.getTime());

                            String uid = stringObjectMap.get("uid").toString();

                            Map<String, String> driver = new HashMap<String, String>();
                            driver.put("name",regName);
                            driver.put("licensePlate",regLicenseNum);
                            driver.put("passType",passType);
                            driver.put("registerTime",time);

                            Firebase newDriver = myRef.child(uid).push();
                            String fbid = newDriver.getKey();

                            newDriver.setValue(driver);

                            // launch next activity
//                            Intent nextIntent = new Intent(getApplicationContext(),LoginActivity.class);
//                            startActivity(nextIntent);
//                            finish();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            // on error report that there was an error and account not created
                            Toast.makeText(getApplicationContext(), "Error creating account! Try again", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });


        // set listener to radio buttons to ensure only one radio button is checked at a time
        passTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                passRadioButton = (RadioButton)findViewById(i);
//                if (i == R.id.radioButtonA) {
//                    passType = "A";
//                } else {
//                    passType = "C";
//                }

            }
        });


//        passA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (passC.isChecked()) {
//                    passC.setChecked(false);
//                    // passA.setChecked(true);
//                }
//            }
//        });
//
//        passC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (passA.isChecked()) {
//                    passA.setChecked(false);
//                    passC.setChecked(true);
//                }
//                // passC.setChecked(true);
//            }
//        });


    }
}
