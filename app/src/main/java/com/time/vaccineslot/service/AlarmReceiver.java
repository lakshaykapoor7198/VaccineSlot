package com.time.vaccineslot.service;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.time.vaccineslot.R;
import com.time.vaccineslot.helper.RestHelper;
import com.time.vaccineslot.pojo.AvailableSlot;
import lombok.SneakyThrows;

import java.util.List;

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
                    List<AvailableSlot> availableSlot = RestHelper.getAvailableSlot(pinCode, age);
                    Log.d("vaccine", String.valueOf(availableSlot));
                    showNotification(availableSlot, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void showNotification(List<AvailableSlot> availableSlots, Context mContext) {

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

        Intent ii = new Intent(Intent.ACTION_VIEW);
        ii.setData(Uri.parse("https://selfregistration.cowin.gov.in/"));
        ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0);

        NotificationCompat.MessagingStyle style = new NotificationCompat.MessagingStyle("Vaccine");

        if (availableSlots != null ){
            for (AvailableSlot availableSlot : availableSlots) {

                StringBuilder text = new StringBuilder(availableSlot.getAvailable_capacity() + " doses available on " + availableSlot.getDate());

                if (!availableSlot.getVaccine().equals("")) {
                    text.append(" of ").append(availableSlot.getVaccine());
                }

                if (availableSlot.getSlots().size() > 0) {
                    text.append("\nSlots: ");

                    for (String slot : availableSlot.getSlots()) {
                        text.append(slot).append(", ");
                    }

                    text.setLength(text.length() - 2);
                }

                style.addMessage(text.toString(), System.currentTimeMillis(),
                        availableSlot.getName());
            }
        }
        else{
            style.addMessage("Will notify whenever available.", System.currentTimeMillis(), "No vaccine :(");
        }

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setChannelId(channelId);
        mBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        mBuilder.setStyle(style);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
