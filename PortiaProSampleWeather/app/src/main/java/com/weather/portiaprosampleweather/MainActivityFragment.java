package com.weather.portiaprosampleweather;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private final String CLASS_NAME = "Main activity Fragment";

    private TextView cityField;
    private TextView updatedField;
    private TextView detailsField;
    private TextView currentTemperatureField;
    private TextView weatherIcon;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        cityField = (TextView) rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Calling the UpdateWeather AsyncTask to get the latest weather condition
        UpdateWeatherData updateWeatherData = new UpdateWeatherData(new CityPreference(getActivity()).getCity());
        updateWeatherData.execute();
    }


    /**
     * Once city is been selected we update the weather by sending a new request
     * @param city
     */
    public void changeCity(String city){
        UpdateWeatherData updateWeatherData = new UpdateWeatherData(city);
        updateWeatherData.execute();
    }

    /**
     * AsyncTask for requesting the weather condition of a particular city
     * In the constructor the city should be provided.
     */
    public class UpdateWeatherData extends AsyncTask<String, Void, JSONObject> {

        private static final String OPEN_WEATHER_MAP_API =
                "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
        private String city;
        UpdateWeatherData(String city) {
            this.city = city;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                URL url = new URL(String.format(OPEN_WEATHER_MAP_API, city));
                HttpURLConnection connection =
                        (HttpURLConnection) url.openConnection();

                connection.addRequestProperty("x-api-key",
                        getActivity().getString(R.string.open_weather_maps_app_id));

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                StringBuffer json = new StringBuffer(1024);
                String tmp = "";
                while ((tmp = reader.readLine()) != null)
                    json.append(tmp).append("\n");
                reader.close();

                JSONObject jsonObject = new JSONObject(json.toString());

                // If we are not receiving a 200 OK then return null
                if (jsonObject.getInt("cod") != 200) {
                    return null;
                }

                return jsonObject;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if (json != null) {
                try {
                    // Parsing the Json return by the weather map and feeding all textfields
                    cityField.setText(json.getString("name").toUpperCase(Locale.CANADA) +
                            ", " + json.getJSONObject("sys").getString("country"));

                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    detailsField.setText(
                            details.getString("description").toUpperCase(Locale.CANADA) +
                                    "\n" + "Humidity: " + main.getString("humidity") + "%" +
                                    "\n" + "Pressure: " + main.getString("pressure") + " hPa");

                    currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp"))+ " C");

                    DateFormat df = DateFormat.getDateTimeInstance();
                    String updatedOn = df.format(new Date(json.getLong("dt")*1000));
                    updatedField.setText("Last update: " + updatedOn);

                }catch(Exception e){
                    Log.e("SimpleWeather", "One or more fields not found in the JSON data");
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Service not available at the moment.", Toast.LENGTH_SHORT).show();
            }
            Log.d(CLASS_NAME, "Error handling the request");
        }
    }
}
