package com.kinfong.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver to start LocationService.
 * Called by MyScheduleReceiver.
 *
 * Created by Kin on 9/8/13.
 */
public class MyStartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, LocationService.class);
        context.startService(service);
    }
}
