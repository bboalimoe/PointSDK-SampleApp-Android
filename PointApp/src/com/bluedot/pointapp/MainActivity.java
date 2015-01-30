package com.bluedot.pointapp;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.widget.Toast;

import com.bluedotinnovation.android.pointapp.R;

import java.util.ArrayList;

import au.com.bluedot.point.ApplicationNotification;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.BDError;
import au.com.bluedot.point.BlueDotLocationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.ZoneInfo;
import au.com.bluedot.point.net.engine.ServiceManager;

public class MainActivity extends FragmentActivity implements
        ServiceStatusListener,
        ApplicationNotificationListener {

    private String mPackageName;
    private String mApiKey;
    private String mEmail;
    private String apiKey;
    private String packageName;
    private String email;
    private ServiceManager mServiceManager;
    private ProgressDialog mProgress;
    private FragmentTabHost mTabHost;
    private boolean quit;

    private ArrayList<ZoneInfo> mZonesInfo = new ArrayList<ZoneInfo>();

    private SharedPreferences profile;

    private boolean serviceStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(),
                android.R.id.tabcontent);

        //Create a shared preference to store credentials
        profile = getSharedPreferences(AppConstants.APP_PROFILE, MODE_PRIVATE);

        //get existing credentials from profile
        mApiKey = profile.getString(AppConstants.KEY_API_KEY, null);
        mPackageName = profile.getString(AppConstants.KEY_PACKAGE_NAME, null);
        mEmail = profile.getString(AppConstants.KEY_USERNAME, null);

        //Checking if the app is started from the Url
        if (getIntent() != null && getIntent().getData() != null) {
            Uri customURI = getIntent().getData();
            packageName = customURI.getQueryParameter("BDPointPackageName");
            apiKey = customURI.getQueryParameter("BDPointAPIKey");
            email = customURI.getQueryParameter("BDPointUsername");
            if (checkForChangeInLoginDetails(apiKey, packageName, email)) {
                showPopup();
            } else {
                mApiKey = apiKey;
                mPackageName = packageName;
                mEmail = email;
            }
        }

        //Setup UI
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_section1))
                        .setIndicator(getString(R.string.title_section1)),
                AuthenticationFragment.class, AuthenticationFragment
                        .setLoginDetails(mPackageName, mApiKey, mEmail));

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_section2))
                        .setIndicator(getString(R.string.title_section2)),
                PointMapFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.title_section3))
                        .setIndicator(getString(R.string.title_section3)),
                ChecklistFragment.class, null);

        //Get an instance of ServiceManager
        mServiceManager = ServiceManager.getInstance(this);
       
        //Setup the notification icon to display when a notification action is triggered
        mServiceManager.setNotificationIDResourceID(R.drawable.ic_launcher);
        
        //Setup the notification activity to start when a fired notification is clicked 
        mServiceManager.setCustomMessageAction(new Intent(MainActivity.this,
                MainActivity.class), this);

        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);
    }

    private boolean checkForChangeInLoginDetails(String sapiKey,
                                                 String spackageName, String semail) {
        return !TextUtils.isEmpty(mApiKey)
                && !TextUtils.isEmpty(mPackageName)
                && !TextUtils.isEmpty(mEmail)
                && !TextUtils.isEmpty(sapiKey)
                && !TextUtils.isEmpty(spackageName)
                && !TextUtils.isEmpty(semail)
                && (!mApiKey.equals(sapiKey) || !mPackageName
                .equals(spackageName));
    }

    private void showPopup() {

        new AlertDialog.Builder(this)
                .setTitle("Different Credentials")
                .setCancelable(false)
                .setMessage(
                        "Bluedot Service is already running with ApiKey :"
                                + mApiKey
                                + "\nDo you want to use new ApiKey : " + apiKey
                                + "?")
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (serviceStarted) {
                                    stopService();
                                }
                                if (mTabHost != null) {
                                    AuthenticationFragment authFragment = (AuthenticationFragment) getSupportFragmentManager()
                                            .findFragmentByTag(
                                                    mTabHost.getCurrentTabTag());
                                    if (authFragment != null) {
                                        authFragment.updateLoginDetails(
                                                packageName, apiKey, email);
                                    }
                                }
                            }
                        })
                .setNegativeButton(R.string.no,
                        null).create().show();
    }

    //stop the Bluedot Point Service
    public void stopService() {
        if (mServiceManager != null) {
        	//Call the method stopPointService in ServiceManager to stop Bluedot PointService
            mServiceManager.stopPointService();
            if (mTabHost != null) {
                refreshCurrentFragment(mTabHost.getCurrentTab());
            }
            mZonesInfo.clear();

        }
    }

    //Using ServiceManager to monitor Bluedot Point Service status
    @Override
    protected void onResume() {
        super.onResume();
        mServiceManager.addBlueDotPointServiceStatusListener(this);
        mServiceManager.subscribeForApplicationNotification(this);
        mServiceManager.isBlueDotPointServiceRunning(this);
    }

    @Override
    public void onApplicationNotificationReceived(final
                                                  ApplicationNotification applicationNotification) {

        if (applicationNotification != null
                && applicationNotification.getFence() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            getApplicationContext(),
                            "Application Notification Received !! You have Entered : "
                                    + applicationNotification.getFence().getName(),
                            Toast.LENGTH_LONG).show();
                }
            });

        }

    }


    public ArrayList<ZoneInfo> getZones() {
        return mServiceManager.getZonesAndFences();
    }


    public void subscribeForLocationUpdates(BlueDotLocationListener listener) {
        mServiceManager.subscribeForLocationUpdates(listener);
    }


    private void startBluedotPointService() {

        if (!TextUtils.isEmpty(mApiKey) && !TextUtils.isEmpty(mPackageName)
                && !TextUtils.isEmpty(mEmail)) {
            mServiceManager.sendAuthenticationRequest(mPackageName,
                    mApiKey, mEmail, this);
        } else {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }

            Toast.makeText(this, "Please enter Login Details",
                    Toast.LENGTH_LONG).show();
        }

    }

    public void startAuthentication(String email, String apiKey,
                                    String packageName) {
        mEmail = email;
        mApiKey = apiKey;
        mPackageName = packageName;

        mProgress
                .setMessage(getString(R.string.please_wait_authenticating));
        mProgress.show();

        startBluedotPointService();

    }


    public void refreshCurrentFragment(int tabIndex) {
        switch (tabIndex) {
            case 0:
                AuthenticationFragment authFragment = (AuthenticationFragment) getSupportFragmentManager()
                        .findFragmentByTag(mTabHost.getCurrentTabTag());
                if (authFragment != null) {
                    authFragment.refresh();
                }
                break;
            case 1:
                PointMapFragment pointMapFragment = (PointMapFragment) getSupportFragmentManager()
                        .findFragmentByTag(mTabHost.getCurrentTabTag());
                if (pointMapFragment != null) {
                    pointMapFragment.refresh();
                }
                break;
            case 2:
                ChecklistFragment checklistFragment = (ChecklistFragment) getSupportFragmentManager()
                        .findFragmentByTag(mTabHost.getCurrentTabTag());
                if (checklistFragment != null) {
                    checklistFragment.refresh();
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }

        if (quit && mServiceManager != null) {
            mServiceManager.stopPointService();
        }

        mServiceManager.removeBlueDotPointServiceStatusListener(this);

        mServiceManager.unsubscribeForApplicationNotification(this);


    }

    @Override
    public void onBackPressed() {
        if (serviceStarted)
            new AlertDialog.Builder(this).setTitle("Quit").setMessage("Do you want to quit the app or put it in the background").setPositiveButton("QUIT", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    quit = true;
                    finish();
                }
            }).setNegativeButton("NO", null).setNeutralButton("Background", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();

                }
            }).create().show();

        else
            super.onBackPressed();
    }

    public void unsubscribeLocationUpdates(BlueDotLocationListener listener) {
        mServiceManager.unsubscribeForLocationUpdates(listener);
    }

    //This is called when Bluedot Point Service started successful, your app logic depending on the Bluedot PointService could be started from here
    @Override
    public void onBlueDotPointServiceStartedSuccess() {
        if (mProgress != null && mProgress.isShowing())
            mProgress.dismiss();

        //Here you can store the credentials in your app shared preference since they are correct
        profile.edit().putString(AppConstants.KEY_API_KEY, mApiKey).putString(AppConstants.KEY_USERNAME, mEmail).putString(AppConstants.KEY_PACKAGE_NAME, mPackageName).commit();

        serviceStarted = true;

        refreshCurrentFragment(mTabHost.getCurrentTab());
    }

    //This is called when Bluedot Point Service stopped. Your app could clear and release resources 
    @Override
    public void onBlueDotPointServiceStop() {
        if (mProgress != null && mProgress.isShowing())
            mProgress.dismiss();

        serviceStarted = false;

        refreshCurrentFragment(mTabHost.getCurrentTab());
    }

    //This is invoked when Bluedot Point Service got error. You can call isFatal() method to check if the error is fatal. 
    //The Bluedot Point Service will stop itself if the error is fatal, then the onBlueDotPointServiceStop() is called 
    @Override
    public void onBlueDotPointServiceError(final BDError bdError) {
        if (mProgress != null && mProgress.isShowing())
            mProgress.dismiss();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Builder(MainActivity.this).setTitle("Error").setMessage(bdError.getReason()).setPositiveButton("OK", null).create().show();
            }
        });
    }


}
