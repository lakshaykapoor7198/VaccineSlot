package com.time.vaccineslot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.time.vaccineslot.service.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText pinCodeInput;
    EditText ageInput;
    SharedPreferences prefs;
    String pinCodeKey = "com.time.vaccineslot.pincode";
    String ageKey = "com.time.vaccineslot.age";

    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button submitButton = findViewById(R.id.button_submit);
        pinCodeInput = findViewById(R.id.pinCode);
        ageInput = findViewById(R.id.age);
        submitButton.setOnClickListener(MainActivity.this);
        prefs = this.getSharedPreferences(
                "com.time.vaccineslot", Context.MODE_PRIVATE);
        String pinCode = prefs.getString(pinCodeKey, "");
        int age = prefs.getInt(ageKey, 0);

        if (!pinCode.equals("") && age != 0) {
            pinCodeInput.setText(pinCode);
            ageInput.setText(String.valueOf(age));
        }

//        // Disable battery optimization
//        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.d("vaccine", String.valueOf(pm.isIgnoringBatteryOptimizations(getPackageName())));
//            askIgnoreOptimization();
//            Log.d("vaccine", String.valueOf(pm.isIgnoringBatteryOptimizations(getPackageName())));
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_submit) {
            String pinCode = pinCodeInput.getText().toString();
            int age = Integer.parseInt(ageInput.getText().toString());
            prefs.edit().putString(pinCodeKey, pinCode).apply();
            prefs.edit().putInt(ageKey, age).apply();
            scheduleAlarm();
        }
    }

    private void scheduleAlarm() {
        Toast.makeText(getApplicationContext(), "Details saved! You will get notified whenever there is a slot", Toast.LENGTH_LONG).show();
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        startAlarm();
    }

//    private void askIgnoreOptimization() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setData(Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, 1002);
//        }
//    }

    private void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 10 * 60 * 1000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }
}