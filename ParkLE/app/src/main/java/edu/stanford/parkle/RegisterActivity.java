package edu.stanford.parkle;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.params.BlackLevelPattern;
import android.media.MediaPlayer;
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

    private EditText name, email, licensePlate, password, confirmPassword;
    private TextView bluetoothMACText;
    private Button registerButton, pairBluetoothDeviceButton;
    private RadioGroup passTypeGroup;
    private RadioButton passA, passC;

    private Calendar c = Calendar.getInstance();

    private Firebase myRef;

    private BluetoothAdapter mAdapter;
    private ListView deviceList;
    private ArrayAdapter deviceAdapter;
    private ArrayList<BluetoothInfo> devices;
    private ProgressDialog mProgress;

    static final String Email = "emailKey";
    static final String Password = "passwordKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        myRef = new Firebase("https://park-le.firebaseio.com");

        name = (EditText)findViewById(R.id.registerNameInput);
        email = (EditText)findViewById(R.id.registerEmail);
        licensePlate = (EditText)findViewById(R.id.licensePlate);
        password = (EditText)findViewById(R.id.passwordInput);
        confirmPassword = (EditText)findViewById(R.id.confirmPasswordInput);

        passTypeGroup = (RadioGroup)findViewById(R.id.passTypeRadioGroup);
        passA = (RadioButton)findViewById(R.id.radioButtonA);
        passC = (RadioButton)findViewById(R.id.radioButtonC);

        registerButton = (Button)findViewById(R.id.registerFirebase);
        pairBluetoothDeviceButton = (Button)findViewById(R.id.registerBluetoothDeviceButton);
        bluetoothMACText = ((TextView) findViewById(R.id.bluetooth_mac_text));

        mProgress = new ProgressDialog(RegisterActivity.this);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        mProgress.setMessage("Registering...");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String regName = name.getText().toString();
                final String regEmail = email.getText().toString();
                final String regLicenseNum = licensePlate.getText().toString();
                final String regPW = password.getText().toString();
                final String regConfirmPW = confirmPassword.getText().toString();
                final String regMAC = bluetoothMACText.getText().toString();

                if (regName.isEmpty() || regEmail.isEmpty() || regLicenseNum.isEmpty() || regPW.isEmpty()
                        || regConfirmPW.isEmpty() || !(passA.isChecked() || passC.isChecked()) || regMAC.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields.", Toast.LENGTH_LONG).show();
                } else {
                    if (!regConfirmPW.equals(regPW)) {
                        Toast.makeText(getApplicationContext(), "Passwords do not match.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    mProgress.show();
                    myRef.createUser(regEmail, regPW, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> stringObjectMap) {
                            // on success, log the user in and store needed information
                            Toast.makeText(getApplicationContext(), regName + ", welcome to ParkLE.", Toast.LENGTH_SHORT).show();

                            // store pass type
                            String passType;
                            if (passA.isChecked()) {
                                passType = "A";
                                Log.e("TYPE:", "A");
                            } else {
                                passType = "C";
                                Log.e("TYPE:", "C");
                            }

                            // store register time
                            c = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
                            String time = df.format(c.getTime());

                            String uid = stringObjectMap.get("uid").toString();

                            Map<String, String> driver = new HashMap<>();
                            driver.put("name", regName);
                            driver.put("licensePlate", regLicenseNum);
                            driver.put("passType", passType);
                            driver.put("registerTime", time);
                            driver.put("moduleMacAddress", regMAC);
                            driver.put("isParked?", "false");
                            driver.put("lotName", "");

                            myRef.child("users").child(uid).setValue(driver, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null) {
                                        Toast.makeText(getApplicationContext(), "Error updating cloud info. Please contact for help.", Toast.LENGTH_LONG).show();
                                        Log.e("FIREBASE", "Could not update the user's firebase info. Data is now inaccurate.");
                                        Log.e("FIREBASE", firebaseError.getMessage());
                                    }
                                }
                            });

                            // add user email, password, and UID to sharedPreferences
                            SharedPreferences.Editor editor = ParkLE.sharedPreferences.edit();

                            editor.putString(Email, regEmail);
                            editor.putString(Password, regPW);

                            editor.putString(ParkLE.UID_KEY, uid);
                            editor.putInt(ParkLE.CAR_STATE_INFO, ParkLE.CAR_NOT_IN_LOT);
                            editor.putString(ParkLE.PASS_TYPE_KEY, passType);
                            editor.putString(ParkLE.MAC_ADDRESS_KEY, regMAC);
                            editor.putBoolean(ParkLE.WAS_PARKED_KEY, false);
                            editor.commit();

                            // Setting Alarm
                            Intent checkBeaconAlarm = new Intent(getApplicationContext(), BeaconWakefulReceiver.class);
                            checkBeaconAlarm.setAction(ParkLE.INTENT_ACTION_CHECK_BEACON);
                            PendingIntent pendingCheckBeaconAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, checkBeaconAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager alarms = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                            alarms.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ParkLE.ALARM_INTERVAL_MS, pendingCheckBeaconAlarm);

                            Log.e("ME202", "Setting alarm");
                            // Done Setting Alarm

                            // launch next activity
                            Intent nextIntent = new Intent(getApplicationContext(), MapActivity.class);
                            startActivity(nextIntent);
                            mProgress.dismiss();
                            finish();

                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            String t;
                            switch (firebaseError.getCode()) {
                                case FirebaseError.EMAIL_TAKEN:
                                    t = "An account with that email already exists.";
                                    break;
                                case FirebaseError.INVALID_EMAIL:
                                    t = "Invalid email.";
                                    break;
                                default:
                                    t = "There was an error creating a new user.";
                                    Log.e("FIREBASE", firebaseError.getMessage());
                                    Log.e("FIREBASE", firebaseError.getDetails());
                                    break;
                            }
                            mProgress.dismiss();
                            Toast.makeText(RegisterActivity.this, t, Toast.LENGTH_LONG).show();
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
