package com.time.vaccineslot.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sessions {
    int available_capacity;
    int available_capacity_dose1;
    int available_capacity_dose2;
    String date;
    int min_age_limit;
    String vaccine;
    List<String> slots;
}
