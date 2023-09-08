    package com.example.work;

    import android.annotation.SuppressLint;
    import android.app.AlarmManager;
    import android.app.PendingIntent;
    import android.content.Context;
    import android.content.Intent;
    import android.icu.util.Calendar;
    import android.os.Bundle;
    import android.os.SystemClock;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.TimePicker;
    import android.widget.Toast;

    import androidx.fragment.app.Fragment;

    public class AlarmFragment extends Fragment implements AdapterView.OnItemSelectedListener {

        AlarmManager alarm_manager;
        TimePicker alarm_timepicker;
        TextView alarm_state;
        PendingIntent pending_intent;
        int sound_select;
        private Spinner amPmSpinner;

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_alarm, container, false);

            // Initialize alarm manager, time picker, text update
            alarm_manager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
            alarm_timepicker = view.findViewById(R.id.timePicker);
            alarm_state = view.findViewById(R.id.alarm_state);

            // Create calendar instance
            final Calendar calendar = Calendar.getInstance();

            // Create intent for AlarmReceiver class, send only once
            final Intent my_intent = new Intent(getActivity(), AlarmReceiver.class);

            // Create spinner in the fragment UI and corresponding ArrayAdapter
            Spinner spinner = view.findViewById(R.id.spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                    (getActivity(), R.array.stepbrothers_array, android.R.layout.simple_spinner_item);
            // Specify layout used for the option list
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply adapter to spinner
            spinner.setAdapter(adapter);
            // Set onClickListener for spinner
            spinner.setOnItemSelectedListener(this);

            // Initialize AM/PM spinner and adapter
            amPmSpinner = view.findViewById(R.id.amPmSpinner);
            ArrayAdapter<CharSequence> amPmAdapter = ArrayAdapter.createFromResource(
                    getActivity(),
                    R.array.am_pm_array, // Create an array resource containing AM and PM strings
                    android.R.layout.simple_spinner_item
            );
            amPmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            amPmSpinner.setAdapter(amPmAdapter);

            // Initialize start button and create onClickListener to start alarm
            Button alarm_on = view.findViewById(R.id.alarm_on);
            alarm_on.setOnClickListener(view1 -> {
                // Get the selected time and AM/PM selection
                int hourOfDay = alarm_timepicker.getHour();
                int minute = alarm_timepicker.getMinute();
                String amPm = amPmSpinner.getSelectedItem().toString();

                // Set the calendar to the selected alarm time
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0); // Optional: Clear seconds to have a precise trigger
                calendar.set(Calendar.MILLISECOND, 0); // Optional: Clear milliseconds to have a precise trigger

                // Adjust the hour if PM is selected (e.g., add 12 hours)
                if (amPm.equals("PM")) {
                    calendar.add(Calendar.HOUR_OF_DAY, 12);
                }

                // Calculate the time until the alarm in milliseconds
                long alarmTimeInMillis = calendar.getTimeInMillis();

                // Calculate the time until the alarm trigger
                long timeUntilAlarmInMillis = alarmTimeInMillis - System.currentTimeMillis();

                // Create an intent for AlarmReceiver class, send only once
                final Intent my_intent1 = new Intent(getActivity(), AlarmReceiver.class);

                // Put extra string into my_intent, indicates on button pressed
                my_intent1.putExtra("extra", "alarm on");

                // Input extra long into my_intent
                // Requests specific value from spinner
                my_intent1.putExtra("sound_choice", sound_select);

                // Pending intent to delay intent until the specified alarm time
                PendingIntent pending_intent = PendingIntent.getBroadcast(getActivity(), 0, my_intent1, PendingIntent.FLAG_UPDATE_CURRENT);

                // Set the alarm using ELAPSED_REALTIME_WAKEUP with the calculated time
                alarm_manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + timeUntilAlarmInMillis, pending_intent);

                // Update the UI to show the alarm set time
                String hour_string = String.valueOf(hourOfDay);
                String minute_string = String.valueOf(minute);

                // Handles to format time data
                if (hourOfDay > 12) hour_string = String.valueOf(hourOfDay - 12);
                if (minute < 10) minute_string = "0" + minute;

                alarm_state.setText("Alarm set to: " + hour_string + ":" + minute_string);
            });

            // Initialize stop button and create onClick listener to stop alarm
            Button alarm_off = view.findViewById(R.id.alarm_off);
            alarm_off.setOnClickListener(view12 -> {
                alarm_state.setText("Alarm Off!");

                // Cancel the alarm if the pending_intent is not null
                if (pending_intent != null) {
                    alarm_manager.cancel(pending_intent);

                    // Put extra string into my_intent, indicates off button pressed
                    my_intent.putExtra("extra", "alarm off");

                    // Also input extra long for alarm off to prevent null pointer exception
                    my_intent.putExtra("sound_choice", sound_select);

                    // Stop ringtone
                    requireActivity().sendBroadcast(my_intent);
                } else {
                    // Handle the case where the pending_intent is null (no alarm set)
                    Toast.makeText(getActivity(), "No alarm is currently set", Toast.LENGTH_SHORT).show();
                }
            });

            return view;
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            // Output id which user selected
            // Toast.makeText(parent.getContext(), "Spinner item is " + id, Toast.LENGTH_SHORT).show();
            sound_select = (int) id;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
