package org.govhack.jk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.imagineteam.dataobjects.DatasetListObject;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This is a global POJO that we attach data to which we want to use across the
 * application
 * 
 */
public class AppDelegate extends Application {
	public static final String CHECKIN_DATA = "CheckinDataFile";
	private ArrayList<DatasetListObject> datasets;
	private SharedPreferences dataStorage;
	private JSONObject checkinData;
	
	private final int POINT_OLDEST = 34;
	private final int POINT_NEWEST = 71;
	private final int POINT_NORTH  = 1;
	private final int POINT_EAST   = 64;
	private final int POINT_SOUTH  = 25;
	private final int POINT_WEST   = 82;

	public ArrayList<DatasetListObject> getDatasets() {
		return datasets;
	}

	public void setDatasets(ArrayList<DatasetListObject> datasets) {
		this.datasets = datasets;
	}
	
	public void loadData(){
		dataStorage = getSharedPreferences(CHECKIN_DATA, 0);
		String dataString = dataStorage.getString("checkin", "loadTemplate");
		if (dataString.equals("loadTemplate")){
			loadDataTemplate();
			dataString = dataStorage.getString("checkin", "loadTemplate");
		}
		try {
			checkinData = new JSONObject(dataString);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkInAt(int id, GoogleApiClient client){
		boolean alreadyBeen = false;
		try {
			checkinData.put("total", checkinData.getInt("total") + 1);
			JSONArray points = checkinData.getJSONArray("points");
			alreadyBeen = points.getBoolean(id);
			points.put(id, true);
			checkinData.put("points", points);
			saveData();
			handleAchievements(alreadyBeen, id, client);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return alreadyBeen;
	}
	
	private void handleAchievements(boolean alreadyBeen, int pointID, GoogleApiClient apiClient){
		Log.d("ACHIEVEMENTS", "Checking in at point " + pointID + ", " + (alreadyBeen ? " already been " : " first time"));
		if (apiClient != null){
			Log.d("ACHIEVEMENTS", "api client is not null");
		}
		if (!alreadyBeen){
			switch (pointID){
				case POINT_OLDEST:
//					TODO oldest and newest achievements
					break;
				case POINT_NEWEST:
//					Games.Achievements.unlock(apiClient, getString(R.string.achievement_));
					break;
				case POINT_NORTH:
					Games.Achievements.unlock(apiClient, getString(R.string.achievement_north));
					break;
				case POINT_EAST:
					Games.Achievements.unlock(apiClient, getString(R.string.achievement_east));
					break;
				case POINT_SOUTH:
					Games.Achievements.unlock(apiClient, getString(R.string.achievement_south));
					break;
				case POINT_WEST:
					Games.Achievements.unlock(apiClient, getString(R.string.achievement_west));
					break;
			}
		}
		
		Games.Achievements.unlock(apiClient, getString(R.string.achievement_one_down));
		//handle the longer term items
		Games.Achievements.increment(apiClient, getString(R.string.achievement_found_five), 1);
        Games.Achievements.increment(apiClient, getString(R.string.achievement_top_ten), 1);
	}
	
	public int getTotal(){
		try {
			return checkinData.getInt("total");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	private void saveData(){
		SharedPreferences.Editor editor = dataStorage.edit();
		editor.putString("checkin", checkinData.toString());
		editor.commit();
	}
	
	private void loadDataTemplate(){
		SharedPreferences.Editor editor = dataStorage.edit();
		String templateString = "loadTemplate";
		
		StringBuilder buf=new StringBuilder();
	    InputStream json;
		try {
			json = getAssets().open("data/storage.json");
			BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
		    String str;

		    while ((str=in.readLine()) != null) {
		      buf.append(str);
		    }

		    in.close();
		    templateString = buf.toString();
		    editor.putString("checkin", templateString);
			editor.commit();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
}
