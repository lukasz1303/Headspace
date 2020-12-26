package com.example.headspace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class ReminderSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        TextView mTextViewTime = findViewById(R.id.tv_time_reminder_settings);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        int minute = pref.getInt("Minute Reminder", 0);
        int hour = pref.getInt("Hour Reminder", 12);

        String stringMinute = String.valueOf(minute);
        if (minute < 10){
            stringMinute = "0" + minute;
        }
        String time = hour + ":" + stringMinute;
        mTextViewTime.setText(time);

        LinearLayout linearLayout = findViewById(R.id.linear_layout_time_reminder_settings);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        SwitchCompat switchCompat = findViewById(R.id.switch_compact_reminder);
        switchCompat.setChecked(pref.getBoolean("Reminder ON",false));
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                Intent intent = new Intent(getApplicationContext(),NotificationReceiver.class);
                intent.setAction("Reminder");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                assert alarmManager != null;

                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                SharedPreferences.Editor editor = pref.edit();

                if(isChecked){
                    Calendar calendar = Calendar.getInstance();
                    int minute = pref.getInt("Minute Reminder", 0);
                    int hour = pref.getInt("Hour Reminder", 12);

                    editor.putBoolean("Reminder ON",true);

                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE,minute);

                    if(calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
                        NotificationChannel channel = new NotificationChannel("Channel Reminder", "Reminder", importance);
                        channel.setDescription("Channel for reminder notification.");

                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.createNotificationChannel(channel);
                    }

                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,pendingIntent);

                } else {
                    alarmManager.cancel(pendingIntent);
                    editor.putBoolean("Reminder ON",false);
                }
                editor.apply();
            }
        });
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

}
