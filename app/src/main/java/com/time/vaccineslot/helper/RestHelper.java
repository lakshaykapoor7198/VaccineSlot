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
import java.util.Calendar;
import java.util.Locale;

public class RestHelper {

    public static AvailableSlot getAvailableSlot(String pinCode, int age) throws ParseException {

        Calendar calendar = Calendar.getInstance();
        String date = getDateInFormat(calendar);
        AvailableSlot bestAvailableSlot = null;

        int daysToCheck = 0;

        while (true){
            String url = getUrl(pinCode, date);
            SlotResponse response = getSlots(url);

            if (response.equals(new SlotResponse())) break;
            if (response.getCenters().size() == 0) break;

            bestAvailableSlot = findAvailableSlot(response, age);
            if (null != bestAvailableSlot) {
                break;
            }

            if (daysToCheck > 28){
                break;
            }

            daysToCheck += 7;
            calendar.add(Calendar.DATE, 7);
            date = getDateInFormat(calendar);
        }

        return bestAvailableSlot;
    }

    private static AvailableSlot findAvailableSlot(SlotResponse response, int age) throws ParseException {

        AvailableSlot availableSlot = null;

        for (Centers center : response.getCenters()){
            for (Sessions session: center.getSessions()){
                if (session.getAvailable_capacity() > 0 &&
                    session.getMin_age_limit() <= age){
                    if (null == availableSlot ||
                             compareDates(session.getDate(), availableSlot.getDate())){
                        availableSlot = new AvailableSlot(center.getName(),
                                session.getDate(),
                                session.getVaccine(),
                                session.getAvailable_capacity(),
                                session.getSlots());
                    }
                }
            }
        }

        return availableSlot;
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
