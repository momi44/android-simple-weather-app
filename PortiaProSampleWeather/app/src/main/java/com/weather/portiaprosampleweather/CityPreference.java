package com.weather.portiaprosampleweather;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by Momeneh on 26/06/2015.
 */
public class CityPreference {

    SharedPreferences prefs;

    public CityPreference(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Ottawa as the default city
    String getCity(){
        return prefs.getString("city", "Ottawa, CA");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }
}
