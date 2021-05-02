package com.time.vaccineslot.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.time.vaccineslot.MainActivity;
import com.time.vaccineslot.R;
import com.time.vaccineslot.helper.RestHelper;
import com.time.vaccineslot.pojo.AvailableSlot;
import lombok.SneakyThrows;

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
                    AvailableSlot availableSlot = RestHelper.getAvailableSlot(pinCode, age);
                    showNotification(availableSlot, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void showNotification(AvailableSlot availableSlot, Context mContext) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext.getApplicationContext(), "notify_001");
        Intent ii = new Intent(mContext.getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.setBigContentTitle("Vaccine Slot Available!!!");

        StringBuilder text = new StringBuilder("Centre: " + availableSlot.getName()
                + "\nDate: " + availableSlot.getDate()
                + "\nDoses Left: " + availableSlot.getAvailable_capacity());

        if (!availableSlot.getVaccine().equals("")){
            text.append(" of ").append(availableSlot.getVaccine());
        }

        text.append("\nSlots: ");

        for (String slot: availableSlot.getSlots()){
            text.append(slot).append(", ");
        }

        bigText.bigText(text);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.icon);
        mBuilder.setContentTitle("Vaccine Slots Available!!!");
        mBuilder.setContentText(availableSlot.getName() + " | " + availableSlot.getDate());
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "notify_001";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Vaccine Slots",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());

    }

}
