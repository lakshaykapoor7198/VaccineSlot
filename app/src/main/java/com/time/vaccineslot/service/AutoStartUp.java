package com.time.vaccineslot.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class AutoStartUp extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String intervalKey = "com.time.vaccineslot.interval";
        SharedPreferences prefs = context.getSharedPreferences(
                "com.time.vaccineslot", Context.MODE_PRIVATE);

        long interval = prefs.getLong(intervalKey, 10 * 60 * 1000);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(context, "Vaccine Alarm Set", Toast.LENGTH_SHORT).show();
    }

}
