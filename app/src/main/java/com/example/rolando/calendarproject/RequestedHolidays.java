package com.example.rolando.calendarproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 28/5/17.
 */

public class RequestedHolidays {

    private List<Long> requestedHolidays = new ArrayList<>();


    public RequestedHolidays(){

    }
    public RequestedHolidays(List<Long> requestedHolidays) {
        this.requestedHolidays = requestedHolidays;
    }

    public List<Long> getRequestedHolidays() {
        return requestedHolidays;
    }
}

