package com.bluedot.pointapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import au.com.bluedot.point.BDError;
import au.com.bluedot.point.ServiceStatusListener;

import au.com.bluedot.point.net.engine.ServiceManager;

import com.bluedotinnovation.android.pointapp.R;

public class AuthenticationFragment extends Fragment implements OnClickListener, ServiceStatusListener{

	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	private static final String ARG_PACKAGE_NAME = "package_name";
	private static final String ARG_API_KEY = "api_key";
	private static final String ARG_EMAIL = "email_id";

	private String mPackageName;
	private String mApiKey;
	private String mEmail;
	private MainActivity mActivity;
	private Button mBtnAuthenticate;

	private Handler handler = new Handler();

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static Bundle setLoginDetails(String packageName, String apiKey,
			String emailId) {
		Bundle args = new Bundle();
		args.putString(ARG_API_KEY, apiKey);
		args.putString(ARG_EMAIL, emailId);
		args.putString(ARG_PACKAGE_NAME, packageName);
		return args;
	}

	EditText mEdtEmail;
	EditText mEdtApiKey;
	EditText mEdtPackageName;
	private boolean mIsAuthenticated;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_authenticate,
				container, false);
		mEdtEmail = (EditText) rootView.findViewById(R.id.edt_email);
		mEdtApiKey = (EditText) rootView.findViewById(R.id.edt_api_key);
		mEdtPackageName = (EditText) rootView
				.findViewById(R.id.edt_package_name);

		mEmail = getArguments().getString(ARG_EMAIL);
		mApiKey = getArguments().getString(ARG_API_KEY);
		mPackageName = getArguments().getString(ARG_PACKAGE_NAME);

		mEdtEmail.setText(mEmail);
		mEdtApiKey.setText(mApiKey);
		mEdtPackageName.setText(mPackageName);
		mBtnAuthenticate = (Button) rootView
				.findViewById(R.id.btn_authenticate);

		mBtnAuthenticate.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		//Checking the Bluedot Point Service status using isBlueDotPointServiceRunning in the ServiceManager by passing a ServiceStatusListener
        ServiceManager.getInstance(getActivity()).isBlueDotPointServiceRunning(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getActivity() == null) {
			mActivity = (MainActivity) activity;
		} else {
			mActivity = (MainActivity) getActivity();
		}
	}

	public void refresh() {
        
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ServiceManager.getInstance(getActivity()).isBlueDotPointServiceRunning(AuthenticationFragment.this);
            }
        },500);

	}

	public void updateLoginDetails(String packageName, String apiKey,
			String emailId) {
		setLoginDetails(packageName, apiKey, emailId);
		mEmail = emailId;
		mApiKey = apiKey;
		mPackageName = packageName;
		if (mEdtEmail != null && mEdtApiKey != null && mEdtPackageName != null) {
			mEdtEmail.setText(mEmail);
			mEdtApiKey.setText(mApiKey);
			mEdtPackageName.setText(mPackageName);
		}
	}

	@Override
	public void onClick(View v) {
		if (mIsAuthenticated) {
			mActivity.stopService();
            mIsAuthenticated = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnAuthenticate.setText(getString(R.string.save_authenticate));
                }
            });
		} else {
			if (TextUtils.isEmpty(mEdtEmail.getText())
					|| TextUtils.isEmpty(mEdtApiKey.getText())
					|| TextUtils.isEmpty(mEdtPackageName.getText())) {
				new AlertDialog.Builder(getActivity()).setTitle("Error")
						.setMessage("Please enter login details.")
						.setPositiveButton("OK", null).create().show();
			} else{
                mActivity.startAuthentication(mEdtEmail.getText().toString(),
                        mEdtApiKey.getText().toString(), mEdtPackageName
                                .getText().toString());
                mIsAuthenticated = true;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBtnAuthenticate.setText(getString(R.string.clear_logout));
                    }
                });
            }

		}

	}

	//Update the button status when the Bluedot Point Service status callback is invoked
    @Override
    public void onBlueDotPointServiceStartedSuccess() {
        mIsAuthenticated = true;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnAuthenticate.setText(getString(R.string.clear_logout));
            }
        });

    }

    @Override
    public void onBlueDotPointServiceStop() {
        mIsAuthenticated = false;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBtnAuthenticate.setText(getString(R.string.save_authenticate));
            }
        });
    }

    @Override
    public void onBlueDotPointServiceError(BDError bdError) {
        if(bdError.isFatal()){
            mIsAuthenticated = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnAuthenticate.setText(getString(R.string.save_authenticate));
                }
            });
        }
    }

    
    @Override
    public void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance(getActivity()).removeBlueDotPointServiceStatusListener(this);
    }
}
