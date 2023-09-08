package com.example.work;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Random;

public class RingtonePlayingService extends Service {

    private MediaPlayer media_song;
    private boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Fetch extra strings (on/off)
        String state = intent.getStringExtra("extra");

        // Fetch sound_choice integer values
        int sound_id = intent.getIntExtra("sound_choice", -1);

        if (state == null || sound_id == -1) {
            // Handle invalid or missing extras gracefully
            stopSelf();
            return START_NOT_STICKY;
        }

        // Create a notification channel if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "your_channel_id",
                    "Your Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            // Configure additional channel settings if needed
            // channel.setDescription("Your Channel Description");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManager notify_manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent_main_activity = new Intent(this.getApplicationContext(), MainActivity.class);
        // Create pending intent with the mutability flag
        PendingIntent pending_intent_main_activity = PendingIntent.getActivity(
                this, 0, intent_main_activity, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set notification parameters
        Notification notify_popup =
                new NotificationCompat.Builder(this, "your_channel_id")
                        .setContentTitle("An alarm is going off!")
                        .setContentText("Click me!")
                        .setSmallIcon(R.drawable.th) // Set a valid small icon resource
                        .setContentIntent(pending_intent_main_activity)
                        .setAutoCancel(true)
                        .build();


        if (!isRunning && state.equals("alarm on")) {
            isRunning = true;

            int notificationId = (int) System.currentTimeMillis(); // Use current time as the notification ID
            notify_manager.notify(notificationId, notify_popup);

            if (sound_id == 0) {
                // Play randomly selected audio
                int min = 1, max = 5;
                Random rand = new Random();
                int sound_number = rand.nextInt(max - min + 1) + min;

                int soundResource = R.raw.alarm; // Default sound

                switch (sound_number) {
                    case 1:
                        soundResource = R.raw.mechanic_alarm;
                        break;
                    case 2:
                        soundResource = R.raw.chiptune;
                        break;
                    case 3:
                        soundResource = R.raw.fantasy;
                        break;
                    case 4:
                        soundResource = R.raw.rooster;
                        break;
                    case 5:
                        soundResource = R.raw.simplified;
                        break;
                }

                media_song = MediaPlayer.create(this, soundResource);
                media_song.start();
            } else if (sound_id >= 1 && sound_id <= 5) {
                // Play a specific sound based on the sound_id
                int soundResource = R.raw.alarm; // Default sound

                switch (sound_id) {
                    case 1:
                        soundResource = R.raw.mechanic_alarm;
                        break;
                    case 2:
                        soundResource = R.raw.chiptune;
                        break;
                    case 3:
                        soundResource = R.raw.fantasy;
                        break;
                    case 4:
                        soundResource = R.raw.rooster;
                        break;
                    case 5:
                        soundResource = R.raw.simplified;
                        break;
                }

                media_song = MediaPlayer.create(this, soundResource);
                media_song.start();
            }
        } else if (isRunning && state.equals("alarm off")) {
            // If music is playing and alarm off is pressed, stop the music
            media_song.stop();
            media_song.reset();
            isRunning = false;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRunning) {
            // If the service is destroyed while music is playing, stop the music
            media_song.stop();
            media_song.reset();
        }
        isRunning = false;
    }
}
