package com.kinfong.weather;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private static Location mLocation;

    private class LocationListener implements android.location.LocationListener{

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLocation.set(location);
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return mBinder;
    }

    private final IBinder mBinder = new LocationBinder();

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocationBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        if(checkGPSEnabled()) {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {

                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }

        } else {
            Log.e(TAG, "GPS not enabled");
            Intent intent = new Intent("showGpsDialog");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean checkGPSEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

//    /**
//     * Show alert dialog to allow user to enable GPS
//     */
//    protected void showGPSAlert(final Context context) {
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
//                context);
//        alertDialogBuilder
//                .setMessage(
//                        "GPS is disabled on your device. Would you like to enable it?")
//                .setCancelable(false)
//                .setPositiveButton("Open Settings",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // set intent to open settings
//                                Intent callGPSSettingIntent = new Intent(
//                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                                context.startActivity(callGPSSettingIntent);
//                            }
//                        })
//                .setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//        AlertDialog alert = alertDialogBuilder.create();
//        //TODO: fix this copypasted junk.
//        alert.show();
//    }

    /**
     * Handler to check for a good location before retrieving location.
     * @param interval interval to check for updates
     */
    public void doRetrieveLocation(long interval) {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (mLocationM != null && mLocation.getLatitude() != 0 && mLocation.getLongitude() != 0) {
                if(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null
                        && mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude() != 0
                        && mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude() != 0) {
                    Log.e(TAG, "Retrieving Location");
                    Intent intent = new Intent("retrieveLocation");
                    intent.putExtra("location", mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    sendBroadcast(intent);
                    h.removeCallbacks(this);
                } else {
                    doRetrieveLocation();
                }
            }
        }, interval);
    }
    public void doRetrieveLocation() {
        doRetrieveLocation(0);
    }


}
