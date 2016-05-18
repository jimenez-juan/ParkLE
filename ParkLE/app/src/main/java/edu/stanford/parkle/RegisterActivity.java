package edu.stanford.parkle;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.params.BlackLevelPattern;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{

    EditText name, email, licensePlate, password, confirmpassword;
    Button registerButton, pairBluetoothDeviceButton;
    RadioGroup passTypeGroup;
    RadioButton passA, passC, passRadioButton;
    String passType;

    Calendar c = Calendar.getInstance();

    Firebase myRef;

    private BluetoothAdapter mAdapter;
    private ListView deviceList;
    private ArrayAdapter deviceAdapter;
    private ArrayList<BluetoothInfo> devices;

    static final String Email = "emailKey";
    static final String Password = "passwordKey";
    static final String Uid = "uidKey";

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

        registerButton = (Button)findViewById(R.id.registerFirebase);
        pairBluetoothDeviceButton = (Button)findViewById(R.id.registerBluetoothDeviceButton);


        registerButton.setOnClickListener(new View.OnClickListener() {
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
                            driver.put("moduleMacAddress","");
                            driver.put("isParked?","false");
                            driver.put("lotName","");

                            Firebase newDriver = myRef.child("users").push();
                            String fbid = newDriver.getKey();

                            newDriver.setValue(driver);

                            // add user email, password, and UID to sharedPreferences
                            SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();

                            editor.putString(Email, email.getText().toString());
                            editor.putString(Password, password.getText().toString());
                            editor.putString(Uid, uid);
                            editor.putString(ParkLE.PASS_TYPE, passType);
                            editor.commit();

                            // launch next activity
                            Intent nextIntent = new Intent(getApplicationContext(),MapActivity.class);
                            startActivity(nextIntent);
                            finish();
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

        pairBluetoothDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                LayoutInflater factory = getLayoutInflater();
                final View vi = factory.inflate(R.layout.dialog_bluetooth_pair, null);
                builder.setView(vi);
                builder.setTitle("Select your Bluetooth device.");
                deviceList = (ListView) vi.findViewById(R.id.device_list);

                devices = new ArrayList<>();
                deviceAdapter = new DeviceListAdapter(RegisterActivity.this, R.layout.item_bluetooth_device, devices);
                mAdapter = BluetoothAdapter.getDefaultAdapter();
                deviceList.setAdapter(deviceAdapter);

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                final AlertDialog dialog = builder.create();

                deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        ((TextView) findViewById(R.id.bluetooth_mac_text)).setText(devices.get(position).device.getAddress());
                        mAdapter.stopLeScan(RegisterActivity.this);
                        dialog.dismiss();
                    }
                });

                dialog.setCancelable(false);
                dialog.show();

                mAdapter.startLeScan(RegisterActivity.this);
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

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        BluetoothInfo bi = new BluetoothInfo(device, rssi);
        int ind = devices.indexOf(bi);
        if (ind < 0) {
            devices.add(bi);
        } else {
            devices.set(ind, bi);
        }
        deviceAdapter.notifyDataSetChanged();
    }
}
