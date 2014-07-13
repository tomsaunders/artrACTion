	package org.govhack.jk;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


public class SplashScreen extends BaseGameActivity implements PlayListener, OnClickListener {
	private static final String TAG = "SPLASHSCREEN";
	Boolean mShowSignIn = true;
	
	// request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_splash_screen);
		findViewById(R.id.sign_in_button).setOnClickListener(this);
		findViewById(R.id.sign_out_button).setOnClickListener(this);
		findViewById(R.id.view_map_button).setOnClickListener(this);
		//home = new Intent(SplashScreen.this, MapActivity.class);
		mShowSignIn = !isSignedIn();
		updateUI();
		
		AppDelegate ad = (AppDelegate)getApplication();
	}
	
	private void showMap(){
		Intent map = new Intent(getApplicationContext(), MapActivity.class);
		startActivity(map);
	}
	
	private void updateUI(){
		findViewById(R.id.sign_in_bar).setVisibility(mShowSignIn ?
                View.VISIBLE : View.GONE);
        findViewById(R.id.sign_out_bar).setVisibility(mShowSignIn ?
                View.GONE : View.VISIBLE);
	}

	@Override
	public void onSignInFailed() {
		// Sign-in failed, so show sign-in button on main menu
       // mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
       // mMainMenuFragment.setShowSignInButton(true);
       // mWinFragment.setShowSignInButton(true);
		mShowSignIn = true;
		updateUI();
	}

	@Override
	public void onSignInSucceeded() {
        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(getApiClient());
        String displayName;
        if (p == null) {
            Log.w(TAG, "mGamesClient.getCurrentPlayer() is NULL!");
            displayName = "???";
        } else {
            displayName = p.getDisplayName();
            ((TextView)findViewById(R.id.welcomeMSG)).setText("Welcome, " + displayName);
        }
        /*
        // if we have accomplishments to push, push them
        if (!mOutbox.isEmpty()) {
            pushAccomplishments();
            Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
                    Toast.LENGTH_LONG).show();
        }
        */
		mShowSignIn = false;
		updateUI();
		Games.Achievements.unlock(getApiClient(), getString(R.string.achievement_first_step));
	}
	
	@Override
    public void onSignInButtonClicked() {
		beginUserInitiatedSignIn();
    }
	
	@Override
    public void onSignOutButtonClicked() {
        signOut();
        mShowSignIn = true;
        updateUI();
        //mMainMenuFragment.setGreeting(getString(R.string.signed_out_greeting));
        //mMainMenuFragment.setShowSignInButton(true);
        //mWinFragment.setShowSignInButton(true);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.sign_in_button:
			this.onSignInButtonClicked();
			break;
		case R.id.sign_out_button:
			this.onSignOutButtonClicked();
			break;
		case R.id.view_map_button:
			this.showMap();
			break;
		}
	}


}