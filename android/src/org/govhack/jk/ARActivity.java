package org.govhack.jk;

import java.io.IOException;

import com.wikitude.architect.*;
import com.wikitude.architect.ArchitectView.ArchitectConfig;
import com.wikitude.architect.ArchitectView.SensorAccuracyChangeListener;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class ARActivity extends Activity {
	
	private ArchitectView architectView;
	private LocationListener locationListener;
	protected Location lastKnownLocaton;
	private SensorAccuracyChangeListener sensorAccuracyListener;
	private LocationManager locationProvider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar);
		this.architectView = (ArchitectView)this.findViewById( R.id.architectView );
		final ArchitectConfig config = new ArchitectConfig( "" /* license key */ );
		this.architectView.onCreate( config );
		
		this.locationListener = new LocationListener() {

			@Override
			public void onStatusChanged( String provider, int status, Bundle extras ) {
			}

			@Override
			public void onProviderEnabled( String provider ) {
			}

			@Override
			public void onProviderDisabled( String provider ) {
			}

			@Override
			public void onLocationChanged( final Location location ) {
				if (location!=null) {
					ARActivity.this.lastKnownLocaton = location;
					if ( ARActivity.this.architectView != null ) {
						if ( location.hasAltitude() ) {
							ARActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
						} else {
							ARActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAccuracy() );
						}
					}
				}
			}
		};
		
		this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
		this.locationProvider = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
		locationProvider.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
	}
	
	protected void onPostCreate( final Bundle savedInstanceState ) {
		super.onPostCreate( savedInstanceState );
		
		if ( this.architectView != null ) {
			
			// call mandatory live-cycle method of architectView
			this.architectView.onPostCreate();
			
			try {
				// load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file, have a look at wikitude.com's developer section for API references
				this.architectView.load("wiki/index.html");
				this.architectView.setCullingDistance(10000);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
