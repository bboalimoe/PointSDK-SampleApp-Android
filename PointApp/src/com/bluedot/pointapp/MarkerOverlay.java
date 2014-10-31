package com.bluedot.pointapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.location.Geocoder;
import au.com.bluedot.application.model.geo.Fence;
import com.bluedotinnovation.android.pointapp.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MarkerOverlay extends Overlay {
	Geocoder geoCoder = null;
	private Context mContext;
	private Fence mFence;

	public MarkerOverlay(Context context, Fence fence) {
		super();
		mContext = context;
		mFence = fence;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			Projection projection = mapView.getProjection();
			Point pt = new Point();
			// projection.toPixels(selectedLatitude, pt);

			GeoPoint newGeos = new GeoPoint((int) (mFence.getGeometry()
					.getCentroid().getLatitude() * 1E6), (int) (mFence
					.getGeometry().getCentroid().getLongitude() * 1E6)); // adjust
			// your
			// radius
			// accordingly
			Point pt2 = new Point();
			projection.toPixels(newGeos, pt2);
			float circleRadius = Math.abs(pt2.y - pt.y);

			Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

			circlePaint.setColor(0x30000000);
			circlePaint.setStyle(Style.FILL_AND_STROKE);
			canvas.drawCircle((float) pt.x, (float) pt.y, circleRadius,
					circlePaint);

			circlePaint.setColor(0x99000000);
			circlePaint.setStyle(Style.STROKE);
			canvas.drawCircle((float) pt.x, (float) pt.y, circleRadius,
					circlePaint);

			Bitmap markerBitmap = BitmapFactory.decodeResource(
					mContext.getResources(), R.drawable.pin);
			canvas.drawBitmap(markerBitmap, pt.x,
					pt.y - markerBitmap.getHeight(), null);

			if (mFence != null) {
				canvas.drawText("Hi there", pt.x, pt.y, circlePaint);
			}
			super.draw(canvas, mapView, shadow);
		}
	}

	@Override
	public boolean onTap(GeoPoint geoPoint, MapView mapView) {
		return true;
	}

}
