package com.time.vaccineslot.helper;

import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.time.vaccineslot.pojo.AvailableSlot;
import com.time.vaccineslot.pojo.Centers;
import com.time.vaccineslot.pojo.Sessions;
import com.time.vaccineslot.pojo.SlotResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RestHelper {

    private static final String serverBaseUrl = "https://cdn-api.co-vin.in/api";

    public static TreeMap<String, List<AvailableSlot>> getAvailableSlot(String id, int age, String callType) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) > 15){
            calendar.add(Calendar.DATE, 1);
        }

        String date = getDateInFormat(calendar);
        TreeMap<String, List<AvailableSlot>> allAvailableSlots = null;

        // Check for next 3 weeks
        for (int i = 0; i < 3; i++) {
            String url = getUrl(id, date, callType);
            SlotResponse response = getSlots(url);

            if (response.equals(new SlotResponse())) break;
            if (response.getCenters().size() == 0) break;

            allAvailableSlots = findAvailableSlot(response, age);
            if (null != allAvailableSlots) {
                break;
            }

            calendar.add(Calendar.DATE, 7);
            date = getDateInFormat(calendar);
        }

        return allAvailableSlots;
    }

    private static TreeMap<String, List<AvailableSlot>> findAvailableSlot(SlotResponse response, int age) throws ParseException {

        AvailableSlot tempSlot;
        TreeMap<String, List<AvailableSlot>> availableSlots = new TreeMap<>((o1, o2) -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            try {
                return sdf.parse(o1).compareTo(sdf.parse(o2));
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }
        });


        for (Centers center : response.getCenters()){
            for (Sessions session: center.getSessions()){
                if (session.getAvailable_capacity() > 1 && //Should be 0, but there is some issue with public api, randomly shows 1 vaccine available, so neglecting that
                    session.getMin_age_limit() <= age &&
                    session.getSlots().size() > 0){
                    tempSlot = new AvailableSlot(
                            center.getAddress(),
                            center.getName(),
                            session.getDate(),
                            session.getVaccine(),
                            session.getAvailable_capacity(),
                            session.getSlots());
                    addToMap(availableSlots, tempSlot, session.getDate());
                }
            }
        }

        if (availableSlots.firstEntry() == null) return null;
        return availableSlots;

    }

    private static void addToMap(Map<String, List<AvailableSlot>> availableSlotsMap,
                                 AvailableSlot slot,
                                 String date){
        List<AvailableSlot> availableSlotList = new ArrayList<>();
        if (availableSlotsMap.containsKey(date)){
            availableSlotList = availableSlotsMap.get(date);
        }
        availableSlotList.add(slot);
        availableSlotsMap.put(date, availableSlotList);
    }

    public static String getUrl(String id, String date, String callType){
        String url = serverBaseUrl + "/v2/appointment/sessions/public/";
        if (callType.equals("District")){
            return url + "calendarByDistrict?district_id=" + id + "&date=" + date;
        }
        else{
            return url + "calendarByPin?pincode=" + id + "&date=" + date;
        }
    }

    public static String getDateInFormat(Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        return sdf.format(calendar.getTime());
    }

    public static SlotResponse getSlots(String url){
        try{
            URL uri = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus)");
            InputStream responseStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            return  mapper.readValue(responseStream, SlotResponse.class);
        } catch (IOException e){
            e.printStackTrace();
            return new SlotResponse();
        }
    }
}
