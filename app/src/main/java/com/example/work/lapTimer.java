package com.example.work;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class lapTimer extends Fragment {

    private int lapCount = 0;
    private long lapTimeMillis = 0;
    private long remainingTimeMillis = 0;
    private boolean isTimerRunning = false;
    private CountDownTimer countDownTimer;

    private TextView timerTextView;
    private TextView lapsCountTextView;
    private final int initialLapTimeSeconds = 60; // Change this to your desired initial lap time in seconds
    private int lapTimeSeconds = initialLapTimeSeconds;


    // Declare EditText variables at class level
    private EditText editTextLaps;
    private EditText editTextLapTime;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_laptimer, container, false);

        timerTextView = rootView.findViewById(R.id.timer);
        lapsCountTextView = rootView.findViewById(R.id.laps_count);
        Button playButton = rootView.findViewById(R.id.play_button);
        Button pauseButton = rootView.findViewById(R.id.pause_button);
        Button restartButton = rootView.findViewById(R.id.restart_button);
        Button configureButton = rootView.findViewById(R.id.configure_button);
        Button resetButton = rootView.findViewById(R.id.reset_button);

        playButton.setOnClickListener(v -> startTimer());

        pauseButton.setOnClickListener(v -> pauseTimer());

        restartButton.setOnClickListener(v -> restartTimer());

        configureButton.setOnClickListener(v -> showConfigureDialog());
        resetButton.setOnClickListener(v -> resetTimer());

        return rootView;
    }

    private void startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            if (remainingTimeMillis == 0) {
                // Start a new countdown timer with lapTimeSeconds
                countDownTimer = new CountDownTimer(lapTimeMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        remainingTimeMillis = millisUntilFinished;
                        updateTimerDisplay();
                    }

                    @Override
                    public void onFinish() {
                        isTimerRunning = false;
                        if (lapCount > 0) {
                            lapCount--;
                            remainingTimeMillis = lapTimeMillis; // Reset remaining time
                            updateTimerDisplay();
                            startTimer(); // Start the timer again
                        }
                    }
                };
                countDownTimer.start();
            } else {
                // Resume the existing countdown timer with remainingTimeMillis
                countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        remainingTimeMillis = millisUntilFinished;
                        updateTimerDisplay();
                    }

                    @Override
                    public void onFinish() {
                        isTimerRunning = false;
                        if (lapCount > 0) {
                            lapCount--;
                            remainingTimeMillis = lapTimeMillis; // Reset remaining time
                            updateTimerDisplay();
                            startTimer(); // Start the timer again
                        }
                    }
                };
                countDownTimer.start();
            }
        }
    }




    private void pauseTimer() {
        if (isTimerRunning) {
            isTimerRunning = false;
            if (countDownTimer != null) {
                countDownTimer.cancel(); // Cancel the countdown timer
            }
        }
    }

    private void restartTimer() {
            lapCount = Integer.parseInt(editTextLaps.getText().toString());
            lapTimeSeconds = Integer.parseInt(editTextLapTime.getText().toString());
            lapTimeMillis = lapTimeSeconds * 1000L;
            remainingTimeMillis = lapTimeMillis;
            updateTimerDisplay();
            startTimer(); // Start the timer again
        }




    private void updateTimerDisplay() {
        String formattedTime = convertMillisToTime(remainingTimeMillis);
        timerTextView.setText(formattedTime);
        lapsCountTextView.setText(String.valueOf(lapCount));
    }

    @SuppressLint("DefaultLocale")
    private String convertMillisToTime(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds %= 60;
        int hours = minutes / 60;
        minutes %= 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    private void resetTimer() {
        // Reset all values to their initial state
        lapCount = 0;
        lapTimeMillis = 0; // Set lapTimeMillis to 0
        remainingTimeMillis = 0;
        isTimerRunning = false;
        lapTimeSeconds = initialLapTimeSeconds;

        // Update the timer display
        updateTimerDisplay();
    }

    private void showConfigureDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Configure Lap Timer");
        builder.setMessage("Enter lap count and lap time:");

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_configure, null);
        builder.setView(dialogView);

        // Initialize the EditText variables here
        editTextLaps = dialogView.findViewById(R.id.editTextLaps);
        editTextLapTime = dialogView.findViewById(R.id.editTextLapTime);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String lapsStr = editTextLaps.getText().toString();
            String lapTimeStr = editTextLapTime.getText().toString();

            if (!lapsStr.isEmpty() && !lapTimeStr.isEmpty()) {
                lapCount = Integer.parseInt(lapsStr);
                lapTimeMillis = Integer.parseInt(lapTimeStr) * 1000L;
                remainingTimeMillis = lapTimeMillis;
                updateTimerDisplay();
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
