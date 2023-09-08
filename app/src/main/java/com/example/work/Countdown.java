package com.example.work;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

public class Countdown extends Fragment {

    private static final String CHANNEL_ID = "CountdownChannel";
    private static final int NOTIFICATION_ID = 1;

    private EditText timeText;
    private Button startStopButton;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning;
    private long timeLeftInMillis;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        createNotificationChannel();

        timeText = view.findViewById(R.id.timeEditText);
        startStopButton = view.findViewById(R.id.startStopButton);
        Button resetButton = view.findViewById(R.id.resetButton);

        String hint = "Enter time (HH:MM:SS)";
        SpannableString hintSpannable = new SpannableString(hint);
        hintSpannable.setSpan(new RelativeSizeSpan(1.2f), 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        timeText.setHint(new SpannedString(hintSpannable));

        startStopButton.setOnClickListener(v -> {
            if (isTimerRunning) {
                stopTimer();
            } else {
                startTimer();
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @SuppressLint("SetTextI18n")
    private void startTimer() {
        String timeInput = timeText.getText().toString().trim();
        String[] timeParts = timeInput.split(":");

        int hours = 0;
        int minutes;
        int seconds;

        try {
            if (timeParts.length == 3) {
                hours = Integer.parseInt(timeParts[0]);
                minutes = Integer.parseInt(timeParts[1]);
                seconds = Integer.parseInt(timeParts[2]);
            } else if (timeParts.length == 1) {
                int totalSeconds = Integer.parseInt(timeParts[0]);
                minutes = totalSeconds / 60;
                seconds = totalSeconds % 60;
            } else {
                timeText.setError("Invalid time format. Please enter time in HH:MM:SS or seconds format.");
                return;
            }

            timeLeftInMillis = (hours * 3600L + minutes * 60L + seconds) * 1000;

            timeText.setEnabled(false);
            startStopButton.setText("stop");

            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateCountdownText();
                }

                @Override
                public void onFinish() {
                    stopTimer();
                    showNotification();
                    playSound();
                }
            }.start();

            isTimerRunning = true;
        } catch (NumberFormatException e) {
            timeText.setError("Invalid time value. Please enter a valid time.");
        }
        foregroundServiceRunning();
    }

    @SuppressLint("SetTextI18n")
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        startStopButton.setText("start");
        timeText.setEnabled(true);
        isTimerRunning = false;
    }

    private void resetTimer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Timer");
        builder.setMessage("Are you sure you want to reset the timer?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            stopTimer();
            timeText.setText("");
            timeText.setHint("Enter time (HH:MM:SS)");
            timeLeftInMillis = 0;
        });
        builder.setNegativeButton("No", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateCountdownText() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        @SuppressLint("DefaultLocale") String timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        SpannableString timeSpannable = new SpannableString(timeLeftFormatted);
        timeSpannable.setSpan(new RelativeSizeSpan(2f), 0, timeSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        timeText.setText(timeSpannable);
    }

    private void createNotificationChannel() {
        CharSequence name = "Countdown Channel";
        String description = "Channel for Countdown notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (CountDownService.class.getName().equals(service.service.getClassName())) {
                return;
            }
        }
    }

    private void showNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.th)
                .setContentTitle("Countdown Timer")
                .setContentText("Timer finished!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void playSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.success);

        mediaPlayer.setOnCompletionListener(MediaPlayer::release);

        mediaPlayer.start();
    }
}
