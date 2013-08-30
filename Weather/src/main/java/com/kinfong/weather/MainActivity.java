package com.kinfong.weather;

import android.app.Activity;
import android.app.DialogFragment;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Class to do really cool stuff with the weather.
 * @author Kin
 */
public class MainActivity extends Activity {

    public final String TAG = "MainActivity";

    public final String API_KEY = "0692d0f09a1e18c05539495deed088d6";

    private static MyLocation myLocation;
    private static WeatherData mData;

    ImageView mainImage;
    TextView mainText;
    TextView temperatureText;

    private static Location location;
    private static double latitude;
    private static double longitude;
    private static long retrieveLocationDefaultDelay = 5000;
    private static JSONObject forecastObject;
    private static FetchForecastData fetchForecastData;
    private static long getForecastDataDefaultDelay = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildUi();
        getLocationOnce();
        retrieveLocation();
    }

    /**
     * Build UI
     */
    public void buildUi() {
        mainImage = (ImageView) findViewById(R.id.main_image);
        mainText = (TextView) findViewById(R.id.main_text);
        temperatureText = (TextView) findViewById(R.id.temperature);

        Button lookAhead = (Button) findViewById(R.id.look_ahead);
        lookAhead.getBackground().setColorFilter(0x60606060, PorterDuff.Mode.MULTIPLY);
        lookAhead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Fragment fragment = (Fragment) getFragmentManager().findFragmentById(R.layout.popup);
//                Bundle args = new Bundle();
//                fragment.setArguments(args);
            }
        });
    }

    /**
     * Get the location once.
     */
    public void getLocationOnce() {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location){
                setLocation(location);
            }
        };
        myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);
    }

    /**
     * Waits for location data to be received at some specified interval
     * By Mr. Victor (ude@learnovatelabs.com)
     */
    private void retrieveLocation(long interval) {
        final Handler h = new Handler();
        final Location location = this.location;
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (location != null) {
                    // start next step (start async task)
                    fetchForecastData = new FetchForecastData(latitude, longitude, API_KEY);
                    retrieveForecastData();
                    h.removeCallbacks(this);
                } else {
                    retrieveLocation();
                }
            }
        }, interval); /* todo:simulate a slow network */
    }
    private void retrieveLocation() {
        retrieveLocation(retrieveLocationDefaultDelay);
    }

    /**
     * Sets member variables to appropriate values after location is found.
     * @param location Location holding desired data
     */
    public void setLocation(Location location) {
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    /**
     * Waits for data to be parsed before moving on.
     * @param interval long time to delay
     */
    private void retrieveForecastData(long interval) {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fetchForecastData.getData() != null) {
                    forecastObject = fetchForecastData.getData();
                    setup();
                    h.removeCallbacks(this);
                } else {
                    retrieveForecastData();
                }
            }
        }, interval); /* todo:simulate a slow network */
    }
    private void retrieveForecastData() {
        retrieveForecastData(getForecastDataDefaultDelay);
    }

    /**
     * Extracts data in preparation for changing the UI
     */
    public void setup() {
        // extract important data
        extractData(forecastObject);
        // update UI
        updateUi();
    }

    /**
     * Pulls relevant info from JSONObject
     * @param jRoot JSONObject from Forecast API
     */
    public void extractData(JSONObject jRoot) {
        mData = new WeatherData();
        try{
            JSONObject currentlyObject = jRoot.getJSONObject("currently");
            mData.setCurrentlyTime(currentlyObject.getString("time"));
            mData.setCurrentlySummary(currentlyObject.getString("summary"));
            mData.setCurrentlyIcon(currentlyObject.getString("icon"));
            mData.setCurrentlyTemperature(currentlyObject.getString("temperature"));
            JSONObject minutelyObject = jRoot.getJSONObject("minutely");
            mData.setMinutelySummary(minutelyObject.getString("summary"));
            mData.setMinutelyIcon(minutelyObject.getString("icon"));
            JSONObject hourlyObject = jRoot.getJSONObject("hourly");
            mData.setHourlySummary(hourlyObject.getString("summary"));
            mData.setHourlyIcon(hourlyObject.getString("icon"));
            JSONObject dailyObject = jRoot.getJSONObject("daily");
            mData.setDailySummary(dailyObject.getString("summary"));
            mData.setDailyIcon(dailyObject.getString("icon"));
        }catch (Exception e) {
        }
    }

    /**
     * Final step in updating UI.
     */
    private void updateUi() {
        mainText.setText(mData.getMinutelySummary());
        // get whole integer for temperature
        String currentTemp = mData.getCurrentlyTemperature();
        String roundedDouble = "";
        roundedDouble = currentTemp.substring(0, currentTemp.indexOf('.'));
        temperatureText.setText(roundedDouble + "\u00B0");
        mainImage.setImageDrawable(findIcon(mData.getMinutelyIcon()));
    }

    /**
     * Makes sure that the timerTask from MyLocation stops, preventing crashes.
     * Also quits the app when paused.
     */
    protected void onPause() {
//        myLocation.cancelTimer();
        finish();
    }

    /**
     * Returns appropriate icon depending on weather conditions.
     * @param input
     * @return Drawable icon that matches weather conditions.
     */
    public Drawable findIcon(String input) {
        Drawable d;
        if(input.equals("clear-day")) {
            d = getResources().getDrawable(R.drawable.clear_day);
        }else if(input.equals("clear-night")) {
            d = getResources().getDrawable(R.drawable.clear_night);
        }else if(input.equals("rain")) {
            d = getResources().getDrawable(R.drawable.rain);
        }else if(input.equals("snow")) {
            d = getResources().getDrawable(R.drawable.snow);
        }else if(input.equals("sleet")) {
            d = getResources().getDrawable(R.drawable.sleet);
        }else if(input.equals("wind")) {
            d = getResources().getDrawable(R.drawable.wind);
        }else if(input.equals("fog")) {
            d = getResources().getDrawable(R.drawable.fog);
        }else if(input.equals("cloudy")) {
            d = getResources().getDrawable(R.drawable.cloudy);
        }else if(input.equals("partly-cloudy-day")) {
            d = getResources().getDrawable(R.drawable.partly_cloudy_day);
        }else if(input.equals("partly-cloudy-night")) {
            d = getResources().getDrawable(R.drawable.partly_cloudy_night);
        }else {
            d = getResources().getDrawable(R.drawable.weather_default);
        }
        return d;
    }

    /**
     * Fragment class to display future weather info
     */
    public class LookAheadFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Remove border from popup window
            setStyle(DialogFragment.STYLE_NO_FRAME, getTheme());

            // Inflate the layout for this fragment
            View rootView = inflater.inflate(R.layout.popup, container, false);
            // Set up ui elements
            TextView hourlySummary = (TextView) rootView.findViewById(R.id.hourly_summary);
            hourlySummary.setText(mData.getHourlySummary());
            ImageView hourlyIcon = (ImageView) rootView.findViewById(R.id.hourly_icon);
            hourlyIcon.setImageDrawable(findIcon(rootView.findViewById(R.id.hourly_icon).toString()));
            TextView dailySummary = (TextView) rootView.findViewById(R.id.daily_summary);
            dailySummary.setText(mData.getHourlySummary());
            ImageView dailyIcon = (ImageView) rootView.findViewById(R.id.daily_icon);
            dailyIcon.setImageDrawable(findIcon(rootView.findViewById(R.id.daily_icon).toString()));
            return rootView;
        }
    }
}
