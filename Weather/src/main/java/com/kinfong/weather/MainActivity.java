package com.kinfong.weather;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Class to do really cool stuff with the weather.
 * @author Kin
 */
public class MainActivity extends Activity implements FragmentManager.OnBackStackChangedListener {

    public static final String API_KEY = "0692d0f09a1e18c05539495deed088d6";

    private static WeatherData mData;

    boolean mIsBound;

    /**
     * A handler object, used for deferring UI operations.
     */
    private Handler mHandler = new Handler();

    /**
     * Whether or not we're showing the back of the card (otherwise showing the front).
     */
    private static boolean mShowingBack = false;

    private static boolean readyToFlip = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        doBindService();

        if (savedInstanceState == null) {

            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new LoadingScreenFragment())
                    .commit();
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }

        getFragmentManager().addOnBackStackChangedListener(this);
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

                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, new MainFragment())
                .addToBackStack(null)
                .commit();

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

    /**
     * A fragment representing the front of the card.
     */
    public class LoadingScreenFragment extends Fragment {

        public LoadingScreenFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.loading_screen, container, false);
            // build UI
            ImageView logo = (ImageView) rootView.findViewById(R.id.logo);

            // RotateAnimation
            final float ROTATE_FROM = 0.0f;
            final float ROTATE_TO = -1.0f * 360.0f;

            RotateAnimation r; // = new RotateAnimation(ROTATE_FROM, ROTATE_TO);
            r = new RotateAnimation(ROTATE_FROM, ROTATE_TO, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            r.setStartOffset(1000);
            r.setDuration((long) 2500);
            r.setRepeatCount(-1);
            logo.startAnimation(r);

            //Start loading activities
//            getLocationOnce();
//            getLocationFromService();
//            retrieveLocation();

            checkIfReadyToFlip();

            final TextView loadingScreenText = (TextView) rootView.findViewById(R.id.loading_screen_text);

            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("looking for location...");
                }
            }, 10000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("");
                }
            }, 15000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Still looking for location...");
                }
            }, 20000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Having trouble finding your location.");
                }
            }, 26000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Hmm, we might have a problem.\nIs your GPS on?");
                }
            }, 30000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Try walking closer to a window.");
                }
            }, 35000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Sorry, this is embarrassing.");
                }
            }, 38000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Make sure your GPS is on!");
                }
            }, 43000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("I'm still trying, hold on.");
                }
            }, 47000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Well, something bad must have happened.\nYou should reset the app.");
                }
            }, 43000);
            loadingScreenText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadingScreenText.setText("Still here? You should reset the app.");
                }
            }, 60000);

            return rootView;
        }
    }

    private void reset() {
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
        TextView highTemp;
        TextView lowTemp;

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

            mainText.setText(mData.getMinutelySummary());
            temperatureText.setText(mData.getCurrentlyTemperature());
            mainImage.setImageDrawable(findIcon(mData.getMinutelyIcon()));



            final ImageView popupButton = (ImageView) rootView.findViewById(R.id.popup_button);
            popupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater layoutInflater
                            = (LayoutInflater) rootView.getContext()
                            .getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View popupView = layoutInflater.inflate(R.layout.popup, null);
                    final PopupWindow popupWindow = new PopupWindow(
                            popupView,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    hourlyIcon = (ImageView) popupView.findViewById(R.id.hourly_icon);
                    hourlySummary = (TextView) popupView.findViewById(R.id.hourly_summary);
                    dailyIcon = (ImageView) popupView.findViewById(R.id.daily_icon);
                    dailySummary = (TextView) popupView.findViewById(R.id.daily_summary);
                    highTemp = (TextView) popupView.findViewById(R.id.high_temp);
                    lowTemp = (TextView) popupView.findViewById(R.id.low_temp);

                    hourlyIcon.setImageDrawable(findIcon(mData.getHourlyIcon()));
                    hourlySummary.setText(mData.getHourlySummary());
                    dailyIcon.setImageDrawable(findIcon(mData.getDailyIcon()));
                    dailySummary.setText(mData.getDailySummary());
                    highTemp.setText("H: " + mData.getHighTemp());
                    lowTemp.setText("L: " + mData.getLowTemp());

                    ImageButton unPopup = (ImageButton)popupView.findViewById(R.id.un_popup);
                    unPopup.setOnClickListener(new Button.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }});

                    popupWindow.showAtLocation(popupButton, 119, 0, 0);

                }});


            final ImageView refreshButton = (ImageView) rootView.findViewById(R.id.refresh);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    getLocationOnce();
//                    getLocationFromService();
//                    retrieveLocation();
                    readyToFlip = false;
                    flipCard();
                    retrieveLocation(mBoundService.getLocation());
//                    mBoundService.reset();
//                    doUnbindService();
//                    doBindService();
                }
            });

            return rootView;
        }

        /**
         * Returns appropriate icon depending on weather conditions.
         * @param input string describing weather conditions
         * @return Drawable icon that matches weather conditions.
         */
        public Drawable findIcon(String input) {
//            Drawable d;
//            switch(input){
//                case "clear-day":
//                    d = getResources().getDrawable(R.drawable.clear_day);
//                    break;
//                default:
//                    d = getResources().getDrawable(R.drawable.weather_default);
//                    break;
//            }
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

    }


//    /**
//     * Get the location once.
//     */
//    public static void getLocationOnce() {
//        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
//            @Override
//            public void gotLocation(Location location){
//                setLocation(location);
//            }
//        };
//        myLocation = new MyLocation();
//        myLocation.getLocation(context, locationResult);
//    }

//    /**
//     * Sets member variables to appropriate values after location is found.
//     * @param location Location holding desired data
//     */
//    public static void setLocation(Location location) {
//        MainActivity.location = location;
//        latitude = location.getLatitude();
//        longitude = location.getLongitude();
//    }


    private static LocationService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mBoundService = ( (LocationService.LocationBinder) binder).getService();
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT)
                    .show();
        }
        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, LocationService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    public static void retrieveLocation(Location location) {
        new FetchForecastData(location.getLatitude(), location.getLongitude(), API_KEY);
    }

    public static void retrieveForecastData(JSONObject data) {
        extractData(data);
        readyToFlip = true;
    }

    /**
     * Waits for data to be parsed before moving on.
     * @param interval long time to delay
     */
    private void checkIfReadyToFlip(long interval) {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (readyToFlip) {
                    flipCard();
                    h.removeCallbacks(this);
                } else {
                    checkIfReadyToFlip();
                }
            }
        }, interval);
    }
    private void checkIfReadyToFlip() {
        checkIfReadyToFlip(0);
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
            JSONArray dailyDataArray = dailyObject.getJSONArray("data");
            JSONObject dailyDataObject = dailyDataArray.getJSONObject(0);
            mData.setHighTemp(dailyDataObject.getString("temperatureMax"));
            mData.setLowTemp(dailyDataObject.getString("temperatureMin"));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
