package edu.stanford.parkle;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

        // Please DO NOT override onCreate. Use init.
        @Override
        public void init(Bundle savedInstanceState) {

            // Add your slide's fragments here.
            // AppIntro will automatically generate the dots indicator and buttons.
            //addSlide(first_fragment);
            //addSlide(second_fragment);
            //addSlide(third_fragment);
            //addSlide(fourth_fragment);

            // Instead of fragments, you can also use our default slide
            // Just set a title, description, background and image. AppIntro will do the rest.
            addSlide(AppIntroFragment.newInstance("Welcome to ParkLE!", "This is the login page. At " +
                    "the bottom you will find the register button which will give you the ability to become a" +
                            "member of the ParkLE community", R.drawable.pic1,
                    getResources().getColor(R.color.mb_gray_dark)));
            addSlide(AppIntroFragment.newInstance("How to Register", "Enter all your information into the fields " +
                            "including your email and your licence plate number", R.drawable.pic2,
                    getResources().getColor(R.color.mb_gray_dark)));
            addSlide(AppIntroFragment.newInstance("How to Register", "Next click the 'Pair Device' button" +
                            "and select your car module device", R.drawable.pic3,
                    getResources().getColor(R.color.mb_gray_dark)));
            addSlide(AppIntroFragment.newInstance("How to Register", "Once you see the Mac Address of the " +
                            "device and the checked symbol you should be ready to confirm your information" +
                            "and login back at the main screen", R.drawable.pic4,
                    getResources().getColor(R.color.mb_gray_dark)));


            // OPTIONAL METHODS
            // Override bar/separator color.
            setBarColor(getResources().getColor(R.color.colorPrimary));
            setSeparatorColor(Color.parseColor("#000000"));

            // SHOW or HIDE the statusbar
            showStatusBar(true);

            // Edit the color of the nav bar on Lollipop+ devices
            //setNavBarColor(Color.parseColor("#3F51B5"));

            // Hide Skip/Done button.
            showSkipButton(true);
            setProgressButtonEnabled(true);
            showDoneButton(true);

            // Turn vibration on and set intensity.
            // NOTE: you will probably need to ask VIBRATE permisssion in Manifest.
            //setVibrate(true);
            //setVibrateIntensity(30);

            // Animations -- use only one of the below. Using both could cause errors.
           // setFadeAnimation(); // OR
            //setZoomAnimation(); // OR
            //setFlowAnimation(); // OR
            setSlideOverAnimation(); // OR
           // setDepthAnimation(); // OR
            //setCustomTransformer(yourCustomTransformer);

            // Permissions -- takes a permission and slide number
            askForPermissions(new String[]{Manifest.permission.CAMERA}, 3);
        }

        @Override
        public void onSkipPressed() {
            // Do something when users tap on Skip button.
        }

        @Override
        public void onDonePressed() {
            // Do something when users tap on Done button.
            finish();
        }

        @Override
        public void onSlideChanged() {
            // Do something when the slide changes.
        }

        @Override
        public void onNextPressed() {
            // Do something when users tap on Next button.
        }

    }
