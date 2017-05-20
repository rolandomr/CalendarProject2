package com.example.rolando.calendarproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 15/4/17.
 */

public class Worker {

    private String name;
    private String number_id;
    private List<String> workDays = new ArrayList<>();
    private List<Long> workInts = new ArrayList<>();

    public Worker(String name, String number_id, List<Long> workInts) {
        this.name = name;
        this.number_id = number_id;
        this.workInts = workInts;
    }

    public List<Long> getWorkInts() {

        return workInts;
    }


//needed for firebase
    public Worker() {
    }

    public List<String> getWorkDays() {

        return workDays;
    }

    /*public Worker(String name, String number_id, List<String> workDays) {

        this.name = name;
        this.number_id = number_id;
        this.workDays = workDays;
    }*/

    public Worker(String name, String number_id) {
        this.name = name;
        this.number_id = number_id;

    }

    public String getName() {
        return name;
    }

    public String getNumber_id() {
        return number_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber_id(String number_id) {
        this.number_id = number_id;
    }
}
