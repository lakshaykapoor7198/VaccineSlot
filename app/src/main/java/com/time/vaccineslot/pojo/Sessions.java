package com.time.vaccineslot.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sessions {
    int available_capacity;
    String date;
    int min_age_limit;
    String vaccine;
    List<String> slots;
}
