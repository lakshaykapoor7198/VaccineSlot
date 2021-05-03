package com.time.vaccineslot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.time.vaccineslot.service.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText pinCodeInput;
    EditText ageInput;
    Spinner spinner;
    SharedPreferences prefs;
    String pinCodeKey = "com.time.vaccineslot.pincode";
    String ageKey = "com.time.vaccineslot.age";
    String intervalKey = "com.time.vaccineslot.interval";
    String[] intervalArray = {"10 min", "1 hour", "2 hour", "6 hour", "12 hour", "1 day"};
    long[] intervalArrayMinutes = { 10, 60, 120, 360, 720, 1440};

    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button submitButton = findViewById(R.id.button_submit);
        spinner = findViewById(R.id.interval_spinner);
        spinner.setSelection(0);
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

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, intervalArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_submit) {
            String pinCode = pinCodeInput.getText().toString();
            int age = Integer.parseInt(ageInput.getText().toString());
            long interval = (intervalArrayMinutes[spinner.getSelectedItemPosition()]) * 60 * 1000;
            prefs.edit().putString(pinCodeKey, pinCode).apply();
            prefs.edit().putInt(ageKey, age).apply();
            prefs.edit().putLong(intervalKey, interval).apply();
            scheduleAlarm(interval);
        }
    }

    private void scheduleAlarm(long interval) {
        Toast.makeText(getApplicationContext(), "Details saved! You will get notified whenever there is a slot", Toast.LENGTH_LONG).show();
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        startAlarm(interval);
    }

    private void startAlarm(long interval) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }

}