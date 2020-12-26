package com.example.headspace;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        SharedPreferences pref = Objects.requireNonNull(getActivity()).getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("Minute Reminder", minute);
        editor.putInt("Hour Reminder", hourOfDay);
        editor.apply();

        String time;
        String stringMinute = String.valueOf(minute);
        if (minute < 10) {
            stringMinute = "0" + minute;
        }
        TextView textView = getActivity().findViewById(R.id.tv_time_reminder_settings);
        boolean switched = pref.getBoolean("Reminder ON", false);
        if(switched){
            SwitchCompat switchCompat = getActivity().findViewById(R.id.switch_compact_reminder);
            switchCompat.setChecked(false);
            switchCompat.setChecked(true);
        }

        if (textView == null) {
            textView = getActivity().findViewById(R.id.tv_time_reminder);
        }
        time = hourOfDay + ":" + stringMinute;
        textView.setText(time);
    }
}