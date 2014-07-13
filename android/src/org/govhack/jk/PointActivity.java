package org.govhack.jk;

import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.example.games.basegameutils.BaseGameActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PointActivity extends BaseGameActivity implements OnClickListener, LocationListener {
	private Typeface mFont;
	private int pointID;
	private String name, date;
	private AppDelegate ad;
	private double lat;
	private double lon;
	private LocationManager locationManager;
	ProgressDialog loading;
	private boolean locationFound = false;
	private CountDownTimer timer;
	
	private final int GPS_TIMEOUT = 5;
	private final int NET_TIMEOUT = 15;
	
	private final int ACCURACY_THRESHOLD = 50;
	private final int CHECKIN_THRESHOLD = 100000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point);
		Bundle extras = getIntent().getExtras();
		String data = extras.getString("json");
		mFont = Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Medium.ttf");
		
		ad = (AppDelegate) getApplication();
		locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
		loading = new ProgressDialog(this);
		findViewById(R.id.pointCheckinButton).setOnClickListener(this);

		displayPoint(data);
	}
	
	private void checkIn(){
		loading.setTitle("Please wait");
		loading.setMessage("Locating...");
		loading.show();
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locateViaGPS();
		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locateViaNetwork();
		} else {
			locationError();
		}
	}
	
	private void locationError(){
		new AlertDialog.Builder(this).setTitle("Sorry!")
		.setMessage("Something went wrong- please try again later")
		.setNegativeButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// do nothing
			}
		}).show();
	}
	
	private void locateViaGPS(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
		timer = new CountDownTimer(GPS_TIMEOUT * 1000L, 1000L) {
			@Override
			public void onTick(long millisUntilFinished) {
				Log.d("Location", "gps tick");
			}
			@Override
			public void onFinish() {
				if (!locationFound){
					locateViaNetwork();		//cant find location via GPS. try network
				} else {
					loading.hide();
				}
			}
		};
		timer.start();
	}
	
	private void locateViaNetwork(){
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
		timer = new CountDownTimer(NET_TIMEOUT * 1000L, 1000L) {
			@Override
			public void onTick(long millisUntilFinished) {
			}
			@Override
			public void onFinish() {
				Log.d("Location", "network Done");
				if (!locationFound){
					locationError();
				} else {
					loading.hide();
				}
			}
		};
		timer.start();

	}
	
	private void displayPoint(String data) {
		try {
			JSONObject json = new JSONObject(data);
			pointID = json.getInt("id");
			lat = json.getDouble("lat");
			lon = json.getDouble("lon");
			
			TextView title = (TextView) findViewById(R.id.pointTitle);
			TextView desc = (TextView) findViewById(R.id.pointDesc);
			TextView artist = (TextView) findViewById(R.id.pointArtist);
			TextView sub = (TextView) findViewById(R.id.pointSuburb);
			TextView medium = (TextView) findViewById(R.id.pointMedium);
			TextView url = (TextView) findViewById(R.id.pointURL);
			
			name = json.getString("Title");
			date = json.getString("Date");
			title.setText(name + " (" + date + ")");
			title.setTypeface(mFont);
			desc.setText(json.getString("Description"));
			desc.setTypeface(mFont);
			artist.setText("Artist: " + json.getString("Artist"));
			artist.setTypeface(mFont);
			sub.setText("Suburb: " + json.getString("Suburb"));
			sub.setTypeface(mFont);
			medium.setText("Medium: " + json.getString("Medium"));
			medium.setTypeface(mFont);
			url.setText("More Info: " + json.getString("URL 1"));
			url.setTypeface(mFont);
			new DownloadImageTask((ImageView)findViewById(R.id.urlImageView)).execute(json.getString("URL 2"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block//
			e.printStackTrace();
		}	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.point, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
		    this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
		    String urldisplay = urls[0];
		    Bitmap bmp = null;
		    try {
		      InputStream in = new java.net.URL(urldisplay).openStream();
		      bmp = BitmapFactory.decodeStream(in);
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		    return bmp;
		}

		protected void onPostExecute(Bitmap result) {
			if(result != null) {
				bmImage.setImageBitmap(result);
				bmImage.setVisibility(View.VISIBLE);
			}
			else {
				bmImage.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.pointCheckinButton:
				checkIn();
				break;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Double iLat = location.getLatitude();
		Double iLon = location.getLongitude();
		Double dist = distanceFromMe(location);
		String log = "onLocationChanged " + iLat + ", " + iLon + " acc " + location.getAccuracy() + " dist " + dist;
		if (location.getAccuracy() < ACCURACY_THRESHOLD){
			loading.hide();
			locationFound = true;
			locationManager.removeUpdates(this);
			if (dist < CHECKIN_THRESHOLD){
				boolean alreadyBeen = ad.checkInAt(pointID, getApiClient());
				if (alreadyBeen){
					Toast toast = Toast.makeText(this, "Checked in at " + name, Toast.LENGTH_LONG);
				}
			} else {
				new AlertDialog.Builder(this).setTitle("Sorry!")
				.setMessage("You're not close enough to check in - " + (int)Math.floor(dist) + "m away. Try moving closer")
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}
				}).show();
			}
		}
		
		Log.d("POINTCHECK", log);
	}
	
	private double distanceFromMe(Location location) {
		
		double lat_a = lat;
		double lat_b = location.getLatitude();
		double lon_a = lon;
		double lon_b = location.getLongitude();
		
		double pk = (double) (180/ Math.PI);

		double a1 = lat_a / pk;
		double a2 = lon_a / pk;
		double b1 = lat_b / pk;
		double b2 = lon_b / pk;

		double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
		double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
		double t3 = Math.sin(a1)*Math.sin(b1);
	    double tt = Math.acos(t1 + t2 + t3);
	   
	    return 6366000*tt;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
}

