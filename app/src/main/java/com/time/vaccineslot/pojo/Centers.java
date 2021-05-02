package com.time.vaccineslot.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Centers {
    String district_name;
    String fee_type;
    String name;
    List<Sessions> sessions;
}
