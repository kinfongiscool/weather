package com.kinfong.weather;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Class to do really cool stuff with the weather.
 * @author Kin
 */
public class MainActivity extends Activity implements FragmentManager.OnBackStackChangedListener {

    public static final String TAG = "MainActivity";

    public static final String API_KEY = "0692d0f09a1e18c05539495deed088d6";

    private static MyLocation myLocation;
    private static WeatherData mData;

    private static Context context = MainActivity.context;

    private static Location location;
    private static double latitude;
    private static double longitude;
    private static long retrieveLocationDefaultDelay = 5000;
    private static JSONObject forecastObject;
    private static FetchForecastData fetchForecastData;
    private static long getForecastDataDefaultDelay = 1000;


    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private boolean mShowingBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start finding location and getting weather data
        getLocationOnce();
        retrieveLocation();


        if (savedInstanceState == null) {
            // If there is no saved instance state, add a fragment representing the
            // front of the card to this activity. If there is saved instance state,
            // this fragment will have already been added to the activity.
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LoadingScreenFragment())
                    .commit();
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }

        // Monitor back stack changes to ensure the action bar shows the appropriate
        // button (either "photo" or "info").
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;

            case R.id.action_flip:
                flipCard();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Flip to the back.

        mShowingBack = true;

        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        getFragmentManager()
                .beginTransaction()

                        // Replace the default fragment animations with animator resources representing
                        // rotations when switching to the back of the card, as well as animator
                        // resources representing rotations when flipping back to the front (e.g. when
                        // the system Back button is pressed).
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)

                        // Replace any fragments currently in the container view with a fragment
                        // representing the next page (indicated by the just-incremented currentPage
                        // variable).
                .replace(R.id.container, new MainFragment())

                        // Add this transaction to the back stack, allowing users to press Back
                        // to get to the front of the card.
                .addToBackStack(null)

                        // Commit the transaction.
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);

        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }




//    public void buildLoadingScreen() {
//        ImageView logo = (ImageView) findViewById(R.id.logo);
//        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_spin);
//        logo.startAnimation(hyperspaceJumpAnimation);
//    }



    /**
     * A fragment representing the front of the card.
     */
    public static class LoadingScreenFragment extends Fragment {
        public LoadingScreenFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.loading_screen, container, false);
            // build UI
            ImageView logo = (ImageView) rootView.findViewById(R.id.logo);
            Animation logoSpin = AnimationUtils.loadAnimation(getActivity(), R.anim.logo_spin);
            logo.startAnimation(logoSpin);

            //Start loading activities
            getLocationOnce();
            retrieveLocation();



            return rootView;
        }
    }


    /**
     * A fragment representing the back of the card (MainActivity).
     */
    public class MainFragment extends Fragment {

        ImageView mainImage;
        TextView mainText;
        TextView temperatureText;
        ImageView hourlyIcon;
        TextView hourlySummary;
        ImageView dailyIcon;
        TextView dailySummary;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.activity_main, container, false);
            mainImage = (ImageView) rootView.findViewById(R.id.main_image);
            mainText = (TextView) rootView.findViewById(R.id.main_text);
            temperatureText = (TextView) rootView.findViewById(R.id.temperature);

            hourlyIcon = (ImageView) rootView.findViewById(R.id.hourly_icon);
            hourlySummary = (TextView) rootView.findViewById(R.id.hourly_summary);
            dailyIcon = (ImageView) rootView.findViewById(R.id.daily_icon);
            dailySummary = (TextView) rootView.findViewById(R.id.daily_summary);

            final ImageView popupButton = (ImageView) rootView.findViewById(R.id.popup_button);
            popupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater layoutInflater
                            = (LayoutInflater) rootView.getContext()
                            .getSystemService(LAYOUT_INFLATER_SERVICE);
                    View popupView = layoutInflater.inflate(R.layout.popup, null);
                    final PopupWindow popupWindow = new PopupWindow(
                            popupView,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);


                    hourlyIcon = (ImageView) popupView.findViewById(R.id.hourly_icon);
                    hourlySummary = (TextView) popupView.findViewById(R.id.hourly_summary);
                    dailyIcon = (ImageView) popupView.findViewById(R.id.daily_icon);
                    dailySummary = (TextView) popupView.findViewById(R.id.daily_summary);

                    hourlyIcon.setImageDrawable(findIcon(mData.getHourlyIcon()));
                    hourlySummary.setText(mData.getHourlySummary());
                    dailyIcon.setImageDrawable(findIcon(mData.getDailyIcon()));
                    dailySummary.setText(mData.getDailySummary());
