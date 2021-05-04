package com.time.vaccineslot.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class AvailableSlot implements Serializable {
    String address;
    String name;
    String date;
    String vaccine;
    int available_capacity;
    List<String> slots;
}
