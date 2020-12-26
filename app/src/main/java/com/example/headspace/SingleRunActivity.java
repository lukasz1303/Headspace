package com.example.headspace;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.Calendar;

public class SingleRunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_run);

        final RelativeLayout relativeLayout = findViewById(R.id.relative_layout_next_single_run);
        final ViewFlipper viewFlipper = findViewById(R.id.view_flipper_single_run);

        TextView timeTextView = findViewById(R.id.tv_time_reminder);
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        String stringMinute = String.valueOf(minute);
        if (minute < 10) {
            stringMinute = "0" + minute;
        }
        String time = hour + ":" + stringMinute;
        timeTextView.setText(time);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.showNext();
                relativeLayout.setOnClickListener(null);
            }
        });

        LinearLayout linearLayout = findViewById(R.id.linear_layout_time_reminder);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });

        RelativeLayout relativeLayout2 = findViewById(R.id.relative_layout_next_single_run_2);
        relativeLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();

                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
                int minute = pref.getInt("Minute Reminder", 0);
                int hour = pref.getInt("Hour Reminder", 12);

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
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

                Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                intent.setAction("Reminder");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                assert alarmManager != null;
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("First launch", true);
                editor.putBoolean("Reminder ON", true);
                editor.apply();
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                if (i.resolveActivity(getPackageManager()) != null) {
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

}
