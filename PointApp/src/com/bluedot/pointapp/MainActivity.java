package com.bluedot.pointapp;

import java.util.ArrayList;
import java.util.Date;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import au.com.bluedot.application.model.geo.Fence;
import au.com.bluedot.point.ApplicationNotification;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.AuthenticationStatusListener;
import au.com.bluedot.point.LocationListener;
import au.com.bluedot.point.LocationServicesNotEnabledException;
import au.com.bluedot.point.ServiceNotReadyException;
import au.com.bluedot.point.ServiceStatus;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.Speed;
import au.com.bluedot.point.SpeedListener;
import au.com.bluedot.point.UnknownArgumentException;
import au.com.bluedot.point.ZoneInfo;
import au.com.bluedot.point.net.engine.ApplicationSharedPreferencesHelper;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.telegraph.process.notation.JSONSimpleNotationStructureFactory;
import com.bluedotinnovation.android.pointapp.R;

public class MainActivity extends FragmentActivity implements
		ServiceStatusListener, AuthenticationStatusListener, SpeedListener,
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

	private static ArrayList<ZoneInfo> mZonesInfo = new ArrayList<ZoneInfo>();
	JSONSimpleNotationStructureFactory androidTemp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(),
				android.R.id.tabcontent);

		if (ApplicationSharedPreferencesHelper.getInstance(this) != null) {
			mApiKey = ApplicationSharedPreferencesHelper.getInstance(this)
					.getApiKey();
			mPackageName = ApplicationSharedPreferencesHelper.getInstance(this)
					.getPackageName();
			mEmail = ApplicationSharedPreferencesHelper.getInstance(this)
					.getEmail();
		}

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

		mServiceManager = ServiceManager.getInstance(this);
		mServiceManager.setNumberOfCheckInsToPersist(50);
		mServiceManager.setNotificationIDResourceID(R.drawable.ic_launcher);
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
		// custom dialog
		Builder dialog = new AlertDialog.Builder(this)
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
								restartService();
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
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// Do nothing
							}
						}).setIcon(android.R.drawable.ic_dialog_alert);
		dialog.show();
	}

	private void restartService() {
		if (mServiceManager != null) {
			if (ServiceManager.isPointServiceRunning(MainActivity.this)) {
				try {
					mServiceManager.stopService();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mApiKey = apiKey;
			mPackageName = packageName;
			mEmail = email;

			startAuthentication(mEmail, mApiKey, mPackageName);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			stopService();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void stopService() {
		if (mServiceManager != null) {
			mServiceManager.stopService();
			if (mTabHost != null) {
				refreshCurrentFragment(mTabHost.getCurrentTab());
			}
			mZonesInfo.clear();
			
		}
	}

	@Override
	public void onApplicationNotificationReceived(
			ApplicationNotification applicationNotification) {
		
		if (applicationNotification != null
				&& applicationNotification.getFence() != null) {
			Toast.makeText(
					this,
					"Application Notification Received !! You have Entered : "
							+ applicationNotification.getFence().getName(),
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	public void onSpeedUpdate(Speed speed) {
		

	}

	@Override
	public void onAuthenticationFailed(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		if (mProgress.isShowing()) {
			mProgress.dismiss();
		}
		Log.i("MainActivity", "HandleMessage : onAuthenticationFailed : "
				+ message);
		mServiceManager.stopService();
	}

	@Override
	public void onAuthenticationSuccessful(String message) {
		mProgress.setMessage(getString(R.string.please_wait_download));
	}

	@Override
	public void onAuthenticationError(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		if (mProgress.isShowing()) {
			mProgress.dismiss();
		}
		Log.i("MainActivity", "HandleMessage : onAuthenticationError : "
				+ message);
		
	}

	@Override
	public void onServiceLaunchStatusReceived(ServiceStatus status) {
		if(status == ServiceStatus.READY){
			authenticate();
		}
	}

	@Override
	public void onServiceStatusChanged(ServiceStatus oldStatus,
			ServiceStatus updatedStatus) {

		Log.i("MainActivity",
				"onServiceStatusChanged() called" + oldStatus.toString()
						+ " ::: " + updatedStatus.toString());
	}

	public ArrayList<ZoneInfo> getZones() {
		return mZonesInfo;
	}

	/**
	 * Start rule based action tests
	 */
	private void startRuleBasedActionTest() {
		mServiceManager.subscribeForApplicationNotification(this);
	}

	public void subscribeForLocationUpdates(LocationListener listener) {
		mServiceManager.subscribeForLocationUpdates(listener);
	}

	@Override
	public void onUpdateZonesFailed(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		if (mProgress.isShowing()) {
			mProgress.dismiss();
		}
		Log.i("MainActivity", "HandleMessage : onUpdateZonesFailed : "
				+ message);
	}

	// Authenticate the Application
	/**
	 * 
	 */
	private void authenticate() {
		try {
			if (!TextUtils.isEmpty(mApiKey) && !TextUtils.isEmpty(mPackageName)
					&& !TextUtils.isEmpty(mEmail)) {
				mServiceManager.sendAuthenticationRequest(mPackageName,
						mApiKey, mEmail, this);
			} else {
				if (mProgress.isShowing()) {
					mProgress.dismiss();
				}
				mServiceManager.stopService();
				Toast.makeText(this, "Please enter Login Details",
						Toast.LENGTH_LONG).show();
			}
		} catch (ServiceNotReadyException e) {
			e.printStackTrace();
		}
	}

	public void startAuthentication(String email, String apiKey,
			String packageName) {
		mEmail = email;
		mApiKey = apiKey;
		mPackageName = packageName;
		try {
			mServiceManager.start(this);
			mProgress
					.setMessage(getString(R.string.please_wait_authenticating));
			mProgress.show();
		} catch (UnknownArgumentException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		} catch (LocationServicesNotEnabledException e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCheckIntoFence(String zoneId, String zoneName, Date date,
			Fence fence) {
		if (mTabHost != null) {
			refreshCurrentFragment(mTabHost.getCurrentTab());
		}
	}

	@Override
	public void onUpdateZoneInfo(ArrayList<ZoneInfo> zonesInfo) {
		startRuleBasedActionTest();
		mZonesInfo = zonesInfo;
		if (mProgress != null && mProgress.isShowing()) {
			mProgress.dismiss();
			if (mTabHost != null) {
				refreshCurrentFragment(mTabHost.getCurrentTab());
			}
		}
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
		if(quit&&mServiceManager!=null){
			mServiceManager.stopService();
		}
	}
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("Quit").setMessage("Do you want to quit the app or put it in the background").setPositiveButton("QUIT", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				quit = true;
				finish();
			}
		}).setNegativeButton("NO", null).setNeutralButton("Put in Background", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				
			}
		}).create().show();
	}
	
	public void unsubscribeLocationUpdates(LocationListener listener){
		mServiceManager.unregisterLocationListener(listener);
	}
}
