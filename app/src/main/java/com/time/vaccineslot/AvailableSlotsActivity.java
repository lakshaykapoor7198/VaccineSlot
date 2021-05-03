package com.time.vaccineslot;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.time.vaccineslot.adapter.SlotsAdapter;
import com.time.vaccineslot.pojo.AvailableSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class AvailableSlotsActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_slots);
        recyclerView = findViewById(R.id.idRecyclerView);

        Intent intent = getIntent();
        TreeMap<String, List<AvailableSlot>> availableSlotsMap = new TreeMap<>((HashMap<String, List<AvailableSlot>>) intent.getSerializableExtra("availableSlotsMap"));
        ArrayList<AvailableSlot> availableSlots = new ArrayList<>();
        availableSlotsMap.values().forEach(availableSlots::addAll);

        SlotsAdapter slotsAdapter = new SlotsAdapter(this, availableSlots);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(slotsAdapter);
    }
}