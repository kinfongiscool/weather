package com.kinfong.weather;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;

    private static Location mLocation;

    private class LocationListener implements android.location.LocationListener{

        public LocationListener(String provider) {
            mLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location) {
            mLocation.set(location);
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();
        if(checkGPSEnabled()) {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
            } catch (java.lang.SecurityException ex) {
            } catch (IllegalArgumentException ex) {
            }
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (java.lang.SecurityException ex) {
            } catch (IllegalArgumentException ex) {
            }

        } else {
            Intent intent = new Intent("showGpsDialog");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean checkGPSEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Handler to check for a good location before retrieving location.
     * @param interval interval to check for updates
     */
    public void doRetrieveLocation(long interval) {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null
                        && mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude() != 0
                        && mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude() != 0) {
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
