package com.time.vaccineslot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_submit) {
            String pinCode = pinCodeInput.getText().toString();
            int age = Integer.parseInt(ageInput.getText().toString());
            prefs.edit().putString(pinCodeKey, pinCode).apply();
            prefs.edit().putInt(ageKey, age).apply();
            Toast.makeText(getApplicationContext(),"Details saved! You will get notified whenever there is a slot",Toast.LENGTH_LONG).show();

            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
            startAlarm();

        }
    }

    public void startAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 10 * 1000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }
}