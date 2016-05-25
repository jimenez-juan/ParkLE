package edu.stanford.parkle;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by juanj on 5/23/16.
 */
public class ProfileDialogFragment extends DialogFragment {

    TextView curPassType, curLotParked, preference1, preference2, preference3, profileTitle;
    Button changePass, changePref;
    String newPassType = "";
    private OnCompleteListener mListener;

    public ProfileDialogFragment() {

    }

    public static ProfileDialogFragment newInstance(String passType, String lotParked, String name) {
        ProfileDialogFragment frag = new ProfileDialogFragment();
        Bundle args = new Bundle();
        args.putString("passType",passType);
        args.putString("lotParked",lotParked);
        args.putString("name",name);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String passType = getArguments().getString("passType");
        String lotParked = getArguments().getString("lotParked");
        String name = getArguments().getString("name");

        profileTitle = (TextView) view.findViewById(R.id.fragmentTitle);
        profileTitle.setText(name + "'s Profile");

        preference1 = (TextView) view.findViewById(R.id.preference1);
        preference2 = (TextView) view.findViewById(R.id.preference2);
        preference3 = (TextView) view.findViewById(R.id.preference3);


        curPassType = (TextView) view.findViewById(R.id.currentPassTypeView);
        curPassType.setText(passType);
        curPassType.setVisibility(View.VISIBLE);

        curLotParked = (TextView) view.findViewById(R.id.fragmentLotParkedView);
        curLotParked.setText(lotParked);
        curPassType.setVisibility(View.VISIBLE);

        changePass = (Button) view.findViewById(R.id.changePass);
        changePref = (Button) view.findViewById(R.id.changePreferences);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog changePassDialog = new AlertDialog.Builder(getActivity()).create();
                final RadioGroup changePassRadioGroup = new RadioGroup(getActivity());
                changePassDialog.setTitle("Change Pass Type");

                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                final RadioButton RB_A = new RadioButton(getActivity());
                final RadioButton RB_C = new RadioButton(getActivity());

                RB_A.setText("A");
                RB_C.setText("C");
                changePassRadioGroup.addView(RB_A);
                changePassRadioGroup.addView(RB_C);

                TextView space = new TextView(getActivity());
                space.setText("");
                layout.addView(space);

                layout.addView(changePassRadioGroup);
                changePassDialog.setView(layout);

                changePassRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        if (RB_A.isChecked()) {
                            newPassType = "A";
                            Log.e("CHANGE_PASS",newPassType);
                        } else {
                            newPassType = "C";
                            Log.e("CHANGE_PASS",newPassType);
                        }
                    }
                });

                changePassDialog.setButton(DialogInterface.BUTTON_POSITIVE,"Enter",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        curPassType.setText(newPassType);
                        curPassType.setVisibility(View.VISIBLE);

                        mListener.onComplete(newPassType);
                    }

                });

                changePassDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        // close dialog
                    }

                });

                changePassDialog.show();
            }
        });

        changePref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog changePreferences = new AlertDialog.Builder(getActivity()).create();
                changePreferences.setTitle("Change your parking preferences");

                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText pref1 = new EditText(getActivity());
                pref1.setHint("Preference 1");
                pref1.setTextSize(15);

                final EditText pref2 = new EditText(getActivity());
                pref2.setHint("Preference 2");
                pref2.setTextSize(15);

                final EditText pref3 = new EditText(getActivity());
                pref3.setHint("Preference 3");
                pref3.setTextSize(15);

                layout.addView(pref1);
                layout.addView(pref2);
                layout.addView(pref3);

                changePreferences.setView(layout);

                changePreferences.setButton(DialogInterface.BUTTON_POSITIVE,"Enter",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(pref1.getText().toString().isEmpty() || pref2.getText().toString().isEmpty() || pref3.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), "Please fill out all preferences", Toast.LENGTH_SHORT).show();
                        } else {
                            preference1.setText(pref1.getText().toString());
                            preference2.setText(pref2.getText().toString());
                            preference3.setText(pref3.getText().toString());
                            mListener.onCompletePref(pref1.getText().toString(), pref2.getText().toString(), pref3.getText().toString());
                            Toast.makeText(getActivity(), "Preferences saved", Toast.LENGTH_SHORT).show();
                        }
                    }

                });

                changePreferences.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //close dialog
                    }

                });

                changePreferences.show();

            }
        });

    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    public static interface OnCompleteListener {
        public abstract void onComplete(String passType);
        public abstract void onCompletePref(String pref1, String pref2, String pref3);
    }


    // make sure the Activity implemented it
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }


}
