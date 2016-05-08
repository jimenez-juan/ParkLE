package edu.stanford.parkle;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by juanj on 5/5/16.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }

}
