package com.bluedot.pointapp;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import au.com.bluedot.application.model.geo.Fence;
import au.com.bluedot.model.geo.BoundingBox;
import au.com.bluedot.model.geo.Circle;
import au.com.bluedot.model.geo.Point;
import au.com.bluedot.model.geo.Polygon;
import au.com.bluedot.point.LocationListener;
import au.com.bluedot.point.ZoneInfo;
import com.bluedotinnovation.android.pointapp.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

public class PointMapFragment extends SupportMapFragment implements
		LocationListener {

	private GoogleMap mMap;
	private MainActivity mActivity;
	private Location mLocation;
	private static boolean mIsInBackbround = true;
	private com.google.android.gms.maps.model.Circle mCircle;
	private com.google.android.gms.maps.model.Circle mCurrentPosition;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getActivity() == null) {
			mActivity = (MainActivity) activity;
		} else {
			mActivity = (MainActivity) getActivity();
		}

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mIsInBackbround = false;
		mMap = getMap();
		if (mMap != null) {
			mMap.setBuildingsEnabled(true);
			CameraPosition cameraPosition = CameraPosition.fromLatLngZoom(
					getLastKnownPosition(), 18);
			mMap.moveCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			
			//loadCurrentLocation();
		}
	}

	/**
	 * Put the fence on the map
	 */
	private void displayFenceOnMap(Fence fence) {
		int color = 0x55880000;
		MarkerOptions markerOptions = new MarkerOptions();
		if (fence.getGeometry() instanceof Circle) {
			Circle circle = (Circle) fence.getGeometry();
			LatLng latLong = new LatLng(circle.getCentroid().getLatitude(),
					circle.getCentroid().getLongitude());
			CircleOptions circleOptions = new CircleOptions().center(latLong)
					.radius(circle.getRadius()).fillColor(color).strokeWidth(2)
					.strokeColor(0x88888888);
			mMap.addCircle(circleOptions);
			markerOptions = new MarkerOptions()
					.position(
							new LatLng(circle.getCentroid().getLatitude(),
									circle.getCentroid().getLongitude()))
					.title("Fence : " + fence.getName())
					.snippet("FenceID : " + fence.getID())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

		} else if (fence.getGeometry() instanceof BoundingBox) {
			BoundingBox bbox = (BoundingBox) fence.getGeometry();
			PolygonOptions polygon = new PolygonOptions()
					.add(new LatLng(bbox.getNorth(), bbox.getEast()))
					.add(new LatLng(bbox.getNorth(), bbox.getWest()))
					.add(new LatLng(bbox.getSouth(), bbox.getWest()))
					.add(new LatLng(bbox.getSouth(), bbox.getEast()))
					.fillColor(color).strokeWidth(2).strokeColor(0x88888888);
			mMap.addPolygon(polygon);
			markerOptions = new MarkerOptions()
					.position(
							new LatLng(bbox.getCentroid().getLatitude(), bbox
									.getCentroid().getLongitude()))
					.title("Fence : " + fence.getName())
					.snippet("FenceID : " + fence.getID())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
		} else if (fence.getGeometry() instanceof Polygon) {

			Polygon truePolygon = (Polygon) fence.getGeometry();
			List<Point> points = truePolygon.getVertices();
			PolygonOptions truePolygonOptions = new PolygonOptions()
					.fillColor(color).strokeWidth(2).strokeColor(0x88888888);
			for (Point p : points) {
				truePolygonOptions.add(new LatLng(p.getLatitude(), p
						.getLongitude()));
			}
			mMap.addPolygon(truePolygonOptions);
			BoundingBox trueBoundingBox = truePolygon.getBoundingBox();
			markerOptions = new MarkerOptions()
					.position(
							new LatLng(trueBoundingBox.getCentroid()
									.getLatitude(), trueBoundingBox
									.getCentroid().getLongitude()))
					.title("Fence Name : " + fence.getName())
					.snippet("FenceID : " + fence.getID())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
		}
		mMap.addMarker(markerOptions);

	}

	private void loadDetails(ArrayList<ZoneInfo> zonesInfo) {
		mMap.clear();
		mCircle = null;
		for (ZoneInfo zoneInfo : zonesInfo) {
			for (Fence fence : zoneInfo.getFences()) {
				if (mMap != null) {
					displayFenceOnMap(fence);
				}
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		mLocation = location;
		if (!mIsInBackbround) {
			loadCurrentLocation();
		}
	}

	private void loadCurrentLocation() {
		LatLng latLong = new LatLng(mLocation.getLatitude(),
				mLocation.getLongitude());

		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLong);
		mMap.animateCamera(cameraUpdate);
		CircleOptions circleOptions = new CircleOptions();
		//if (mLocation.hasAccuracy()) {
			circleOptions.center(latLong).radius(mLocation.getAccuracy());
			circleOptions.fillColor(0x88769cc7);
			circleOptions.strokeWidth(2);
			circleOptions.strokeColor(0x770000);

			if (mCircle == null) {
				mCircle = mMap.addCircle(circleOptions);
				mCurrentPosition = mMap.addCircle(new CircleOptions()
						.center(latLong).radius(1).fillColor(0x000000));
			} else {
				mCircle.setRadius(mLocation.getAccuracy());
				mCircle.setCenter(latLong);
				mCurrentPosition.setCenter(latLong);
			}
		//}
	}

	@Override
	public void onError(String message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsInBackbround = true;
		mActivity.unsubscribeLocationUpdates(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		loadDetails(mActivity.getZones());
		mActivity.subscribeForLocationUpdates(this);
		mIsInBackbround = false;
	}

	public LatLng getLastKnownPosition() {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) mActivity
				.getSystemService(Context.LOCATION_SERVICE);
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		// Or use LocationManager.GPS_PROVIDER

		Location lastKnownLocation = locationManager
				.getLastKnownLocation(locationProvider);
		if (lastKnownLocation != null) {
			mLocation = lastKnownLocation;
			return new LatLng(lastKnownLocation.getLatitude(),
					lastKnownLocation.getLongitude());
		}
		LatLng lt = new LatLng(-37.818049, 144.9795319);
		//mLocation = new Location();
		return lt;
	}

	public void refresh() {
		loadDetails(mActivity.getZones());
	}
}
