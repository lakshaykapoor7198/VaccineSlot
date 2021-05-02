package com.time.vaccineslot.helper;

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

    public static List<AvailableSlot> getAvailableSlot(String pinCode, int age) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) > 15){
            calendar.add(Calendar.DATE, 1);
        }

        String date = getDateInFormat(calendar);
        List<AvailableSlot> nearestAvailableSlots = null;

        // Check for next 8 weeks
        for (int i = 0; i < 8; i++) {
            String url = getUrl(pinCode, date);
            SlotResponse response = getSlots(url);

            if (response.equals(new SlotResponse())) break;
            if (response.getCenters().size() == 0) break;

            nearestAvailableSlots = findAvailableSlot(response, age);
            if (null != nearestAvailableSlots) {
                break;
            }

            calendar.add(Calendar.DATE, 7);
            date = getDateInFormat(calendar);
        }

        return nearestAvailableSlots;
    }

    private static List<AvailableSlot> findAvailableSlot(SlotResponse response, int age) throws ParseException {

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
                if (session.getAvailable_capacity() > 0 &&
                    session.getMin_age_limit() <= age &&
                    session.getSlots().size() > 0){
                    tempSlot = new AvailableSlot(center.getName(),
                            session.getDate(),
                            session.getVaccine(),
                            session.getAvailable_capacity(),
                            session.getSlots());
                    addToMap(availableSlots, tempSlot, session.getDate());
                }
            }
        }

        if (availableSlots.firstEntry() == null) return null;
        return availableSlots.firstEntry().getValue();

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

    public static boolean compareDates(String d1, String d2) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        return sdf.parse(d1).before(sdf.parse(d2));
    }

    public static String getUrl(String pinCode, String date){
        return "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=" + pinCode + "&date=" + date;
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
            InputStream responseStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            return  mapper.readValue(responseStream, SlotResponse.class);
        } catch (IOException e){
            e.printStackTrace();
            return new SlotResponse();
        }
    }
}
