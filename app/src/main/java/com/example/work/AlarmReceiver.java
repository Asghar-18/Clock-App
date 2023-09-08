package com.example.work;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Josh on 8/30/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Fetch extra strings from MainActivity on button intent
        String fetch_string = intent.getExtras().getString("extra");

        // Fetch extra longs from MainActivity intent
        int get_sound_choice = intent.getExtras().getInt("sound_choice");

        // Create intent for the RingtonePlayingService
        Intent service_intent = new Intent(context, RingtonePlayingService.class);

        // Pass extra string from receiver to RingtonePlayingService
        service_intent.putExtra("extra", fetch_string);

        // Pass extra integer from receiver to RingtonePlayingService
        service_intent.putExtra("sound_choice", get_sound_choice);

        // Start or stop the ringtone service based on the "extra" value
        if (fetch_string != null) {
            if (fetch_string.equals("alarm on")) {
                // Start ringtone service
                context.startService(service_intent);
            } else if (fetch_string.equals("alarm off")) {
                // Stop ringtone service
                context.stopService(service_intent);
            }
        }
    }
}
