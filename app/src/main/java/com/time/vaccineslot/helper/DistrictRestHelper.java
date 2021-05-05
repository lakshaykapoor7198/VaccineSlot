package com.time.vaccineslot.helper;

import android.util.Log;
import android.util.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.time.vaccineslot.pojo.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DistrictRestHelper {

    private static final String serverBaseUrl = "https://cdn-api.co-vin.in/api";

    private static StatesApiResponse getStates(){
        try{
            URL uri = new URL(serverBaseUrl + "/v2/admin/location/states");
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus)");
            InputStream responseStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(responseStream, StatesApiResponse.class);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private static DistrictsApiResponse getDistricts(int stateId){
        try{
            URL uri = new URL(serverBaseUrl + "/v2/admin/location/districts/" + stateId);
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus)");
            InputStream responseStream = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(responseStream, DistrictsApiResponse.class);
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Pair<List<String>, List<Integer>> getStatesNameAndIds(){
        StatesApiResponse statesApiResponse = getStates();
        List<String> stateNames = new ArrayList<>();
        List<Integer> stateIds = new ArrayList<>();

        if (statesApiResponse != null){
            for (State state: statesApiResponse.getStates()){
                stateNames.add(state.getState_name());
                stateIds.add(state.getState_id());
            }
        }

        return Pair.create(stateNames, stateIds);
    }

    public static Pair<List<String>, List<Integer>> getDistrictsNameAndIds(int stateId){
        DistrictsApiResponse districtsApiResponse = getDistricts(stateId);
        List<String> districtNames = new ArrayList<>();
        List<Integer> districtIds = new ArrayList<>();

        if (districtsApiResponse != null){
            for (District district: districtsApiResponse.getDistricts()){
                districtNames.add(district.getDistrict_name());
                districtIds.add(district.getDistrict_id());
            }
        }

        return Pair.create(districtNames, districtIds);
    }

}
