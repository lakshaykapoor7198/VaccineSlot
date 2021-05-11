package com.time.vaccineslot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.time.vaccineslot.helper.DistrictRestHelper;
import com.time.vaccineslot.service.AlarmReceiver;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText pinCodeInput;
    Spinner spinner;
    Spinner stateSpinner;
    Spinner districtSpinner;
    RadioButton ageRadioBtn;
    RadioGroup ageRadioGroup;
    RadioButton noVaccineRadioBtn;
    RadioGroup noVaccineRadioGroup;

    SharedPreferences prefs;
    String pinCodeKey = "com.time.vaccineslot.pincode";
    String ageKey = "com.time.vaccineslot.age";
    String intervalKey = "com.time.vaccineslot.interval";
    String stateNameKey = "com.time.vaccineslot.statename";
    String districtNameKey = "com.time.vaccineslot.districtname";
    String districtIdKey = "com.time.vaccineslot.districtid";
    String slotsApiCallKey = "com.time.vaccineslot.slotsapicall";
    String noVaccineShowKey = "com.time.vaccineslot.novaccine";

    String[] intervalArray = {"1 min", "10 min", "1 hour", "2 hour", "6 hour", "12 hour", "1 day"};
    long[] intervalArrayMinutes = { 1, 10, 60, 120, 360, 720, 1440};
    static List<String> stateNames = new ArrayList<>();
    static List<Integer> stateIds = new ArrayList<>();
    static List<String> districtNames = new ArrayList<>();
    static List<Integer> districtIds = new ArrayList<>();

    private PendingIntent pendingIntent;
    private static Drawable spinnerBackground;

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button submitButton = findViewById(R.id.button_submit);
        spinner = findViewById(R.id.interval_spinner);
        stateSpinner = findViewById(R.id.state_spinner);
        districtSpinner = findViewById(R.id.district_spinner);
        pinCodeInput = findViewById(R.id.pinCode);
        ageRadioGroup= findViewById(R.id.age);
        noVaccineRadioGroup= findViewById(R.id.noVaccineNotification);

        spinnerBackground = spinner.getBackground();
        submitButton.setOnClickListener(MainActivity.this);

        prefs = this.getSharedPreferences(
                "com.time.vaccineslot", Context.MODE_PRIVATE);
        String pinCode = prefs.getString(pinCodeKey, "");
        int age = prefs.getInt(ageKey, 18);
        String noVaccine = prefs.getString(noVaccineShowKey, "Yes");

        if (!pinCode.equals("")) {
            pinCodeInput.setText(pinCode);
        }

        if (age == 45){
            ageRadioBtn = findViewById(R.id.fortyfive);
        }else{
            ageRadioBtn = findViewById(R.id.eighteen);
        }

        if (noVaccine.equals("No")){
            noVaccineRadioBtn = findViewById(R.id.noVaccineNo);
        }else{
            noVaccineRadioBtn = findViewById(R.id.noVaccineYes);
        }

        ageRadioBtn.setChecked(true);
        noVaccineRadioBtn.setChecked(true);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, intervalArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(1);


        String stateName = prefs.getString(stateNameKey, "");
        String districtName = prefs.getString(districtNameKey, "");
        if (!stateName.equals("") && !districtName.equals("")){
            loadStateSpinner(stateName, districtName);
        } else{
            loadStateSpinner(null, null);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_submit) {
            String pinCode = pinCodeInput.getText().toString();
            int ageSelectedId = ageRadioGroup.getCheckedRadioButtonId();
            ageRadioBtn = findViewById(ageSelectedId);
            int age = getAge(ageRadioBtn.getText());
            int noVaccineSelectedId = noVaccineRadioGroup.getCheckedRadioButtonId();
            noVaccineRadioBtn = findViewById(noVaccineSelectedId);
            String noVaccine = (String) noVaccineRadioBtn.getText();
            long interval = (intervalArrayMinutes[spinner.getSelectedItemPosition()]) * 60 * 1000;
            int stateId = (stateIds.get(stateSpinner.getSelectedItemPosition()));
            String stateName = (stateNames.get(stateSpinner.getSelectedItemPosition()));

            int districtId = districtIds.size() > 0 ? (districtIds.get(districtSpinner.getSelectedItemPosition())): -1;
            String districtName = districtNames.size() > 0 ? (districtNames.get(districtSpinner.getSelectedItemPosition())) : null;

            if (stateId != -1 && districtId != -1){
                prefs.edit().putString(slotsApiCallKey, "District").apply();
            }
            else if (!pinCode.equals("")){
                prefs.edit().putString(slotsApiCallKey, "PinCode").apply();
            }
            else{
                Toast.makeText(getApplicationContext(), "Select state and district or Enter pincode", Toast.LENGTH_LONG).show();
                return;
            }

            prefs.edit().putString(pinCodeKey, pinCode).apply();
            prefs.edit().putInt(ageKey, age).apply();
            prefs.edit().putString(noVaccineShowKey, noVaccine).apply();
            prefs.edit().putLong(intervalKey, interval).apply();
            prefs.edit().putString(stateNameKey, stateName).apply();
            prefs.edit().putString(districtNameKey, districtName).apply();
            prefs.edit().putString(districtIdKey, String.valueOf(districtId)).apply();
            scheduleAlarm(interval);
            this.onBackPressed();
        }
    }

    private int getAge(CharSequence text) {
        if (text.equals("45+")){
            return 45;
        } else{
            return 18;
        }
    }

    private void scheduleAlarm(long interval) {
        Toast.makeText(getApplicationContext(), "Details saved! You will get notified whenever there is a slot in next 3 weeks", Toast.LENGTH_LONG).show();
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        startAlarm(interval);
    }

    private void startAlarm(long interval) {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
    }


    private void loadStateSpinner(String stateName, String districtName) throws InterruptedException {

        CountDownLatch doneSignal = new CountDownLatch(1);
        new Thread(()->{
            Pair<List<String>, List<Integer>> states = DistrictRestHelper.getStatesNameAndIds();

            MainActivity.stateNames = states.first;
            MainActivity.stateIds = states.second;
            doneSignal.countDown();
        }).start();
        doneSignal.await();

        stateNames.add(0, "Select State");
        stateIds.add(0, -1);

        if (stateNames.size() <= 1){
            Toast.makeText(getApplicationContext(), "Error loading states", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, stateNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(adapter);
        int defaultPosition = stateName != null ? stateNames.indexOf(stateName) : 0;
        stateSpinner.setSelection(defaultPosition);

        if (stateName!= null && districtName != null){
            districtSpinner.setEnabled(true);
            districtSpinner.setClickable(true);
            loadDistrictSpinner(stateIds.get(defaultPosition), districtName);
        } else{
            districtSpinner.setEnabled(false);
            districtSpinner.setClickable(false);
        }

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @SneakyThrows
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int stateId = stateIds.get(position);
                if (stateId == -1){
                    Toast.makeText(getApplicationContext(), "Select any state or enter pin code", Toast.LENGTH_LONG).show();
                } else{
                    districtSpinner.setEnabled(true);
                    districtSpinner.setClickable(true);
                    loadDistrictSpinner(stateId, districtName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadDistrictSpinner(int stateId, String districtName) throws InterruptedException {

        CountDownLatch doneSignal = new CountDownLatch(1);

        new Thread(()->{
            Pair<List<String>, List<Integer>> districts = DistrictRestHelper.getDistrictsNameAndIds(stateId);
            MainActivity.districtNames = districts.first;
            MainActivity.districtIds = districts.second;
            doneSignal.countDown();
        }).start();
        doneSignal.await();

        districtNames.add(0, "Select District");
        districtIds.add(0, -1);

        if (districtNames.size() <= 1){
            Toast.makeText(getApplicationContext(), "Error loading districts", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, districtNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(adapter);

        int defaultPosition = districtName != null ? districtNames.indexOf(districtName) : 0;
        districtSpinner.setSelection(defaultPosition);

        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int districtId = districtIds.get(position);
                if (districtId == -1){
                    Toast.makeText(getApplicationContext(), "Select any district or enter pin code", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


}