//                updatePopup();

                    ImageButton unPopup = (ImageButton)popupView.findViewById(R.id.un_popup);
                    unPopup.setOnClickListener(new Button.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            popupWindow.dismiss();
                        }});

                    popupWindow.showAtLocation(popupButton, 80, 0, 0);

                }});


            final ImageView refreshButton = (ImageView) rootView.findViewById(R.id.refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getLocationOnce();
                    retrieveLocation();;
                }
            });

            return rootView;
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
    }

    /**
     * Get the location once.
     */
    public static void getLocationOnce() {
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            public void gotLocation(Location location){
                setLocation(location);
            }
        };
        myLocation = new MyLocation();
        myLocation.getLocation(MainActivity.context, locationResult);
    }

    /**
     * Waits for location data to be received at some specified interval
     * By Mr. Victor (ude@learnovatelabs.com)
     */
    private static void retrieveLocation(long interval) {
        final Handler h = new Handler();
        final Location location = MainActivity.location;
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
    private static void retrieveLocation() {
        retrieveLocation(retrieveLocationDefaultDelay);
    }

    /**
     * Sets member variables to appropriate values after location is found.
     * @param location Location holding desired data
     */
    public static void setLocation(Location location) {
        this.location = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    /**
     * Waits for data to be parsed before moving on.
     * @param interval long time to delay
     */
    private static void retrieveForecastData(long interval) {
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
    private static void retrieveForecastData() {
        retrieveForecastData(getForecastDataDefaultDelay);
    }

    /**
     * Extracts data in preparation for changing the UI
     */
    public static void setup() {
        // extract important data
        extractData(forecastObject);
        // update UI
        updateUi();
    }

    /**
     * Pulls relevant info from JSONObject
     * @param jRoot JSONObject from Forecast API
     */
    public static void extractData(JSONObject jRoot) {
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



//    private void updatePopup() {
//        hourlyIcon.setImageDrawable(findIcon(mData.getHourlyIcon()));
//        hourlySummary.setText(mData.getHourlySummary());
//        dailyIcon.setImageDrawable(findIcon(mData.getDailyIcon()));
//        dailySummary.setText(mData.getDailySummary());
//    }

//    /**
//     * Makes sure that the timerTask from MyLocation stops, preventing crashes.
//     * Also quits the app when paused.
//     */
//    protected void onPause() {
//        myLocation.cancelTimer();
//    }



//    /**
//     * Fragment class to display future weather info
//     */
//    public class LookAheadFragment extends DialogFragment {
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            // Remove border from popup window
//            setStyle(DialogFragment.STYLE_NO_FRAME, getTheme());
//
//            // Inflate the layout for this fragment
//            View rootView = inflater.inflate(R.layout.popup, container, false);
//            // Set up ui elements
//            TextView hourlySummary = (TextView) rootView.findViewById(R.id.hourly_summary);
//            hourlySummary.setText(mData.getHourlySummary());
//            ImageView hourlyIcon = (ImageView) rootView.findViewById(R.id.hourly_icon);
//            hourlyIcon.setImageDrawable(findIcon(rootView.findViewById(R.id.hourly_icon).toString()));
//            TextView dailySummary = (TextView) rootView.findViewById(R.id.daily_summary);
//            dailySummary.setText(mData.getHourlySummary());
//            ImageView dailyIcon = (ImageView) rootView.findViewById(R.id.daily_icon);
//            dailyIcon.setImageDrawable(findIcon(rootView.findViewById(R.id.daily_icon).toString()));
//            return rootView;
//        }
//    }
}
