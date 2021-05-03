package com.time.vaccineslot.service;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.time.vaccineslot.AvailableSlotsActivity;
import com.time.vaccineslot.MainActivity;
import com.time.vaccineslot.R;
import com.time.vaccineslot.helper.RestHelper;
import com.time.vaccineslot.pojo.AvailableSlot;
import lombok.SneakyThrows;

import java.text.ParseException;
import java.util.*;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    String pinCodeKey = "com.time.vaccineslot.pincode";
    String ageKey = "com.time.vaccineslot.age";

    @SneakyThrows
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences(
                "com.time.vaccineslot", Context.MODE_PRIVATE);
        String pinCode = prefs.getString(pinCodeKey, "");
        int age = prefs.getInt(ageKey, 18);


        if (pinCode.equals("")){
            return;
        }

        new Thread(){
            @Override
            public void run() {
                try {
                    TreeMap<String, List<AvailableSlot>> availableSlot = RestHelper.getAvailableSlot(pinCode, age);
                    Log.d("vaccine", String.valueOf(availableSlot));
                    showNotification(availableSlot, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void showNotification(TreeMap<String, List<AvailableSlot>> allAvailableSlots, Context mContext) throws ParseException {

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        String channelId = "notify_001";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Vaccine Slots",
                    NotificationManager.IMPORTANCE_LOW);
            mNotificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext.getApplicationContext(), "notify_001");

        if (allAvailableSlots == null || allAvailableSlots.size() == 0){
            mBuilder.setContentTitle("No vaccine slot \uD83D\uDE37\uD83D\uDE15");
            mBuilder.setContentText("Will notify whenever available.");
        } else{
            Intent ii = new Intent(mContext, AvailableSlotsActivity.class);
            ii.putExtra("availableSlotsMap", allAvailableSlots);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setContentText("Vaccine available on " + allAvailableSlots.size() +  " days. Check slots here \uD83D\uDE37\uD83D\uDE01");
        }

        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setChannelId(channelId);
        mBuilder.setAutoCancel(true);
        mBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
